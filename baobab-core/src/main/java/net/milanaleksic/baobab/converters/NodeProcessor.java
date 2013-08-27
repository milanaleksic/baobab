package net.milanaleksic.baobab.converters;

import net.milanaleksic.baobab.builders.BuilderContext;
import org.codehaus.jackson.JsonNode;

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 27/08/2013
 */
public interface NodeProcessor {

    TransformationWorkingContext visitHierarchyItem(TransformationWorkingContext context, String key, JsonNode value);

    BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters);

    void visitSingleField(TransformationWorkingContext context, String key, JsonNode value);
}
