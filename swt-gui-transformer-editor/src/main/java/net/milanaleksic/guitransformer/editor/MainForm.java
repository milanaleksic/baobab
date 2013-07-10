package net.milanaleksic.guitransformer.editor;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.model.TransformerModel;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.*;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.*;

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

    /* editor's own context */
    private Shell shell;
    private ResourceBundle resourceBundle;

    private AtomicReference<WatchKey> currentFileExternalChangesWatchKey = new AtomicReference<>(null);

    private final Thread externalWatcherThread = new Thread() {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                long lastUpdated = 0;
                for (; ; ) {
                    WatchKey localizedWatchKey = currentFileExternalChangesWatchKey.get();
                    if (localizedWatchKey == null) {
                        Thread.sleep(500);
                        continue;
                    }
                    for (WatchEvent<?> event : localizedWatchKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        }

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;

                        Path fullFilename = ((Path)localizedWatchKey.watchable()).resolve(ev.context());

                        if (fullFilename.equals(model.getCurrentFile().toPath()) && (System.currentTimeMillis()-lastUpdated>100)) {
                            lastUpdated = System.currentTimeMillis();
                            System.out.println("File reloaded: "+fullFilename);
                            shell.getDisplay().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    openFile(model.getCurrentFile());
                                }
                            });
                        }
                    }
                    boolean valid = localizedWatchKey.reset();
                    if (!valid) {
                        currentFileExternalChangesWatchKey.set(null);
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
    };

    public MainForm() {
        externalWatcherThread.setDaemon(true);
        externalWatcherThread.start();
    }

    @EmbeddedEventListener(component = "editorDropTarget", event = DND.Drop)
    private void editorDropTargetDropListener(Event event) {
        if (event.data != null && event.data instanceof String[]) {
            String[] typedData = (String[]) event.data;
            if (typedData.length > 0) {
                openFile(new File(typedData[0]));
            }
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @EmbeddedEventListener(component = "infoLabel", event = SWT.MouseDown)
    private void infoLabelMouseDownListener() {
        if (model.getLastException() == null)
            return;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        model.getLastException().printStackTrace(pw);
        errorDialog.showMessage(sw.toString());
    }

    private class EditorModifyRunnableListener implements Listener {

        @Override
        public void handleEvent(Event event) {
            model.showInformation("", null);
            String text = editor.getText();
            if (Strings.isNullOrEmpty(text))
                return;
            try {
                TransformationContext nonManagedForm = editorTransformer.createNonManagedForm(shell, editor.getText());
                Shell newShell = nonManagedForm.getShell();
                newShell.setLocation(20, 20);

                removePreviousShell();

                setSizeOverride(newShell);
                model.setCurrentShell(newShell);
                newShell.open();

                ((Control) event.widget).setFocus();

                model.setActiveWidgets(nonManagedForm.getMappedObjects());
            } catch (TransformerException e) {
                model.clearActiveWidgets();
                model.showInformation(String.format(resourceBundle.getString("mainForm.transformationError"), e.getMessage()), e);
            } catch (Exception e) {
                model.clearActiveWidgets();
                model.showInformation(resourceBundle.getString("mainForm.error"), e);
            } finally {
                model.setModified(true);
            }
        }

        private void removePreviousShell() {
            if (model.getCurrentShell() == null)
                return;
            Shell shell = model.getCurrentShell();
            if (!shell.isDisposed())
                shell.dispose();
            model.setCurrentShell(null);
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
        } else if (Character.toLowerCase(event.keyCode) == 'f' && (event.stateMask & SWT.CTRL) == SWT.CTRL) {
            findText();
        } else if (event.keyCode == SWT.F3) {
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
        if (!model.isModified())
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
        final File currentFile = model.getCurrentFile();
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
        model.setModified(false);
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
        model.setCurrentFile(file);
        shell.setText(String.format("%s - [%s]",  //NON-NLS
                resourceBundle.getString("mainForm.title"),
                file == null
                        ? resourceBundle.getString("mainForm.newFile")
                        : file.getName()));
        model.setModified(false);

        setupExternalFSChangesWatcher(file);
    }

    private void setupExternalFSChangesWatcher(File file) {
        WatchKey watchKey = currentFileExternalChangesWatchKey.get();
        if (watchKey != null) {
            watchKey.cancel();
            currentFileExternalChangesWatchKey.set(null);
        }
        try {
            if (file != null) {
                //TODO: you don't close the watchservice!
                WatchService currentFileExternalChangesWatcher = FileSystems.getDefault().newWatchService();
                currentFileExternalChangesWatchKey.set(
                        file.toPath().getParent().register(currentFileExternalChangesWatcher,
                                StandardWatchEventKinds.ENTRY_MODIFY)
                );
            } else {
                currentFileExternalChangesWatchKey.set(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        model.setLastSearchString(findDialog.getSearchString());
        executeSearch();
    }

    private void findNext() {
        if (model.getLastSearchString() == null)
            model.setLastSearchString(findDialog.getSearchString());
        executeSearch();
    }

    private void executeSearch() {
        if (model.getLastSearchString() == null)
            return;
        try {
            model.showInformation("", null);
            int caretOffset = editor.getCaretOffset();
            int loc = editor.getText().indexOf(model.getLastSearchString(), caretOffset);
            if (loc == -1) {
                model.showInformation(resourceBundle.getString("mainForm.find.noMore"), null);
                loc = editor.getText().indexOf(model.getLastSearchString(), 0);
            }
            if (loc == -1) {
                model.showInformation(resourceBundle.getString("mainForm.find.noMoreForSure"), null);
                return;
            }
            editor.setSelection(loc, loc + model.getLastSearchString().length());
        } catch (Throwable t) {
            t.printStackTrace();
            showError(t.getMessage());
        }
    }
}
