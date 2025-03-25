package org.cardanofoundation.rosetta.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolsConfig {

    @Bean
    @Qualifier("ioBoundExecutorService")
    @SuppressWarnings("java:S6831")
    // we use green threads from JDK 21 in this project so no need to use real operating system threads thread pool for IO bound tasks
    public ExecutorService ioBoundExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

}
