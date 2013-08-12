package net.milanaleksic.guitransformer.builders;

import com.google.common.collect.ImmutableMap;
import net.milanaleksic.guitransformer.TransformerException;
import org.eclipse.swt.layout.GridData;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:18 AM
 */
public class GridDataBuilder implements Builder<GridData> {

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static final Map<String, Integer> stringToAlignmentConversionTable =
            ImmutableMap.<String, Integer>builder()
                    .put("center", GridData.CENTER)
                    .put("begin", GridData.BEGINNING)
                    .put("end", GridData.END)
                    .put("fill", GridData.FILL)
                    .build();

    @Override
    public BuilderContext<GridData> create(Object parent, List<String> parameters) {
        if (parameters.size() == 4)
            return new BuilderContext<>(createGridDataBasedOn4Params(parameters));
        if (parameters.size() == 6)
            return new BuilderContext<>(createGridDataBasedOn6Params(parameters));
        throw new TransformerException("GridData builder supports only either 4 or 6 parameters!");
    }

    private GridData createGridDataBasedOn6Params(List<String> parameters) {
        int horizontalAlignment = convertAlignment(parameters.get(0));
        int verticalAlignment = convertAlignment(parameters.get(1));
        boolean grabExcessHorizontalSpace = Boolean.parseBoolean(parameters.get(2));
        boolean grabExcessVerticalSpace = Boolean.parseBoolean(parameters.get(3));
        int horizontalSpan = Integer.parseInt(parameters.get(4));
        int verticalSpan = Integer.parseInt(parameters.get(5));
        return new GridData(horizontalAlignment, verticalAlignment,
                grabExcessHorizontalSpace, grabExcessVerticalSpace,
                horizontalSpan, verticalSpan);
    }

    private GridData createGridDataBasedOn4Params(List<String> parameters) {
        int horizontalAlignment = convertAlignment(parameters.get(0));
        int verticalAlignment = convertAlignment(parameters.get(1));
        boolean grabExcessHorizontalSpace = Boolean.parseBoolean(parameters.get(2));
        boolean grabExcessVerticalSpace = Boolean.parseBoolean(parameters.get(3));
        return new GridData(horizontalAlignment, verticalAlignment,
                grabExcessHorizontalSpace, grabExcessVerticalSpace);
    }

    private int convertAlignment(String value) {
        final Integer ofTheJedi = stringToAlignmentConversionTable.get(value);
        if (ofTheJedi == null)
            throw new IllegalArgumentException("Could not convert expected alignment magic value: "+value+", supported are: center,begin,end,fill");
        return ofTheJedi;
    }

}
