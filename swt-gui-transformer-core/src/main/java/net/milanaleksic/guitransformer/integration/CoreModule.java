package net.milanaleksic.guitransformer.integration;

import com.google.inject.*;
import net.milanaleksic.guitransformer.loader.Loader;
import net.milanaleksic.guitransformer.loader.impl.GuiceLoader;
import net.milanaleksic.guitransformer.providers.*;
import net.milanaleksic.guitransformer.providers.impl.*;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 10:43 AM
 */
public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().bind(ObjectProvider.class).to(AlwaysReturnNullObjectProvider.class).in(Scopes.SINGLETON);
        binder().bind(ResourceBundleProvider.class).to(SimpleResourceBundleProvider.class).in(Scopes.SINGLETON);
        binder().bind(ImageProvider.class).to(AlwaysReturnEmptyImageProvider.class).in(Scopes.SINGLETON);
        binder().bind(BuilderProvider.class).to(ConfigurableBuilderProvider.class).in(Scopes.SINGLETON);
        binder().bind(ConverterProvider.class).to(ConfigurableConverterProvider.class).in(Scopes.SINGLETON);
        binder().bind(ShortcutsProvider.class).to(ConfigurableShortcutsProvider.class).in(Scopes.SINGLETON);
        binder().bind(Loader.class).to(GuiceLoader.class).in(Scopes.SINGLETON);
    }

}
