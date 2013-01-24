package net.milanaleksic.guitransformer.editor;

import com.google.common.base.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.model.TransformerModel;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.*;
import java.io.*;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 3:00 PM
 */
public class MainForm {

    @Inject
    private Transformer transformer;

    @Named(value = "EditorTransformer")
    @Inject
    private Transformer editorTransformer;

    @Inject
    private ErrorDialog errorDialog;

    @Inject
    private FindDialog findDialog;

    @Inject
    private ResourceBundleProvider resourceBundleProvider;

    @EmbeddedComponent
    private StyledText editor;

    @TransformerModel(observe = true)
    private MainFormModel model;

    /* editing context */
    private Shell currentShell = null;
    private File currentFile = null;
    private boolean modified = false;
    private Exception lastException = null;
    private String lastSearchString = null;

    /* editor's own context */
    private Shell shell;
    private ResourceBundle resourceBundle;

    @EmbeddedEventListener(component = "editorDropTarget", event = DND.Drop)
    private void editorDropTargetDropListener(Event event) {
        if (event.data != null && event.data instanceof String[]) {
            String[] typedData = (String[]) event.data;
            if (typedData.length > 0) {
                openFile(new File(typedData[0]));
            }
        }
    }

    @EmbeddedEventListener(component = "infoLabel", event = SWT.MouseDown)
    private void infoLabelMouseDownListener() {
        if (lastException == null)
            return;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        lastException.printStackTrace(pw);
        errorDialog.showMessage(sw.toString());
    }

    private class EditorModifyRunnableListener implements Listener {

        @Override
        public void handleEvent(Event event) {
            showInformation("", null);
            String text = editor.getText();
            if (Strings.isNullOrEmpty(text))
                return;
            try {
                TransformationContext nonManagedForm = editorTransformer.createNonManagedForm(shell, editor.getText());
                Shell newShell = nonManagedForm.getShell();
                newShell.setLocation(20, 20);

                removePreviousShell();

                setSizeOverride(newShell);
                currentShell = newShell;
                currentShell.open();

                ((Control)event.widget).setFocus();

                model.setActiveWidgets(nonManagedForm.getMappedObjects());

                modified = true;
            } catch (TransformerException e) {
                showInformation(String.format(resourceBundle.getString("mainForm.transformationError"), e.getMessage()), e);
            } catch (Exception e) {
                showInformation(resourceBundle.getString("mainForm.error"), e);
            }
        }

        private void removePreviousShell() {
            if (currentShell == null)
                return;
            Shell shell = currentShell;
            if (!shell.isDisposed())
                shell.dispose();
            currentShell = null;
        }

        private void setSizeOverride(Shell shell) {
            final String width = model.getWidthText();
            final String height = model.getHeightText();
            if (Strings.isNullOrEmpty(width) || Strings.isNullOrEmpty(height))
                return;
            try {
                int widthAsInt = Integer.parseInt(width, 10);
                int heightAsInt = Integer.parseInt(height, 10);
                if (widthAsInt <= 0 || heightAsInt <= 0)
                    return;
                shell.setSize(widthAsInt, heightAsInt);
            } catch (Exception e) {
                showError("Invalid size parameters: " + e);
            }
        }

    }

    @EmbeddedEventListeners({
            @EmbeddedEventListener(component = "editor", event = SWT.Modify),
            @EmbeddedEventListener(component = "textWidth", event = SWT.Modify),
            @EmbeddedEventListener(component = "textHeight", event = SWT.Modify)
    })
    private final EditorModifyRunnableListener editorModifyListener = new EditorModifyRunnableListener();

    @EmbeddedEventListener(component = "editor", event = SWT.KeyDown)
    private void editorKeyDown(Event event) {
        if (Character.toLowerCase(event.keyCode) == 'a' && (event.stateMask & SWT.CTRL) == SWT.CTRL) {
            editor.selectAll();
        }
        else if (Character.toLowerCase(event.keyCode) == 'f' && (event.stateMask & SWT.CTRL) == SWT.CTRL) {
            findText();
        }
        else if (event.keyCode == SWT.F3) {
            findNext();
        }
    }

    @EmbeddedEventListener(component = "editor", event = 3011 /*StyledText.CaretMoved*/)
    private void editorMouseDown() {
        refreshCaretPositionInformation();
    }

    @EmbeddedEventListener(component = "btnNew", event = SWT.Selection)
    private void btnNewSelectionListener() {
        setCurrentFile(null);
        editor.setText("");
    }

    @EmbeddedEventListener(component = "btnOpen", event = SWT.Selection)
    private void btnOpenSelectionListener() {
        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
        dlg.setFilterNames(new String[]{resourceBundle.getString("mainForm.openFilters")});
        dlg.setFilterExtensions(new String[]{"*.gui"}); //NON-NLS
        final String selectedFile = dlg.open();
        if (selectedFile == null)
            return;
        openFile(new File(selectedFile));
    }

    @EmbeddedEventListener(component = "btnSave", event = SWT.Selection)
    private void btnSaveSelectionListener() {
        saveCurrentDocument();
    }

    @EmbeddedEventListener(component = "btnFindText", event = SWT.Selection)
    private void btnFindTextSelectionListener() {
        findText();
    }

