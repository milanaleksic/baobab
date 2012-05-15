package net.milanaleksic.guitransformer;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import net.milanaleksic.guitransformer.providers.ObjectProvider;
import org.codehaus.jackson.JsonNode;

import javax.inject.Inject;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import static com.google.common.base.Preconditions.checkState;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 */
public class ObjectConverter implements Converter<Object> {

    private static final Pattern factoryValue = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]*)\\)"); //NON-NLS

    private static final Pattern springObjectValue = Pattern.compile("\\((.*)\\)");

    public static final String GUI_TRANSFORMER_SHORTCUTS_PROPERTIES = "/META-INF/guitransformer.shortcuts.properties"; //NON-NLS

    @Inject
    private ObjectProvider objectProvider;

    @Inject
    private Transformer transformer;

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
            method.invoke(targetObject, getValueFromJson(value, mappedObjects, argType));
        } catch (Exception e) {
            throw new TransformerException("Wrapped invoke failed: method="+method+
                    ", targetObject="+targetObject+", json="+(value==null?"<NULL>":value.asText())+
                    ", argType="+argType, e);
        }
    }

    @Override
    public final void setField(Field field, Object targetObject, JsonNode value, Map<String, Object> mappedObjects, Class<Object> argType) throws TransformerException {
        try {
            field.set(targetObject, getValueFromJson(value, mappedObjects, argType));
        } catch (IllegalAccessException e) {
            throw new TransformerException("Wrapped setField failed: ", e);
        }
    }

    @Override
    public void cleanUp() {
    }

    protected Object getValueFromJson(JsonNode node, Map<String, Object> mappedObjects, Class<?> argType) throws TransformerException {
        if (!node.isTextual())
            return createWidgetFromNode(argType, node, mappedObjects);

        String originalValue = node.asText();

        Matcher matcher = springObjectValue.matcher(originalValue);
        if (matcher.matches())
            return getSpringObject(mappedObjects, matcher.group(1));

        matcher = factoryValue.matcher(originalValue);
        if (matcher.matches())
            return constructObjectUsingFactoryNotation(matcher.group(1), matcher.group(2));

        throw new TransformerException("Invalid syntax for object definition - " + originalValue);
    }

    private Object constructObjectUsingFactoryNotation(String factoryName, String parameters) throws TransformerException {
        final List<String> params = Lists.newArrayList(Splitter.on(",").trimResults().split(parameters));
        final Builder<?> builder = registeredBuilders.get(factoryName);
        if (builder == null)
            throw new TransformerException("net.milanaleksic.guitransformer.Builder is not registered: " + builder);
        return builder.create(params);
    }

    private Object getSpringObject(Map<String, Object> mappedObjects, String magicName) throws TransformerException {
        Object mappedObject = mappedObjects.get(magicName);
        if (mappedObject == null)
            mappedObject = objectProvider.provideObjectNamed(magicName);
        return mappedObject;
    }

    public Object createWidgetFromNode(Class<?> widgetClass, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        try {
            Object ofTheJedi = isWidgetUsingFactory(value)
                    ? createWidgetUsingFactory(value)
                    : createWidgetUsingClassInstantination(widgetClass, value);
            transformer.transformNodeToProperties(value, ofTheJedi, mappedObjects);
            return ofTheJedi;
        } catch (Exception e) {
            throw new TransformerException("Widget creation of class " + widgetClass.getName() + " failed", e);
        }
    }

    private Object createWidgetUsingFactory(JsonNode value) throws TransformerException {
        final Matcher matcher = factoryValue.matcher(value.get(Transformer.KEY_SPECIAL_TYPE).asText());
        final boolean processingResult = matcher.matches();
        checkState(processingResult);
        return constructObjectUsingFactoryNotation(matcher.group(1), matcher.group(2));
    }

    private Object createWidgetUsingClassInstantination(Class<?> widgetClass, JsonNode value) throws TransformerException, IllegalAccessException, InstantiationException {
        Object ofTheJedi;
        Class<?> deducedClass = deduceClassFromNode(value);
        if (deducedClass != null)
            widgetClass = deducedClass;
        ofTheJedi = widgetClass.newInstance();
        return ofTheJedi;
    }

    private boolean isWidgetUsingFactory(JsonNode value) {
        return value.has(Transformer.KEY_SPECIAL_TYPE) && factoryValue.matcher(value.get(Transformer.KEY_SPECIAL_TYPE).asText()).matches();
    }

    public Class<?> deduceClassFromNode(JsonNode value) throws TransformerException {
        if (value.has(Transformer.KEY_SPECIAL_TYPE)) {
            String classIdentifier = value.get(Transformer.KEY_SPECIAL_TYPE).asText();
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

}
