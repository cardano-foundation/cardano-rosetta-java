package org.cardanofoundation.rosetta.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class YaciHttpGatewayImpl implements YaciHttpGateway {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cardano.rosetta.YACI_HTTP_BASE_URL}")
    protected String yaciBaseUrl;

    @Value("${cardano.rosetta.HTTP_REQUEST_TIMEOUT_SECONDS}")
    protected int httpRequestTimeoutSeconds;

    @PostConstruct
    public void init() {
        log.info("YaciHttpGatewayImpl initialized with yaciBaseUrl: {}, httpRequestTimeoutSeconds: {}", yaciBaseUrl, httpRequestTimeoutSeconds);
    }

    @Override
    public StakeAccountInfo getStakeAccountRewards(String stakeAddress) {
        var getStakeAccountDetailsHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(yaciBaseUrl + "/rosetta/account/by-stake-address/" + stakeAddress))
                .GET()
                .timeout(Duration.ofSeconds(httpRequestTimeoutSeconds))
                .header("Content-Type", "application/json")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(getStakeAccountDetailsHttpRequest, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                return objectMapper.readValue(responseBody, StakeAccountInfo.class);
            } else if (statusCode == 400) {
                throw ExceptionFactory.gatewayError(false);
            } else if (statusCode == 500) {
                throw ExceptionFactory.gatewayError(true);
            } else {
                throw ExceptionFactory.gatewayError(false);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error during yaci-indexer HTTP request", e);

            Thread.currentThread().interrupt();

            throw ExceptionFactory.gatewayError(true);
        }
    }

}
