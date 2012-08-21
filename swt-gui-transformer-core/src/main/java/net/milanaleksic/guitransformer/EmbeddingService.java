package net.milanaleksic.guitransformer;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.milanaleksic.guitransformer.model.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;

class EmbeddingService {

    private MethodEventListenerExceptionHandler methodEventListenerExceptionHandler;

    private Map<Object, ModelBindingMetaData> modelToModelBinding = Maps.newHashMap();

    public void setMethodEventListenerExceptionHandler(MethodEventListenerExceptionHandler methodEventListenerExceptionHandler) {
        this.methodEventListenerExceptionHandler = methodEventListenerExceptionHandler;
    }

    void embed(Object formObject, TransformationContext transformationContext) throws TransformerException {
        embedComponents(formObject, transformationContext);
        embedEventListenersAsFields(formObject, transformationContext);
        embedEventListenersAsMethods(formObject, transformationContext);
        embedModels(formObject, transformationContext);
    }

    private interface OperationOnField {
        void operate(Field field) throws ReflectiveOperationException, TransformerException;
    }

    private void allowOperationOnField(Field field, OperationOnField operation) throws TransformerException {
        boolean wasPublic = Modifier.isPublic(field.getModifiers());
        if (!wasPublic)
            field.setAccessible(true);
        try {
            operation.operate(field);
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Error while operating on field named " + field.getName(), e);
        } finally {
            if (!wasPublic)
                field.setAccessible(false);
        }
    }

