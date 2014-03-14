package net.milanaleksic.baobab.builders;

import com.google.common.base.Preconditions;
import net.milanaleksic.baobab.converters.ObjectConverter;
import net.milanaleksic.baobab.converters.TransformationWorkingContext;
import net.milanaleksic.baobab.util.StreamUtil;

import javax.inject.Inject;
import java.util.List;

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 09/03/2014
 */
public class IncludeBuilder implements Builder<Object> {

    @Inject
    private ObjectConverter objectConverter;

    @Override
    public BuilderContext<Object> create(final TransformationWorkingContext context, List<String> parameters) {
        Preconditions.checkArgument(parameters.size() == 1, "Include builder supports only one parameter: location of the GUI definition file");
        return new BuilderContext<>(StreamUtil.loanRelativeResource(context.getFormLocation(), parameters.get(0),
                definitionStream -> objectConverter
                        .createHierarchy(context, definitionStream)
                        .getWorkItem()));
    }

}
