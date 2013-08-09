package com.foobar;

import com.google.inject.Guice;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.integration.CoreModule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class MyForm {

    @Inject private Transformer transformer;

    @EmbeddedComponent private Text usernameBox;

    @EmbeddedComponent private Text passwordBox;

    @EmbeddedComponent private Shell myFormShell;

    @EmbeddedEventListener(component = "btnLogin", event = SWT.Selection)
    private void login() {
        MessageBox box = new MessageBox(myFormShell, SWT.ICON_ERROR);
        box.setMessage("You entered: username=" + usernameBox.getText() + ", password=" + passwordBox.getText());
        box.setText("Information");
        box.open();
    }

    @EmbeddedEventListeners({
            @EmbeddedEventListener(component = "usernameBox", event = SWT.Traverse),
            @EmbeddedEventListener(component = "passwordBox", event = SWT.Traverse)
    })
    private void loginOnEnterPressed(Event event) {
        if (event.detail == SWT.TRAVERSE_RETURN)
            login();
    }

    public static void main(String[] args) throws TransformerException {
        MyForm myForm = Guice.createInjector(new CoreModule()).getInstance(MyForm.class);
        myForm.transformer.fillManagedForm(myForm).showAndAwaitClosed();
    }
}
