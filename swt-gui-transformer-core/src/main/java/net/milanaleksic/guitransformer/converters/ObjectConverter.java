package net.milanaleksic.guitransformer.converters;

import com.google.common.collect.ImmutableSet;
import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.builders.BuilderContext;
import net.milanaleksic.guitransformer.providers.ObjectProvider;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
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

    static final String KEY_SPECIAL_TYPE = "_type"; //NON-NLS
    static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    static final String KEY_SPECIAL_NAME = "_name"; //NON-NLS
    static final String KEY_SPECIAL_STYLE = "_style"; //NON-NLS
    static final String KEY_SPECIAL_COMMENT = "__comment"; //NON-NLS

    static final Set<String> SPECIAL_KEYS = ImmutableSet
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
    private EmbeddingService embeddingService;

    @Inject
    private NodeProcessor nodeProcessor;

    public ObjectConverter() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationWorkingContext createHierarchy(TransformationWorkingContext context, String content) {
        try {
            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            return create(context, null, shellDefinition);
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + context.getFormName(), e);
        }
    }

    public TransformationWorkingContext createHierarchy(Object formObject, TransformationWorkingContext context, InputStream content) {
        try {
            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            final TransformationWorkingContext transformationWorkingContext = create(context, null, shellDefinition);
            if (formObject != null)
                embeddingService.embed(formObject, transformationWorkingContext);
            return transformationWorkingContext;
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + context.getFormName(), e);
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
    public Object getValueFromJson(Object targetObject, JsonNode value, Map<String, Object> mappedObjects) {
        final TransformationWorkingContext transformationWorkingContext = new TransformationWorkingContext();
        transformationWorkingContext.setWorkItem(targetObject);
        transformationWorkingContext.mapAll(mappedObjects);
        return getValueFromJson(transformationWorkingContext, value);
    }

    private Object getValueFromJson(TransformationWorkingContext context, JsonNode node) {
        if (!node.isTextual()) {
            final TransformationWorkingContext widgetFromNode = create(context, null, node);
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

        throw new TransformerException("Invalid syntax for object definition - " + originalValue);
    }

    private Object provideObjectFromDIContainer(TransformationWorkingContext mappedObjects, String magicName) {
        Object mappedObject = mappedObjects.getMappedObject(magicName);
        if (mappedObject == null)
            mappedObject = objectProvider.provideObjectNamed(magicName);
        return mappedObject;
    }

    private void transformNodeToProperties(TransformationWorkingContext context, JsonNode jsonNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getKey().equals(ObjectConverter.KEY_SPECIAL_CHILDREN))
                transformChildren(context, field.getValue());
            else
                nodeProcessor.visitSingleField(context, field.getKey(), field.getValue());
        }
    }

    void transformChildren(TransformationWorkingContext context, JsonNode childrenNodes) {
        final Object parentWidget = context.getWorkItem();
        if (!(parentWidget instanceof Composite) && !(parentWidget instanceof Menu))
            throw new IllegalStateException("Can not create children for parent which is not Composite nor Menu (" + parentWidget.getClass().getName() + " in this case)");
        try {
            if (childrenNodes.isArray())
                transformChildrenAsArray(context, childrenNodes);
            else
                transformChildrenUsingShortHandSyntax(context, childrenNodes);
        } catch (IOException e) {
            throw new TransformerException("IO exception while trying to parse child nodes", e);
        }
    }

    private void transformChildrenUsingShortHandSyntax(TransformationWorkingContext context, JsonNode childrenNodes) {
        final Iterator<String> fieldNames = childrenNodes.getFieldNames();
        while (fieldNames.hasNext()) {
            final String field = fieldNames.next();
            create(context, field, childrenNodes.get(field));
        }
    }

    private void transformChildrenAsArray(TransformationWorkingContext context, JsonNode childrenNodes) throws IOException {
        for (JsonNode node : mapper.readValue(childrenNodes, JsonNode[].class)) {
            getValueFromJson(context, node);
        }
    }

}
