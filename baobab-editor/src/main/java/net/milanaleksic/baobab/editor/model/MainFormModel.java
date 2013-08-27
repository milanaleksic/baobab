package net.milanaleksic.baobab.editor.model;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import net.milanaleksic.baobab.model.TransformerFireUpdate;
import net.milanaleksic.baobab.model.*;
import org.eclipse.swt.widgets.Shell;

import javax.annotation.Nullable;
import java.io.File;
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

    /* editing context */
    @TransformerIgnoredProperty
    private Shell currentShell = null;

    @TransformerIgnoredProperty
    private File currentFile = null;

    @TransformerIgnoredProperty
    private boolean modified = false;

    @TransformerIgnoredProperty
    private Optional<Exception> lastException = Optional.absent();

    @TransformerIgnoredProperty
    private String lastSearchString = null;


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
        final ArrayList<String> newWidgets = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : widgets.entrySet()) {
            newWidgets.add(String.format("[%s] - %s", entry.getKey(), entry.getValue().getClass().getName()));
        }
        Collections.sort(newWidgets);
        activeWidgets = newWidgets.toArray(new String[newWidgets.size()]);
    }

    public Shell getCurrentShell() {
        return currentShell;
    }

    public void setCurrentShell(Shell currentShell) {
        this.currentShell = currentShell;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Optional<Exception> getLastException() {
        return lastException;
    }

    public String getLastSearchString() {
        return lastSearchString;
    }

    public void setLastSearchString(String lastSearchString) {
        this.lastSearchString = lastSearchString;
    }

    public void showInformation(String infoText, @Nullable Exception exception) {
        infoText = infoText.replaceAll("\r", "");
        infoText = infoText.replaceAll("\n", "");
        setInfoText(infoText);
        this.lastException = Optional.fromNullable(exception);
    }

    @TransformerFireUpdate
    public void clearActiveWidgets() {
        activeWidgets = new String[] {};
    }
}
