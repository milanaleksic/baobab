package net.milanaleksic.baobab;

import com.google.common.base.Optional;
import net.milanaleksic.baobab.test.GuiceRunner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 09/03/2014
 */
@RunWith(GuiceRunner.class)
public class TransformerIncludeTest {

    @Inject
    private Transformer transformer;

    @Test
    public void tree_transform() {
        TransformationContext context = transformer.createFormFromResource("/net/milanaleksic/baobab/TransformerIncludeTest_Main.gui", null, null);
        assertThat(context.<Text>getMappedObject("output").isPresent(), equalTo(true));
        Optional<Canvas> canvas = context.getMappedObject("canvas");
        Optional<Composite> canvasParent = context.getMappedObject("canvasParent");
        Optional<Shell> root = context.getMappedObject("root");
        assertThat(canvas.isPresent(), equalTo(true));
        assertThat(canvasParent.isPresent(), equalTo(true));
        assertThat(root.isPresent(), equalTo(true));
        assertThat(canvas.get().getParent(), equalTo(canvasParent.get()));
        assertThat((Shell)canvasParent.get().getParent(), equalTo(root.get()));
    }
}
