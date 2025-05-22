package org.cardanofoundation.rosetta.config;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfig {

    @Value("${cardano.rosetta.HTTP_CONNECT_TIMEOUT_SECONDS}")
    private int httpConnectTimeoutSeconds;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(httpConnectTimeoutSeconds))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

}
