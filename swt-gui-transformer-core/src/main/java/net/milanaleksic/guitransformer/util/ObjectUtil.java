package net.milanaleksic.guitransformer.util;

import net.milanaleksic.guitransformer.TransformerException;

import java.lang.reflect.*;

/**
 * User: Milan Aleksic
 * Date: 1/23/13
 * Time: 1:48 PM
 */
public class ObjectUtil {

    public interface OperationOnField {
        void operate(Field field) throws ReflectiveOperationException, TransformerException;
    }

    public static void allowOperationOnField(Field field, OperationOnField operation) throws TransformerException {
        boolean wasPublic = Modifier.isPublic(field.getModifiers());
        if (!wasPublic)
            field.setAccessible(true);
        try {
            operation.operate(field);
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Error while operating on field named " + field.getName(), e);
        } finally {
            if (!wasPublic)
                field.setAccessible(false);
        }
    }

}
