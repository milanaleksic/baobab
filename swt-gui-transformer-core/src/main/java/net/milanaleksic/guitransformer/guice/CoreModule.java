package net.milanaleksic.guitransformer.guice;

import com.google.inject.*;
import com.google.inject.multibindings.MapBinder;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.builders.GridDataBuilder;
import net.milanaleksic.guitransformer.providers.*;
import net.milanaleksic.guitransformer.providers.impl.*;
import net.milanaleksic.guitransformer.typed.*;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 10:43 AM
 */
public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
        prepareProviders();
        prepareRegisteredBuildersForObjectConverter();
        prepareRegisteredConvertersForConverterFactory();
    }

    private void prepareProviders() {
        binder().bind(ObjectProvider.class).to(AlwaysReturnNullObjectProvider.class).in(Scopes.SINGLETON);
        binder().bind(ResourceBundleProvider.class).to(SimpleResourceBundleProvider.class).in(Scopes.SINGLETON);
        binder().bind(ImageProvider.class).to(AlwaysReturnEmptyImageProvider.class).in(Scopes.SINGLETON);
    }

    private void prepareRegisteredBuildersForObjectConverter() {
        MapBinder<String, Builder<?>> mapBinder = MapBinder.newMapBinder(binder(),
                new TypeLiteral<String>() {},
                new TypeLiteral<Builder<?>>() {});
        mapBinder.addBinding("gridData").to(GridDataBuilder.class); //NON-NLS
    }

    private void prepareRegisteredConvertersForConverterFactory() {
        MapBinder<Class<?>, Converter<?>> mapBinder = MapBinder.newMapBinder(binder(),
                new TypeLiteral<Class<?>>() {},
                new TypeLiteral<Converter<?>>() {});
        mapBinder.addBinding(Object.class).to(ObjectConverter.class);
        mapBinder.addBinding(String.class).to(StringConverter.class);
        mapBinder.addBinding(boolean.class).to(BooleanConverter.class);
        mapBinder.addBinding(int.class).to(IntegerConverter.class);
        mapBinder.addBinding(org.eclipse.swt.graphics.Point.class).to(PointConverter.class);
        mapBinder.addBinding(org.eclipse.swt.graphics.Color.class).to(ColorConverter.class);
        mapBinder.addBinding(org.eclipse.swt.graphics.Font.class).to(FontConverter.class);
        mapBinder.addBinding(org.eclipse.swt.graphics.Image.class).to(ImageConverter.class);
    }


}
