package net.milanaleksic.baobab.builders;

import net.milanaleksic.baobab.converters.TransformationWorkingContext;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:55 AM
 */
public interface Builder<T> {

    BuilderContext<T> create(TransformationWorkingContext context, List<String> parameters);

}
