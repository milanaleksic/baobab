package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.model.TransformerIgnoredProperty;
import net.milanaleksic.guitransformer.model.TransformerProperty;

/**
 * User: Milan Aleksic
 * Date: 8/7/12
 * Time: 2:14 PM
 */
public class ModelAcceptanceTestModel {

    @TransformerProperty(component = "text1", value = "data")
    private Object data;

    @TransformerProperty("items")
    private String[] aList;

    @TransformerIgnoredProperty
    private String ignoredProperty;

    private int numericalValue;

    private String text1;

    public String getIgnoredProperty() {
        return ignoredProperty;
    }

    public void setIgnoredProperty(String ignoredProperty) {
        this.ignoredProperty = ignoredProperty;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public int getNumericalValue() {
        return numericalValue;
    }

    public void setNumericalValue(int numericalValue) {
        this.numericalValue = numericalValue;
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String[] getAList() {
        return aList;
    }

    public void setAList(String[] aList) {
        this.aList = aList;
    }
}
