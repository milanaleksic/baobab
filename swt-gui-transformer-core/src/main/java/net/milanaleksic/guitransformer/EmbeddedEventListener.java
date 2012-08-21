package net.milanaleksic.guitransformer;

import org.eclipse.swt.SWT;

import java.lang.annotation.*;

/**
 * User: Milan Aleksic
 * Date: 4/23/12
 * Time: 8:33 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EmbeddedEventListener {

    String component() default "";

    int event() default SWT.Selection;

}
