package net.milanaleksic.baobab.util;

import net.milanaleksic.baobab.TransformerException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WidgetCreatorTest {

    private static class ComponentWithConstructorAsSWTWidget {
        private final Control parent;
        private final int style;

        public ComponentWithConstructorAsSWTWidget(Control parent, int style) {
            this.parent = parent;
            this.style = style;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComponentWithConstructorAsSWTWidget that = (ComponentWithConstructorAsSWTWidget) o;

            if (style != that.style) return false;
            if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

            return true;
        }
    }

    private static class ComponentWithConstructorAsSWTWidgetWithDisplay {
        private final Display display;
        private final int style;

        public ComponentWithConstructorAsSWTWidgetWithDisplay(Display display, int style) {
            this.display = display;
            this.style = style;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComponentWithConstructorAsSWTWidgetWithDisplay that = (ComponentWithConstructorAsSWTWidgetWithDisplay) o;

            if (style != that.style) return false;
            if (display != null ? !display.equals(that.display) : that.display != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = display != null ? display.hashCode() : 0;
            result = 31 * result + style;
            return result;
        }
    }

    @Test
    public void non_reflective_creation_of_widget_zero_constructor() {
        Control parent = new Shell();
        int style = SWT.BORDER;
        GridLayout instance = WidgetCreator
                .get(GridLayout.class)
                .newInstance(parent, style);
        assertThat(instance, is(not(nullValue())));
    }

    @Test
    public void non_reflective_creation_of_widget() {
        Control parent = new Shell();
        int style = SWT.BORDER;
        ComponentWithConstructorAsSWTWidget instance = WidgetCreator
                .get(ComponentWithConstructorAsSWTWidget.class)
                .newInstance(parent, style);
        assertThat(instance, is(not(nullValue())));
        assertThat(instance.parent, is(equalTo(parent)));
        assertThat(instance.style, is(equalTo(style)));
        ComponentWithConstructorAsSWTWidget manuallyCreatedInstance = new ComponentWithConstructorAsSWTWidget(parent, style);
        assertThat(instance, equalTo(manuallyCreatedInstance));
    }

    @Test
    public void non_reflective_creation_of_widget_with_display() {
        Control parent = new Shell();
        int style = SWT.BORDER;
        ComponentWithConstructorAsSWTWidgetWithDisplay instance = WidgetCreator
                .get(ComponentWithConstructorAsSWTWidgetWithDisplay.class)
                .newInstance(parent, style);
        assertThat(instance, is(not(nullValue())));
        assertThat(instance.display, is(equalTo(parent.getDisplay())));
        assertThat(instance.style, is(equalTo(style)));
        ComponentWithConstructorAsSWTWidgetWithDisplay manuallyCreatedInstance = getComponentWithConstructorAsSWTWidgetWithDisplay(parent, style);
        assertThat(instance, equalTo(manuallyCreatedInstance));
    }

    private ComponentWithConstructorAsSWTWidgetWithDisplay getComponentWithConstructorAsSWTWidgetWithDisplay(Object parent, int style) {
        if (parent == null)
            throw new TransformerException("Null parent widget detected!");
        final Widget parentAsWidget = (Widget) parent;
        return new ComponentWithConstructorAsSWTWidgetWithDisplay(parentAsWidget.getDisplay(), style);
    }

}
