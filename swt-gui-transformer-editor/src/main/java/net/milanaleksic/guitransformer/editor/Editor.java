package net.milanaleksic.guitransformer.editor;

import com.google.inject.*;
import net.milanaleksic.guitransformer.guice.CoreModule;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 2:44 PM
 */
public class Editor {

    public static void main(String[] args) {
        Injector rootInjector = Guice.createInjector(new CoreModule());
    }

}
