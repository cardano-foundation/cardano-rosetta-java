package org.cardanofoundation.rosetta.api;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anySet;
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
    public TokenRegistryHttpGateway tokenRegistryHttpGateway() {
        TokenRegistryHttpGateway mock = Mockito.mock(TokenRegistryHttpGateway.class);
        
        // Create a default response with all mandatory fields populated
        when(mock.getTokenMetadataBatch(anySet())).thenAnswer(invocation -> {
            Set<String> subjects = invocation.getArgument(0);
            Map<String, Optional<TokenSubject>> result = new HashMap<>();
            
            for (String subject : subjects) {
                // Create a TokenSubject with all mandatory fields
                TokenSubject tokenSubject = new TokenSubject();
                tokenSubject.setSubject(subject);
                
                TokenMetadata metadata = new TokenMetadata();
                // Set mandatory fields with mock values
                metadata.setName(TokenProperty.builder().value("TestToken").source("test").build());
                metadata.setDescription(TokenProperty.builder().value("Test token description").source("test").build());
                
                // Set optional fields
                metadata.setTicker(TokenProperty.builder().value("TST").source("test").build());
                metadata.setUrl(TokenProperty.builder().value("https://example.com").source("test").build());
                metadata.setDecimals(TokenPropertyNumber.builder().value(6L).source("test").build());
                
                tokenSubject.setMetadata(metadata);
                result.put(subject, Optional.of(tokenSubject));
            }
            
            return result;
        });
        
        return mock;
    }

}
