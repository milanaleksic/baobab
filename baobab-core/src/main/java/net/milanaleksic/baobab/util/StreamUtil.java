package net.milanaleksic.baobab.util;

import net.milanaleksic.baobab.TransformerException;

import java.io.*;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * User: Milan Aleksic
 * Date: 8/23/13
 * Time: 4:26 PM
 */
public class StreamUtil {

    public static <T> T loanResourceReader(String resourceName, Function<Reader, T> loaner) {
        Reader contentAsReader = null;
        try {
            InputStream resourceAsStream = StreamUtil.class.getResourceAsStream(resourceName);
            Preconditions.checkNotNull(resourceAsStream, String.format("Resource does not exist: %s", resourceName));
            contentAsReader = new InputStreamReader(resourceAsStream);
            return loaner.apply(contentAsReader);
        } finally {
            try {
                if (contentAsReader != null) contentAsReader.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static <T> T loanStringStream(String content, Function<Reader, T> loaner) {
        Reader contentAsReader = null;
        try {
            contentAsReader = new StringReader(content);
            return loaner.apply(contentAsReader);
        } finally {
            try {
                if (contentAsReader != null) contentAsReader.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static <T> T loanRelativeResource(String parentResourceLocation, String relativeLocation, Function<Reader, T> loaner) {
        Reader contentAsReader = null;
        try {
            InputStream childResource = StreamUtil.class.getResourceAsStream(parentResourceLocation + relativeLocation);
            if (childResource != null) {
                contentAsReader = new InputStreamReader(childResource);
            } else {
                File childFileResource = Paths.get(parentResourceLocation, relativeLocation).toFile();
                Preconditions.checkArgument(childFileResource.exists(), String.format("Resource %s could not be found relative to %s", relativeLocation, parentResourceLocation));
                contentAsReader = new FileReader(childFileResource);
            }
            return loaner.apply(contentAsReader);
        } catch (FileNotFoundException ignored) {
            throw new TransformerException("File not found", ignored);
        } finally {
            try {
                if (contentAsReader != null) contentAsReader.close();
            } catch (Exception ignored) {
            }
        }
    }
}
