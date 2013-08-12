package net.milanaleksic.guitransformer.builders;

import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.converters.typed.IntegerConverter;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 6/22/12
 * Time: 3:58 PM
 */
public class LabelBuilder implements Builder<Label> {

    @Inject
    private IntegerConverter integerConverter;

    @Override
    public BuilderContext<Label> create(Object parent, List<String> parameters) {
        if (parameters.size() != 2)
            throw new TransformerException("Label builder supports only two parameters (style and text)!");
        int styleParameter = integerConverter.getValueFromString(parameters.get(0));
        final String name = parameters.get(1);
        return new BuilderContext<>(new Label((Composite) parent, styleParameter), name);
    }

}
