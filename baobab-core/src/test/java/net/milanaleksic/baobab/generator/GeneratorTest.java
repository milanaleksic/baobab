package net.milanaleksic.baobab.generator;

import net.milanaleksic.baobab.Transformer;
import net.milanaleksic.baobab.test.GuiceGeneratorRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(GuiceGeneratorRunner.class)
public class GeneratorTest {

    @Inject
    private Transformer transformer;

    @Test
    public void simple_transform() {
        transformer.fillManagedForm(this);
    }

}
