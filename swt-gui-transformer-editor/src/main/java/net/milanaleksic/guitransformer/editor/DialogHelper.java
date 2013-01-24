package net.milanaleksic.guitransformer.editor;

import net.milanaleksic.guitransformer.*;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 1/24/13
 * Time: 3:49 PM
 */
public class DialogHelper {

    public enum BlockingMode {
        BLOCKING, NON_BLOCKING
    }

    @Inject
    private Transformer transformer;

    @Inject
    private MainForm mainForm;

    public void bootUpDialog(Object dialog, BlockingMode blockingMode) {
        try {
            final TransformationContext transformationContext = transformer.fillManagedForm(mainForm.getShell(), dialog);
            final Shell shell = transformationContext.getShell();

            shell.open();

            if (blockingMode == BlockingMode.BLOCKING) {
                Display display = shell.getDisplay();
                while (!shell.isDisposed()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

}
