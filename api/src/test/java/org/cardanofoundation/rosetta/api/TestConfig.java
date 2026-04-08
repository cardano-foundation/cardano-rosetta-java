package org.cardanofoundation.rosetta.api;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.Cip26StorageReader;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.model.TokenMetadata;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.Cip68StorageReader;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
    public Cip26StorageReader cip26StorageReader() {
        Cip26StorageReader mock = Mockito.mock(Cip26StorageReader.class);

        when(mock.findBySubject(anyString())).thenAnswer(invocation -> {
            String subject = invocation.getArgument(0);
            TokenMetadata metadata = new TokenMetadata();
            metadata.setSubject(subject);
            metadata.setName("TestToken");
            metadata.setDescription("Test token description");
            metadata.setTicker("TST");
            metadata.setUrl("https://example.com");
            metadata.setDecimals(6L);
            return Optional.of(metadata);
        });

        when(mock.findBySubjects(anyList())).thenAnswer(invocation -> {
            List<String> subjects = invocation.getArgument(0);
            return subjects.stream().map(subject -> {
                TokenMetadata metadata = new TokenMetadata();
                metadata.setSubject(subject);
                metadata.setName("TestToken");
                metadata.setDescription("Test token description");
                metadata.setTicker("TST");
                metadata.setUrl("https://example.com");
                metadata.setDecimals(6L);
                return metadata;
            }).toList();
        });

        when(mock.findLogosBySubjects(anyList())).thenReturn(Map.of());

        return mock;
    }

    @Bean
    @Primary
    public Cip68StorageReader cip68StorageReader() {
        Cip68StorageReader mock = Mockito.mock(Cip68StorageReader.class);
        when(mock.findBySubject(anyString())).thenReturn(Optional.empty());
        return mock;
    }

}
