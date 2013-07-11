package net.milanaleksic.guitransformer.editor;

import com.google.common.eventbus.*;
import net.milanaleksic.guitransformer.EmbeddedEventListener;
import net.milanaleksic.guitransformer.editor.messages.ErrorMessage;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import javax.inject.Inject;

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

    @Inject
    public ErrorDialog(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void showMessage(ErrorMessage errorMessage) {
        dialogHelper.bootUpDialog(this);
        model.setText(errorMessage.getMessage());
    }

}
