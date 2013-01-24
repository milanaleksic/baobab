package net.milanaleksic.guitransformer.model;

import java.lang.annotation.*;

/**
 * User: Milan Aleksic
 * Date: 1/24/13
 * Time: 5:54 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TransformerFireUpdate {
}
