package net.milanaleksic.guitransformer.swt;

import org.eclipse.swt.widgets.Display;

/**
 * User: Milan Aleksic
 * Date: 7/30/12
 * Time: 4:08 PM
 */
public class NativeSwtWrapper implements SwtWrapper{

    @Override
    public Display getDisplay() {
        return Display.getDefault();
    }

}
