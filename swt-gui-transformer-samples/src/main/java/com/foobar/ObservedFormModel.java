package com.foobar;

import net.milanaleksic.guitransformer.model.TransformerProperty;

public class ObservedFormModel {

    // the following is not needed for a simple binding,
    // but shows that that customization is available
    @TransformerProperty(component = "text1", value = "text")
    private String sourceTextField;

    private String text2;

    public String getSourceTextField() {
        return sourceTextField;
    }

    // will fire update of SWT component "text2" because it is a
    // properly named setter for a field with
    public void setText2(String text2) {
        this.text2 = text2;
    }
}
