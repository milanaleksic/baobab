package net.milanaleksic.guitransformer.editor;

import com.google.common.eventbus.EventBus;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 7:37 PM
 */
public class FindDialog {

    @Inject
    private Transformer transformer;

    @Inject
    private EventBus eventBus;

    @TransformerModel(observe = true)
    private FindDialogModel model;

    @EmbeddedEventListener(component = "shell", event = SWT.Close)
    private void shellCloseListener(Event event) {
        event.widget.dispose();
    }

    @EmbeddedEventListener(component = "btnAccept")
    private void btnAcceptSelectionListener() {
        model.setAccepted(true);
        Display.getDefault().getActiveShell().close();
    }

    @EmbeddedEventListener(component = "btnCancel")
    private void btnCancelSelectionListener() {
        Display.getDefault().getActiveShell().close();
    }

    public String getSearchString() {
        final TransformationContext transformationContext;
        try {
            transformationContext = transformer.fillManagedForm(this);
            model.setAccepted(false);
            transformationContext.showAndAwaitClosed();
            return model.isAccepted() ? model.getSearchText() : null;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

}
