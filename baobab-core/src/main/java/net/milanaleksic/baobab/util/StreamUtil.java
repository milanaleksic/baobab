package net.milanaleksic.baobab.util;

import java.io.InputStream;

/**
 * User: Milan Aleksic
 * Date: 8/23/13
 * Time: 4:26 PM
 */
public class StreamUtil {

    public static <T> T loanResourceStream(String resourceName, StreamLoaner<T> loaner) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = StreamUtil.class.getResourceAsStream(resourceName);
            return loaner.loan(resourceAsStream);
        } finally {
            try {
                if (resourceAsStream != null) resourceAsStream.close();
            } catch (Exception ignored) {
            }
        }
    }

}
