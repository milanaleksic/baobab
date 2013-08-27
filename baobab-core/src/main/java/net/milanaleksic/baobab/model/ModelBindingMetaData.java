package net.milanaleksic.baobab.model;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.Map;

public class ModelBindingMetaData {

    private boolean formIsBeingUpdatedFromModelRightNow = false;

    private final Map<Field, FieldMapping> fieldMapping = Maps.newHashMap();

    public ModelBindingMetaData() {
    }

    public Map<Field, FieldMapping> getFieldMapping() {
        return fieldMapping;
    }

    @Override
    public String toString() {
        return "ModelBindingMetaData{" +
                "formIsBeingUpdatedFromModelRightNow=" + formIsBeingUpdatedFromModelRightNow +
                ", fieldMapping=" + fieldMapping +
                '}';
    }

    public boolean isFormIsBeingUpdatedFromModelRightNow() {
        return formIsBeingUpdatedFromModelRightNow;
    }

    public void setFormIsBeingUpdatedFromModelRightNow(boolean formIsBeingUpdatedFromModelRightNow) {
        this.formIsBeingUpdatedFromModelRightNow = formIsBeingUpdatedFromModelRightNow;
    }
}
