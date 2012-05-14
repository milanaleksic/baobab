package net.milanaleksic.guitransformer.typed;

import net.milanaleksic.guitransformer.TransformerException;

/**
 * User: Milan Aleksic
 * Date: 5/2/12
 * Time: 11:02 AM
 */
public class IncapableToExecuteTypedConversionException extends TransformerException {

    public IncapableToExecuteTypedConversionException(String message) {
        super(message);
    }

    public IncapableToExecuteTypedConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
