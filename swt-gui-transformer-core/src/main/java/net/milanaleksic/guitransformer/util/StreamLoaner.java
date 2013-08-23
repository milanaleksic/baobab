package net.milanaleksic.guitransformer.util;

import java.io.InputStream;

/**
 * User: Milan Aleksic
 * Date: 8/23/13
 * Time: 4:30 PM
 */
public interface StreamLoaner<T> {

    T loan(InputStream stream);

}
