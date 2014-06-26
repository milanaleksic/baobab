package net.milanaleksic.baobab.editor.messages;

/**
 * User: Milan Aleksic
 * Date: 8/12/13
 * Time: 5:12 PM
 */
public class ApplicationError extends Message {

    private final Throwable throwable;

    public ApplicationError(String message, Throwable t) {
        super(message);
        this.throwable = t;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
