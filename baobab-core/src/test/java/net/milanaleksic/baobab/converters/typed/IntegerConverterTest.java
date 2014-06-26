package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.node.TextNode;
import org.eclipse.swt.SWT;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 9:44 AM
 */
public class IntegerConverterTest {

    @Test
    public void convert_simple_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat(integerConverter.getValueFromJson(new TextNode("173")), equalTo(173));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void convert_magic_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat(integerConverter.getValueFromJson(new TextNode("{center}")), equalTo(SWT.CENTER));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void convert_multiple_magic_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat(integerConverter.getValueFromJson(new TextNode("1|2")), equalTo(1 | 2));
            assertThat(integerConverter.getValueFromJson(new TextNode("{transparent}|{up}")), equalTo(SWT.TRANSPARENT | SWT.UP));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
