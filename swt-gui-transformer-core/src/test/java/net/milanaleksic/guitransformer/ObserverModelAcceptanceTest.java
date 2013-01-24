package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.model.TransformerModel;
import net.milanaleksic.guitransformer.test.GuiceRunner;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

}
