package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.JsonNode;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.util.Preconditions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 9:59 AM
 */
public class ColorConverter extends TypedConverter<Color> {

    private static final Pattern properValue = Pattern.compile("#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})", Pattern.CASE_INSENSITIVE); //NON-NLS

    private Set<Color> referencedColors = new HashSet<>();

    private static final Map<String, Integer> systemColors;

    static {
        Map<String, Integer> table = new HashMap<>();
        table.put("color_white", SWT.COLOR_WHITE);
        table.put("color_black", SWT.COLOR_BLACK);
        table.put("color_red", SWT.COLOR_RED);
        table.put("color_dark_red", SWT.COLOR_DARK_RED);
        table.put("color_green", SWT.COLOR_GREEN);
        table.put("color_dark_green", SWT.COLOR_DARK_GREEN);
        table.put("color_yellow", SWT.COLOR_YELLOW);
        table.put("color_dark_yellow", SWT.COLOR_DARK_YELLOW);
        table.put("color_blue", SWT.COLOR_BLUE);
        table.put("color_dark_blue", SWT.COLOR_DARK_BLUE);
        table.put("color_magenta", SWT.COLOR_MAGENTA);
        table.put("color_dark_magenta", SWT.COLOR_DARK_MAGENTA);
        table.put("color_cyan", SWT.COLOR_CYAN);
        table.put("color_dark_cyan", SWT.COLOR_DARK_CYAN);
        table.put("color_gray", SWT.COLOR_GRAY);
        table.put("color_dark_gray", SWT.COLOR_DARK_GRAY);
        table.put("color_widget_dark_shadow", SWT.COLOR_WIDGET_DARK_SHADOW);
        table.put("color_widget_normal_shadow", SWT.COLOR_WIDGET_NORMAL_SHADOW);
        table.put("color_widget_light_shadow", SWT.COLOR_WIDGET_LIGHT_SHADOW);
        table.put("color_widget_highlight_shadow", SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        table.put("color_widget_foreground", SWT.COLOR_WIDGET_FOREGROUND);
        table.put("color_widget_background", SWT.COLOR_WIDGET_BACKGROUND);
        table.put("color_widget_border", SWT.COLOR_WIDGET_BORDER);
        table.put("color_list_foreground", SWT.COLOR_LIST_FOREGROUND);
        table.put("color_list_background", SWT.COLOR_LIST_BACKGROUND);
        table.put("color_list_selection", SWT.COLOR_LIST_SELECTION);
        table.put("color_list_selection_text", SWT.COLOR_LIST_SELECTION_TEXT);
        table.put("color_info_foreground", SWT.COLOR_INFO_FOREGROUND);
        table.put("color_info_background", SWT.COLOR_INFO_BACKGROUND);
        table.put("color_title_foreground", SWT.COLOR_TITLE_FOREGROUND);
        table.put("color_title_background", SWT.COLOR_TITLE_BACKGROUND);
        table.put("color_title_background_gradient", SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        table.put("color_title_inactive_foreground", SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
        table.put("color_title_inactive_background", SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
        table.put("color_title_inactive_background_gradient", SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
        systemColors = Collections.unmodifiableMap(table);
    }

    @Override
    public Color getValueFromJson(JsonNode node) {
        String input = node.asText();
        Preconditions.checkNotNull(input, "Can't convert value from null");
        if (systemColors.containsKey(input))
            return convertSystemColorType(systemColors.get(input));
        Matcher matcher = properValue.matcher(input);
        if (!matcher.matches())
            throw new TransformerException("Value is not a proper HTML color (e.g. #ff0011) - " + input);

        Color color = new Color(Display.getDefault(),
                Integer.parseInt(matcher.group(1), 16),
                Integer.parseInt(matcher.group(2), 16),
                Integer.parseInt(matcher.group(3), 16));
        referencedColors.add(color);
        return color;
    }

    private Color convertSystemColorType(Integer keyCode) {
        return Display.getDefault().getSystemColor(keyCode);
    }

    @Override
    public void cleanUp() {
        if (referencedColors == null)
            return;
        for (Color color : referencedColors)
            color.dispose();
        referencedColors = null;
    }

}
