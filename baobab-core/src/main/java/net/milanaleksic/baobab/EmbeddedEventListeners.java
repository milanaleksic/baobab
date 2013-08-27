package net.milanaleksic.baobab;

import java.lang.annotation.*;

/**
 * User: Milan Aleksic
 * Date: 4/23/12
 * Time: 8:33 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EmbeddedEventListeners {

    EmbeddedEventListener[] value();

}
