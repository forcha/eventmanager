package net.cardieri.eventmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

/**
 * Created by ariel on 28/08/2016.
 */
@Component
public class EventReader implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(EventReader.class);

    private EventProcessor eventProcessor;

    private FileWatcher fileWatcher;

    private String pattern;

    private Path inboxPath;

    @Autowired
    public EventReader(@Value("${eventmanager.directory.inbox}") String inbox,
                       @Value("${eventmanager.file.pattern}") String pattern,
                       EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
        this.pattern = pattern;
        this.inboxPath = Paths.get(inbox);

        if (!Files.exists(this.inboxPath)) {
            throw new IllegalArgumentException(String.format("'%s' does not exists.", inbox));
        }

        if (!Files.isDirectory(this.inboxPath)) {
            throw new IllegalArgumentException(String.format("'%s' is not a directory.", inbox));
        }

        fileWatcher = new FileWatcher(inboxPath, pattern, eventProcessor::process);
    }

    @Override
    @Async("consumerTaskExecutor")
    public void run() {
        // TODO Process incomplete events

        // Process INBOX directory
        processDirectory(inboxPath);

        // Start watching for changes
        fileWatcher.run();
    }

    private void processDirectory(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, pattern)) {
            stream.forEach(eventProcessor::process);
        } catch (IOException | DirectoryIteratorException e) {
            logger.error(String.format("Error processing directory '%s'", path.getFileName()), e);
        }
    }
}
