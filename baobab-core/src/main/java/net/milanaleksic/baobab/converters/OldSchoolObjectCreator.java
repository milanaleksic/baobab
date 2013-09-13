package net.milanaleksic.baobab.converters;

import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.converters.typed.IntegerConverter;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.widgets.Shell;

import java.util.regex.Matcher;

import static com.google.common.base.Preconditions.checkState;

/**
 * User: Milan Aleksic
 * Date: 2/5/13
 * Time: 2:19 PM
 */
class OldSchoolObjectCreator extends ObjectCreator {

    @Override
    public boolean isEligibleForItem(String key, JsonNode value) {
        return key == null;
    }

    @Override
    public boolean isWidgetUsingBuilder(String key, JsonNode value) {
        return value.has(ObjectConverter.KEY_SPECIAL_TYPE) &&
                ObjectConverter.builderValue.matcher(value.get(ObjectConverter.KEY_SPECIAL_TYPE).asText()).matches();
    }

    @Override
    public TransformationWorkingContext createWidgetUsingBuilder(TransformationWorkingContext context, String key, JsonNode value) {
        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        final Matcher matcher = ObjectConverter.builderValue.matcher(value.get(ObjectConverter.KEY_SPECIAL_TYPE).asText());
        final boolean processingResult = matcher.matches();
        checkState(processingResult);
        final BuilderContext<?> builderContext = nodeProcessor.visitBuilderNotationItem(context, matcher.group(1), matcher.group(2));
        if (builderContext.getName() != null)
            ofTheJedi.mapObject(builderContext.getName(), builderContext.getBuiltElement());
        ofTheJedi.setWorkItem(builderContext.getBuiltElement());
        if (value.has(ObjectConverter.KEY_SPECIAL_NAME)) {
            String objectName = value.get(ObjectConverter.KEY_SPECIAL_NAME).asText();
            context.mapObject(objectName, builderContext.getBuiltElement());
        }
        return ofTheJedi;
    }

    @Override
    public TransformationWorkingContext createWidgetUsingClassInstantiation(TransformationWorkingContext context, String key, JsonNode objectDefinition) {
        final Class<?> widgetClass = deduceClassFromNode(objectDefinition);
        int style = widgetClass == Shell.class ? DEFAULT_STYLE_SHELL : DEFAULT_STYLE_REST;
        if (objectDefinition.has(ObjectConverter.KEY_SPECIAL_STYLE)) {
            JsonNode styleNode = objectDefinition.get(ObjectConverter.KEY_SPECIAL_STYLE);
            IntegerConverter exactTypeConverter = (IntegerConverter)
                    converterProvider.provideTypedConverterForClass(int.class).get();
            style = exactTypeConverter.getValueFromString(styleNode.asText());
        }

        style = fixStyleIfNoModalDialogs(context, style);
        final Object instanceOfSWTWidget = nodeProcessor.visitObjectItem(widgetClass, context.getWorkItem(), style);
        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        ofTheJedi.setWorkItem(instanceOfSWTWidget);
        if (objectDefinition.has(ObjectConverter.KEY_SPECIAL_NAME)) {
            String objectName = objectDefinition.get(ObjectConverter.KEY_SPECIAL_NAME).asText();
            context.mapObject(objectName, instanceOfSWTWidget);
        }
        return ofTheJedi;
    }
}
