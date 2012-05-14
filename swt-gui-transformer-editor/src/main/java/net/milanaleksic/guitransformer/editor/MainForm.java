package net.milanaleksic.guitransformer.editor;

import com.google.common.base.Charsets;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;

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
    private ResourceBundleProvider resourceBundleProvider;

    @EmbeddedComponent
    private StyledText editor;

    private ResourceBundle resourceBundle;

    @EmbeddedEventListener(component = "editorDropTarget", event = DND.Drop)
    private final Listener editorDragEnterListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (event.data instanceof String[]) {
                String[] typedData = (String[]) event.data;
                if (typedData != null && typedData.length>0) {
                    openFile(new File(typedData[0]));
                }
            }
            System.out.println(event.data);
        }
    };

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
                .setTransfer(new Transfer[]{ FileTransfer.getInstance() } );
    }

}
