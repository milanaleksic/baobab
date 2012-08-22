package net.milanaleksic.guitransformer.model;

import com.google.common.base.Preconditions;

import java.lang.reflect.Method;

public class FieldMapping {

    public enum BindingType {
        BY_REFERENCE,
        CONVERSION
    }

    private final Object component;

    private final Method getterMethod;

    private final Method setterMethod;

    private final BindingType bindingType;

    public FieldMapping(Object component, Method getterMethod, Method setterMethod, BindingType bindingType) {
        this.component = component;
        this.getterMethod = getterMethod;
        this.setterMethod = setterMethod;
        this.bindingType = bindingType;
    }

    public Object getComponent() {
        return component;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public BindingType getBindingType() {
        return bindingType;
    }

    public static FieldMappingBuilder builder() {
        return new FieldMappingBuilder();
    }

    public static class FieldMappingBuilder {

        private Object component;
        private BindingType bindingType;
        private Method setterMethod;
        private Method getterMethod;

        private FieldMappingBuilder() {}

        public FieldMappingBuilder setComponent(Object component) {
            this.component = component;
            return this;
        }

        public FieldMappingBuilder setGetterMethod(Method getterMethod) {
            this.getterMethod = getterMethod;
            return this;
        }

        public FieldMappingBuilder setSetterMethod(Method setterMethod) {
            this.setterMethod = setterMethod;
            return this;
        }

        public FieldMappingBuilder setBindingType(BindingType bindingType) {
            this.bindingType = bindingType;
            return this;
        }

        public FieldMapping build() {
            Preconditions.checkNotNull(component);
            Preconditions.checkNotNull(getterMethod);
            // setter method is allowed to be null
            Preconditions.checkNotNull(bindingType);
            return new FieldMapping(component, getterMethod, setterMethod, bindingType);
        }
    }

    @Override
    public String toString() {
        return "FieldMapping{" +
                "component=" + component +
                ", getterMethod=" + getterMethod +
                ", setterMethod=" + setterMethod +
                ", bindingType=" + bindingType +
                '}';
    }
}
