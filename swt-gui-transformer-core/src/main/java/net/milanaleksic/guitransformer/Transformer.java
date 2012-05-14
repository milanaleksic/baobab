package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import net.milanaleksic.guitransformer.typed.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.SWT;
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

    public static final int DEFAULT_STYLE_SHELL = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
    public static final int DEFAULT_STYLE_REST = SWT.NONE;

    static final String KEY_SPECIAL_TYPE = "_type"; //NON-NLS
    static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    static final String KEY_SPECIAL_NAME = "_name"; //NON-NLS
    static final String KEY_SPECIAL_STYLE = "_style"; //NON-NLS
    static final String KEY_SPECIAL_COMMENT = "__comment"; //NON-NLS

    private static final Set<String> SPECIAL_KEYS = ImmutableSet
            .<String>builder()
            .add(KEY_SPECIAL_TYPE)
            .add(KEY_SPECIAL_CHILDREN)
            .add(KEY_SPECIAL_NAME)
            .add(KEY_SPECIAL_STYLE)
            .add(KEY_SPECIAL_COMMENT)
            .build();

    private ObjectMapper mapper;

    @Inject
    private ConverterFactory converterFactory;

    public Transformer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationContext fillManagedForm(Object formObject) throws TransformerException {
        return this.fillManagedForm(null, formObject);
    }

    public TransformationContext fillManagedForm(@Nullable Shell parent, Object formObject) throws TransformerException {
        String thisClassNameAsResourceLocation = formObject.getClass().getCanonicalName().replaceAll("\\.", "/");
        String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS

        TransformationContext transformationContext = fillForm(parent, formName);
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
        return fillForm(null, fullName);
    }

    private TransformationContext fillForm(@Nullable Shell parent, String fullName) throws TransformerException {
        Map<String, Object> mappedObjects = Maps.newHashMap();

        mappedObjects.put("bundle", resourceBundleProvider.getResourceBundle()); //NON-NLS
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = Transformer.class.getResourceAsStream(fullName);
            final JsonNode shellDefinition = mapper.readValue(resourceAsStream, JsonNode.class);
            Object shellObject = createObject(parent, shellDefinition, mappedObjects);
            return new TransformationContext((Shell) shellObject, mappedObjects);
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        } finally {
            try {
                if (resourceAsStream != null) resourceAsStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void deSerializeObjectFromNode(JsonNode jsonNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
        if (jsonNode.has(KEY_SPECIAL_NAME)) {
            String objectName = jsonNode.get(KEY_SPECIAL_NAME).asText();
            mappedObjects.put(objectName, object);
        }
        transformNodeToProperties(jsonNode, object, mappedObjects);
    }

    private void transformChildren(JsonNode childrenNodes, Object parentWidget, Map<String, Object> mappedObjects) throws TransformerException {
        if (!(parentWidget instanceof Composite) && !(parentWidget instanceof Menu))
            throw new IllegalStateException("Can not create children for parent which is not Composite nor Menu (" + parentWidget.getClass().getName() + " in this case)");
        try {
            for (JsonNode node : mapper.readValue(childrenNodes, JsonNode[].class)) {
                // TODO: parent hierarchy stack!
                createObject(parentWidget, node, mappedObjects);
            }
        } catch (IOException e) {
            throw new TransformerException("IO exception while trying to parse child nodes", e);
        }
    }

    void transformNodeToProperties(JsonNode jsonNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getKey().equals(KEY_SPECIAL_CHILDREN))
                transformChildren(field.getValue(), object, mappedObjects);
            else
                transformSingleJsonNode(object, field, mappedObjects);
        }
    }

    private void transformSingleJsonNode(Object object, Map.Entry<String, JsonNode> field, Map<String, Object> mappedObjects) throws TransformerException {
        try {
            if (SPECIAL_KEYS.contains(field.getKey()))
                return;
            Optional<Method> method = getSetterByName(object, getSetterForField(field.getKey()));
            if (method.isPresent()) {
                Class<?> argType = method.get().getParameterTypes()[0];
                Converter converter = converterFactory.getConverter(argType);
                safeCallInvoke(object, field, mappedObjects, method, argType, converter);
            } else {
                Optional<Field> fieldByName = getFieldByName(object, field.getKey());
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    Converter converter = converterFactory.getConverter(argType);
                    safeCallSetField(object, field, mappedObjects, fieldByName, argType, converter);
                } else
                    throw new TransformerException("No setter nor field " + field.getKey() + " could be found in class " + object.getClass().getName() + "; context: " + field.getValue());
            }
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void safeCallSetField(Object object, Map.Entry<String, JsonNode> field, Map<String, Object> mappedObjects, Optional<Field> fieldByName, Class<?> argType, Converter converter) throws TransformerException {
        try {
            converter.setField(fieldByName.get(), object, field.getValue(), mappedObjects, argType);
        } catch (IncapableToExecuteTypedConversionException e) {
            converter.setField(fieldByName.get(), object, field.getValue(), mappedObjects, Object.class);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void safeCallInvoke(Object object, Map.Entry<String, JsonNode> field, Map<String, Object> mappedObjects, Optional<Method> method, Class<?> argType, Converter converter) throws TransformerException {
        try {
            converter.invoke(method.get(), object, field.getValue(), mappedObjects, argType);
        } catch (IncapableToExecuteTypedConversionException e) {
            converter.invoke(method.get(), object, field.getValue(), mappedObjects, Object.class);
        }
    }

    private Optional<Field> getFieldByName(Object object, String fieldName) {
        for (Field field : object.getClass().getFields()) {
            if (field.getName().equals(fieldName)) {
                return Optional.of(field);
            }
        }
        return Optional.absent();
    }

    private Optional<Method> getSetterByName(Object object, String setterName) {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                return Optional.of(method);
            }
        }
        return Optional.absent();
    }

    private String getSetterForField(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase(Locale.getDefault()) + fieldName.substring(1); //NON-NLS
    }

    private Object createObject(Object parent, JsonNode objectDefinition, Map<String, Object> mappedObjects) throws TransformerException {
        try {
            if (!objectDefinition.has(KEY_SPECIAL_TYPE))
                throw new IllegalArgumentException("Could not deduce the child type without explicit definition: " + objectDefinition);
            Class<?> widgetClass = objectConverter.deduceClassFromNode(objectDefinition);
            Constructor<?> chosenConstructor = null;

            Constructor<?>[] constructors = widgetClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 2) {
                    if ((Composite.class.isAssignableFrom(parameterTypes[0]) ||
                            Menu.class.isAssignableFrom(parameterTypes[0])) &&
                            parameterTypes[1].equals(int.class)) {
                        chosenConstructor = constructor;
                        break;
                    }
                }
            }

            if (chosenConstructor == null)
                throw new TransformerException("Could not find adequate constructor(? extends Composite, int) in class "
                        + widgetClass.getName());

            int style = widgetClass == Shell.class ? DEFAULT_STYLE_SHELL : DEFAULT_STYLE_REST;
            if (objectDefinition.has(KEY_SPECIAL_STYLE)) {
                JsonNode styleNode = objectDefinition.get(KEY_SPECIAL_STYLE);
                TypedConverter<Integer> exactTypeConverter = (TypedConverter<Integer>)
                        converterFactory.getExactTypeConverter(int.class).get();
                style = exactTypeConverter.getValueFromJson(styleNode, mappedObjects);
            }

            final Object objectInstance = chosenConstructor.newInstance(parent, style);

            deSerializeObjectFromNode(objectDefinition, objectInstance, mappedObjects);

            return objectInstance;
        } catch (Exception e) {
            throw new TransformerException("Widget creation of class failed", e);
        }
    }

}
