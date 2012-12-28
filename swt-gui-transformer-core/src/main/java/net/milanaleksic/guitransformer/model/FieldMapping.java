package net.milanaleksic.guitransformer.model;

import com.google.common.base.Preconditions;

import java.lang.reflect.Method;
import java.util.Arrays;

public class FieldMapping {

    public enum BindingType {
        BY_REFERENCE,
        CONVERSION
    }

    private final Object component;

    private final Method getterMethod;

    private final Method setterMethod;

    private final BindingType bindingType;

    private final int[] events;

    public FieldMapping(Object component, Method getterMethod, Method setterMethod, BindingType bindingType, int[] events) {
        this.component = component;
        this.getterMethod = getterMethod;
        this.setterMethod = setterMethod;
        this.bindingType = bindingType;
        this.events = Arrays.copyOf(events, events.length);
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

    public int[] getEvents() {
        return Arrays.copyOf(events, events.length);
    }

    public static FieldMappingBuilder builder() {
        return new FieldMappingBuilder();
    }

    public static class FieldMappingBuilder {

        private Object component;
        private BindingType bindingType;
        private Method setterMethod;
        private Method getterMethod;
        private int[] events;

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
            // setter method is allowed to be null
            Preconditions.checkNotNull(component);
            Preconditions.checkNotNull(getterMethod);
            Preconditions.checkNotNull(bindingType);
            Preconditions.checkNotNull(events);
            return new FieldMapping(component, getterMethod, setterMethod, bindingType, events);
        }

        public void setEvents(int[] events) {
            this.events = Arrays.copyOf(events, events.length);
        }

        public int[] getEvents() {
            return Arrays.copyOf(events, events.length);
        }
    }

    @Override
    public String toString() {
        return "FieldMapping{" +
                "component=" + component +
                ", getterMethod=" + getterMethod +
                ", setterMethod=" + setterMethod +
                ", bindingType=" + bindingType +
                ", events=" + Arrays.asList(events) +
                '}';
    }
}
