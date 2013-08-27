package net.milanaleksic.baobab.editor;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import net.milanaleksic.baobab.editor.messages.ApplicationError;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.*;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * User: Milan Aleksic
 * Date: 7/11/13
 * Time: 9:02 AM
 */
public class MainFormFileChangesObservable extends Observable {

    public static final int DELAY_BETWEEN_EVENTS = 100;

    private final AtomicReference<WatchKey> currentFileExternalChangesWatchKey = new AtomicReference<>(null);
    private final EventBus eventBus;

    private Optional<WatchService> fileExternalChangesWatcher = Optional.absent();

    private class ExternalWatcherThread extends Thread {

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                long lastUpdated = 0;
                while (true) {
                    WatchKey localizedWatchKey = currentFileExternalChangesWatchKey.get();
                    if (localizedWatchKey == null) {
                        Thread.sleep(500);
                        continue;
                    }
                    for (WatchEvent<?> event : localizedWatchKey.pollEvents()) {
                        if (event.kind() == OVERFLOW)
                            continue;

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fullFilename = ((Path) localizedWatchKey.watchable()).resolve(ev.context());

                        if (System.currentTimeMillis() - lastUpdated > DELAY_BETWEEN_EVENTS) {
                            lastUpdated = System.currentTimeMillis();
                            informListeners(fullFilename);
                        }
                    }
                    boolean valid = localizedWatchKey.reset();
                    if (!valid) {
                        currentFileExternalChangesWatchKey.set(null);
                    }
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
    public MainFormFileChangesObservable(EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            fileExternalChangesWatcher = Optional.of(FileSystems.getDefault().newWatchService());
            ExternalWatcherThread externalWatcherThread = new ExternalWatcherThread();
            externalWatcherThread.setDaemon(true);
            externalWatcherThread.start();
        } catch (IOException e) {
            eventBus.post(new ApplicationError("Watcher could not have been set", e));
        }
    }


    public void close() {
        if (fileExternalChangesWatcher.isPresent()) {
            try {
                fileExternalChangesWatcher.get().close();
            } catch (IOException e) {
                eventBus.post(new ApplicationError("Watcher problem", e));
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
            eventBus.post(new ApplicationError("Watcher problem", e));
        }
    }

}
