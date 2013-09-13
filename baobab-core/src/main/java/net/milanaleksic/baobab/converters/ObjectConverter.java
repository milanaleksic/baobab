package net.milanaleksic.baobab.converters;

import com.google.common.collect.ImmutableSet;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.providers.ObjectProvider;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.regex.*;

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

    @Inject
    private OldSchoolObjectCreator oldSchoolObjectCreator;

    @Inject
    private ShortHandObjectCreator shortHandObjectCreator;

    public ObjectConverter() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationWorkingContext createHierarchy(Object formObject, TransformationWorkingContext context, Reader content) {
        try {
            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            final TransformationWorkingContext transformationWorkingContext = create(context, null, shellDefinition);
            if (formObject != null)
                embeddingService.embed(formObject, transformationWorkingContext);
            return transformationWorkingContext;
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form", e);
        }
    }

    private TransformationWorkingContext create(TransformationWorkingContext context, @Nullable String key, JsonNode value) {
        try {
            final TransformationWorkingContext ofTheJedi;
            if (shortHandObjectCreator.isEligibleForItem(key, value))
                ofTheJedi = shortHandObjectCreator.create(context, key, value);
            else if (oldSchoolObjectCreator.isEligibleForItem(key, value))
                ofTheJedi = oldSchoolObjectCreator.create(context, key, value);
            else
                throw new TransformerException("No creator eligible for key=" + key + ", value=" + value);
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
            else {
                if (!ObjectConverter.SPECIAL_KEYS.contains(field.getKey()))
                    nodeProcessor.visitSingleField(context, field.getKey(), field.getValue());
            }
        }
    }

    void transformChildren(TransformationWorkingContext context, JsonNode childrenNodes) {
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
