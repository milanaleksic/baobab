package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.widgets.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;

class EmbeddingService {

    private MethodEventListenerExceptionHandler methodEventListenerExceptionHandler;

    private class ModelBindingMetaData {

        private Map<Field, Object> fieldToComponentMapping = Maps.newHashMap();

        private Map<Field, Method> modelFieldToComponentGetterMapping = Maps.newHashMap();

        public Map<Field, Method> modelFieldToComponentSetterMapping = Maps.newHashMap();
    }

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

    private void embedComponents(Object targetObject, TransformationContext transformationContext) throws TransformerException {
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                continue;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            Optional<Object> mappedObject = transformationContext.getMappedObject(name);
            if (!mappedObject.isPresent())
                throw new IllegalStateException("Field marked as embedded could not be found: " + targetObject.getClass().getName() + "." + field.getName());
            boolean wasPublic = Modifier.isPublic(field.getModifiers());
            if (!wasPublic)
                field.setAccessible(true);
            try {
                field.set(targetObject, mappedObject.get());
            } catch (Exception e) {
                throw new TransformerException("Error while embedding component field named " + field.getName(), e);
            } finally {
                if (!wasPublic)
                    field.setAccessible(false);
            }
        }
    }

    private void embedEventListenersAsFields(Object targetObject, TransformationContext transformationContext) throws TransformerException {
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
            for (EmbeddedEventListener listenerAnnotation : allListeners) {
                String componentName = listenerAnnotation.component();
                Optional<Object> mappedObject = componentName.isEmpty()
                        ? Optional.<Object>of(transformationContext.getShell())
                        : transformationContext.getMappedObject(componentName);
                if (!mappedObject.isPresent())
                    throw new IllegalStateException("Event source could not be found in the GUI definition: " + targetObject.getClass().getName() + "." + field.getName());
                handleSingleEventToFieldListenerDelegation(targetObject, field, listenerAnnotation.event(), (Widget) mappedObject.get());
            }
        }
    }

    private void handleSingleEventToFieldListenerDelegation(Object targetObject, Field field, int event, Widget mappedObject) throws TransformerException {
        boolean wasPublic = Modifier.isPublic(field.getModifiers());
        if (!wasPublic)
            field.setAccessible(true);
        try {
            mappedObject.addListener(event, (Listener) field.get(targetObject));
        } catch (Exception e) {
            throw new TransformerException("Error while embedding component field named " + field.getName(), e);
        } finally {
            if (!wasPublic)
                field.setAccessible(false);
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

    private void embedModels(Object formObject, TransformationContext transformationContext) throws TransformerException {
        Field[] fields = formObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            TransformerModel annotation = field.getAnnotation(TransformerModel.class);
            if (annotation == null)
                continue;
            boolean wasPublic = Modifier.isPublic(field.getModifiers());
            if (!wasPublic)
                field.setAccessible(true);
            try {
                Object model = field.getType().newInstance();
                bindModel(model, transformationContext);
                field.set(formObject, model);
            } catch (Exception e) {
                throw new TransformerException("Error while embedding model into field named " + field.getName(), e);
            } finally {
                if (!wasPublic)
                    field.setAccessible(false);
            }
        }
    }

    private void bindModel(Object model, TransformationContext transformationContext) throws TransformerException {
        modelToModelBinding.put(model, createBindingMetaData(model, transformationContext));
        try {
            updateModelFromForm(model);
        } catch (ReflectiveOperationException e) {
            throw new TransformerException("Reflection problem while binding model", e);
        }
    }

    private ModelBindingMetaData createBindingMetaData(Object model, TransformationContext transformationContext) throws TransformerException {
        ModelBindingMetaData bindingData = new ModelBindingMetaData();
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            try {
                Optional<Object> mappedObject = transformationContext.getMappedObject(name);
                if (!mappedObject.isPresent())
                    throw new IllegalStateException("Field could not be found in form: " + model.getClass().getName() + "." + name);
                bindingData.fieldToComponentMapping.put(field, mappedObject.get());
                bindingData.modelFieldToComponentGetterMapping.put(field, mappedObject.get().getClass().getDeclaredMethod("getText", new Class[0]));
                bindingData.modelFieldToComponentSetterMapping.put(field, mappedObject.get().getClass().getDeclaredMethod("setText", new Class[] { String.class}));
            } catch (Exception e) {
                throw new TransformerException("Error while creating binding metadata for component field named " + name, e);
            }
        }
        return bindingData;
    }

    private void updateModelFromForm(Object model) throws ReflectiveOperationException, TransformerException {
        ModelBindingMetaData modelBindingMetaData = modelToModelBinding.get(model);
        for (Map.Entry<Field, Object> binding : modelBindingMetaData.fieldToComponentMapping.entrySet()) {
            Field field = binding.getKey();
            Object component = binding.getValue();
            Method getterMethod = modelBindingMetaData.modelFieldToComponentGetterMapping.get(field);
            boolean wasPublic = Modifier.isPublic(field.getModifiers());
            if (!wasPublic)
                field.setAccessible(true);
            try {
                field.set(model, fromComponentToModelValue((String) getterMethod.invoke(component), field.getType()));
            } finally {
                if (!wasPublic)
                    field.setAccessible(false);
            }
        }
    }

    private Object fromComponentToModelValue(String value, Class<?> targetClass) throws ReflectiveOperationException, TransformerException {
        if (String.class.isAssignableFrom(targetClass))
            return value;
        else if (Long.class.isAssignableFrom(targetClass) || long.class.isAssignableFrom(targetClass))
            return Long.parseLong(value, 10);
        else if (Integer.class.isAssignableFrom(targetClass) || int.class.isAssignableFrom(targetClass))
            return Integer.parseInt(value, 10);
        throw new TransformerException("Value transformation to model class " + targetClass + " not supported");
    }

    public void updateFormFromModel(Object model) throws TransformerException {
        ModelBindingMetaData modelBindingMetaData = modelToModelBinding.get(model);
        for (Map.Entry<Field, Object> binding : modelBindingMetaData.fieldToComponentMapping.entrySet()) {
            Field field = binding.getKey();
            Object component = binding.getValue();
            Method setterMethod = modelBindingMetaData.modelFieldToComponentSetterMapping.get(field);
            boolean wasPublic = Modifier.isPublic(field.getModifiers());
            if (!wasPublic)
                field.setAccessible(true);
            try {
                Object modelValue = field.get(model);
                Preconditions.checkNotNull(modelValue);
                setterMethod.invoke(component, modelValue.toString());
            } catch (ReflectiveOperationException e) {
                throw new TransformerException("Reflective exception occurred when mapping component from model", e);
            } finally {
                if (!wasPublic)
                    field.setAccessible(false);
            }
        }
    }

}
