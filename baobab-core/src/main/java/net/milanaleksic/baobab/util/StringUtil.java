package net.milanaleksic.baobab.util;

import javax.annotation.Nullable;
import java.nio.charset.Charset;

public class StringUtil {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

}
