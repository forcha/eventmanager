package net.cardieri.eventmanager;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Created by ariel on 28/08/2016.
 */
@Component
public class KafkaSender {

    private static Logger logger = LoggerFactory.getLogger(KafkaSender.class);

    private KafkaProducer<byte[], byte[]> kafkaProducer;

    public void send(Path file) {
        //TODO read file and send it to kafka
        logger.debug("sending file {}", file.getFileName());
    }

}
