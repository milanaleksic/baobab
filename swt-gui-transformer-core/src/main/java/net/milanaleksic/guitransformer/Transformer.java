package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:35 AM
 */
public class Transformer {

    @Inject
    private ResourceBundleProvider resourceBundleProvider;

    @Inject
    private ObjectConverter objectConverter;

    private final ObjectMapper mapper;

    private boolean doNotCreateModalDialogs = false;

    private MethodEventListenerExceptionHandler methodEventListenerExceptionHandler;

    public Transformer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public void setMethodEventListenerExceptionHandler(MethodEventListenerExceptionHandler methodEventListenerExceptionHandler) {
        this.methodEventListenerExceptionHandler = methodEventListenerExceptionHandler;
    }

    public TransformationContext createNonManagedForm(@Nullable Shell parent, String definition) throws TransformerException {
        return transformFromContent(parent, definition);
    }

    public TransformationContext fillManagedForm(Object formObject) throws TransformerException {
        return this.fillManagedForm(null, formObject);
    }

    public TransformationContext fillManagedForm(@Nullable Shell parent, Object formObject) throws TransformerException {
        String thisClassNameAsResourceLocation = formObject.getClass().getCanonicalName().replaceAll("\\.", "/");
        String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS

        TransformationContext transformationContext = transformFromResourceName(parent, formName);
        embedComponents(formObject, transformationContext);
        embedEventListenersAsFields(formObject, transformationContext);
        embedEventListenersAsMethods(formObject, transformationContext);
        return transformationContext;
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
            boolean wasPublic = Modifier.isPublic(field.getModifiers());
            if (!wasPublic)
                field.setAccessible(true);
            Optional<Object> mappedObject = transformationContext.getMappedObject(name);
            if (!mappedObject.isPresent())
                throw new IllegalStateException("Field marked as embedded could not be found: " + targetObject.getClass().getName() + "." + field.getName());
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
                    if (method.getParameterTypes().length>0)
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

    TransformationContext createFormFromResource(String fullName) throws TransformerException {
        return transformFromResourceName(null, fullName);
    }

    private TransformationContext transformFromResourceName(@Nullable Shell parent, String fullName) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext();
        InputStream resourceAsStream = null;
        try {
            mapResourceBundleIfExists(context);
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
            context.setWorkItem(parent);

            resourceAsStream = Transformer.class.getResourceAsStream(fullName);
            final JsonNode shellDefinition = mapper.readValue(resourceAsStream, JsonNode.class);
            context = objectConverter.createHierarchy(context, shellDefinition);
            return context.createTransformationContext();
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        } finally {
            try {
                if (resourceAsStream != null) resourceAsStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void mapResourceBundleIfExists(TransformationWorkingContext context) {
        final ResourceBundle resourceBundle = resourceBundleProvider.getResourceBundle();
        if (resourceBundle != null)
            context.mapObject("bundle", resourceBundle); //NON-NLS
    }

    private TransformationContext transformFromContent(@Nullable Shell parent, String content) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext();
        try {
            mapResourceBundleIfExists(context);
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
            context.setWorkItem(parent);

            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            context = objectConverter.createHierarchy(context, shellDefinition);
            return context.createTransformationContext();
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to parse content: " + content, e);
        }
    }

    public void setDoNotCreateModalDialogs(boolean doNotCreateModalDialogs) {
        this.doNotCreateModalDialogs = doNotCreateModalDialogs;
    }
}
