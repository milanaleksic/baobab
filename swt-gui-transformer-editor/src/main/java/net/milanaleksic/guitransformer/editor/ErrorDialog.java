package net.milanaleksic.guitransformer.editor;

import net.milanaleksic.guitransformer.EmbeddedEventListener;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import javax.inject.Inject;

import static net.milanaleksic.guitransformer.editor.DialogHelper.BlockingMode.NON_BLOCKING;


/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 7:37 PM
 */
public class ErrorDialog {

    @Inject
    private DialogHelper dialogHelper;

    @TransformerModel(observe = true)
    private ErrorDialogModel model;

    @EmbeddedEventListener(component = "shell", event = SWT.Close)
    private void shellCloseListener(Event event) {
        event.widget.dispose();
    }

    public void showMessage(final String stackTrace) {
        model.setText(stackTrace);
        dialogHelper.bootUpDialog(this, NON_BLOCKING);
    }

}
