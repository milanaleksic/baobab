package net.milanaleksic.baobab.editor;

import com.google.common.eventbus.*;
import net.milanaleksic.baobab.editor.messages.ApplicationError;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 8/12/13
 * Time: 5:13 PM
 */
public class ApplicationErrorHandler {

    private ResourceBundle resourceBundle;

    @Inject
    public ApplicationErrorHandler(EventBus eventBus, ResourceBundleProvider resourceBundleProvider) {
        eventBus.register(this);
        this.resourceBundle = resourceBundleProvider.getResourceBundle();
    }

    @Subscribe
    public void showError(ApplicationError error) {
        error.getThrowable().printStackTrace();
        MessageBox box = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR);
        box.setMessage(error.getMessage());
        box.setText(resourceBundle.getString("mainForm.error"));
        box.open();
    }

}
