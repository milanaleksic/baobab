package com.foobar;

import com.google.inject.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.guice.CoreModule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * User: Milan Aleksic
 * Date: 7/5/12
 * Time: 3:20 PM
 */
public class MyForm {

    @EmbeddedComponent private Text usernameBox;

    @EmbeddedComponent private Text passwordBox;

    private Shell shell; // we can embed it also, just needs to be named in GUI file

    @EmbeddedEventListener(component = "btnLogin", event = SWT.Selection)
    private void webSiteVisitor(Event event) {
        MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
        box.setMessage(
                String.format("You entered: username=%s, password=%s", usernameBox.getText(), passwordBox.getText()));
        box.setText("Information");
        box.open();
    }

    public static void main(String[] args) throws TransformerException {
        Injector rootInjector = Guice.createInjector(new CoreModule());
        rootInjector.getInstance(MyForm.class).execute(rootInjector.getInstance(Transformer.class));
    }

    public void execute(Transformer transformer) throws TransformerException {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        this.shell = transformationContext.getShell();

        shell.open();

        Display display = Display.getDefault();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
}
