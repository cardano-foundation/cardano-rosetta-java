package org.cardanofoundation.rosetta.api;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class TestConfig {

    @Bean
    @Primary
    public Clock clockFixed() {
        return Clock.systemDefaultZone();
    }

}
