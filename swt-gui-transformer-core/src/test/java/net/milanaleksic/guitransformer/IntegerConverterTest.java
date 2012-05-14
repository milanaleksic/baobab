package net.milanaleksic.guitransformer;

import com.google.common.collect.Maps;
import net.milanaleksic.guitransformer.typed.IntegerConverter;
import org.codehaus.jackson.node.TextNode;
import org.eclipse.swt.SWT;
import org.junit.*;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 9:44 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class IntegerConverterTest {

    private HashMap<String, Object> mappedObjects;

    @Before
    public void create_dependencies() {
        this.mappedObjects = Maps.newHashMap();
    }

    @Test
    public void convert_simple_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat(integerConverter.getValueFromJson(new TextNode("173"), mappedObjects), equalTo(173));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void convert_magic_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat(integerConverter.getValueFromJson(new TextNode("{center}"), mappedObjects), equalTo(SWT.CENTER));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void convert_multiple_magic_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat(integerConverter.getValueFromJson(new TextNode("1|2"), mappedObjects), equalTo(1 | 2));
            assertThat(integerConverter.getValueFromJson(new TextNode("{transparent}|{up}"), mappedObjects), equalTo(SWT.TRANSPARENT | SWT.UP));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
