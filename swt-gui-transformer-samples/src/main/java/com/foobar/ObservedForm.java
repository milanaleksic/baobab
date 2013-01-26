package com.foobar;

import com.google.inject.Guice;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.guice.CoreModule;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class ObservedForm {

    @Inject
    private Transformer transformer;

    @TransformerModel(observe = true)
    private ObservedFormModel model;

    @EmbeddedEventListener(component = "text1", event = SWT.Modify)
    private void textModified() {
        model.setText2(reverse(model.getSourceTextField()));
    }

    private String reverse(String text) {
        if (text.isEmpty())
            return "";
        char[] chars = text.toCharArray();
        StringBuilder builder = new StringBuilder(chars.length);
        builder.setLength(chars.length);
        for (int i = 0; i < text.length(); i++)
            builder.setCharAt(text.length() - i - 1, chars[i]);
        return builder.toString();
    }

    public static void main(String[] args) throws TransformerException {
        Guice.createInjector(new CoreModule()).getInstance(ObservedForm.class).execute();
    }

    public void execute() throws TransformerException {
        TransformationContext transformationContext = transformer.fillManagedForm(this);
        // all SGT injection is done
        Shell shell = transformationContext.getShell();
        shell.open();
        Display display = Display.getDefault();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

}
