package net.milanaleksic.guitransformer.editor;

import com.google.common.eventbus.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.editor.messages.ErrorMessage;
import net.milanaleksic.guitransformer.editor.model.ErrorDialogModel;
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
    private Transformer transformer;

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
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        model.setText(errorMessage.getMessage());
        transformationContext.showAndAwaitClosed();
    }

}
