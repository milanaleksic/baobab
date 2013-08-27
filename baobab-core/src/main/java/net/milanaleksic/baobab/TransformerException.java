package net.milanaleksic.baobab;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:46 AM
 */
public class TransformerException extends RuntimeException {

    public TransformerException(String message) {
        super(message);
    }

    public TransformerException(String message, Throwable cause) {
        super(message, cause);
    }
}
