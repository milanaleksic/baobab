package net.milanaleksic.baobab.providers.impl;

import net.milanaleksic.baobab.providers.ObjectProvider;

/**
 * User: Milan Aleksic
 * Date: 5/14/12
 * Time: 10:15 AM
 */
public class AlwaysReturnNullObjectProvider implements ObjectProvider {

    @Override
    public Object provideObjectNamed(String name) {
        return null;
    }

}
