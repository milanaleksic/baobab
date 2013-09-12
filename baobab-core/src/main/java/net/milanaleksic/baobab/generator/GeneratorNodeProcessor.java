package net.milanaleksic.baobab.generator;

import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.converters.*;
import org.codehaus.jackson.JsonNode;

public class GeneratorNodeProcessor implements NodeProcessor {

    @Override
    public TransformationWorkingContext visitHierarchyItem(TransformationWorkingContext context, String key, JsonNode value) {
        System.out.println("Builder notation visited: key=" + key + ", value=" + value.toString());
        return context;
    }

    @Override
    public BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters) {
        System.out.println("Builder notation visited: builderName=" + builderName + ", parameters=" + parameters);
        return new BuilderContext<>(new Object());
    }

    @Override
    public void visitSingleField(TransformationWorkingContext context, String key, JsonNode value) {
        System.out.println("Field visited: key=" + key + ", value=" + value.toString());
    }

}
