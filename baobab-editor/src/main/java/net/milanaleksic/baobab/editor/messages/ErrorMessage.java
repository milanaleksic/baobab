package net.milanaleksic.baobab.editor.messages;

/**
 * User: Milan Aleksic
 * Date: 7/11/13
 * Time: 9:44 AM
 */
public class ErrorMessage {

    private final String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
