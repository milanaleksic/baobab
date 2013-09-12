package net.milanaleksic.baobab.test;

import com.google.inject.Guice;
import com.google.inject.util.Modules;
import net.milanaleksic.baobab.generator.GeneratorModule;
import net.milanaleksic.baobab.integration.CoreModule;
import org.junit.runners.model.InitializationError;

public class GuiceGeneratorRunner extends GuiceRunner {

    public GuiceGeneratorRunner(Class<?> classToRun) throws InitializationError {
        super(classToRun, Guice.createInjector(Modules.override(new CoreModule()).with(new GeneratorModule())));
    }

}
