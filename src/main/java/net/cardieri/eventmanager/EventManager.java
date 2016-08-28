package net.cardieri.eventmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by ariel on 28/08/2016.
 */
@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class EventManager implements ApplicationRunner {

    private static Logger logger = LoggerFactory.getLogger(EventManager.class);

    private EventReader eventReader;

    @Autowired
    public EventManager(EventReader eventReader) {
        this.eventReader = eventReader;
    }

    @Bean
    public TaskExecutor consumerTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public TaskExecutor producerTaskExecutor(@Value("${eventmanager.threadsNumber}") Integer threadsNumber) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadsNumber);
        return executor;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        eventReader.run();
    }

    public static void main(String[] args) {
        SpringApplication.run(EventManager.class, args);
    }
}
