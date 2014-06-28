package net.milanaleksic.baobab.editor.messages;

import java.nio.file.Path;

public class FileModified extends Message {

    private Path file;

    public FileModified(Path file) {
        super("File changed: " + file.toFile().getAbsolutePath());
        this.file = file;
    }

    public Path getFile() {
        return file;
    }
}
