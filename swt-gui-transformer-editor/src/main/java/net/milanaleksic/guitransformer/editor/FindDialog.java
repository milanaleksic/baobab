package net.milanaleksic.guitransformer.editor;

import net.milanaleksic.guitransformer.EmbeddedEventListener;
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
    private DialogHelper dialogHelper;

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
        Shell shell = dialogHelper.bootUpDialog(this);
        model.setAccepted(false);
        dialogHelper.blockUntilClosed(shell);
        return model.isAccepted() ? model.getSearchText() : null;
    }

}
