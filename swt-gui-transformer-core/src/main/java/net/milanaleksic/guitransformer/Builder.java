package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.builders.BuilderContext;
import org.eclipse.swt.widgets.Label;

import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:55 AM
 */
public interface Builder<T> {

    BuilderContext<T> create(Object parent, List<String> parameters) throws TransformerException;

}
