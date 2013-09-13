package net.milanaleksic.baobab.generator;

import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.converters.*;
import org.codehaus.jackson.JsonNode;

public class GeneratorNodeProcessor implements NodeProcessor {

    @Override
    public BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters) {
        System.out.printf("Builder notation visited: builderName=%s, parameters=%s%n", builderName, parameters);
        return new BuilderContext<>(new Object());
    }

    @Override
    public void visitSingleField(TransformationWorkingContext context, String key, JsonNode value) {
        System.out.printf("Field visited: key=%s, value=%s%n", key, value.toString());
    }

    @Override
    public <T> T visitObjectItem(Class<T> widgetClass, Object parent, int style) {
        System.out.printf("Visit Object Item: widgetClass=%s, parent=%s%n", widgetClass, parent);
        return (T)new Object();
    }

}
