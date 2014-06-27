package net.milanaleksic.baobab.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.providers.ObjectProvider;
import net.milanaleksic.baobab.util.Preconditions;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 */
public class ObjectConverter implements Converter {

    static final Pattern builderValue = Pattern.compile("\\[([^\\]]+)\\]\\s*\\(([^\\)]*)\\)"); //NON-NLS

    private static final Pattern injectedObjectValue = Pattern.compile("\\((.*)\\)");

    private final ObjectMapper mapper;

    static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    static final String KEY_SPECIAL_DATA = "_data"; //NON-NLS
    static final String KEY_SPECIAL_COMMENT = "__comment"; //NON-NLS

    static final Set<String> SPECIAL_KEYS;

    static {
        Set<String> specialKeys = new HashSet<>();
        specialKeys.add(KEY_SPECIAL_CHILDREN);
        specialKeys.add(KEY_SPECIAL_COMMENT);
        specialKeys.add(KEY_SPECIAL_DATA);
        SPECIAL_KEYS = Collections.unmodifiableSet(specialKeys);
    }

    @Inject
    private ObjectProvider objectProvider;

    @Inject
    private EmbeddingService embeddingService;

    @Inject
    private NodeProcessor nodeProcessor;

    public ObjectConverter() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationWorkingContext createHierarchy(TransformationWorkingContext context, Reader content) {
        try {
            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            Preconditions.checkArgument(shellDefinition.size() == 1, "Hierarchy must be defined with a single root element");
            Map.Entry<String, JsonNode> rootField = shellDefinition.fields().next();
            final TransformationWorkingContext transformationWorkingContext;
            transformationWorkingContext = create(context, rootField.getKey(), rootField.getValue());
            if (context.getFormObject() != null)
                embeddingService.embed(context.getFormObject(), transformationWorkingContext);
            return transformationWorkingContext;
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form", e);
        }
    }

    private TransformationWorkingContext create(TransformationWorkingContext context, @Nullable String key, JsonNode value) {
        try {
            TransformationWorkingContext ofTheJedi = nodeProcessor.visitHierarchyItem(context, key, value);
            transformNodeToProperties(ofTheJedi, value);
            return ofTheJedi;
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Widget creation failed", e);
        }
    }

    @Override
    public void cleanUp() {
    }

    @Override
    public Object getValueFromJson(TransformationWorkingContext context, JsonNode node) {
        if (node.isObject()) {
            Preconditions.checkArgument(node.size() == 1, "Hierarchy must be defined with a single root element");
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            Map.Entry<String, JsonNode> subRoot = fields.next();
            final TransformationWorkingContext widgetFromNode = create(context, subRoot.getKey(), subRoot.getValue());
            return widgetFromNode.getWorkItem();
        }
        String originalValue = node.asText();
        Matcher matcher = injectedObjectValue.matcher(originalValue);
        if (matcher.matches())
            return provideObjectFromDIContainer(context, matcher.group(1));
        matcher = builderValue.matcher(originalValue);
        if (matcher.matches()) {
            final BuilderContext<?> builderContext = nodeProcessor.visitBuilderNotationItem(context, matcher.group(1), matcher.group(2));
            if (builderContext.getName() != null)
                context.mapObject(builderContext.getName(), builderContext.getBuiltElement());
            return builderContext.getBuiltElement();
        }
        return node.asText();
    }

    private Object provideObjectFromDIContainer(TransformationWorkingContext mappedObjects, String magicName) {
        Object mappedObject = mappedObjects.getMappedObject(magicName);
        if (mappedObject == null)
            mappedObject = objectProvider.provideObjectNamed(magicName);
        return mappedObject;
    }

    private void transformNodeToProperties(TransformationWorkingContext context, JsonNode jsonNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            switch (key) {
                case ObjectConverter.KEY_SPECIAL_CHILDREN:
                    transformChildren(context, value);
                    break;
                case ObjectConverter.KEY_SPECIAL_DATA:
                    transformDataFields(context, value);
                    break;
                default:
                    nodeProcessor.visitSingleField(context, key, value);
            }
        }
    }

    private void transformDataFields(TransformationWorkingContext context, JsonNode dataFieldsMapping) {
        final Object workItem = context.getWorkItem();
        if (!(workItem instanceof Widget))
            throw new TransformerException("Can not set data fields for object which is not Widget (" + workItem.getClass().getName() + " in this case)");
        final Widget widget = (Widget) workItem;
        Iterator<Map.Entry<String, JsonNode>> fields = dataFieldsMapping.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            if (value.isTextual())
                widget.setData(key, getValueFromJson(context, value));
            else if (value.isBoolean())
                widget.setData(key, value.asBoolean());
            else if (value.isFloatingPointNumber())
                widget.setData(key, value.asDouble());
            else if (value.isIntegralNumber())
                widget.setData(key, value.asLong());
            else
                throw new TransformerException("Node conversion not supported: " + value);
        }
    }

    void transformChildren(TransformationWorkingContext context, JsonNode childrenNodes) {
        final Object parentWidget = context.getWorkItem();
        if (!(parentWidget instanceof Composite) && !(parentWidget instanceof Menu))
            throw new TransformerException("Can not create children for parent which is not Composite nor Menu (" + parentWidget.getClass().getName() + " in this case)");
        if (childrenNodes.isArray())
            throw new TransformerException("Since version 0.5.0 array children syntax is deprecated");
        transformChildrenUsingShortHandSyntax(context, childrenNodes);
    }

    private void transformChildrenUsingShortHandSyntax(TransformationWorkingContext context, JsonNode childrenNodes) {
        final Iterator<String> fieldNames = childrenNodes.fieldNames();
        while (fieldNames.hasNext()) {
            final String field = fieldNames.next();
            create(context, field, childrenNodes.get(field));
        }
    }

}
