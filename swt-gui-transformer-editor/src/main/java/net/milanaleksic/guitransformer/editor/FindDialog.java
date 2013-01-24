package net.milanaleksic.guitransformer.editor;

import net.milanaleksic.guitransformer.EmbeddedEventListener;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

import static net.milanaleksic.guitransformer.editor.DialogHelper.BlockingMode.BLOCKING;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 7:37 PM
 */
public class FindDialog {

    @Inject
    private DialogHelper dialogHelper;

    private boolean accepted;

    @TransformerModel(observe = true)
    private FindDialogModel model;

    @EmbeddedEventListener(component = "shell", event = SWT.Close)
    private void shellCloseListener(Event event) {
        event.widget.dispose();
    }

    @EmbeddedEventListener(component = "btnAccept")
    private void btnAcceptSelectionListener() {
        accepted = true;
        Display.getDefault().getActiveShell().close();
    }

    @EmbeddedEventListener(component = "btnCancel")
    private void btnCancelSelectionListener() {
        Display.getDefault().getActiveShell().close();
    }

    public String getSearchString() {
        accepted = false;
        dialogHelper.bootUpDialog(this, BLOCKING);
        return accepted ? model.getSearchText() : null;
    }

}
