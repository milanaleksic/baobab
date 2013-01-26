package net.milanaleksic.guitransformer.loader;

import net.milanaleksic.guitransformer.TransformerException;

public interface Loader {

    void load(Object raw) throws TransformerException;

}
