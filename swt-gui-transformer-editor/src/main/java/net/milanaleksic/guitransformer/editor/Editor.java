package net.milanaleksic.guitransformer.editor;

import com.google.inject.Guice;
import net.milanaleksic.guitransformer.editor.guice.EditorModule;
import net.milanaleksic.guitransformer.integration.CoreModule;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 2:44 PM
 */
public class Editor {

    public static void main(String[] args) {
        Guice.createInjector(new CoreModule(), new EditorModule())
                .getInstance(MainForm.class)
                .entryPoint();
    }

}
