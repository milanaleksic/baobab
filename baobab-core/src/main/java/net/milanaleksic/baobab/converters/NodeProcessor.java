package net.milanaleksic.baobab.converters;

import net.milanaleksic.baobab.builders.BuilderContext;
import org.codehaus.jackson.JsonNode;

public interface NodeProcessor {

    <T> T visitObjectItem(Class<T> widgetClass, Object parent, int style);

    BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters);

    void visitSingleField(TransformationWorkingContext context, String key, JsonNode value);
}
