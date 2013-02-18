package net.milanaleksic.guitransformer.converters;

import com.google.common.base.*;
import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.builders.BuilderContext;
import net.milanaleksic.guitransformer.converters.typed.IntegerConverter;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.*;

/**
 * User: Milan Aleksic
 * Date: 2/5/13
 * Time: 2:19 PM
 */
class ShortHandObjectCreator extends ObjectCreator {

    private static final Pattern builderValueShortHandSyntax = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]*)\\)\\(([^\\),]*)\\s*,?\\s*([^\\)]*)\\)"); //NON-NLS
    private static final Pattern shortHandSyntaxKey = Pattern.compile("([^\\)]+)\\(([^\\),]*)\\s*,?\\s*([^\\)]*)\\)"); //NON-NLS

    protected boolean isWidgetUsingBuilder(String key, JsonNode value) {
        return builderValueShortHandSyntax.matcher(key).matches();
    }

    protected TransformationWorkingContext createWidgetUsingBuilder(TransformationWorkingContext context, String key, JsonNode value) throws TransformerException {
        final Matcher matcher = builderValueShortHandSyntax.matcher(key);
        Preconditions.checkArgument(matcher.matches(), "Invalid short hand syntax detected: " + key);
        String builderName = matcher.group(1);
        String builderParams = matcher.group(2);
        String name = matcher.group(3);
        String styleDefinition = matcher.group(4);

        if (!Strings.isNullOrEmpty(styleDefinition))
            throw new IllegalStateException("When using short-hand syntax + builder notation for object creation, you can't set styles " +
                    "as parameter for short-hand constructor - styling must be set in builder");

        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        final BuilderContext<?> builderContext = objectConverter.constructObjectUsingBuilderNotation(context, builderName, builderParams);
        if (builderContext.getName() != null)
            ofTheJedi.mapObject(builderContext.getName(), builderContext.getBuiltElement());
        ofTheJedi.setWorkItem(builderContext.getBuiltElement());
        context.mapObject(name, builderContext.getBuiltElement());
        return ofTheJedi;
    }

    protected TransformationWorkingContext createWidgetUsingClassInstantiation(TransformationWorkingContext context, String key, JsonNode value)
            throws TransformerException, InvocationTargetException, IllegalAccessException, InstantiationException {
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
        final Object instanceOfSWTWidget = objectConverter.createInstanceOfSWTWidget(widgetClass, context.getWorkItem(), style);
        final TransformationWorkingContext ofTheJedi = new TransformationWorkingContext(context);
        ofTheJedi.setWorkItem(instanceOfSWTWidget);
        context.mapObject(name, instanceOfSWTWidget);
        return ofTheJedi;
    }
}
