package net.milanaleksic.baobab.providers.impl;

import net.milanaleksic.baobab.providers.ImageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 1:16 PM
 */
public class AlwaysReturnEmptyImageProvider implements ImageProvider {

    @Override
    public Image provideImageForName(String name) {
        Display display = Display.getCurrent();
        final Image image = new Image(display, 100, 100);
        GC gc = new GC(image);
        gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
        gc.fillRectangle(0,0,100,100);
        gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.drawText(name,0,0);
        gc.dispose();
        return image;
    }

}
