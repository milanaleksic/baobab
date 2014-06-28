package net.milanaleksic.baobab;

import net.milanaleksic.baobab.test.GuiceRunner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 02/03/2014
 */
@RunWith(GuiceRunner.class)
public class TransformerPartialTest {

    @Inject
    private Transformer transformer;

    @Test
    public void partial_transform() {
        Shell parentShellForPartial = new Shell(SWT.DIALOG_TRIM);
        transformer.createFormFromResource("/net/milanaleksic/baobab/TransformerPartialTest.gui", Optional.of(parentShellForPartial), Optional.empty());
        parentShellForPartial.dispose();
    }

}
