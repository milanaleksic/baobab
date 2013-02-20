package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.model.*;

/**
 * User: Milan Aleksic
 * Date: 8/7/12
 * Time: 2:14 PM
 */
public class ModelAcceptanceTestModel {

    @TransformerProperty(component = "text1", value = "data")
    private Object data;

    @TransformerIgnoredProperty
    private int data1;

    @TransformerIgnoredProperty
    private long data2;

    @TransformerIgnoredProperty
    private double data3;

    @TransformerIgnoredProperty
    private float data4;

    @TransformerIgnoredProperty
    private Double data5;

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

    @TransformerFireUpdate
    public void setWithSideEffectValueOfData(Object data) {
        this.data = data;
    }

    @TransformerFireUpdate
    public void setWithSideEffectValueOfData(Object data, int data1, long data2, double data3, float data4, Double data5) {
        this.data = data;
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.data4 = data4;
        this.data5 = data5;
    }

    public int getData1() {
        return data1;
    }

    public long getData2() {
        return data2;
    }

    public double getData3() {
        return data3;
    }

    public float getData4() {
        return data4;
    }

    public Double getData5() {
        return data5;
    }
}
