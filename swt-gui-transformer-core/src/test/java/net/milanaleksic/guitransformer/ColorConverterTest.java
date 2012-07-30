package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.test.GuiceRunner;
import net.milanaleksic.guitransformer.typed.ColorConverter;
import org.codehaus.jackson.node.TextNode;
import org.eclipse.swt.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 10:04 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
@RunWith(GuiceRunner.class)
public class ColorConverterTest {

    @Inject ColorConverter colorConverter;

    @Test
    public void convert_simple_value() {
        try {
            Color color = colorConverter.getValueFromJson(new TextNode("#ff1001"));
            assertThat(color.getRed(), equalTo(255));
            assertThat(color.getGreen(), equalTo(16));
            assertThat(color.getBlue(), equalTo(1));
            Color color2 = colorConverter.getValueFromJson(new TextNode("#00AA66"));
            assertThat(color2.getRed(), equalTo(0));
            assertThat(color2.getGreen(), equalTo(170));
            assertThat(color2.getBlue(), equalTo(102));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
