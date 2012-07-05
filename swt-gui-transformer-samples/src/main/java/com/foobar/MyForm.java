package com.foobar;

import com.google.inject.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.guice.CoreModule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import javax.inject.Inject;

public class MyForm {

    @Inject private Transformer transformer;

    @EmbeddedComponent private Text usernameBox;

    @EmbeddedComponent private Text passwordBox;

    @EmbeddedComponent private Shell myFormShell;

    @EmbeddedEventListener(component = "btnLogin", event = SWT.Selection)
    private void webSiteVisitor() {
        MessageBox box = new MessageBox(myFormShell, SWT.ICON_ERROR);
        box.setMessage("You entered: username="+usernameBox.getText()+", password="+passwordBox.getText());
        box.setText("Information");
        box.open();
    }

    public static void main(String[] args) throws TransformerException {
        // I use here Guice, but you can use any javax.inject - compatible DI container
        Injector rootInjector = Guice.createInjector(new CoreModule());
        final MyForm myFormInstance = rootInjector.getInstance(MyForm.class);
        myFormInstance.execute();
    }

    public void execute() throws TransformerException {
        transformer.fillManagedForm(this);
        // all SGT injection is done
        myFormShell.open();
        Display display = Display.getDefault();
        while (!this.myFormShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
}
