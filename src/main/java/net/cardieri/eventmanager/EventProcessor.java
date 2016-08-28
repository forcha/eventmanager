package net.cardieri.eventmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ariel on 28/08/2016.
 */
@Component
public class EventProcessor {

    private static Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    private KafkaSender kafkaSender;

    private Path processing;

    @Autowired
    public EventProcessor(@Value("${eventmanager.directory.processing}") String processing,
                          KafkaSender kafkaSender) throws IOException {
        this.kafkaSender = kafkaSender;
        this.processing = Paths.get(processing);

        if (Files.exists(this.processing)) {
            if (!Files.isDirectory(this.processing)) {
                throw new IllegalArgumentException(String.format("'%s' is not a directory.", processing));
            }
        } else {
            Files.createDirectories(this.processing);
        }
    }

    @Async("producerTaskExecutor")
    public void process(Path file) {
        logger.debug("processing file {}", file);

        // move file to processing
        Path tmpFile = processing.resolve(file.getFileName());
        logger.debug("moving file {}", tmpFile);
        try {
            Files.move(file, tmpFile);
        } catch (IOException e) {
            logger.error(String.format("Error moving file '%s'.", file), e);
            //TODO move file to error
            return;
        }

        // send file to KafkaSender
        kafkaSender.send(tmpFile);

        // if fail, move file to error

        // if success, delete file
    }
}
