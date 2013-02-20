package net.milanaleksic.guitransformer.converters;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.builders.*;
import net.milanaleksic.guitransformer.providers.*;
import net.milanaleksic.guitransformer.util.WidgetCreator;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import static net.milanaleksic.guitransformer.util.ObjectUtil.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:03 PM
 * <p>
 * ObjectConverter's sole purpose is to convert object nodes to SWT objects
 * </p>
 */
public class ObjectConverter implements Converter {

    static final Pattern builderValue = Pattern.compile("\\[([^\\]]+)\\]\\s*\\(([^\\)]*)\\)"); //NON-NLS

    private static final Pattern injectedObjectValue = Pattern.compile("\\((.*)\\)");

    static final String KEY_SPECIAL_TYPE = "_type"; //NON-NLS
    static final String KEY_SPECIAL_CHILDREN = "_children"; //NON-NLS
    static final String KEY_SPECIAL_NAME = "_name"; //NON-NLS
    static final String KEY_SPECIAL_STYLE = "_style"; //NON-NLS
    static final String KEY_SPECIAL_COMMENT = "__comment"; //NON-NLS

    private final ObjectMapper mapper;

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
    private ConverterProvider converterProvider;

    @Inject
    private BuilderProvider builderProvider;

    @Inject
    private ShortcutsProvider shortcutsProvider;

    @Inject
    private EmbeddingService embeddingService;

    @Inject
    private OldSchoolObjectCreator oldSchoolObjectCreator;

    @Inject
    private ShortHandObjectCreator shortHandObjectCreator;

    public ObjectConverter() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationWorkingContext createHierarchy(TransformationWorkingContext context, String content) throws TransformerException {
        try {
            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            return getTransformationWorkingContext(context, shellDefinition);
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + context.getFormName(), e);
        }
    }

    public TransformationWorkingContext createHierarchy(Object formObject, TransformationWorkingContext context, InputStream content) throws TransformerException {
        try {
            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            final TransformationWorkingContext transformationWorkingContext = getTransformationWorkingContext(context, shellDefinition);
            if (formObject != null)
                embeddingService.embed(formObject, transformationWorkingContext);
            return transformationWorkingContext;
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + context.getFormName(), e);
        }
    }

    private TransformationWorkingContext getTransformationWorkingContext(TransformationWorkingContext context, JsonNode shellDefinition) throws TransformerException {
        return oldSchoolObjectCreator.create(context, null, shellDefinition);
    }

    @Override
    public void cleanUp() {
    }

    @Override
    public Object getValueFromJson(Object targetObject, JsonNode value, Map<String, Object> mappedObjects) throws TransformerException {
        final TransformationWorkingContext transformationWorkingContext = new TransformationWorkingContext();
        transformationWorkingContext.setWorkItem(targetObject);
        transformationWorkingContext.mapAll(mappedObjects);
        return getValueFromJson(transformationWorkingContext, value);
    }

    private Object getValueFromJson(TransformationWorkingContext context, JsonNode node) throws TransformerException {
        if (!node.isTextual()) {
            final TransformationWorkingContext widgetFromNode = oldSchoolObjectCreator.create(context, null, node);
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

    BuilderContext<?> constructObjectUsingBuilderNotation(TransformationWorkingContext context, String builderName, String parameters) throws TransformerException {
        final List<String> params = Lists.newArrayList(Splitter.on(",").trimResults().split(parameters));
        final Builder<?> builder = builderProvider.provideBuilderForName(builderName);
        if (builder == null)
            throw new TransformerException("Builder is not registered: " + builderName);
        return builder.create(context.getWorkItem(), params);
    }

    private Object provideObjectFromDIContainer(TransformationWorkingContext mappedObjects, String magicName) {
        Object mappedObject = mappedObjects.getMappedObject(magicName);
        if (mappedObject == null)
            mappedObject = objectProvider.provideObjectNamed(magicName);
        return mappedObject;
    }

    <T> T createInstanceOfSWTWidget(Class<T> widgetClass, Object parent, int style) throws TransformerException {
        try {
            return WidgetCreator.get(widgetClass).newInstance(parent, style);
        } catch (Exception e) {
            throw new TransformerException("Unexpected exception encountered while processing widget creation, widgetClass="+widgetClass.getName()+", parent="+parent+", style="+style, e);
        } catch (VerifyError error) {
            throw new TransformerException("Code generation verify error encountered while processing widget creation, widgetClass="+widgetClass.getName()+", parent="+parent+", style="+style, error);
        }
    }

    void transformChildren(TransformationWorkingContext context, JsonNode childrenNodes) throws TransformerException {
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

    private void transformChildrenUsingShortHandSyntax(TransformationWorkingContext context, JsonNode childrenNodes) throws TransformerException {
        final Iterator<String> fieldNames = childrenNodes.getFieldNames();
        while (fieldNames.hasNext()) {
            final String field = fieldNames.next();
            shortHandObjectCreator.create(context, field, childrenNodes.get(field));
        }
    }

    private void transformChildrenAsArray(TransformationWorkingContext context, JsonNode childrenNodes) throws TransformerException, IOException {
        for (JsonNode node : mapper.readValue(childrenNodes, JsonNode[].class)) {
            getValueFromJson(context, node);
        }
    }

    @SuppressWarnings("unchecked")
    void transformSingleJsonNode(TransformationWorkingContext context, Map.Entry<String, JsonNode> field) throws TransformerException {
        try {
            if (SPECIAL_KEYS.contains(field.getKey()))
                return;

            Optional<Method> method = getSetterByName(context.getWorkItem(), getSetterForField(field.getKey()));
            if (method.isPresent()) {
                Class<?> argType = method.get().getParameterTypes()[0];
                Converter converter = converterProvider.provideConverterForClass(argType);
                Object value = converter.getValueFromJson(context.getWorkItem(), field.getValue(), context.getMutableRootMappedObjects());
                method.get().invoke(context.getWorkItem(), value);
            } else {
                Optional<Field> fieldByName = getFieldByName(context.getWorkItem(), field.getKey());
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    Converter converter = converterProvider.provideConverterForClass(argType);
                    Object value = converter.getValueFromJson(context.getWorkItem(), field.getValue(), context.getMutableRootMappedObjects());
                    fieldByName.get().set(context.getWorkItem(), value);
                } else
                    throw new TransformerException("No setter nor field " + field.getKey() + " could be found in class " + context.getWorkItem().getClass().getName() + "; context: " + field.getValue());
            }
        } catch (TransformerException e) {
            throw e;
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

}
