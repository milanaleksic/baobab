package net.milanaleksic.guitransformer.providers.impl;

import net.milanaleksic.guitransformer.providers.ImageProvider;
import org.eclipse.swt.graphics.Image;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 1:16 PM
 */
public class AlwaysReturnNullImageProvider implements ImageProvider {

    @Override
    public Image provideImageForName(String name) {
        return null;
    }

}
