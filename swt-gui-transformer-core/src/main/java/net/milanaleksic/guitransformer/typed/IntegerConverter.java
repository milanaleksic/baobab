package net.milanaleksic.guitransformer.typed;

import com.google.common.collect.*;
import net.milanaleksic.guitransformer.TransformerException;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;

import java.util.Map;
import java.util.regex.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class IntegerConverter extends TypedConverter<Integer> {

    private static final Pattern magicConstantsValue = Pattern.compile("\\{(.*)\\}");

    private static Map<String, Integer> magicConstants;

    // do not use ImmutableMap.builder() because it crashes JDK compiler
    static {
        Map<String, Integer> temp = Maps.newHashMap();

        temp.put("dnd.clipboard", DND.CLIPBOARD);
        temp.put("dnd.selection_clipboard", DND.SELECTION_CLIPBOARD);
        temp.put("dnd.drop_none", DND.DROP_NONE);
        temp.put("dnd.drop_copy", DND.DROP_COPY);
        temp.put("dnd.drop_move", DND.DROP_MOVE);
        temp.put("dnd.drop_link", DND.DROP_LINK);
        temp.put("dnd.drop_target_move", DND.DROP_TARGET_MOVE);
        temp.put("dnd.drop_default", DND.DROP_DEFAULT);

        temp.put("composition_changed", SWT.COMPOSITION_CHANGED);
        temp.put("composition_offset", SWT.COMPOSITION_OFFSET);
        temp.put("composition_selection", SWT.COMPOSITION_SELECTION);
        temp.put("drag", SWT.DRAG);
        temp.put("selected", SWT.SELECTED);
        temp.put("focused", SWT.FOCUSED);
        temp.put("background", SWT.BACKGROUND);
        temp.put("foreground", SWT.FOREGROUND);
        temp.put("hot", SWT.HOT);
        temp.put("traverse_none", SWT.TRAVERSE_NONE);
        temp.put("traverse_escape", SWT.TRAVERSE_ESCAPE);
        temp.put("traverse_return", SWT.TRAVERSE_RETURN);
        temp.put("traverse_tab_previous", SWT.TRAVERSE_TAB_PREVIOUS);
        temp.put("traverse_tab_next", SWT.TRAVERSE_TAB_NEXT);
        temp.put("traverse_arrow_previous", SWT.TRAVERSE_ARROW_PREVIOUS);
        temp.put("traverse_arrow_next", SWT.TRAVERSE_ARROW_NEXT);
        temp.put("traverse_mnemonic", SWT.TRAVERSE_MNEMONIC);
        temp.put("traverse_page_previous", SWT.TRAVERSE_PAGE_PREVIOUS);
        temp.put("traverse_page_next", SWT.TRAVERSE_PAGE_NEXT);
        temp.put("gesture_begin", SWT.GESTURE_BEGIN);
        temp.put("gesture_end", SWT.GESTURE_END);
        temp.put("gesture_rotate", SWT.GESTURE_ROTATE);
        temp.put("gesture_swipe", SWT.GESTURE_SWIPE);
        temp.put("gesture_magnify", SWT.GESTURE_MAGNIFY);
        temp.put("gesture_pan", SWT.GESTURE_PAN);
        temp.put("touchstate_down", SWT.TOUCHSTATE_DOWN);
        temp.put("touchstate_move", SWT.TOUCHSTATE_MOVE);
        temp.put("touchstate_up", SWT.TOUCHSTATE_UP);
        temp.put("changed", SWT.CHANGED);
        temp.put("defer", SWT.DEFER);
        temp.put("none", SWT.NONE);
        temp.put("null", SWT.NULL);
        temp.put("default", SWT.DEFAULT);
        temp.put("off", SWT.OFF);
        temp.put("on", SWT.ON);
        temp.put("low", SWT.LOW);
        temp.put("high", SWT.HIGH);
        temp.put("bar", SWT.BAR);
        temp.put("drop_down", SWT.DROP_DOWN);
        temp.put("pop_up", SWT.POP_UP);
        temp.put("separator", SWT.SEPARATOR);
        temp.put("separator_fill", SWT.SEPARATOR_FILL);
        temp.put("toggle", SWT.TOGGLE);
        temp.put("arrow", SWT.ARROW);
        temp.put("push", SWT.PUSH);
        temp.put("radio", SWT.RADIO);
        temp.put("check", SWT.CHECK);
        temp.put("cascade", SWT.CASCADE);
        temp.put("multi", SWT.MULTI);
        temp.put("single", SWT.SINGLE);
        temp.put("read_only", SWT.READ_ONLY);
        temp.put("wrap", SWT.WRAP);
        temp.put("search", SWT.SEARCH);
        temp.put("simple", SWT.SIMPLE);
        temp.put("password", SWT.PASSWORD);
        temp.put("shadow_in", SWT.SHADOW_IN);
        temp.put("shadow_out", SWT.SHADOW_OUT);
        temp.put("shadow_etched_in", SWT.SHADOW_ETCHED_IN);
        temp.put("shadow_etched_out", SWT.SHADOW_ETCHED_OUT);
        temp.put("shadow_none", SWT.SHADOW_NONE);
        temp.put("indeterminate", SWT.INDETERMINATE);
        temp.put("tool", SWT.TOOL);
        temp.put("no_trim", SWT.NO_TRIM);
        temp.put("resize", SWT.RESIZE);
        temp.put("title", SWT.TITLE);
        temp.put("close", SWT.CLOSE);
        temp.put("menu", SWT.MENU);
        temp.put("min", SWT.MIN);
        temp.put("max", SWT.MAX);
        temp.put("h_scroll", SWT.H_SCROLL);
        temp.put("v_scroll", SWT.V_SCROLL);
        temp.put("no_scroll", SWT.NO_SCROLL);
        temp.put("border", SWT.BORDER);
        temp.put("clip_children", SWT.CLIP_CHILDREN);
        temp.put("clip_siblings", SWT.CLIP_SIBLINGS);
        temp.put("on_top", SWT.ON_TOP);
        temp.put("sheet", SWT.SHEET);
        temp.put("shell_trim", SWT.SHELL_TRIM);
        temp.put("dialog_trim", SWT.DIALOG_TRIM);
        temp.put("modeless", SWT.MODELESS);
        temp.put("primary_modal", SWT.PRIMARY_MODAL);
        temp.put("application_modal", SWT.APPLICATION_MODAL);
        temp.put("system_modal", SWT.SYSTEM_MODAL);
        temp.put("hide_selection", SWT.HIDE_SELECTION);
        temp.put("full_selection", SWT.FULL_SELECTION);
        temp.put("flat", SWT.FLAT);
        temp.put("smooth", SWT.SMOOTH);
        temp.put("no_background", SWT.NO_BACKGROUND);
        temp.put("no_focus", SWT.NO_FOCUS);
        temp.put("no_redraw_resize", SWT.NO_REDRAW_RESIZE);
        temp.put("no_merge_paints", SWT.NO_MERGE_PAINTS);
        temp.put("no_radio_group", SWT.NO_RADIO_GROUP);
        temp.put("left_to_right", SWT.LEFT_TO_RIGHT);
        temp.put("right_to_left", SWT.RIGHT_TO_LEFT);
        temp.put("mirrored", SWT.MIRRORED);
        temp.put("embedded", SWT.EMBEDDED);
        temp.put("virtual", SWT.VIRTUAL);
        temp.put("double_buffered", SWT.DOUBLE_BUFFERED);
        temp.put("transparent", SWT.TRANSPARENT);
        temp.put("up", SWT.UP);
        temp.put("underline_single", SWT.UNDERLINE_SINGLE);
        temp.put("underline_double", SWT.UNDERLINE_DOUBLE);
        temp.put("underline_error", SWT.UNDERLINE_ERROR);
        temp.put("underline_squiggle", SWT.UNDERLINE_SQUIGGLE);
        temp.put("underline_link", SWT.UNDERLINE_LINK);
        temp.put("border_solid", SWT.BORDER_SOLID);
        temp.put("border_dash", SWT.BORDER_DASH);
        temp.put("border_dot", SWT.BORDER_DOT);
        temp.put("top", SWT.TOP);
        temp.put("down", SWT.DOWN);
        temp.put("bottom", SWT.BOTTOM);
        temp.put("lead", SWT.LEAD);
        temp.put("left", SWT.LEFT);
        temp.put("trail", SWT.TRAIL);
        temp.put("right", SWT.RIGHT);
        temp.put("center", SWT.CENTER);
        temp.put("horizontal", SWT.HORIZONTAL);
        temp.put("vertical", SWT.VERTICAL);
        temp.put("date", SWT.DATE);
        temp.put("time", SWT.TIME);
        temp.put("calendar", SWT.CALENDAR);
        temp.put("short", SWT.SHORT);
        temp.put("medium", SWT.MEDIUM);
        temp.put("long", SWT.LONG);
        temp.put("mozilla", SWT.MOZILLA);
        temp.put("webkit", SWT.WEBKIT);
        temp.put("balloon", SWT.BALLOON);
        temp.put("beginning", SWT.BEGINNING);
        temp.put("fill", SWT.FILL);
        temp.put("dbcs", SWT.DBCS);
        temp.put("alpha", SWT.ALPHA);
        temp.put("native", SWT.NATIVE);
        temp.put("phonetic", SWT.PHONETIC);
        temp.put("roman", SWT.ROMAN);
        temp.put("alt", SWT.ALT);
        temp.put("shift", SWT.SHIFT);
        temp.put("ctrl", SWT.CTRL);
        temp.put("control", SWT.CONTROL);
        temp.put("command", SWT.COMMAND);
        temp.put("modifier_mask", SWT.MODIFIER_MASK);
        temp.put("button1", SWT.BUTTON1);
        temp.put("button2", SWT.BUTTON2);
        temp.put("button3", SWT.BUTTON3);
        temp.put("button4", SWT.BUTTON4);
        temp.put("button5", SWT.BUTTON5);
        temp.put("button_mask", SWT.BUTTON_MASK);
        temp.put("mod1", SWT.MOD1);
        temp.put("mod2", SWT.MOD2);
        temp.put("mod3", SWT.MOD3);
        temp.put("mod4", SWT.MOD4);
        temp.put("scroll_line", SWT.SCROLL_LINE);
        temp.put("scroll_page", SWT.SCROLL_PAGE);
        temp.put("keycode_bit", SWT.KEYCODE_BIT);
        temp.put("key_mask", SWT.KEY_MASK);
        temp.put("arrow_up", SWT.ARROW_UP);
        temp.put("arrow_down", SWT.ARROW_DOWN);
        temp.put("arrow_left", SWT.ARROW_LEFT);
        temp.put("arrow_right", SWT.ARROW_RIGHT);
        temp.put("page_up", SWT.PAGE_UP);
        temp.put("page_down", SWT.PAGE_DOWN);
        temp.put("home", SWT.HOME);
        temp.put("end", SWT.END);
        temp.put("insert", SWT.INSERT);
        temp.put("f1", SWT.F1);
        temp.put("f2", SWT.F2);
        temp.put("f3", SWT.F3);
        temp.put("f4", SWT.F4);
        temp.put("f5", SWT.F5);
        temp.put("f6", SWT.F6);
        temp.put("f7", SWT.F7);
        temp.put("f8", SWT.F8);
        temp.put("f9", SWT.F9);
        temp.put("f10", SWT.F10);
        temp.put("f11", SWT.F11);
        temp.put("f12", SWT.F12);
        temp.put("f13", SWT.F13);
        temp.put("f14", SWT.F14);
        temp.put("f15", SWT.F15);
        temp.put("f16", SWT.F16);
        temp.put("f17", SWT.F17);
        temp.put("f18", SWT.F18);
        temp.put("f19", SWT.F19);
        temp.put("f20", SWT.F20);
        temp.put("keypad", SWT.KEYPAD);
        temp.put("keypad_multiply", SWT.KEYPAD_MULTIPLY);
        temp.put("keypad_add", SWT.KEYPAD_ADD);
        temp.put("keypad_subtract", SWT.KEYPAD_SUBTRACT);
        temp.put("keypad_decimal", SWT.KEYPAD_DECIMAL);
        temp.put("keypad_divide", SWT.KEYPAD_DIVIDE);
        temp.put("keypad_0", SWT.KEYPAD_0);
        temp.put("keypad_1", SWT.KEYPAD_1);
        temp.put("keypad_2", SWT.KEYPAD_2);
        temp.put("keypad_3", SWT.KEYPAD_3);
        temp.put("keypad_4", SWT.KEYPAD_4);
        temp.put("keypad_5", SWT.KEYPAD_5);
        temp.put("keypad_6", SWT.KEYPAD_6);
        temp.put("keypad_7", SWT.KEYPAD_7);
        temp.put("keypad_8", SWT.KEYPAD_8);
        temp.put("keypad_9", SWT.KEYPAD_9);
        temp.put("keypad_equal", SWT.KEYPAD_EQUAL);
        temp.put("keypad_cr", SWT.KEYPAD_CR);
        temp.put("help", SWT.HELP);
        temp.put("caps_lock", SWT.CAPS_LOCK);
        temp.put("num_lock", SWT.NUM_LOCK);
        temp.put("scroll_lock", SWT.SCROLL_LOCK);
        temp.put("pause", SWT.PAUSE);
        temp.put("break", SWT.BREAK);
        temp.put("print_screen", SWT.PRINT_SCREEN);
        temp.put("icon_error", SWT.ICON_ERROR);
        temp.put("icon_information", SWT.ICON_INFORMATION);
        temp.put("icon_question", SWT.ICON_QUESTION);
        temp.put("icon_warning", SWT.ICON_WARNING);
        temp.put("icon_working", SWT.ICON_WORKING);
        temp.put("icon_search", SWT.ICON_SEARCH);
        temp.put("icon_cancel", SWT.ICON_CANCEL);
        temp.put("ok", SWT.OK);
        temp.put("yes", SWT.YES);
        temp.put("no", SWT.NO);
        temp.put("cancel", SWT.CANCEL);
        temp.put("abort", SWT.ABORT);
        temp.put("retry", SWT.RETRY);
        temp.put("ignore", SWT.IGNORE);
        temp.put("open", SWT.OPEN);
        temp.put("save", SWT.SAVE);
        temp.put("inherit_none", SWT.INHERIT_NONE);
        temp.put("inherit_default", SWT.INHERIT_DEFAULT);
        temp.put("inherit_force", SWT.INHERIT_FORCE);
        temp.put("color_white", SWT.COLOR_WHITE);
        temp.put("color_black", SWT.COLOR_BLACK);
        temp.put("color_red", SWT.COLOR_RED);
        temp.put("color_dark_red", SWT.COLOR_DARK_RED);
        temp.put("color_green", SWT.COLOR_GREEN);
        temp.put("color_dark_green", SWT.COLOR_DARK_GREEN);
        temp.put("color_yellow", SWT.COLOR_YELLOW);
        temp.put("color_dark_yellow", SWT.COLOR_DARK_YELLOW);
        temp.put("color_blue", SWT.COLOR_BLUE);
        temp.put("color_dark_blue", SWT.COLOR_DARK_BLUE);
        temp.put("color_magenta", SWT.COLOR_MAGENTA);
        temp.put("color_dark_magenta", SWT.COLOR_DARK_MAGENTA);
        temp.put("color_cyan", SWT.COLOR_CYAN);
        temp.put("color_dark_cyan", SWT.COLOR_DARK_CYAN);
        temp.put("color_gray", SWT.COLOR_GRAY);
        temp.put("color_dark_gray", SWT.COLOR_DARK_GRAY);
        temp.put("color_widget_dark_shadow", SWT.COLOR_WIDGET_DARK_SHADOW);
        temp.put("color_widget_normal_shadow", SWT.COLOR_WIDGET_NORMAL_SHADOW);
        temp.put("color_widget_light_shadow", SWT.COLOR_WIDGET_LIGHT_SHADOW);
        temp.put("color_widget_highlight_shadow", SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        temp.put("color_widget_foreground", SWT.COLOR_WIDGET_FOREGROUND);
        temp.put("color_widget_background", SWT.COLOR_WIDGET_BACKGROUND);
        temp.put("color_widget_border", SWT.COLOR_WIDGET_BORDER);
        temp.put("color_list_foreground", SWT.COLOR_LIST_FOREGROUND);
        temp.put("color_list_background", SWT.COLOR_LIST_BACKGROUND);
        temp.put("color_list_selection", SWT.COLOR_LIST_SELECTION);
        temp.put("color_list_selection_text", SWT.COLOR_LIST_SELECTION_TEXT);
        temp.put("color_info_foreground", SWT.COLOR_INFO_FOREGROUND);
        temp.put("color_info_background", SWT.COLOR_INFO_BACKGROUND);
        temp.put("color_title_foreground", SWT.COLOR_TITLE_FOREGROUND);
        temp.put("color_title_background", SWT.COLOR_TITLE_BACKGROUND);
        temp.put("color_title_background_gradient", SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        temp.put("color_title_inactive_foreground", SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
        temp.put("color_title_inactive_background", SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
        temp.put("color_title_inactive_background_gradient", SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
        temp.put("draw_transparent", SWT.DRAW_TRANSPARENT);
        temp.put("draw_delimiter", SWT.DRAW_DELIMITER);
        temp.put("draw_tab", SWT.DRAW_TAB);
        temp.put("draw_mnemonic", SWT.DRAW_MNEMONIC);
        temp.put("delimiter_selection", SWT.DELIMITER_SELECTION);
        temp.put("last_line_selection", SWT.LAST_LINE_SELECTION);
        temp.put("error_unspecified", SWT.ERROR_UNSPECIFIED);
        temp.put("error_no_handles", SWT.ERROR_NO_HANDLES);
        temp.put("error_no_more_callbacks", SWT.ERROR_NO_MORE_CALLBACKS);
        temp.put("error_null_argument", SWT.ERROR_NULL_ARGUMENT);
        temp.put("error_invalid_argument", SWT.ERROR_INVALID_ARGUMENT);
        temp.put("error_invalid_range", SWT.ERROR_INVALID_RANGE);
        temp.put("error_cannot_be_zero", SWT.ERROR_CANNOT_BE_ZERO);
        temp.put("error_cannot_get_item", SWT.ERROR_CANNOT_GET_ITEM);
        temp.put("error_cannot_get_selection", SWT.ERROR_CANNOT_GET_SELECTION);
        temp.put("error_cannot_invert_matrix", SWT.ERROR_CANNOT_INVERT_MATRIX);
        temp.put("error_cannot_get_item_height", SWT.ERROR_CANNOT_GET_ITEM_HEIGHT);
        temp.put("error_cannot_get_text", SWT.ERROR_CANNOT_GET_TEXT);
        temp.put("error_cannot_set_text", SWT.ERROR_CANNOT_SET_TEXT);
        temp.put("error_item_not_added", SWT.ERROR_ITEM_NOT_ADDED);
        temp.put("error_item_not_removed", SWT.ERROR_ITEM_NOT_REMOVED);
        temp.put("error_no_graphics_library", SWT.ERROR_NO_GRAPHICS_LIBRARY);
        temp.put("error_not_implemented", SWT.ERROR_NOT_IMPLEMENTED);
        temp.put("error_menu_not_drop_down", SWT.ERROR_MENU_NOT_DROP_DOWN);
        temp.put("error_thread_invalid_access", SWT.ERROR_THREAD_INVALID_ACCESS);
        temp.put("error_widget_disposed", SWT.ERROR_WIDGET_DISPOSED);
        temp.put("error_menuitem_not_cascade", SWT.ERROR_MENUITEM_NOT_CASCADE);
        temp.put("error_cannot_set_selection", SWT.ERROR_CANNOT_SET_SELECTION);
        temp.put("error_cannot_set_menu", SWT.ERROR_CANNOT_SET_MENU);
        temp.put("error_cannot_set_enabled", SWT.ERROR_CANNOT_SET_ENABLED);
        temp.put("error_cannot_get_enabled", SWT.ERROR_CANNOT_GET_ENABLED);
        temp.put("error_invalid_parent", SWT.ERROR_INVALID_PARENT);
        temp.put("error_menu_not_bar", SWT.ERROR_MENU_NOT_BAR);
        temp.put("error_cannot_get_count", SWT.ERROR_CANNOT_GET_COUNT);
        temp.put("error_menu_not_pop_up", SWT.ERROR_MENU_NOT_POP_UP);
        temp.put("error_unsupported_depth", SWT.ERROR_UNSUPPORTED_DEPTH);
        temp.put("error_io", SWT.ERROR_IO);
        temp.put("error_invalid_image", SWT.ERROR_INVALID_IMAGE);
        temp.put("error_unsupported_format", SWT.ERROR_UNSUPPORTED_FORMAT);
        temp.put("error_invalid_subclass", SWT.ERROR_INVALID_SUBCLASS);
        temp.put("error_graphic_disposed", SWT.ERROR_GRAPHIC_DISPOSED);
        temp.put("error_device_disposed", SWT.ERROR_DEVICE_DISPOSED);
        temp.put("error_failed_exec", SWT.ERROR_FAILED_EXEC);
        temp.put("error_failed_load_library", SWT.ERROR_FAILED_LOAD_LIBRARY);
        temp.put("error_invalid_font", SWT.ERROR_INVALID_FONT);
        temp.put("error_function_disposed", SWT.ERROR_FUNCTION_DISPOSED);
        temp.put("error_failed_evaluate", SWT.ERROR_FAILED_EVALUATE);
        temp.put("error_invalid_return_value", SWT.ERROR_INVALID_RETURN_VALUE);
        temp.put("bitmap", SWT.BITMAP);
        temp.put("icon", SWT.ICON);
        temp.put("image_copy", SWT.IMAGE_COPY);
        temp.put("image_disable", SWT.IMAGE_DISABLE);
        temp.put("image_gray", SWT.IMAGE_GRAY);
        temp.put("error", SWT.ERROR);
        temp.put("paused", SWT.PAUSED);
        temp.put("normal", SWT.NORMAL);
        temp.put("bold", SWT.BOLD);
        temp.put("italic", SWT.ITALIC);
        temp.put("cursor_arrow", SWT.CURSOR_ARROW);
        temp.put("cursor_wait", SWT.CURSOR_WAIT);
        temp.put("cursor_cross", SWT.CURSOR_CROSS);
        temp.put("cursor_appstarting", SWT.CURSOR_APPSTARTING);
        temp.put("cursor_help", SWT.CURSOR_HELP);
        temp.put("cursor_sizeall", SWT.CURSOR_SIZEALL);
        temp.put("cursor_sizenesw", SWT.CURSOR_SIZENESW);
        temp.put("cursor_sizens", SWT.CURSOR_SIZENS);
        temp.put("cursor_sizenwse", SWT.CURSOR_SIZENWSE);
        temp.put("cursor_sizewe", SWT.CURSOR_SIZEWE);
        temp.put("cursor_sizen", SWT.CURSOR_SIZEN);
        temp.put("cursor_sizes", SWT.CURSOR_SIZES);
        temp.put("cursor_sizee", SWT.CURSOR_SIZEE);
        temp.put("cursor_sizew", SWT.CURSOR_SIZEW);
        temp.put("cursor_sizene", SWT.CURSOR_SIZENE);
        temp.put("cursor_sizese", SWT.CURSOR_SIZESE);
        temp.put("cursor_sizesw", SWT.CURSOR_SIZESW);
        temp.put("cursor_sizenw", SWT.CURSOR_SIZENW);
        temp.put("cursor_uparrow", SWT.CURSOR_UPARROW);
        temp.put("cursor_ibeam", SWT.CURSOR_IBEAM);
        temp.put("cursor_no", SWT.CURSOR_NO);
        temp.put("cursor_hand", SWT.CURSOR_HAND);
        temp.put("cap_flat", SWT.CAP_FLAT);
        temp.put("cap_round", SWT.CAP_ROUND);
        temp.put("cap_square", SWT.CAP_SQUARE);
        temp.put("join_miter", SWT.JOIN_MITER);
        temp.put("join_round", SWT.JOIN_ROUND);
        temp.put("join_bevel", SWT.JOIN_BEVEL);
        temp.put("line_solid", SWT.LINE_SOLID);
        temp.put("line_dash", SWT.LINE_DASH);
        temp.put("line_dot", SWT.LINE_DOT);
        temp.put("line_dashdot", SWT.LINE_DASHDOT);
        temp.put("line_dashdotdot", SWT.LINE_DASHDOTDOT);
        temp.put("line_custom", SWT.LINE_CUSTOM);
        temp.put("path_move_to", SWT.PATH_MOVE_TO);
        temp.put("path_line_to", SWT.PATH_LINE_TO);
        temp.put("path_quad_to", SWT.PATH_QUAD_TO);
        temp.put("path_cubic_to", SWT.PATH_CUBIC_TO);
        temp.put("path_close", SWT.PATH_CLOSE);
        temp.put("fill_even_odd", SWT.FILL_EVEN_ODD);
        temp.put("fill_winding", SWT.FILL_WINDING);
        temp.put("image_undefined", SWT.IMAGE_UNDEFINED);
        temp.put("image_bmp", SWT.IMAGE_BMP);
        temp.put("image_bmp_rle", SWT.IMAGE_BMP_RLE);
        temp.put("image_gif", SWT.IMAGE_GIF);
        temp.put("image_ico", SWT.IMAGE_ICO);
        temp.put("image_jpeg", SWT.IMAGE_JPEG);
        temp.put("image_png", SWT.IMAGE_PNG);
        temp.put("image_tiff", SWT.IMAGE_TIFF);
        temp.put("image_os2_bmp", SWT.IMAGE_OS2_BMP);
        temp.put("dm_unspecified", SWT.DM_UNSPECIFIED);
        temp.put("dm_fill_none", SWT.DM_FILL_NONE);
        temp.put("dm_fill_background", SWT.DM_FILL_BACKGROUND);
        temp.put("dm_fill_previous", SWT.DM_FILL_PREVIOUS);
        temp.put("transparency_none", SWT.TRANSPARENCY_NONE);
        temp.put("transparency_alpha", SWT.TRANSPARENCY_ALPHA);
        temp.put("transparency_mask", SWT.TRANSPARENCY_MASK);
        temp.put("transparency_pixel", SWT.TRANSPARENCY_PIXEL);
        temp.put("movement_char", SWT.MOVEMENT_CHAR);
        temp.put("movement_cluster", SWT.MOVEMENT_CLUSTER);
        temp.put("movement_word", SWT.MOVEMENT_WORD);
        temp.put("movement_word_end", SWT.MOVEMENT_WORD_END);
        temp.put("movement_word_start", SWT.MOVEMENT_WORD_START);
        temp.put("all", SWT.ALL);
        temp.put("id_about", SWT.ID_ABOUT);
        temp.put("id_preferences", SWT.ID_PREFERENCES);
        temp.put("id_hide", SWT.ID_HIDE);
        temp.put("id_hide_others", SWT.ID_HIDE_OTHERS);
        temp.put("id_show_all", SWT.ID_SHOW_ALL);
        temp.put("id_quit", SWT.ID_QUIT);
        magicConstants = ImmutableMap.<String,Integer>builder().putAll(temp).build();
    }

    @Override
    public Integer getValueFromJson(JsonNode node, Map<String, Object> mappedObjects) throws TransformerException {
        String input = node.asText();
        return getValueFromString(input);
    }

    public Integer getValueFromString(String input) throws TransformerException {
        checkNotNull(input);
        String[] values = input.split("\\|");
        int ofTheJedi = 0;
        for (String value : values) {
            Matcher matcher = magicConstantsValue.matcher(value);
            if (matcher.matches()) {
                Integer matchedMagicConstantValue = magicConstants.get(matcher.group(1));
                if (matchedMagicConstantValue == null)
                    throw new TransformerException("Magic constant does not exist - " + matcher.group(1));
                ofTheJedi |= matchedMagicConstantValue;
            } else
                ofTheJedi |= Integer.parseInt(value);
        }
        return ofTheJedi;
    }

}
