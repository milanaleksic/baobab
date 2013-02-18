package net.milanaleksic.guitransformer.editor;

import com.google.inject.*;
import net.milanaleksic.guitransformer.editor.guice.EditorModule;
import net.milanaleksic.guitransformer.integration.CoreModule;
import org.eclipse.swt.widgets.Display;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 2:44 PM
 */
public class Editor {

    @Inject
    private MainForm mainForm;

    public static void main(String[] args) {
        Injector rootInjector = Guice.createInjector(new CoreModule(), new EditorModule());
        rootInjector.getInstance(Editor.class).execute();
    }

    public void execute() {
        mainForm.init();

        Display display = Display.getDefault();
        while (!mainForm.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

}
