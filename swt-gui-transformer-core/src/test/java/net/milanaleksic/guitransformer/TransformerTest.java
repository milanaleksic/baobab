package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import net.milanaleksic.guitransformer.test.GuiceRunner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:37 AM
 */
@RunWith(GuiceRunner.class)
public class TransformerTest {

    @Inject
    Transformer transformer;

    @Test
    public void form_creation() throws TransformerException {
        test_form_creation(transformer.createFormFromResource("/net/milanaleksic/guitransformer/TestForm.gui"));
    }

    @Test
    public void form_creation_short_notation() throws TransformerException {
        test_form_creation(transformer.createFormFromResource("/net/milanaleksic/guitransformer/TestForm_ShortChildrenNotation.gui"));
    }

    private void test_form_creation(TransformationContext context) {
        Shell form = context.getShell();
        assertThat(form, not(nullValue()));
        assertThat(form.getText(), equalTo("Delete movie"));
        assertThat(form.getSize(), equalTo(new Point(431,154)));
        assertThat(form.getLayout(), not(nullValue()));
        assertThat(form.getLayout(), instanceOf(GridLayout.class));
        GridLayout layout = (GridLayout) form.getLayout();
        assertThat(layout.numColumns, equalTo(2));

        Object[] children = form.getChildren();
        assertThat(children, not(nullValue()));
        assertThat(children.length, equalTo(5));

        assertThat(children[0], Matchers.instanceOf(Canvas.class));
        Canvas canvas = (Canvas) children[0];
        GridData canvasGridData = (GridData) canvas.getLayoutData();
        assertThat(canvasGridData.verticalSpan, equalTo(2));
        assertThat(canvasGridData.heightHint, equalTo(64));
        assertThat(canvasGridData.widthHint, equalTo(64));
        assertThat(canvas.getBackground(), equalTo(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)));

        assertThat(children[1], Matchers.instanceOf(Label.class));
        assertThat(((Label)children[1]).getText(), equalTo("Do you really wish to delete movie??"));
        Font labelFont = ((Control)children[1]).getFont();
        assertThat(labelFont.getFontData()[0].getStyle() & SWT.BOLD, equalTo(SWT.BOLD));
        assertThat(labelFont.getFontData()[0].getHeight(), equalTo(12));

        assertThat(children[2], Matchers.<Object>instanceOf(Label.class));
        assertThat(((Label)children[2]).getText(), equalTo(""));
        assertThat((Control)context.getMappedObject("labFilmNaziv").get(), equalTo(children[2]));

        assertThat(children[3], Matchers.instanceOf(Label.class));
        assertThat(((Label)children[3]).getText(), equalTo(""));

        assertThat(children[4], Matchers.instanceOf(Label.class));
        Optional<Cursor> cursor = context.getMappedObject("handCursor");
        assertThat(cursor, not(nullValue()));
        assertThat(cursor.isPresent(), equalTo(true));
        assertThat(((Label)children[4]).getCursor(), equalTo(cursor.get()));
        assertThat(((Label)children[4]).getForeground(), equalTo(Display.getDefault().getSystemColor(SWT.COLOR_RED)));
    }

}
