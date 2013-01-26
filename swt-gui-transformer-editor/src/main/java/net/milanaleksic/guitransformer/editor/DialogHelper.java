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

    @Inject
    private Transformer transformer;

    @Inject
    private MainForm mainForm;

    public Shell bootUpDialog(Object dialog) {
        try {
            final TransformationContext transformationContext = transformer.fillManagedForm(mainForm.getShell(), dialog);
            final Shell shell = transformationContext.getShell();

            shell.open();

            return shell;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void blockUntilClosed(Shell shell) {
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

}
