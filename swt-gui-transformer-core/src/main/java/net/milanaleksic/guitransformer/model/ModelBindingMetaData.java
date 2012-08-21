package net.milanaleksic.guitransformer.model;

import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.Map;

public class ModelBindingMetaData {

    private Map<Field, FieldMapping> fieldMapping = Maps.newHashMap();

    public ModelBindingMetaData() {
    }

    public Map<Field, FieldMapping> getFieldMapping() {
        return fieldMapping;
    }
}
