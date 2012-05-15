package net.milanaleksic.guitransformer.editor;

import net.milanaleksic.guitransformer.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 7:37 PM
 */
public class ErrorDialog {

    @Inject
    private Transformer transformer;

    @Inject
    private MainForm mainForm;

    @EmbeddedComponent
    private Text text;

    private Shell shell;

    @EmbeddedEventListener(component = "shell", event = SWT.Close)
    private final Listener shellCloseListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            shell.dispose();
            shell = null;
        }
    };

    public void showMessage(String stackTrace) {
        try {
            final TransformationContext transformationContext = transformer.fillManagedForm(mainForm.getShell(), this);
            this.shell = transformationContext.getShell();

            text.setText(stackTrace);

            shell.open();
        } catch (TransformerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
