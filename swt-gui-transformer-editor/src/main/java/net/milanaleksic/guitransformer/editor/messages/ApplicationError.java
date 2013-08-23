package net.milanaleksic.guitransformer.editor.messages;

/**
 * User: Milan Aleksic
 * Date: 8/12/13
 * Time: 5:12 PM
 */
public class ApplicationError {

    private final String message;
    private final Throwable throwable;

    public ApplicationError(String message, Throwable t) {
        this.message = message;
        this.throwable = t;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
