package net.milanaleksic.guitransformer.providers;

import net.milanaleksic.guitransformer.TransformerException;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 10:14 AM
 */
public interface ObjectProvider {

    Object provideObjectNamed(String name) throws TransformerException;

}
