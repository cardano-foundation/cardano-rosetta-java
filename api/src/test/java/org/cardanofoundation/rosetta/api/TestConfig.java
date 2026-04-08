package org.cardanofoundation.rosetta.api;

import java.time.Clock;
import java.util.Map;
import java.util.stream.Collectors;

import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenQueryService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;


@Configuration
public class TestConfig {

    @Bean
    @Primary
    public Clock clockFixed() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Primary
    public TokenQueryService tokenQueryService() {
        TokenQueryService mock = Mockito.mock(TokenQueryService.class);

        when(mock.queryMetadataBatch(anyList(), anyMap())).thenAnswer(invocation -> {
            java.util.List<String> subjects = invocation.getArgument(0);
            Map<String, String> subjectToPolicyId = invocation.getArgument(1);
            return subjects.stream().collect(Collectors.toMap(
                    subject -> subject,
                    subject -> TokenRegistryCurrencyData.builder()
                            .policyId(subjectToPolicyId.getOrDefault(subject, "unknown"))
                            .subject(subject)
                            .name("TestToken")
                            .description("Test token description")
                            .ticker("TST")
                            .url("https://example.com")
                            .decimals(6)
                            .build()));
        });

        return mock;
    }

}
