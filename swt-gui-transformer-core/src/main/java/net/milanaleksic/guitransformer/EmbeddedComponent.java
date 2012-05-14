package net.milanaleksic.guitransformer;

import java.lang.annotation.*;

/**
 * User: Milan Aleksic
 * Date: 4/22/12
 * Time: 10:36 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EmbeddedComponent {
    String name() default "";
}
