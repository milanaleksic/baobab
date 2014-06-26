package net.milanaleksic.baobab.converters;

import com.google.common.collect.*;
import net.milanaleksic.baobab.EmbeddedComponent;
import net.milanaleksic.baobab.EmbeddedEventListener;
import net.milanaleksic.baobab.EmbeddedEventListeners;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.model.*;
import net.milanaleksic.baobab.util.Preconditions;
import net.milanaleksic.baobab.util.ProxyFactoryForPostProcessingOfObservableMethods;
import net.milanaleksic.baobab.util.StringUtil;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static net.milanaleksic.baobab.util.ObjectUtil.*;

class EmbeddingService {

    private MethodEventListenerExceptionHandler methodEventListenerExceptionHandler;

    @SuppressWarnings("UnusedDeclaration")
    public void setMethodEventListenerExceptionHandler(MethodEventListenerExceptionHandler methodEventListenerExceptionHandler) {
        this.methodEventListenerExceptionHandler = methodEventListenerExceptionHandler;
    }

    public void embed(Object formObject, TransformationWorkingContext transformationContext) {
        embedComponents(formObject, transformationContext);
        embedModels(formObject, transformationContext);
        embedEventListenersAsFields(formObject, transformationContext);
        embedEventListenersAsMethods(formObject, transformationContext);
    }

    private void embedComponents(final Object targetObject, TransformationWorkingContext transformationContext) {
        Field[] fields = targetObject.getClass().getDeclaredFields();
        Arrays.asList(fields).forEach(field -> {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                return;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            final Object mappedObject = transformationContext.getMappedObject(name);
            if (mappedObject == null)
                throw new TransformerException("Field marked as embedded could not be found: " + targetObject.getClass().getName() + "." + field.getName());
            setFieldValueOnObject(field, targetObject, mappedObject);
        });
    }

