package net.milanaleksic.baobab.converters;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.converters.typed.IntegerConverter;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.widgets.Shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 2/5/13
 * Time: 2:19 PM
 */
class ShortHandObjectCreator extends ObjectCreator {

    private static final Pattern builderValueShortHandSyntaxKey = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]*)\\)\\(([^\\),]*)\\s*,?\\s*([^\\)]*)\\)"); //NON-NLS
    private static final Pattern shortHandSyntaxKey = Pattern.compile("([^\\)]+)\\(([^\\),]*)\\s*,?\\s*([^\\)]*)\\)"); //NON-NLS

    @Override
    public boolean isEligibleForItem(String key, JsonNode value) {
        return (key != null) && (shortHandSyntaxKey.matcher(key).matches()
                || builderValueShortHandSyntaxKey.matcher(key).matches());
    }

    @Override
    public boolean isWidgetUsingBuilder(String key, JsonNode value) {
        return builderValueShortHandSyntaxKey.matcher(key).matches();
    }

    @Override
    public TransformationWorkingContext createWidgetUsingBuilder(TransformationWorkingContext context, String key, JsonNode value) {
        final Matcher matcher = builderValueShortHandSyntaxKey.matcher(key);
        Preconditions.checkArgument(matcher.matches(), "Invalid short hand syntax detected: " + key);
        String builderName = matcher.group(1);
        String builderParams = matcher.group(2);
        String name = matcher.group(3);
        String styleDefinition = matcher.group(4);

        if (!Strings.isNullOrEmpty(styleDefinition))
            throw new TransformerException("When using short-hand syntax + builder notation for object creation, you can't set styles " +
                    "as parameter for short-hand constructor - styling must be set in builder");

        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        final BuilderContext<?> builderContext = nodeProcessor.visitBuilderNotationItem(context, builderName, builderParams);
        Preconditions.checkNotNull(builderContext, "Builders must not return null values (builder \"%s\" for params \"%s\")", builderName, builderParams);
        if (builderContext.getName() != null)
            ofTheJedi.mapObject(builderContext.getName(), builderContext.getBuiltElement());
        ofTheJedi.setWorkItem(builderContext.getBuiltElement());
        context.mapObject(name, builderContext.getBuiltElement());
        return ofTheJedi;
    }

    @Override
    public TransformationWorkingContext createWidgetUsingClassInstantiation(TransformationWorkingContext context, String key, JsonNode value) {
        final Matcher matcher = shortHandSyntaxKey.matcher(key);
        Preconditions.checkArgument(matcher.matches(), "Invalid short hand syntax detected: " + key);
        String typeDefinition = matcher.group(1);
        String name = matcher.group(2);
        String styleDefinition = matcher.group(3);

        final Class<?> widgetClass = deduceClassFromNode(typeDefinition);
        int style = widgetClass == Shell.class ? DEFAULT_STYLE_SHELL : DEFAULT_STYLE_REST;
        if (!Strings.isNullOrEmpty(styleDefinition)) {
            IntegerConverter exactTypeConverter = (IntegerConverter)
                    converterProvider.provideTypedConverterForClass(int.class).get();
            style = exactTypeConverter.getValueFromString(styleDefinition);
        }

        style = fixStyleIfNoModalDialogs(context, style);
        final Object instanceOfSWTWidget = createInstanceOfSWTWidget(widgetClass, context.getWorkItem(), style);
        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        ofTheJedi.setWorkItem(instanceOfSWTWidget);
        context.mapObject(name, instanceOfSWTWidget);
        return ofTheJedi;
    }

}