    @EmbeddedEventListener(component = "btnFindNext", event = SWT.Selection)
    private void btnFindNextSelectionListener() {
        findNext();
    }

    @EmbeddedEventListener(component = "btnSaveAs", event = SWT.Selection)
    private void btnSaveAsSelectionListener() {
        saveDocumentAs();
    }

    @EmbeddedEventListener(component = "btnExit", event = SWT.Selection)
    private void btnExitSelectionListener() {
        shell.close();
    }

    @EmbeddedEventListener(component = "shell", event = SWT.Close)
    private void shellCloseListener(Event event) {
        if (!modified)
            return;
        int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION;
        MessageBox messageBox = new MessageBox(shell, style);
        messageBox.setText(resourceBundle.getString("mainForm.information"));
        messageBox.setMessage(resourceBundle.getString("mainForm.saveBeforeClosing"));
        switch (messageBox.open()) {
            case SWT.CANCEL:
                event.doit = false;
                break;
            case SWT.YES:
                saveCurrentDocument();
                event.doit = true;
                break;
            case SWT.NO:
            default:
                event.doit = true;
                break;
        }
    }

    private void openFile(File targetFile) {
        if (!targetFile.exists()) {
            showError(String.format(resourceBundle.getString("mainForm.fileDoesNotExist"),
                    targetFile.getAbsolutePath()));
            return;
        }
        try {
            editor.setText(com.google.common.io.Files.toString(targetFile, Charsets.UTF_8));
            setCurrentFile(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            showError(String.format(resourceBundle.getString("mainForm.ioError.open"), targetFile.getAbsolutePath()));
        }
    }

    private void saveCurrentDocument() {
        if (currentFile == null && editor.getText().trim().length() == 0)
            return;
        if (currentFile == null) {
            saveDocumentAs();
            return;
        }
        try {
            com.google.common.io.Files.write(editor.getText(), currentFile, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            showError(String.format(resourceBundle.getString("mainForm.ioError.save"), currentFile.getAbsolutePath()));
        }
        modified = false;
    }

    private void saveDocumentAs() {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        dlg.setFilterNames(new String[]{resourceBundle.getString("mainForm.openFilters")});
        dlg.setFilterExtensions(new String[]{"*.gui"}); //NON-NLS
        final String selectedFile = dlg.open();
        if (selectedFile == null)
            return;
        setCurrentFile(new File(selectedFile));
        saveCurrentDocument();
    }

    private void showInformation(String infoText, @Nullable Exception exception) {
        infoText = infoText.replaceAll("\r", "");
        infoText = infoText.replaceAll("\n", "");
        model.setInfoText(infoText);
        lastException = exception;
    }

    private void showError(String errorMessage) {
        MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
        box.setMessage(errorMessage);
        box.setText(resourceBundle.getString("mainForm.error"));
        box.open();
    }

    public boolean isDisposed() {
        return shell.isDisposed();
    }

    private void setCurrentFile(@Nullable File file) {
        this.currentFile = file;
        shell.setText(String.format("%s - [%s]",  //NON-NLS
                resourceBundle.getString("mainForm.title"),
                currentFile == null
                        ? resourceBundle.getString("mainForm.newFile")
                        : currentFile.getName()));
        modified = false;
    }

    public void init() {
        try {
            resourceBundle = resourceBundleProvider.getResourceBundle();
            final TransformationContext transformationContext = transformer.fillManagedForm(this);
            this.shell = transformationContext.getShell();

            postTransformation(transformationContext);

            shell.open();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void postTransformation(TransformationContext transformationContext) {
        transformationContext.<DropTarget>getMappedObject("editorDropTarget").get() //NON-NLS
                .setTransfer(new Transfer[]{FileTransfer.getInstance()});
        editorTransformer.setDoNotCreateModalDialogs(true);
        setCurrentFile(null);
    }

    Shell getShell() {
        return shell;
    }

    private void refreshCaretPositionInformation() {
        final int caretOffset = editor.getCaretOffset();
        final int line = editor.getLineAtOffset(caretOffset);
        model.setCaretPositionText(String.format("%dx%d",
                line + 1,
                caretOffset - editor.getContent().getOffsetAtLine(line) + 1));
    }

    private void findText() {
        lastSearchString = findDialog.getSearchString();
        executeSearch();
    }

    private void findNext() {
        if (lastSearchString == null)
            lastSearchString = findDialog.getSearchString();
        executeSearch();
    }

    private void executeSearch() {
        if (lastSearchString == null)
            return;
        try {
            showInformation("", null);
            int caretOffset = editor.getCaretOffset();
            int loc = editor.getText().indexOf(lastSearchString, caretOffset);
            if (loc == -1) {
                showInformation(resourceBundle.getString("mainForm.find.noMore"), null);
                loc = editor.getText().indexOf(lastSearchString, 0);
            }
            if (loc == -1) {
                showInformation(resourceBundle.getString("mainForm.find.noMoreForSure"), null);
                return;
            }
            editor.setSelection(loc, loc + lastSearchString.length());
        } catch (Throwable t) {
            t.printStackTrace();
            showError(t.getMessage());
        }
    }
}
