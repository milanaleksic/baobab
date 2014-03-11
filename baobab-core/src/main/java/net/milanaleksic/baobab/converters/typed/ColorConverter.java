package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.milanaleksic.baobab.TransformerException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 9:59 AM
 */
public class ColorConverter extends TypedConverter<Color> {

    private static final Pattern properValue = Pattern.compile("#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})", Pattern.CASE_INSENSITIVE); //NON-NLS

    private List<Color> referencedColors = Lists.newLinkedList();

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static final Map<String, Integer> systemColors = ImmutableMap
            .<String, Integer>builder()
            .put("color_white", SWT.COLOR_WHITE)
            .put("color_black", SWT.COLOR_BLACK)
            .put("color_red", SWT.COLOR_RED)
            .put("color_dark_red", SWT.COLOR_DARK_RED)
            .put("color_green", SWT.COLOR_GREEN)
            .put("color_dark_green", SWT.COLOR_DARK_GREEN)
            .put("color_yellow", SWT.COLOR_YELLOW)
            .put("color_dark_yellow", SWT.COLOR_DARK_YELLOW)
            .put("color_blue", SWT.COLOR_BLUE)
            .put("color_dark_blue", SWT.COLOR_DARK_BLUE)
            .put("color_magenta", SWT.COLOR_MAGENTA)
            .put("color_dark_magenta", SWT.COLOR_DARK_MAGENTA)
            .put("color_cyan", SWT.COLOR_CYAN)
            .put("color_dark_cyan", SWT.COLOR_DARK_CYAN)
            .put("color_gray", SWT.COLOR_GRAY)
            .put("color_dark_gray", SWT.COLOR_DARK_GRAY)
            .put("color_widget_dark_shadow", SWT.COLOR_WIDGET_DARK_SHADOW)
            .put("color_widget_normal_shadow", SWT.COLOR_WIDGET_NORMAL_SHADOW)
            .put("color_widget_light_shadow", SWT.COLOR_WIDGET_LIGHT_SHADOW)
            .put("color_widget_highlight_shadow", SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW)
            .put("color_widget_foreground", SWT.COLOR_WIDGET_FOREGROUND)
            .put("color_widget_background", SWT.COLOR_WIDGET_BACKGROUND)
            .put("color_widget_border", SWT.COLOR_WIDGET_BORDER)
            .put("color_list_foreground", SWT.COLOR_LIST_FOREGROUND)
            .put("color_list_background", SWT.COLOR_LIST_BACKGROUND)
            .put("color_list_selection", SWT.COLOR_LIST_SELECTION)
            .put("color_list_selection_text", SWT.COLOR_LIST_SELECTION_TEXT)
            .put("color_info_foreground", SWT.COLOR_INFO_FOREGROUND)
            .put("color_info_background", SWT.COLOR_INFO_BACKGROUND)
            .put("color_title_foreground", SWT.COLOR_TITLE_FOREGROUND)
            .put("color_title_background", SWT.COLOR_TITLE_BACKGROUND)
            .put("color_title_background_gradient", SWT.COLOR_TITLE_BACKGROUND_GRADIENT)
            .put("color_title_inactive_foreground", SWT.COLOR_TITLE_INACTIVE_FOREGROUND)
            .put("color_title_inactive_background", SWT.COLOR_TITLE_INACTIVE_BACKGROUND)
            .put("color_title_inactive_background_gradient", SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT)
            .build();

    @Override
    public Color getValueFromJson(JsonNode node) {
        String input = node.asText();
        checkNotNull(input);
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
