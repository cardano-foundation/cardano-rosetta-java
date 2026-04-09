package org.cardanofoundation.rosetta.api;

import java.time.Clock;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenQueryService;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyCollection;
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

        when(mock.queryMetadataBatch(anyCollection())).thenAnswer(invocation -> {
            Collection<AssetFingerprint> fingerprints = invocation.getArgument(0);
            return fingerprints.stream().collect(Collectors.toMap(
                    fp -> fp,
                    fp -> TokenRegistryCurrencyData.builder()
                            .policyId(fp.getPolicyId())
                            .subject(fp.toSubject())
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
