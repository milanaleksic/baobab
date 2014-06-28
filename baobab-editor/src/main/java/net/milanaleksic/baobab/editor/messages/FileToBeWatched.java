package net.milanaleksic.baobab.editor.messages;

import java.nio.file.Path;

public class FileToBeWatched extends Message {
    private Path file;

    public FileToBeWatched(Path file) {
        super("Path to be watched: " + file.toFile().getAbsolutePath());
        this.file = file;
    }

    public Path getFile() {
        return file;
    }
}
