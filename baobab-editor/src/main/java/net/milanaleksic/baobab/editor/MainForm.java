package net.milanaleksic.baobab.editor;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.milanaleksic.baobab.*;
import net.milanaleksic.baobab.editor.messages.*;
import net.milanaleksic.baobab.editor.model.MainFormModel;
import net.milanaleksic.baobab.model.TransformerModel;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import net.milanaleksic.baobab.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainForm {

    @Inject
    private Transformer transformer;

    @Named(value = "EditorTransformer")
    @Inject
    private Transformer editorTransformer;


    @Inject
    private FindDialog findDialog;

    @Inject
    private ResourceBundleProvider resourceBundleProvider;

    @EmbeddedComponent
    private StyledText editor;

    @TransformerModel(observe = true)
    private MainFormModel model;

    /* editor's own context */
    private MBassador<Message> bus;
    private Shell editorShell;
    private ResourceBundle resourceBundle;

    @EmbeddedEventListener(component = "editorDropTarget", event = DND.Drop)
    private void editorDropTargetDropListener(Event event) {
        if (event.data != null && event.data instanceof String[]) {
            String[] typedData = (String[]) event.data;
            if (typedData.length > 0) {
                openFile(Paths.get(typedData[0]));
            }
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @EmbeddedEventListener(component = "infoLabel", event = SWT.MouseDown)
    private void infoLabelMouseDownListener() {
        if (!model.getLastException().isPresent())
            return;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        model.getLastException().get().printStackTrace(pw);
        bus.publish(new EditorErrorShowDetails(sw.toString()));
    }

    @Handler
    public void fileModified(FileModified modified) {
        Path file = model.getCurrentFile().get();
        if (!modified.getFile().equals(file))
            return;
        editorShell.getDisplay().asyncExec(() -> {
            openFile(file);
            if (!model.getLastException().isPresent())
                model.showInformation("File modification externally, reloaded!");
        });
    }

    public class MainFormBackgroundFormCreator implements Listener {

        @Override
        public void handleEvent(Event event) {
            final Control widget = (Control) event.widget;
            reCreateForm();
            widget.setFocus();
        }

        private void reCreateForm() {
            model.showInformation("");
            String text = editor.getText();
            if (StringUtil.isNullOrEmpty(text))
                return;
            try {
                Shell prototypeShell = createNewPrototypeShell();
                prototypeShell.setLocation(20, 20);
                setSizeOverride(prototypeShell);
                model.setPrototypeShell(prototypeShell);
                prototypeShell.open();
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

        private Shell createNewPrototypeShell() {
            Shell prototypeShell = model.getPrototypeShell();
            if (prototypeShell != null) {
                if (!prototypeShell.isDisposed())
                    prototypeShell.dispose();
            }
            // we might decide to promote ghost shell to prototype (visible) shell
            // if the root of hierarchy is not a Shell
            Shell ghostShell = createGhostShell();
            TransformationContext nonManagedForm = editorTransformer.createFormFromString(editor.getText(), Optional.of(ghostShell));
            model.setActiveWidgets(nonManagedForm.getMappedObjects());
            Composite prototypeRoot = nonManagedForm.getRoot();

            if (prototypeRoot instanceof Shell)
                return (Shell) prototypeRoot;
            else {
                prototypeRoot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                return ghostShell;
            }
        }

        private Shell createGhostShell() {
            Shell ghostShell = new Shell(editorShell, SWT.SHELL_TRIM);
            ghostShell.setLayout(new GridLayout(1, true));
            ghostShell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            return ghostShell;
        }

        private void setSizeOverride(Shell shell) {
            final String width = model.getWidthText();
            final String height = model.getHeightText();
            if (StringUtil.isNullOrEmpty(width) || StringUtil.isNullOrEmpty(height))
                return;
            try {
                int widthAsInt = Integer.parseInt(width, 10);
                int heightAsInt = Integer.parseInt(height, 10);
                if (widthAsInt <= 0 || heightAsInt <= 0)
                    return;
                shell.setSize(widthAsInt, heightAsInt);
            } catch (Exception e) {
                bus.publish(new ApplicationError("Invalid size parameters: " + e, e));
            }
        }
    }

    @EmbeddedEventListeners({
            @EmbeddedEventListener(component = "editor", event = SWT.Modify),
            @EmbeddedEventListener(component = "textWidth", event = SWT.Modify),
            @EmbeddedEventListener(component = "textHeight", event = SWT.Modify)
    })
    private final MainFormBackgroundFormCreator formCreator = new MainFormBackgroundFormCreator();

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
        setCurrentFile(Optional.empty());
        editor.setText(resourceBundle.getString("mainForm.editorDefaultContents"));
    }

    @EmbeddedEventListener(component = "btnOpen", event = SWT.Selection)
    private void btnOpenSelectionListener() {
        FileDialog dlg = new FileDialog(editorShell, SWT.OPEN);
        dlg.setFilterNames(new String[]{resourceBundle.getString("mainForm.openFilters")});
        dlg.setFilterExtensions(new String[]{"*.gui"}); //NON-NLS
        final String selectedFile = dlg.open();
        if (selectedFile == null)
            return;
        openFile(Paths.get(selectedFile));
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
        editorShell.close();
    }

    @EmbeddedEventListener(component = "shell", event = SWT.Close)
    private void shellCloseListener(Event event) {
        try {
            if (!model.isModified())
                return;
            int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION;
            MessageBox messageBox = new MessageBox(editorShell, style);
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
        } finally {
            if (event.doit) {
                bus.publish(new FileWatcherClose());
            }
        }
    }

    @Inject
    public MainForm(MBassador<Message> bus) {
        this.bus = bus;
        bus.subscribe(this);
    }

    private void openFile(Path targetFile) {
        try {
            editor.setText(new String(Files.readAllBytes(targetFile)));
            setCurrentFile(Optional.of(targetFile));
        } catch (IOException e) {
            bus.publish(new ApplicationError(String.format(resourceBundle.getString("mainForm.ioError.open"),
                    targetFile.toFile().getAbsolutePath()), e));
        }
    }

    private void saveCurrentDocument() {
        final Optional<Path> currentFile = model.getCurrentFile();
        if (!currentFile.isPresent() && editor.getText().trim().length() == 0)
            return;
        if (!currentFile.isPresent()) {
            saveDocumentAs();
            return;
        }
        try {
            Files.write(currentFile.get(), editor.getText().getBytes());
        } catch (IOException e) {
            bus.publish(new ApplicationError(String.format(resourceBundle.getString("mainForm.ioError.save"),
                    currentFile.get().toFile().getAbsolutePath()), e));
        }
        model.setModified(false);
    }

    private void saveDocumentAs() {
        FileDialog dlg = new FileDialog(editorShell, SWT.SAVE);
        dlg.setFilterNames(new String[]{resourceBundle.getString("mainForm.openFilters")});
        dlg.setFilterExtensions(new String[]{"*.gui"}); //NON-NLS
        final String selectedFile = dlg.open();
        if (selectedFile == null)
            return;
        setCurrentFile(Optional.of(Paths.get(selectedFile)));
        saveCurrentDocument();
    }

    private void setCurrentFile(Optional<Path> file) {
        model.setCurrentFile(file);
        editorShell.setText(String.format("%s - [%s]",  //NON-NLS
                resourceBundle.getString("mainForm.title"),
                file.map(p -> p.toFile().getAbsolutePath()).orElse(resourceBundle.getString("mainForm.newFile"))));
        model.setModified(false);
        file.ifPresent(f -> bus.publish(new FileToBeWatched(f)));
    }

    public void entryPoint() {
        resourceBundle = resourceBundleProvider.getResourceBundle();
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        this.editorShell = transformationContext.getRoot();
        postTransformation(transformationContext);
        transformationContext.showAndAwaitClosed();
    }

    private void postTransformation(TransformationContext transformationContext) {
        transformationContext.<DropTarget>getMappedObject("editorDropTarget").get() //NON-NLS
                .setTransfer(new Transfer[]{FileTransfer.getInstance()});
        editorTransformer.setDoNotCreateModalDialogs(true);
        setCurrentFile(Optional.empty());
        formCreator.reCreateForm();
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
            model.showInformation("");
            int caretOffset = editor.getCaretOffset();
            int loc = editor.getText().indexOf(model.getLastSearchString(), caretOffset);
            if (loc == -1) {
                model.showInformation(resourceBundle.getString("mainForm.find.noMore"));
                loc = editor.getText().indexOf(model.getLastSearchString(), 0);
            }
            if (loc == -1) {
                model.showInformation(resourceBundle.getString("mainForm.find.noMoreForSure"));
                return;
            }
            editor.setSelection(loc, loc + model.getLastSearchString().length());
        } catch (Throwable t) {
            bus.publish(new ApplicationError("Search failed: " + t.getMessage(), t));
        }
    }
}
