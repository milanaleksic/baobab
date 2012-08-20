package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.model.TransformerProperty;

/**
 * User: Milan Aleksic
 * Date: 8/7/12
 * Time: 2:14 PM
 */
public class ModelAcceptanceTestModel {

    @TransformerProperty("items")
    private String[] aList;

    private int numericalValue;

    private String text1;

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

    public String[] getaList() {
        return aList;
    }

    public void setaList(String[] aList) {
        this.aList = aList;
    }
}