    private void embedEventListenersAsFields(final Object targetObject, TransformationWorkingContext transformationContext) {
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            List<EmbeddedEventListener> allListeners = Lists.newArrayList();
            EmbeddedEventListeners annotations = field.getAnnotation(EmbeddedEventListeners.class);
            if (annotations != null)
                allListeners.addAll(Arrays.asList(annotations.value()));
            else {
                EmbeddedEventListener annotation = field.getAnnotation(EmbeddedEventListener.class);
                if (annotation != null)
                    allListeners.add(annotation);
            }
            for (final EmbeddedEventListener listenerAnnotation : allListeners) {
                String componentName = listenerAnnotation.component();
                final Object mappedObject = componentName.isEmpty()
                        ? Optional.of(transformationContext.getWorkItem())
                        : transformationContext.getMappedObject(componentName);
                if (mappedObject == null)
                    throw new TransformerException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + field.getName());
                allowOperationOnField(field, safeField ->
                        ((Widget) mappedObject).addListener(listenerAnnotation.event(), (Listener) field.get(targetObject)));
            }
        }
    }

    private void embedEventListenersAsMethods(Object targetObject, TransformationWorkingContext transformationContext) {
        Method[] methods = getAllAvailableDeclaredMethodsForClass(targetObject.getClass());
        for (Method method : methods) {
            List<EmbeddedEventListener> allListeners = Lists.newArrayList();
            EmbeddedEventListeners annotations = method.getAnnotation(EmbeddedEventListeners.class);
            if (annotations != null)
                allListeners.addAll(Arrays.asList(annotations.value()));
            else {
                EmbeddedEventListener annotation = method.getAnnotation(EmbeddedEventListener.class);
                if (annotation != null)
                    allListeners.add(annotation);
            }
            allListeners.forEach(listenerAnnotation -> {
                String componentName = listenerAnnotation.component();
                Object mappedObject = componentName.isEmpty()
                        ? transformationContext.getWorkItem()
                        : transformationContext.getMappedObject(componentName);
                if (mappedObject == null)
                    throw new TransformerException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + method.getName());
                if (!void.class.equals(method.getReturnType()))
                    throw new TransformerException("Method event listeners must be with void return type " + targetObject.getClass().getName() + "." + method.getName());
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 0) {
                    if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0]))
                        throw new TransformerException("Method event listeners must have exactly one parameter, of type org.eclipse.swt.widgets.Event: " + targetObject.getClass().getName() + "." + method.getName());
                }
                handleSingleEventToMethodListenerDelegation(transformationContext, targetObject, method, listenerAnnotation.event(), (Widget) mappedObject);
            });
        }
    }

    private void handleSingleEventToMethodListenerDelegation(final TransformationWorkingContext transformationContext, final Object targetObject, final Method method, int eventId, Widget mappedObject) {
        mappedObject.addListener(eventId, event -> {
            final boolean wasPublic = Modifier.isPublic(method.getModifiers());
            try {
                if (!wasPublic)
                    method.setAccessible(true);
                if (method.getParameterTypes().length > 0)
                    method.invoke(targetObject, event);
                else
                    method.invoke(targetObject);
            } catch (InvocationTargetException invocationException) {
                if (methodEventListenerExceptionHandler != null)
                    methodEventListenerExceptionHandler.handleException((Shell) transformationContext.getWorkItem(), (Exception) invocationException.getCause());
                else
                    throw new TransformerException("Transformer event delegation got an exception: " + invocationException.getCause().getMessage(), invocationException.getCause());
            } catch (Exception e) {
                if (methodEventListenerExceptionHandler != null)
                    methodEventListenerExceptionHandler.handleException((Shell) transformationContext.getWorkItem(), e);
                else
                    throw new TransformerException("Transformer event delegation got an exception: " + e.getMessage(), e);
            } finally {
                if (!wasPublic)
                    method.setAccessible(false);
            }
        });
    }

    private void embedModels(final Object formObject, final TransformationWorkingContext transformationContext) {
        Field[] fields = formObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            TransformerModel annotation = field.getAnnotation(TransformerModel.class);
            if (annotation == null)
                continue;
            final ModelBindingMetaData bindingMetaData = createBindingMetaData(field.getType(), transformationContext);
            Object model;
            try {
                if (annotation.observe())
                    model = createObservableModel(field.getType(), bindingMetaData);
                else
                    model = createInstanceForType(field.getType());
            } catch (ReflectiveOperationException e1) {
                throw new TransformerException("Reflection problem while binding model", e1);
            } catch (Exception e) {
                throw new TransformerException("Could not create instance of model object!", e);
            }
            bindModel(model, transformationContext, bindingMetaData);
            setFieldValueOnObject(field, formObject, model);
        }
    }

    private void bindModel(Object model, TransformationWorkingContext transformationContext, ModelBindingMetaData bindingMetaData) {
        transformationContext.setModelBindingMetaData(bindingMetaData);
        try {
            mapOnChangeListeners(model, transformationContext);
            updateModelFromForm(model, transformationContext);
        } catch (ReflectiveOperationException e) {
            throw new TransformerException("Reflection problem while binding model", e);
        }
    }

    private <T> T createObservableModel(final Class<T> modelType, final ModelBindingMetaData bindingMetaData) {
        return ProxyFactoryForPostProcessingOfObservableMethods.wrapMethodCalls(
                modelType, getObservableMethods(modelType, bindingMetaData),
                target -> FormUpdater.updateFormFromModel(target, bindingMetaData)
        );
    }

    private Set<Method> getObservableMethods(final Class<?> type, ModelBindingMetaData bindingMetaData) {
        final ImmutableListMultimap<String, Method> methods =
                Multimaps.index(Arrays.asList(getAllAvailableDeclaredMethodsForClass(type)), Method::getName);
        return Sets.union(FluentIterable
                .from(bindingMetaData.getFieldMapping().keySet())
                .filter(input ->
                        input.getAnnotation(TransformerIgnoredProperty.class) == null && methods.keySet().contains(getSetterForField(input.getName())))
                .transform(matchedField -> {
                    ImmutableList<Method> matchedMethods = methods.get(getSetterForField(matchedField.getName()));
                    Preconditions.checkState(matchedMethods.size() == 1, "could not make an unique match for setter method");
                    return matchedMethods.get(0);
                })
                .toSet(),
                Sets.filter(Sets.newHashSet(methods.values()), method -> method.getAnnotation(TransformerFireUpdate.class) != null)
        );
    }

    private void mapOnChangeListeners(final Object model, final TransformationWorkingContext transformationContext) throws ReflectiveOperationException {
        final ModelBindingMetaData modelBindingMetaData = transformationContext.getModelBindingMetaData();
        for (Map.Entry<Field, FieldMapping> mapping : modelBindingMetaData.getFieldMapping().entrySet()) {
            final Field field = mapping.getKey();
            FieldMapping fieldMapping = mapping.getValue();
            final Object component = fieldMapping.getComponent();
            Method addListener = component.getClass().getMethod("addListener", new Class[]{int.class, Listener.class});

            for (int eventType : fieldMapping.getEvents()) {
                addListener.invoke(component, eventType, (Listener) event -> {
                    if (modelBindingMetaData.isFormIsBeingUpdatedFromModelRightNow())
                        return;
                    setModelFieldValue(model, field, component, modelBindingMetaData, transformationContext);
                });
            }
        }
    }

    private void setModelFieldValue(final Object model, Field field, final Object component, final ModelBindingMetaData modelBindingMetaData, final TransformationWorkingContext transformationContext) {
        try {
            FieldMapping fieldMapping = modelBindingMetaData.getFieldMapping().get(field);
            final Method getterMethod = fieldMapping.getGetterMethod();
            if (fieldMapping.getBindingType().equals(FieldMapping.BindingType.BY_REFERENCE))
                setFieldValueOnObject(field, model, getterMethod.invoke(component));
            else
                setFieldValueOnObject(field, model, convertFromComponentToModelValue((String) getterMethod.invoke(component), field.getType()));
        } catch (Exception e) {
            handleException(e, transformationContext);
        }
    }

    private void handleException(Exception e, TransformationWorkingContext transformationContext) {
        if (methodEventListenerExceptionHandler != null) {
            Object workItem = transformationContext.getWorkItem();
            Shell parentShell = (workItem != null && workItem instanceof Shell) ? (Shell) workItem : null;
            methodEventListenerExceptionHandler.handleException(parentShell, e);
        } else
            throw new TransformerException("Transformer event delegation got an exception: " + e.getMessage(), e);
    }

    private ModelBindingMetaData createBindingMetaData(Class<?> modelClazz, TransformationWorkingContext transformationContext) {
        ModelBindingMetaData bindingData = new ModelBindingMetaData();
        Field[] fields = modelClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(TransformerIgnoredProperty.class) != null)
                continue;
            if (field.isSynthetic())
                continue;
            try {
                bindingData.getFieldMapping().put(field, createSingleBindingMetaData(field, transformationContext));
            } catch (TransformerException e) {
                throw e;
            } catch (Exception e) {
                throw new TransformerException("Error while creating binding metadata for component field " + field, e);
            }
        }
        return bindingData;
    }

    private FieldMapping createSingleBindingMetaData(Field field, TransformationWorkingContext transformationContext) throws NoSuchMethodException {
        TransformerProperty propertyAnnotation = field.getAnnotation(TransformerProperty.class);
        String name = propertyAnnotation == null ? null : propertyAnnotation.component();
        if (StringUtil.isNullOrEmpty(name))
            name = field.getName();
        String propertyNameSentenceCase = getPropertyNameSentenceCaseForModelField(propertyAnnotation);

        FieldMapping.FieldMappingBuilder builder = FieldMapping.builder();

        Object mappedObject = transformationContext.getMappedObject(name);
        if (mappedObject == null)
            throw new TransformerException("Mapped object could not be found: " + name);
        builder.setComponent(mappedObject);

        Method getterMethod = mappedObject.getClass().getMethod("get" + propertyNameSentenceCase, new Class[0]);
        builder.setGetterMethod(getterMethod);
        builder.setEvents(propertyAnnotation == null
                ? new int[]{TransformerPropertyConstants.DEFAULT_EVENT}
                : propertyAnnotation.events());

        if (field.getType().isAssignableFrom(getterMethod.getReturnType()))
            builder.setBindingType(FieldMapping.BindingType.BY_REFERENCE);
        else
            builder.setBindingType(FieldMapping.BindingType.CONVERSION);

        boolean isReadOnly = propertyAnnotation != null && propertyAnnotation.readOnly();
        if (!isReadOnly) {
            try {
                builder.setSetterMethod(mappedObject.getClass().getMethod("set" + propertyNameSentenceCase, new Class[]{field.getType()}));
            } catch (NoSuchMethodException e) {
                builder.setSetterMethod(mappedObject.getClass().getMethod("set" + propertyNameSentenceCase, new Class[]{String.class}));
            }
        }
        return builder.build();
    }

    private String getPropertyNameSentenceCaseForModelField(TransformerProperty annotation) {
        String propertyName = getPropertyNameForModelField(annotation);
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(propertyName), "Property name was not found");
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private String getPropertyNameForModelField(TransformerProperty annotation) {
        if (annotation == null)
            return TransformerPropertyConstants.DEFAULT_PROPERTY_NAME;
        return annotation.value();
    }

    private void updateModelFromForm(Object model, TransformationWorkingContext transformationContext) {
        ModelBindingMetaData modelBindingMetaData = transformationContext.getModelBindingMetaData();
        for (Map.Entry<Field, FieldMapping> binding : modelBindingMetaData.getFieldMapping().entrySet()) {
            Field field = binding.getKey();
            Object component = binding.getValue().getComponent();
            setModelFieldValue(model, field, component, modelBindingMetaData, transformationContext);
        }
    }

    private Object convertFromComponentToModelValue(String value, Class<?> targetClass) {
        if (String.class.isAssignableFrom(targetClass))
            return value;
        else if (Long.class.isAssignableFrom(targetClass) || long.class.isAssignableFrom(targetClass))
            return safeLongValue(value);
        else if (Integer.class.isAssignableFrom(targetClass) || int.class.isAssignableFrom(targetClass))
            return safeIntValue(value);
        throw new TransformerException("Value transformation to model class " + targetClass + " not supported");
    }

    private long safeLongValue(String value) {
        if (StringUtil.isNullOrEmpty(value))
            return -1L;
        try {
            return Long.parseLong(value, 10);
        } catch (Exception e) {
            return -1;
        }
    }

    private int safeIntValue(String value) {
        if (StringUtil.isNullOrEmpty(value))
            return -1;
        try {
            return Integer.parseInt(value, 10);
        } catch (Exception e) {
            return -1;
        }
    }


}
