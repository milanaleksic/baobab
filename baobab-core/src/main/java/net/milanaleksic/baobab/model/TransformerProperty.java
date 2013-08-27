package net.milanaleksic.baobab.model;

import org.eclipse.swt.SWT;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransformerProperty {

    boolean readOnly() default false;

    String value() default TransformerPropertyConstants.DEFAULT_PROPERTY_NAME;

    String component() default "";

    int[] events() default SWT.Modify; // can't use the TransformerPropertyConstants.DEFAULT_EVENTS here

}
