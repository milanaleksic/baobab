package com.foobar;

import com.google.inject.Guice;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.integration.CoreModule;
import net.milanaleksic.guitransformer.model.TransformerModel;
import org.eclipse.swt.SWT;

import javax.inject.Inject;

public class ObservedForm {

    @Inject private Transformer transformer;

    @TransformerModel(observe = true)
    private ObservedFormModel model;

    // this method will be executed every time SWT Text component
    // we named "text1" in GUI file fires Modify event
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
        ObservedForm observedForm = Guice.createInjector(new CoreModule()).getInstance(ObservedForm.class);
        observedForm.transformer.fillManagedForm(observedForm).showAndAwaitClosed();
    }

}
