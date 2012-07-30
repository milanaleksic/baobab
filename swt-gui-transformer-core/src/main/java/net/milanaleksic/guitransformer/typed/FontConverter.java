package net.milanaleksic.guitransformer.typed;

import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.swt.SwtWrapper;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 10:18 AM
 */
public class FontConverter extends TypedConverter<Font> {

    @Inject
    private SwtWrapper swtWrapper;

    private static final String FIELD_NAME = "name"; //NON-NLS
    private static final String FIELD_HEIGHT = "height"; //NON-NLS
    private static final String FIELD_STYLE = "style"; //NON-NLS
    private static final String FIELD_STYLE_BOLD = "bold"; //NON-NLS
    private static final String FIELD_STYLE_ITALIC = "italic"; //NON-NLS

    private FontData getSystemFontData() {
        return swtWrapper.getDisplay().getSystemFont().getFontData()[0];
    }

    @Override
    public Font getValueFromJson(JsonNode node) throws TransformerException {
        FontData systemFontData = getSystemFontData();
        int style = parseStyle(systemFontData, node);
        int height = parseHeight(systemFontData, node);
        String fontName = parseFontName(systemFontData, node);
        return new Font(Display.getDefault(), fontName, height, style);
    }

    private String parseFontName(FontData systemFontData, JsonNode node) {
        if (!node.has(FIELD_NAME))
            return systemFontData.getName();
        return node.get(FIELD_NAME).asText();
    }

    private int parseHeight(FontData systemFontData, JsonNode node) {
        if (!node.has(FIELD_HEIGHT))
            return systemFontData.getHeight();
        return node.get(FIELD_HEIGHT).asInt();
    }

    private int parseStyle(FontData systemFontData, JsonNode node) throws TransformerException {
        int ofTheJedi = systemFontData.getStyle();
        if (!node.has(FIELD_STYLE)) {
            return ofTheJedi;
        }

        String[] styles = node.get(FIELD_STYLE).asText().split("\\|");
        for (String style : styles) {
            if (FIELD_STYLE_BOLD.equals(style))
                ofTheJedi |= SWT.BOLD;
            else if (FIELD_STYLE_ITALIC.equals(style))
                ofTheJedi |= SWT.ITALIC;
            else
                throw new TransformerException("Unrecognized field style - "+style);
        }
        return ofTheJedi;
    }

}
