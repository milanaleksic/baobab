package net.milanaleksic.guitransformer.editor;

import com.google.common.base.Charsets;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 3:00 PM
 */
public class MainForm {

    @Inject
    private Transformer transformer;

    @Inject
    private ErrorDialog errorDialog;

    @Inject
    private ResourceBundleProvider resourceBundleProvider;

    @EmbeddedComponent
    private StyledText editor;

    @EmbeddedComponent
    private Label infoLabel;

    private TransformerException lastException = null;

    private ResourceBundle resourceBundle;

    private Shell currentShell = null;

    @EmbeddedEventListener(component = "editorDropTarget", event = DND.Drop)
    private final Listener editorDropTargetDropListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (event.data instanceof String[]) {
                String[] typedData = (String[]) event.data;
                if (typedData != null && typedData.length > 0) {
                    openFile(new File(typedData[0]));
                }
            }
        }
    };

    @EmbeddedEventListener(component = "infoLabel", event = SWT.MouseDown)
    private final Listener infoLabelMouseDownListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (lastException == null)
                return;
            errorDialog.showMessage(getStackTrace(lastException));
        }

        public String getStackTrace(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return sw.toString();
        }
    };

    @EmbeddedEventListener(component = "editor", event = SWT.Modify)
    private final Listener editorModifyListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            showInformation("", null);
            String text = editor.getText();
            try {
                TransformationContext nonManagedForm = transformer.createNonManagedForm(text);
                Shell newShell = nonManagedForm.getShell();

                removePreviousShell();

                currentShell = newShell;
                currentShell.open();
            } catch (TransformerException e) {
                showInformation(String.format(resourceBundle.getString("mainForm.transformationError"), e.getMessage()), e);
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
    };

    private void showInformation(String infoText, @Nullable TransformerException exception) {
        infoLabel.setText(infoText);
        lastException = exception;
    }

    private void openFile(File targetFile) {
        if (!targetFile.exists()) {
            showError(String.format(resourceBundle.getString("mainForm.fileDoesNotExist"),
                    targetFile.getAbsolutePath()));
            return;
        }
        try {
            editor.setText(com.google.common.io.Files.toString(targetFile, Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            showError(String.format(resourceBundle.getString("mainForm.ioError"), targetFile.getAbsolutePath()));
        }
    }

    private void showError(String errorMessage) {
        MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
        box.setMessage(errorMessage);
        box.setText(resourceBundle.getString("mainForm.error"));
        box.open();
    }

    private Shell shell;

    public boolean isDisposed() {
        return shell.isDisposed();
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
            System.exit(1);
        }
    }

    private void postTransformation(TransformationContext transformationContext) {
        transformationContext.<DropTarget>getMappedObject("editorDropTarget").get() //NON-NLS
                .setTransfer(new Transfer[]{FileTransfer.getInstance()});
    }

}
