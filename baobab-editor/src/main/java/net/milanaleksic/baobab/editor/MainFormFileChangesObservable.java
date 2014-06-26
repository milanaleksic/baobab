package net.milanaleksic.baobab.editor;

import net.engio.mbassy.bus.MBassador;
import net.milanaleksic.baobab.editor.messages.ApplicationError;
import net.milanaleksic.baobab.editor.messages.Message;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * User: Milan Aleksic
 * Date: 7/11/13
 * Time: 9:02 AM
 */
public class MainFormFileChangesObservable extends Observable {

    private final AtomicReference<WatchKey> currentFileExternalChangesWatchKey = new AtomicReference<>(null);
    private final MBassador<Message> bus;

    private Optional<WatchService> fileExternalChangesWatcher = Optional.empty();

    private class ExternalWatcherThread extends Thread {

        private long lastUpdated = Long.MIN_VALUE;

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                while (true) {
                    WatchKey localizedWatchKey = currentFileExternalChangesWatchKey.get();
                    if (localizedWatchKey == null) {
                        Thread.sleep(500);
                        continue;
                    }
                    List<WatchEvent<?>> watchEvents = localizedWatchKey.pollEvents();
                    if (watchEvents.isEmpty())
                        Thread.sleep(100);
                    else {
                        watchEvents
                                .stream()
                                .filter(event -> event.kind() != OVERFLOW)
                                .forEach(event -> {
                                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                                    Path fullFilename = ((Path) localizedWatchKey.watchable()).resolve(ev.context());
                                    long fileUpdatedTimestamp = fullFilename.toFile().lastModified();
                                    if (fileUpdatedTimestamp != lastUpdated) {
                                        // avoiding double event trigger handling
                                        lastUpdated = fileUpdatedTimestamp;
                                        informListeners(fullFilename);
                                    }
                                });
                    }
                    if (!localizedWatchKey.reset())
                        localizedWatchKey.cancel();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void informListeners(Path fullFilename) {
        setChanged();
        notifyObservers(fullFilename);
    }

    @Inject
    public MainFormFileChangesObservable(MBassador<Message> bus) {
        this.bus = bus;
        try {
            fileExternalChangesWatcher = Optional.of(FileSystems.getDefault().newWatchService());
            ExternalWatcherThread externalWatcherThread = new ExternalWatcherThread();
            externalWatcherThread.setDaemon(true);
            externalWatcherThread.start();
        } catch (IOException e) {
            bus.publish(new ApplicationError("Watcher could not have been set", e));
        }
    }


    public void close() {
        if (fileExternalChangesWatcher.isPresent()) {
            try {
                fileExternalChangesWatcher.get().close();
            } catch (IOException e) {
                bus.publish(new ApplicationError("Watcher problem", e));
            }
        }
    }

    public void setupExternalFSChangesWatcher(File file) {
        WatchKey watchKey = currentFileExternalChangesWatchKey.get();
        if (watchKey != null) {
            watchKey.cancel();
            currentFileExternalChangesWatchKey.set(null);
        }
        try {
            if (file != null) {
                currentFileExternalChangesWatchKey.set(
                        file.toPath().getParent().register(fileExternalChangesWatcher.get(),
                                StandardWatchEventKinds.ENTRY_MODIFY)
                );
            } else {
                currentFileExternalChangesWatchKey.set(null);
            }
        } catch (IOException e) {
            bus.publish(new ApplicationError("Watcher problem", e));
        }
    }

}
