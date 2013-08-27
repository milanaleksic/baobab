package net.milanaleksic.baobab.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TransformerModel {

    String value() default "";

    boolean observe() default false;
}
