package net.milanaleksic.baobab.util;

import net.milanaleksic.baobab.TransformationContext;

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

    public static TransformationContext loanFileStream(File file, ReaderLoaner<TransformationContext> loaner) {
        Reader contentAsReader = null;
        try {
            contentAsReader = new FileReader(file);
            return loaner.loan(contentAsReader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + file, e);
        } finally {
            try {
                if (contentAsReader != null) contentAsReader.close();
            } catch (Exception ignored) {
            }
        }
    }
}
