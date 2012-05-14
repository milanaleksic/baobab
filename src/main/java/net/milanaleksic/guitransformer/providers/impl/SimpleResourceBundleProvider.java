package net.milanaleksic.guitransformer.providers.impl;

import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;

import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 10:02 AM
 */
public class SimpleResourceBundleProvider implements ResourceBundleProvider{

    private ResourceBundle resourceBundle;

    public SimpleResourceBundleProvider() {
        resourceBundle = ResourceBundle.getBundle("messages", new Locale("en"));//NON-NLS
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

}
