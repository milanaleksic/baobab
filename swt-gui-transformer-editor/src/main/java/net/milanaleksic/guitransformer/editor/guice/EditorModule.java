package net.milanaleksic.guitransformer.editor.guice;

import com.google.inject.*;
import com.google.inject.name.Names;
import net.milanaleksic.guitransformer.Transformer;

/**
 * User: Milan Aleksic
 * Date: 5/15/12
 * Time: 9:07 AM
 */
public class EditorModule extends AbstractModule{

    @Override
    protected void configure() {
        binder().
                bind(Transformer.class).
                annotatedWith(Names.named("EditorTransformer")). //NON-NLS
                to(Transformer.class).
                in(Scopes.SINGLETON);
    }

}