package net.milanaleksic.baobab.builders;

import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.converters.TransformationWorkingContext;
import org.eclipse.swt.layout.GridData;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:18 AM
 */
public class GridDataBuilder implements Builder<GridData> {

    private static final Map<String, Integer> STRING_TO_GRID_DATA;

    static {
        Map<String, Integer> stringToAlignmentConversionTable = new HashMap<>();
        stringToAlignmentConversionTable.put("center", GridData.CENTER);
        stringToAlignmentConversionTable.put("begin", GridData.BEGINNING);
        stringToAlignmentConversionTable.put("end", GridData.END);
        stringToAlignmentConversionTable.put("fill", GridData.FILL);
        STRING_TO_GRID_DATA = Collections.unmodifiableMap(stringToAlignmentConversionTable);
    }

    @Override
    public BuilderContext<GridData> create(TransformationWorkingContext context, List<String> parameters) {
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
        final Integer ofTheJedi = STRING_TO_GRID_DATA.get(value);
        if (ofTheJedi == null)
            throw new TransformerException("Could not convert expected alignment magic value: "+value+", supported are: center,begin,end,fill");
        return ofTheJedi;
    }

}
