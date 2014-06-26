package net.milanaleksic.baobab.editor;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.milanaleksic.baobab.*;
import net.milanaleksic.baobab.editor.messages.EditorErrorShowDetails;
import net.milanaleksic.baobab.editor.messages.Message;
import net.milanaleksic.baobab.editor.model.ErrorDialogModel;
import net.milanaleksic.baobab.model.TransformerModel;
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
    public ErrorDialog(MBassador<Message> bus) {
        bus.subscribe(this);
    }

    @Handler
    public void showMessage(EditorErrorShowDetails errorMessage) {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        model.setText(errorMessage.getMessage());
        transformationContext.showAndAwaitClosed();
    }

}
