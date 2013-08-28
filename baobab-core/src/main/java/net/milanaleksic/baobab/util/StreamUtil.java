package net.milanaleksic.baobab.util;

import java.io.*;

/**
 * User: Milan Aleksic
 * Date: 8/23/13
 * Time: 4:26 PM
 */
public class StreamUtil {

    public static <T> T loanResourceReader(String resourceName, ReaderLoaner<T> loaner) {
        Reader contentAsReader = null;
        try {
            contentAsReader = new InputStreamReader(StreamUtil.class.getResourceAsStream(resourceName));
            return loaner.loan(contentAsReader);
        } finally {
            try {
                if (contentAsReader != null) contentAsReader.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static <T> T loanStringStream(String content, ReaderLoaner<T> loaner) {
        Reader contentAsReader = null;
        try {
            contentAsReader = new StringReader(content);
            return loaner.loan(contentAsReader);
        } finally {
            try {
                if (contentAsReader != null) contentAsReader.close();
            } catch (Exception ignored) {
            }
        }
    }

}
