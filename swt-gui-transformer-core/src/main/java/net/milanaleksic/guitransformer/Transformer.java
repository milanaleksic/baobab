package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import com.google.common.collect.*;
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

    private ObjectMapper mapper;

    private boolean doNotCreateModalDialogs = false;

    public Transformer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationContext createNonManagedForm(String definition) throws TransformerException {
        return transformFromContent(definition);
    }

    public TransformationContext fillManagedForm(Object formObject) throws TransformerException {
        return this.fillManagedForm(null, formObject);
    }

    public TransformationContext fillManagedForm(@Nullable Shell parent, Object formObject) throws TransformerException {
        String thisClassNameAsResourceLocation = formObject.getClass().getCanonicalName().replaceAll("\\.", "/");
        String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS

        TransformationContext transformationContext = transformFromResourceName(parent, formName);
        embedComponents(formObject, transformationContext);
        embedEvents(formObject, transformationContext);
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

    private void embedEvents(Object targetObject, TransformationContext transformationContext) throws TransformerException {
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
                handleSingleEventDelegation(targetObject, field, listenerAnnotation.event(), (Widget) mappedObject.get());
            }
        }
    }

    private void handleSingleEventDelegation(Object targetObject, Field field, int event, Widget mappedObject) throws TransformerException {
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

    TransformationContext createFormFromResource(String fullName) throws TransformerException {
        return transformFromResourceName(null, fullName);
    }

    private TransformationContext transformFromResourceName(@Nullable Shell parent, String fullName) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext();
        InputStream resourceAsStream = null;
        try {
            context.mapObject("bundle", resourceBundleProvider.getResourceBundle()); //NON-NLS
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
            context.setWorkItem(parent);

            resourceAsStream = Transformer.class.getResourceAsStream(fullName);
            final JsonNode shellDefinition = mapper.readValue(resourceAsStream, JsonNode.class);
            context = objectConverter.createWidgetFromNode(context, shellDefinition);
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

    private TransformationContext transformFromContent(String content) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext();
        try {
            context.mapObject("bundle", resourceBundleProvider.getResourceBundle()); //NON-NLS
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);

            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            context = objectConverter.createWidgetFromNode(context, shellDefinition);
            return context.createTransformationContext();
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to parse content: " + content, e);
        }
    }

    public void setDoNotCreateModalDialogs(boolean doNotCreateModalDialogs) {
        this.doNotCreateModalDialogs = doNotCreateModalDialogs;
    }
}
