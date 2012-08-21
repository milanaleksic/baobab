package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import net.milanaleksic.guitransformer.model.TransformerModel;
import net.milanaleksic.guitransformer.test.GuiceRunner;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(GuiceRunner.class)
public class ModelAcceptanceTest {

    @Inject
    private Transformer transformer;

    @TransformerModel
    private ModelAcceptanceTestModel model;

    @Test
    public void from_form_to_model() throws TransformerException {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        Optional<Text> text = transformationContext.getMappedObject("text1");
        final Text text1 = text.get();
        text1.setText("test value");
        assertThat(model, notNullValue());
        assertThat(model.getText1(), notNullValue());
        assertThat(model.getText1(), equalTo("test value"));
        assertThat(model.getNumericalValue(), notNullValue());
        assertThat(model.getNumericalValue(), equalTo(175));
        text1.setText("new value");
        assertThat(model.getText1(), equalTo("new value"));
        model.setaList(new String[]{"1", "2", "3"});
        assertThat(model.getaList(), notNullValue());
        assertThat(Arrays.asList(model.getaList()), hasItems("1", "2", "3"));
    }

    @Test
    public void from_model_to_form() throws TransformerException {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        Text text1 = transformationContext.<Text>getMappedObject("text1").get();
        Text numericalValue = transformationContext.<Text>getMappedObject("numericalValue").get();

        assertThat(model, notNullValue());
        model.setText1("test value");
        model.setData("123");
        model.setNumericalValue(-293);
        transformer.updateFormFromModel(model, transformationContext);

        assertThat(text1.getText(), notNullValue());
        assertThat(numericalValue.getText(), equalTo("-293"));
        assertThat(text1.getData(), notNullValue());
        assertThat(text1.getData().toString(), equalTo("123"));
    }

}
