package net.milanaleksic.guitransformer.builders;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:55 AM
 */
public interface Builder<T> {

    BuilderContext<T> create(Object parent, List<String> parameters);

}
