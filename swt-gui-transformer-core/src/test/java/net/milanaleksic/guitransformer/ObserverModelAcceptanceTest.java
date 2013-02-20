package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.model.TransformerModel;
import net.milanaleksic.guitransformer.test.GuiceRunner;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(GuiceRunner.class)
public class ObserverModelAcceptanceTest {

    @Inject
    private Transformer transformer;

    @TransformerModel(observe = true)
    private ModelAcceptanceTestModel model;

    @Test
    public void from_model_to_form() throws TransformerException {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        Text text1 = transformationContext.<Text>getMappedObject("text1").get();
        Text numericalValue = transformationContext.<Text>getMappedObject("numericalValue").get();

        assertThat(model, notNullValue());
        model.setText1("test value");
        model.setData("123");
        model.setNumericalValue(-293);
        model.setIgnoredProperty("test value");

        assertThat(text1.getText(), notNullValue());
        assertThat(numericalValue.getText(), equalTo("-293"));
        assertThat(text1.getData(), notNullValue());
        assertThat(text1.getData().toString(), equalTo("123"));
        assertThat(model.getIgnoredProperty(), equalTo("test value"));
    }

    @Test
    public void from_model_to_form_also_via_method() throws TransformerException {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        Text text1 = transformationContext.<Text>getMappedObject("text1").get();

        assertThat(model, notNullValue());
        model.setData("123");
        assertThat(text1.getData().toString(), equalTo("123"));
        model.setWithSideEffectValueOfData("456");
        assertThat(text1.getData().toString(), equalTo("456"));
    }

    @Test
    public void from_model_to_form_also_via_method_with_multiple_params() throws TransformerException {
        final TransformationContext transformationContext = transformer.fillManagedForm(this);
        Text text1 = transformationContext.<Text>getMappedObject("text1").get();

        assertThat(model, notNullValue());
        model.setData("123");
        assertThat(text1.getData().toString(), equalTo("123"));
        model.setWithSideEffectValueOfData("456");
        assertThat(text1.getData().toString(), equalTo("456"));
        model.setWithSideEffectValueOfData("data", 1, 2, 3, 4, 5D);
        assertThat(text1.getData().toString(), equalTo("data"));
    }

}
