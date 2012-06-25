package net.milanaleksic.guitransformer;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.guitransformer.builders.BuilderContext;
import net.milanaleksic.guitransformer.providers.ObjectProvider;
import net.milanaleksic.guitransformer.typed.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 *
 * ObjectConverter's soul purpose is to convert object nodes to SWT objects
 */
public class ObjectConverter implements Converter<Object> {

    private static final Pattern builderValue = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]*)\\)"); //NON-NLS

    private static final Pattern injectedObjectValue = Pattern.compile("\\((.*)\\)");

    public static final int DEFAULT_STYLE_SHELL = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
    public static final int DEFAULT_STYLE_REST = SWT.NONE;


    public static final String GUI_TRANSFORMER_SHORTCUTS_PROPERTIES = "/META-INF/guitransformer.shortcuts.properties"; //NON-NLS

    static final String KEY_SPECIAL_TYPE = "_type"; //NON-NLS
    static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    static final String KEY_SPECIAL_NAME = "_name"; //NON-NLS
    static final String KEY_SPECIAL_STYLE = "_style"; //NON-NLS
    static final String KEY_SPECIAL_COMMENT = "__comment"; //NON-NLS

    private ObjectMapper mapper;

    private static final Set<String> SPECIAL_KEYS = ImmutableSet
            .<String>builder()
            .add(KEY_SPECIAL_TYPE)
            .add(KEY_SPECIAL_CHILDREN)
            .add(KEY_SPECIAL_NAME)
            .add(KEY_SPECIAL_STYLE)
            .add(KEY_SPECIAL_COMMENT)
            .build();

    @Inject
    private ObjectProvider objectProvider;

    @Inject
    private ConverterFactory converterFactory;

    private Map<String, Builder<?>> registeredBuilders;

    private final ImmutableMap<String, Class<?>> registeredShortcuts;

    @Inject
    public void setRegisteredBuilders(Map<String, Builder<?>> registeredBuilders) {
        this.registeredBuilders = registeredBuilders;
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static ImmutableMap<String, Class<?>> knownShortcuts = ImmutableMap
            .<String, Class<?>>builder()

            .put("gridData", org.eclipse.swt.layout.GridData.class)
            .put("gridLayout", org.eclipse.swt.layout.GridLayout.class)

            .put("shell", org.eclipse.swt.widgets.Shell.class)
            .put("button", org.eclipse.swt.widgets.Button.class)
            .put("canvas", org.eclipse.swt.widgets.Canvas.class)
            .put("composite", org.eclipse.swt.widgets.Composite.class)
            .put("group", org.eclipse.swt.widgets.Group.class)
            .put("label", org.eclipse.swt.widgets.Label.class)
            .put("tabFolder", org.eclipse.swt.widgets.TabFolder.class)
            .put("tabItem", org.eclipse.swt.widgets.TabItem.class)
            .put("table", org.eclipse.swt.widgets.Table.class)
            .put("tableColumn", org.eclipse.swt.widgets.TableColumn.class)
            .put("link", org.eclipse.swt.widgets.Link.class)
            .put("list", org.eclipse.swt.widgets.List.class)
            .put("text", org.eclipse.swt.widgets.Text.class)
            .put("combo", org.eclipse.swt.widgets.Combo.class)
            .put("toolBar", org.eclipse.swt.widgets.ToolBar.class)
            .put("toolItem", org.eclipse.swt.widgets.ToolItem.class)
            .put("menu", org.eclipse.swt.widgets.Menu.class)
            .put("menuItem", org.eclipse.swt.widgets.MenuItem.class)

            .put("cursor", org.eclipse.swt.graphics.Cursor.class)

            .put("scrolledComposite", org.eclipse.swt.custom.ScrolledComposite.class)
            .put("styledText", org.eclipse.swt.custom.StyledText.class)

            .put("dropTarget", org.eclipse.swt.dnd.DropTarget.class)
            .put("dragSource", org.eclipse.swt.dnd.DragSource.class)

            .build();


    public ObjectConverter() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        registeredShortcuts = ImmutableMap.<String, Class<?>>builder()
                .putAll(knownShortcuts)
                .putAll(getStringToClassMappingFromPropertiesFile(GUI_TRANSFORMER_SHORTCUTS_PROPERTIES))
                .build();
    }

    private Map<? extends String, ? extends Class<?>> getStringToClassMappingFromPropertiesFile(String propertiesLocation) {
        Map<String, Class<?>> ofTheJedi = new HashMap<String, Class<?>>();
        try {
            final InputStream additionalShortcutsStream = ObjectConverter.class.getResourceAsStream(propertiesLocation);
            if (additionalShortcutsStream == null)
                return ofTheJedi;
            final Properties properties = new Properties();
            properties.load(additionalShortcutsStream);
            for (Map.Entry entry : properties.entrySet()) {
                ofTheJedi.put(entry.getKey().toString(), Class.forName(entry.getValue().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ofTheJedi;
    }

    @Override
    public final void invoke(Method method, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<Object> argType) throws TransformerException {
        try {
            method.invoke(targetObject, getValueFromJson(targetObject, value, mappedObjects, argType));
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Wrapped invoke failed: method="+method+
                    ", targetObject="+targetObject+", json="+(value==null?"<NULL>":value.asText())+
                    ", argType="+argType, e);
        }
    }

    @Override
    public final void setField(Field field, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<Object> argType) throws TransformerException {
        try {
            field.set(targetObject, getValueFromJson(targetObject, value, mappedObjects, argType));
        } catch (IllegalAccessException e) {
            throw new TransformerException("Wrapped setField failed: ", e);
        }
    }

    @Override
    public void cleanUp() {
    }

    private Object getValueFromJson(Object parent, JsonNode node, Map<String, Object> mappedObjects, Class<?> argType) throws TransformerException {
        if (!node.isTextual())
            return createWidgetFromNode(parent, argType, node, mappedObjects);

        String originalValue = node.asText();

        Matcher matcher = injectedObjectValue.matcher(originalValue);
        if (matcher.matches())
            return provideObjectFromDIContainer(mappedObjects, matcher.group(1));

        matcher = builderValue.matcher(originalValue);
        if (matcher.matches()) {
            final BuilderContext<?> builderContext = constructObjectUsingBuilderNotation(parent, matcher.group(1), matcher.group(2));
            if (builderContext.getName() != null)
                mappedObjects.put(builderContext.getName(), builderContext.getBuiltElement());
            return builderContext.getBuiltElement();
        }

        throw new TransformerException("Invalid syntax for object definition - " + originalValue);
    }

    private BuilderContext<?> constructObjectUsingBuilderNotation(Object parent, String builderName, String parameters) throws TransformerException {
        final List<String> params = Lists.newArrayList(Splitter.on(",").trimResults().split(parameters));
        final Builder<?> builder = registeredBuilders.get(builderName);
        if (builder == null)
            throw new TransformerException("Builder is not registered: " + builderName);
        return builder.create(parent, params);
    }

    private Object provideObjectFromDIContainer(Map<String, Object> mappedObjects, String magicName) throws TransformerException {
        Object mappedObject = mappedObjects.get(magicName);
        if (mappedObject == null)
            mappedObject = objectProvider.provideObjectNamed(magicName);
        return mappedObject;
    }

    private Object createWidgetFromNode(Object parent, Class<?> widgetClass, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        try {
            Object ofTheJedi = isWidgetUsingBuilder(value)
                    ? createWidgetUsingBuilder(parent, value, mappedObjects)
                    : createWidgetUsingClassInstantination(parent, widgetClass, value);
            transformNodeToProperties(value, ofTheJedi, mappedObjects);
            return ofTheJedi;
        } catch (Exception e) {
            throw new TransformerException("Widget creation of class " + widgetClass.getName() + " failed", e);
        }
    }

    private Object createWidgetUsingBuilder(Object parent, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        final Matcher matcher = builderValue.matcher(value.get(KEY_SPECIAL_TYPE).asText());
        final boolean processingResult = matcher.matches();
        checkState(processingResult);
        final BuilderContext<?> builderContext = constructObjectUsingBuilderNotation(parent, matcher.group(1), matcher.group(2));
        if (builderContext.getName() != null)
            mappedObjects.put(builderContext.getName(), builderContext.getBuiltElement());
        return builderContext.getBuiltElement();
    }

    private Object createWidgetUsingClassInstantination(Object parent, Class<?> widgetClass, JsonNode value) throws TransformerException, IllegalAccessException, InstantiationException {
        Object ofTheJedi;
        Class<?> deducedClass = deduceClassFromNode(value);
        if (deducedClass != null)
            widgetClass = deducedClass;
        ofTheJedi = widgetClass.newInstance();
        return ofTheJedi;
    }

    private boolean isWidgetUsingBuilder(JsonNode value) {
        return value.has(KEY_SPECIAL_TYPE) && builderValue.matcher(value.get(KEY_SPECIAL_TYPE).asText()).matches();
    }

    private Class<?> deduceClassFromNode(JsonNode value) throws TransformerException {
        if (value.has(KEY_SPECIAL_TYPE)) {
            String classIdentifier = value.get(KEY_SPECIAL_TYPE).asText();
            Class<?> aClass = registeredShortcuts.get(classIdentifier);
            if (aClass != null)
                return aClass;
            else
                try {
                    return Class.forName(classIdentifier);
                } catch (ClassNotFoundException e) {
                    throw new TransformerException("Class was not found: " + classIdentifier, e);
                }
        }
        return null;
    }

    private Object createInstanceOfSWTWidget(Object parent, Class<?> widgetClass, int style) throws TransformerException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?> chosenConstructor = findAppropriateSWTStyledConstructor(widgetClass);
        if (Device.class.isAssignableFrom(chosenConstructor.getParameterTypes()[0])) {
            final Widget parentAsWidget = (Widget) parent;
            if (parentAsWidget == null)
                throw new TransformerException("Null parent widget detected! parent=" + parent + ", widgetClass=" + widgetClass);
            return chosenConstructor.newInstance(parentAsWidget.getDisplay(), style);
        } else
            return chosenConstructor.newInstance(parent, style);
    }

    private Constructor<?> findAppropriateSWTStyledConstructor(Class<?> widgetClass) throws TransformerException {
        Constructor<?>[] constructors = widgetClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 2) {
                if ((Composite.class.isAssignableFrom(parameterTypes[0]) ||   // most cases
                        Menu.class.isAssignableFrom(parameterTypes[0]) ||        // in case MenuItems
                        Control.class.isAssignableFrom(parameterTypes[0])) &&   // in case of DropTarget
                        parameterTypes[1].equals(int.class)) {
                    return constructor;
                }
            }
        }
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 2) {
                if (Device.class.isAssignableFrom(parameterTypes[0])   // in case of Cursor
                        && parameterTypes[1].equals(int.class)) {
                    return constructor;
                }
            }
        }
        throw new TransformerException("Could not find adequate constructor(? extends {Device,Composite,Menu,Control}, int) in class "
                + widgetClass.getName());
    }

    Object createObject(@Nullable Object parent, JsonNode objectDefinition, Map<String, Object> mappedObjects, boolean doNotCreateModalDialogs) throws TransformerException {
        try {
            if (!objectDefinition.has(KEY_SPECIAL_TYPE))
                throw new IllegalArgumentException("Could not deduce the child type without explicit definition: " + objectDefinition);
            Object objectInstance;
            if (isWidgetUsingBuilder(objectDefinition))
                objectInstance = createWidgetUsingBuilder(parent, objectDefinition, mappedObjects);
            else {
                Class<?> widgetClass = deduceClassFromNode(objectDefinition);

                int style = widgetClass == Shell.class ? DEFAULT_STYLE_SHELL : DEFAULT_STYLE_REST;
                if (objectDefinition.has(KEY_SPECIAL_STYLE)) {
                    JsonNode styleNode = objectDefinition.get(KEY_SPECIAL_STYLE);
                    TypedConverter<Integer> exactTypeConverter = (TypedConverter<Integer>)
                            converterFactory.getExactTypeConverter(int.class).get();
                    style = exactTypeConverter.getValueFromJson(styleNode, mappedObjects);
                }

                if (doNotCreateModalDialogs) {
                    style = style & (~SWT.APPLICATION_MODAL);
                    style = style & (~SWT.SYSTEM_MODAL);
                    style = style & (~SWT.PRIMARY_MODAL);
                }

                objectInstance = createInstanceOfSWTWidget(parent, widgetClass, style);

                if (objectDefinition.has(KEY_SPECIAL_NAME)) {
                    String objectName = objectDefinition.get(KEY_SPECIAL_NAME).asText();
                    mappedObjects.put(objectName, objectInstance);
                }
            }
            transformNodeToProperties(objectDefinition, objectInstance, mappedObjects);
            return objectInstance;
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Widget creation of class failed", e);
        }
    }

    private void transformChildren(JsonNode childrenNodes, Object parentWidget, Map<String, Object> mappedObjects) throws TransformerException {
        if (!(parentWidget instanceof Composite) && !(parentWidget instanceof Menu))
            throw new IllegalStateException("Can not create children for parent which is not Composite nor Menu (" + parentWidget.getClass().getName() + " in this case)");
        try {
            for (JsonNode node : mapper.readValue(childrenNodes, JsonNode[].class)) {
                // TODO: parent hierarchy stack!
                createObject(parentWidget, node, mappedObjects, false);
            }
        } catch (IOException e) {
            throw new TransformerException("IO exception while trying to parse child nodes", e);
        }
    }

    private void transformNodeToProperties(JsonNode jsonNode, Object object, Map<String, Object> mappedObjects) throws TransformerException {
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
        } catch (TransformerException e) {
            throw e;
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

}
