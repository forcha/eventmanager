package net.cardieri.eventmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Created by ariel on 28/08/2016.
 */
public class FileWatcher implements Runnable {

    public interface Callback {

        void newFile(Path filename);

    }

    private static Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    private Path directory;

    private PathMatcher pathMatcher;

    private Callback callback;

    private boolean running;

    public FileWatcher(Path directory, String pattern, Callback callback) {
        this.callback = callback;
        this.directory = directory;
        this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            directory.register(watcher, ENTRY_CREATE);
            running = true;

            while (running) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    logger.debug("watcher.take interrupted");
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        logger.debug("OVERFLOW");
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    if (pathMatcher.matches(filename)) {
                        callback.newFile(directory.resolve(filename));
                    } else {
                        logger.debug("other file {}", filename);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    logger.debug("key.reset not valid");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Error while watching directory changes.", e);
        }
    }
}
