package net.milanaleksic.baobab.editor.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.milanaleksic.baobab.Transformer;
import net.milanaleksic.baobab.editor.ApplicationErrorHandler;
import net.milanaleksic.baobab.editor.ErrorDialog;
import net.milanaleksic.baobab.editor.MainFormFileChangesWatcher;
import net.milanaleksic.baobab.editor.messages.Message;

/**
 * User: Milan Aleksic
 * Date: 5/15/12
 * Time: 9:07 AM
 */
public class EditorModule extends AbstractModule {

    @Override
    protected void configure() {
        final Binder binder = binder();
        binder.bind(Transformer.class)
                .annotatedWith(Names.named("EditorTransformer")) //NON-NLS
                .to(Transformer.class)
                .in(Scopes.SINGLETON);
        binder.bind(new TypeLiteral<MBassador<Message>>() {
        }).toInstance(new MBassador<>(BusConfiguration.Default()));

        binder.bind(ErrorDialog.class).asEagerSingleton();
        binder.bind(ApplicationErrorHandler.class).asEagerSingleton();
        binder.bind(MainFormFileChangesWatcher.class).asEagerSingleton();
    }

}
