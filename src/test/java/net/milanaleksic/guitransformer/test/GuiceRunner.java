package net.milanaleksic.guitransformer.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class GuiceRunner extends BlockJUnit4ClassRunner {

    private final Injector injector;

    public GuiceRunner(final Class<?> classToRun) throws InitializationError {
        super(classToRun);
        this.injector = Guice.createInjector(new TestModule());
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