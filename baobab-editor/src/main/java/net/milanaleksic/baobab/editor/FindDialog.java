package net.milanaleksic.baobab.editor;

import net.milanaleksic.baobab.*;
import net.milanaleksic.baobab.editor.model.FindDialogModel;
import net.milanaleksic.baobab.model.TransformerModel;
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
        // model is overwritten during transformation
        // another way to overcome OW is by avoiding transformation (hide form, don't recreate)
        final FindDialogModel oldModel = model;
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        model.setAccepted(false);
        model.copyFrom(oldModel);
        transformationContext.showAndAwaitClosed();
        return model.isAccepted() ? model.getSearchText() : null;
    }

}
