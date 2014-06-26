package net.milanaleksic.baobab.test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.List;

import net.milanaleksic.baobab.integration.CoreModule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class GuiceRunner extends BlockJUnit4ClassRunner {

    private final Injector injector;

    public GuiceRunner(final Class<?> classToRun) throws InitializationError {
        super(classToRun);
        this.injector = Guice.createInjector(new CoreModule());
    }

    @Override
    public Object createTest() {
        return injector.getInstance(getTestClass().getJavaClass());
    }

    @Override
    protected void validateZeroArgConstructor(List<Throwable> errors) {}

    protected Injector getInjector() {
        return injector;
    }
}