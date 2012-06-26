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
 * <p/>
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
            method.invoke(targetObject, getValueFromJson(targetObject, value, mappedObjects));
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Wrapped invoke failed: method=" + method +
                    ", targetObject=" + targetObject + ", json=" + (value == null ? "<NULL>" : value.asText()) +
                    ", argType=" + argType, e);
        }
    }

    @Override
    public final void setField(Field field, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<Object> argType) throws TransformerException {
        try {
            final Object valueFromJson = getValueFromJson(targetObject, value, mappedObjects);
            field.set(targetObject, valueFromJson);
        } catch (IllegalAccessException e) {
            throw new TransformerException("Wrapped setField failed: ", e);
        }
    }

    @Override
    public void cleanUp() {
    }

    private Object getValueFromJson(Object targetObject, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        final TransformationWorkingContext transformationWorkingContext = new TransformationWorkingContext();
        transformationWorkingContext.setWorkItem(targetObject);
        transformationWorkingContext.mapAll(mappedObjects);
        return getValueFromJson(transformationWorkingContext, value);
    }

    private Object getValueFromJson(TransformationWorkingContext context, JsonNode node) throws TransformerException {
        if (!node.isTextual()) {
            final TransformationWorkingContext widgetFromNode = createWidgetFromNode(context, node);
            if (node.has(KEY_SPECIAL_NAME)) {
                String objectName = node.get(KEY_SPECIAL_NAME).asText();
                context.mapObject(objectName, widgetFromNode.getWorkItem());
            }
            return widgetFromNode.getWorkItem();
        }

        String originalValue = node.asText();

        Matcher matcher = injectedObjectValue.matcher(originalValue);
        if (matcher.matches())
            return provideObjectFromDIContainer(context, matcher.group(1));

        matcher = builderValue.matcher(originalValue);
        if (matcher.matches()) {
            final BuilderContext<?> builderContext = constructObjectUsingBuilderNotation(context, matcher.group(1), matcher.group(2));
            if (builderContext.getName() != null)
                context.mapObject(builderContext.getName(), builderContext.getBuiltElement());
            return builderContext.getBuiltElement();
        }

        throw new TransformerException("Invalid syntax for object definition - " + originalValue);
    }

    private BuilderContext<?> constructObjectUsingBuilderNotation(TransformationWorkingContext context, String builderName, String parameters) throws TransformerException {
        final List<String> params = Lists.newArrayList(Splitter.on(",").trimResults().split(parameters));
        final Builder<?> builder = registeredBuilders.get(builderName);
        if (builder == null)
            throw new TransformerException("Builder is not registered: " + builderName);
        return builder.create(context.getWorkItem(), params);
    }

    private Object provideObjectFromDIContainer(TransformationWorkingContext mappedObjects, String magicName) throws TransformerException {
        Object mappedObject = mappedObjects.getMappedObject(magicName);
        if (mappedObject == null)
            mappedObject = objectProvider.provideObjectNamed(magicName);
        return mappedObject;
    }

    TransformationWorkingContext createWidgetFromNode(TransformationWorkingContext context, JsonNode value) throws TransformerException {
        try {
            TransformationWorkingContext ofTheJedi = isWidgetUsingBuilder(value)
                    ? createWidgetUsingBuilder(context, value)
                    : createWidgetUsingClassInstantiation(context, value);
            transformNodeToProperties(ofTheJedi, value);
            return ofTheJedi;
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Widget creation failed", e);
        }
    }

    private TransformationWorkingContext createWidgetUsingBuilder(TransformationWorkingContext context, JsonNode value) throws TransformerException {
        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        final Matcher matcher = builderValue.matcher(value.get(KEY_SPECIAL_TYPE).asText());
        final boolean processingResult = matcher.matches();
        checkState(processingResult);
        final BuilderContext<?> builderContext = constructObjectUsingBuilderNotation(context, matcher.group(1), matcher.group(2));
        if (builderContext.getName() != null)
            ofTheJedi.mapObject(builderContext.getName(), builderContext.getBuiltElement());
        ofTheJedi.setWorkItem(builderContext.getBuiltElement());
        return ofTheJedi;
    }

    private TransformationWorkingContext createWidgetUsingClassInstantiation(TransformationWorkingContext context, JsonNode objectDefinition)
            throws TransformerException, IllegalAccessException, InstantiationException, InvocationTargetException {
        final Class<?> widgetClass = deduceClassFromNode(objectDefinition);
        int style = widgetClass == Shell.class ? DEFAULT_STYLE_SHELL : DEFAULT_STYLE_REST;
        if (objectDefinition.has(KEY_SPECIAL_STYLE)) {
            JsonNode styleNode = objectDefinition.get(KEY_SPECIAL_STYLE);
            TypedConverter<Integer> exactTypeConverter = (TypedConverter<Integer>)
                    converterFactory.getExactTypeConverter(int.class).get();
            style = exactTypeConverter.getValueFromJson(styleNode, context.getMappedObjects());
        }

        if (context.isDoNotCreateModalDialogs()) {
            style = style & (~SWT.APPLICATION_MODAL);
            style = style & (~SWT.SYSTEM_MODAL);
            style = style & (~SWT.PRIMARY_MODAL);
        }
        final Object instanceOfSWTWidget = createInstanceOfSWTWidget(context.getWorkItem(), widgetClass, style);
        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        ofTheJedi.setWorkItem(instanceOfSWTWidget);
        if (objectDefinition.has(KEY_SPECIAL_NAME)) {
            String objectName = objectDefinition.get(KEY_SPECIAL_NAME).asText();
            context.mapObject(objectName, instanceOfSWTWidget);
        }
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
        if (chosenConstructor.getParameterTypes().length == 0)
            return chosenConstructor.newInstance();
        if (Device.class.isAssignableFrom(chosenConstructor.getParameterTypes()[0])) {
            final Widget parentAsWidget = (Widget) parent;
            if (parentAsWidget == null)
                throw new TransformerException("Null parent widget detected! parent=" + parent + ", widgetClass=" + widgetClass);
            return chosenConstructor.newInstance(parentAsWidget.getDisplay(), style);
        } else
            return chosenConstructor.newInstance(parent, style);
    }

    private Constructor<?> findAppropriateSWTStyledConstructor(Class<?> widgetClass) throws TransformerException {
        Constructor<?> defaultConstructor = null;
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
            } else if (parameterTypes.length == 0)
                defaultConstructor = constructor;
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
        if (defaultConstructor != null)
            return defaultConstructor;
        throw new TransformerException("Could not find adequate default constructor or constructor of type " +
                "(? extends {Device,Composite,Menu,Control}, int) in class " + widgetClass.getName());
    }

    private void transformChildren(TransformationWorkingContext context, JsonNode childrenNodes) throws TransformerException {
        final Object parentWidget = context.getWorkItem();
        if (!(parentWidget instanceof Composite) && !(parentWidget instanceof Menu))
            throw new IllegalStateException("Can not create children for parent which is not Composite nor Menu (" + parentWidget.getClass().getName() + " in this case)");
        try {
            if (childrenNodes.isArray())
                transformChildrenAsArray(context, childrenNodes);
            else
                transformChildrenAsShortHandSyntax(context, childrenNodes);
        } catch (IOException e) {
            throw new TransformerException("IO exception while trying to parse child nodes", e);
        }
    }

    private void transformChildrenAsShortHandSyntax(TransformationWorkingContext context, JsonNode childrenNodes) {
        final Iterator<String> fieldNames = childrenNodes.getFieldNames();
        while (fieldNames.hasNext()) {
            final String field = fieldNames.next();
            System.out.println("Found field " + field);
            System.out.println("Field value is " + childrenNodes.get(field));
        }
    }

    private void transformChildrenAsArray(TransformationWorkingContext context, JsonNode childrenNodes) throws TransformerException, IOException {
        for (JsonNode node : mapper.readValue(childrenNodes, JsonNode[].class)) {
            getValueFromJson(context, node);
        }
    }

    private void transformNodeToProperties(TransformationWorkingContext context, JsonNode jsonNode) throws TransformerException {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getKey().equals(KEY_SPECIAL_CHILDREN))
                transformChildren(context, field.getValue());
            else
                transformSingleJsonNode(context, field);
        }
    }

    private void transformSingleJsonNode(TransformationWorkingContext context, Map.Entry<String, JsonNode> field) throws TransformerException {
        try {
            if (SPECIAL_KEYS.contains(field.getKey()))
                return;
            Optional<Method> method = getSetterByName(context.getWorkItem(), getSetterForField(field.getKey()));
            if (method.isPresent()) {
                Class<?> argType = method.get().getParameterTypes()[0];
                Converter converter = converterFactory.getConverter(argType);
                safeCallInvoke(context, field, method, argType, converter);
            } else {
                Optional<Field> fieldByName = getFieldByName(context.getWorkItem(), field.getKey());
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    Converter converter = converterFactory.getConverter(argType);
                    safeCallSetField(context, field, fieldByName, argType, converter);
                } else
                    throw new TransformerException("No setter nor field " + field.getKey() + " could be found in class " + context.getWorkItem().getClass().getName() + "; context: " + field.getValue());
            }
        } catch (TransformerException e) {
            throw e;
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void safeCallSetField(TransformationWorkingContext context, Map.Entry<String, JsonNode> field, Optional<Field> fieldByName, Class<?> argType, Converter converter) throws TransformerException {
        try {
            converter.setField(fieldByName.get(), context.getWorkItem(), field.getValue(), context.getMappedObjects(), argType);
        } catch (IncapableToExecuteTypedConversionException e) {
            converter.setField(fieldByName.get(), context.getWorkItem(), field.getValue(), context.getMappedObjects(), Object.class);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void safeCallInvoke(TransformationWorkingContext context, Map.Entry<String, JsonNode> field, Optional<Method> method, Class<?> argType, Converter converter) throws TransformerException {
        try {
            converter.invoke(method.get(), context.getWorkItem(), field.getValue(), context.getMappedObjects(), argType);
        } catch (IncapableToExecuteTypedConversionException e) {
            converter.invoke(method.get(), context.getWorkItem(), field.getValue(), context.getMappedObjects(), Object.class);
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
