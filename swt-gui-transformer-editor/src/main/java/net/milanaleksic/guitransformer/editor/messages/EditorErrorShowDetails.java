package net.milanaleksic.guitransformer.editor.messages;

/**
 * User: Milan Aleksic
 * Date: 7/11/13
 * Time: 9:44 AM
 */
public class EditorErrorShowDetails {

    private final String message;

    public EditorErrorShowDetails(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
