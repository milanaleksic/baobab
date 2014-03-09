package net.milanaleksic.baobab.util;

import com.google.common.base.Preconditions;

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
            InputStream resourceAsStream = StreamUtil.class.getResourceAsStream(resourceName);
            Preconditions.checkNotNull(resourceAsStream, "Resource does not exist: %s", resourceName);
            contentAsReader = new InputStreamReader(resourceAsStream);
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
