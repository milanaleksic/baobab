package net.milanaleksic.baobab.util;

import java.nio.charset.Charset;

public class StringUtil {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