    private void embedComponents(final Object targetObject, TransformationContext transformationContext) throws TransformerException {
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                continue;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            final Optional<Object> mappedObject = transformationContext.getMappedObject(name);
            if (!mappedObject.isPresent())
                throw new IllegalStateException("Field marked as embedded could not be found: " + targetObject.getClass().getName() + "." + field.getName());
            allowOperationOnField(field, new OperationOnField() {
                @Override
                public void operate(Field field) throws ReflectiveOperationException {
                    field.set(targetObject, mappedObject.get());
                }
            });
        }
    }

    private void embedEventListenersAsFields(final Object targetObject, TransformationContext transformationContext) throws TransformerException {
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
                final Optional<Object> mappedObject = componentName.isEmpty()
                        ? Optional.<Object>of(transformationContext.getShell())
                        : transformationContext.getMappedObject(componentName);
                if (!mappedObject.isPresent())
                    throw new IllegalStateException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + field.getName());
                allowOperationOnField(field, new OperationOnField() {
                    @Override
                    public void operate(Field field) throws ReflectiveOperationException {
                        ((Widget) mappedObject.get()).addListener(listenerAnnotation.event(), (Listener) field.get(targetObject));
                    }
                });
            }
        }
    }

    private void embedEventListenersAsMethods(Object targetObject, TransformationContext transformationContext) throws TransformerException {
        Method[] methods = targetObject.getClass().getDeclaredMethods();
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
            for (EmbeddedEventListener listenerAnnotation : allListeners) {
                String componentName = listenerAnnotation.component();
                Optional<Object> mappedObject = componentName.isEmpty()
                        ? Optional.<Object>of(transformationContext.getShell())
                        : transformationContext.getMappedObject(componentName);
                if (!mappedObject.isPresent())
                    throw new IllegalStateException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + method.getName());
                if (!void.class.equals(method.getReturnType()))
                    throw new IllegalStateException("Method event listeners must be with void return type " + targetObject.getClass().getName() + "." + method.getName());
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length > 0) {
                    if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0]))
                        throw new IllegalStateException("Method event listeners must have exactly one parameter, of type org.eclipse.swt.widgets.Event: " + targetObject.getClass().getName() + "." + method.getName());
                }
                handleSingleEventToMethodListenerDelegation(transformationContext, targetObject, method, listenerAnnotation.event(), (Widget) mappedObject.get());
            }
        }
    }

    private void handleSingleEventToMethodListenerDelegation(final TransformationContext transformationContext, final Object targetObject, final Method method, int event, Widget mappedObject) {
        mappedObject.addListener(event, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final boolean wasPublic = Modifier.isPublic(method.getModifiers());
                try {
                    if (!wasPublic)
                        method.setAccessible(true);
                    if (method.getParameterTypes().length > 0)
                        method.invoke(targetObject, event);
                    else
                        method.invoke(targetObject);
                } catch (Exception e) {
                    if (methodEventListenerExceptionHandler != null)
                        methodEventListenerExceptionHandler.handleException(transformationContext.getShell(), e);
                    else
                        throw new RuntimeException("Transformer event delegation got an exception: " + e.getMessage(), e);
                } finally {
                    if (!wasPublic)
                        method.setAccessible(false);
                }
            }
        });
    }

    private void embedModels(final Object formObject, final TransformationContext transformationContext) throws TransformerException {
        Field[] fields = formObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            TransformerModel annotation = field.getAnnotation(TransformerModel.class);
            if (annotation == null)
                continue;
            allowOperationOnField(field, new OperationOnField() {
                @Override
                public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                    Object model = field.getType().newInstance();
                    bindModel(model, transformationContext);
                    field.set(formObject, model);
                }
            });
        }
    }

    private void bindModel(Object model, TransformationContext transformationContext) throws TransformerException {
        modelToModelBinding.put(model, createBindingMetaData(model, transformationContext));
        try {
            mapOnChangeListeners(model, transformationContext);
            updateModelFromForm(model, transformationContext);
        } catch (ReflectiveOperationException e) {
            throw new TransformerException("Reflection problem while binding model", e);
        }
    }

    private void mapOnChangeListeners(final Object model, final TransformationContext transformationContext) throws ReflectiveOperationException {
        final ModelBindingMetaData modelBindingMetaData = modelToModelBinding.get(model);
        for (Map.Entry<Field, FieldMapping> mapping : modelBindingMetaData.getFieldMapping().entrySet()) {
            final Field field = mapping.getKey();
            final Object component = mapping.getValue().getComponent();
            Method addListener = component.getClass().getMethod("addListener", new Class[]{int.class, Listener.class});

            addListener.invoke(component, SWT.Modify, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    setModelFieldValue(model, field, component, modelBindingMetaData, transformationContext);
                }
            });
        }
    }

    private void setModelFieldValue(final Object model, Field field, final Object component, final ModelBindingMetaData modelBindingMetaData, final TransformationContext transformationContext) {
        try {
            allowOperationOnField(field, new OperationOnField() {
                @Override
                public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                    FieldMapping fieldMapping = modelBindingMetaData.getFieldMapping().get(field);
                    final Method getterMethod = fieldMapping.getGetterMethod();
                    if (fieldMapping.getBindingType().equals(FieldMapping.BindingType.BY_REFERENCE))
                        field.set(model, getterMethod.invoke(component));
                    else
                        field.set(model, convertFromComponentToModelValue((String) getterMethod.invoke(component), field.getType()));
                }
            });
        } catch (Exception e) {
            handleException(e, transformationContext);
        }
    }

    private void handleException(Exception e, TransformationContext transformationContext) {
        if (methodEventListenerExceptionHandler != null)
            methodEventListenerExceptionHandler.handleException(transformationContext.getShell(), e);
        else
            throw new RuntimeException("Transformer event delegation got an exception: " + e.getMessage(), e);
    }

    private ModelBindingMetaData createBindingMetaData(Object model, TransformationContext transformationContext) throws TransformerException {
        ModelBindingMetaData bindingData = new ModelBindingMetaData();
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            String propertyNameSentenceCase = getPropertyNameSentenceCaseForModelField(field);
            String name = field.getName();
            try {
                FieldMapping.FieldMappingBuilder builder = FieldMapping.builder();
                Optional<Object> mappedObject = transformationContext.getMappedObject(name);
                if (!mappedObject.isPresent())
                    throw new IllegalStateException("Field could not be found in form: " + model.getClass().getName() + "." + name);
                builder.setComponent(mappedObject.get());

                Method getterMethod = mappedObject.get().getClass().getDeclaredMethod("get" + propertyNameSentenceCase, new Class[0]);
                if (field.getType().isAssignableFrom(getterMethod.getReturnType()))
                    builder.setBindingType(FieldMapping.BindingType.BY_REFERENCE);
                else
                    builder.setBindingType(FieldMapping.BindingType.CONVERSION);

                builder.setGetterMethod(getterMethod);
                try {
                    builder.setSetterMethod(mappedObject.get().getClass().getDeclaredMethod("set" + propertyNameSentenceCase, new Class[]{field.getType()}));
                } catch (NoSuchMethodException e) {
                    builder.setSetterMethod(mappedObject.get().getClass().getDeclaredMethod("set" + propertyNameSentenceCase, new Class[]{String.class}));
                }

                bindingData.getFieldMapping().put(field, builder.build());
            } catch (Exception e) {
                throw new TransformerException("Error while creating binding metadata for component field named " + name, e);
            }
        }
        return bindingData;
    }

    private String getPropertyNameSentenceCaseForModelField(Field field) {
        String propertyName = getPropertyNameForModelField(field);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName));
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private String getPropertyNameForModelField(Field field) {
        TransformerProperty annotation = field.getAnnotation(TransformerProperty.class);
        if (annotation == null)
            return TransformerProperty.DEFAULT_PROPERTY_NAME;
        return annotation.value();
    }

    private void updateModelFromForm(Object model, TransformationContext transformationContext) throws ReflectiveOperationException, TransformerException {
        ModelBindingMetaData modelBindingMetaData = modelToModelBinding.get(model);
        for (Map.Entry<Field, FieldMapping> binding : modelBindingMetaData.getFieldMapping().entrySet()) {
            Field field = binding.getKey();
            Object component = binding.getValue().getComponent();
            setModelFieldValue(model, field, component, modelBindingMetaData, transformationContext);
        }
    }

    private Object convertFromComponentToModelValue(String value, Class<?> targetClass) throws ReflectiveOperationException, TransformerException {
        if (String.class.isAssignableFrom(targetClass))
            return value;
        else if (Long.class.isAssignableFrom(targetClass) || long.class.isAssignableFrom(targetClass))
            return Long.parseLong(value, 10);
        else if (Integer.class.isAssignableFrom(targetClass) || int.class.isAssignableFrom(targetClass))
            return Integer.parseInt(value, 10);
        throw new TransformerException("Value transformation to model class " + targetClass + " not supported");
    }

    public void updateFormFromModel(final Object model) {
        try {
            ModelBindingMetaData modelBindingMetaData = modelToModelBinding.get(model);
            for (Map.Entry<Field, FieldMapping> binding : modelBindingMetaData.getFieldMapping().entrySet()) {
                Field field = binding.getKey();
                final FieldMapping fieldMapping = binding.getValue();
                final Object component = fieldMapping.getComponent();
                    allowOperationOnField(field, new OperationOnField() {
                        @Override
                        public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                            Object modelValue = field.get(model);
                            Preconditions.checkNotNull(modelValue);
                            if (fieldMapping.getBindingType().equals(FieldMapping.BindingType.BY_REFERENCE))
                                fieldMapping.getSetterMethod().invoke(component, modelValue);
                            else
                                fieldMapping.getSetterMethod().invoke(component, modelValue.toString());
                        }
                    });
            }
        } catch (TransformerException e) {
            throw new IllegalStateException("Unexpected error occurred when using invalid model", e);
        }
    }

}
