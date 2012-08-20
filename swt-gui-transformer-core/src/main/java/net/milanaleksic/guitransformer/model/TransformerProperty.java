package net.milanaleksic.guitransformer.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransformerProperty {

    public static String DEFAULT_PROPERTY_NAME = "text";

    String value() default DEFAULT_PROPERTY_NAME;

}
