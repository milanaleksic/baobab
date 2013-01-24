package net.milanaleksic.guitransformer.editor;

import com.google.common.collect.Lists;
import net.milanaleksic.guitransformer.model.TransformerProperty;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 1/24/13
 * Time: 4:06 PM
 */
public class MainFormModel {

    @TransformerProperty(component = "caretPositionLabel")
    private String caretPositionText;

    @TransformerProperty(component = "infoLabel")
    private String infoText;

    @TransformerProperty(component = "textWidth")
    private String widthText;

    @TransformerProperty(component = "textHeight")
    private String heightText;

    @TransformerProperty(component = "contextWidgets", value = "items")
    private String[] activeWidgets;

    public String getCaretPositionText() {
        return caretPositionText;
    }

    public void setCaretPositionText(String caretPositionText) {
        this.caretPositionText = caretPositionText;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public String getWidthText() {
        return widthText;
    }

    public void setWidthText(String widthText) {
        this.widthText = widthText;
    }

    public String getHeightText() {
        return heightText;
    }

    public void setHeightText(String heightText) {
        this.heightText = heightText;
    }

    public String[] getActiveWidgets() {
        return activeWidgets;
    }

    public void setActiveWidgets(Map<String, Object> widgets) {
        final ArrayList<Object> newWidgets = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : widgets.entrySet()) {
            newWidgets.add(String.format("[%s] - %s", entry.getKey(), entry.getValue().getClass().getName()));
        }
        activeWidgets = newWidgets.toArray(new String[newWidgets.size()]);
    }
}
