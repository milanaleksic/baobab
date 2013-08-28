package net.milanaleksic.baobab.util;

import java.io.Reader;

/**
 * User: Milan Aleksic
 * Date: 8/23/13
 * Time: 4:30 PM
 */
public interface ReaderLoaner<T> {

    T loan(Reader reader);

}
