package net.milanaleksic.baobab.converters;

import com.fasterxml.jackson.databind.JsonNode;
import net.milanaleksic.baobab.builders.BuilderContext;

import java.util.Optional;

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 27/08/2013
 */
public interface NodeProcessor {

    TransformationWorkingContext visitHierarchyItem(TransformationWorkingContext context, Optional<String> key, JsonNode value);

    BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters);

    void visitSingleField(TransformationWorkingContext context, String key, JsonNode value);
}
