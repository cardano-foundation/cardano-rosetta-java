package org.cardanofoundation.rosetta.api;

import java.time.Clock;
import java.time.Instant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.time.ZoneOffset.UTC;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public Clock clockFixed() {
        return Clock.fixed(Instant.ofEpochMilli(1740053291446L), UTC);
    }

}
