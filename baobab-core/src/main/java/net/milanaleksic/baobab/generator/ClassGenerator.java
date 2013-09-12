package net.milanaleksic.baobab.generator;

import com.google.inject.Guice;
import com.google.inject.util.Modules;
import net.milanaleksic.baobab.Transformer;
import net.milanaleksic.baobab.integration.CoreModule;

import java.io.File;

public class ClassGenerator {

    public static void main(String[] args) {
        Guice.createInjector(Modules.override(new CoreModule()).with(new GeneratorModule()))
                .getInstance(Transformer.class)
                .createFormFromFile(new File(args[0]), null);
    }

}
