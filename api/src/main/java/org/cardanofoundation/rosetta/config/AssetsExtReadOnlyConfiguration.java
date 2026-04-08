package org.cardanofoundation.rosetta.config;

import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.Cip26StorageReader;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.Cip26StorageReaderImpl;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.repository.TokenLogoRepository;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.repository.TokenMetadataRepository;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.service.Cip68TokenService;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.Cip68StorageReader;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.Cip68StorageReaderImpl;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.impl.repository.MetadataReferenceNftRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Read-only configuration for yaci-store assets-ext.
 * Enables only the JPA repositories, entities and storage readers needed to query
 * CIP-26/CIP-68 token metadata from the shared database.
 * Processors, cron jobs, and other write-path components are intentionally excluded
 * (the full AssetsExtConfiguration is not used because its @ComponentScan would pull them in).
 */
@Configuration
@EntityScan(basePackages = {
    "com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.model",
    "com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.impl.model"
})
@EnableJpaRepositories(basePackages = {
    "com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.repository",
    "com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.impl.repository"
})
public class AssetsExtReadOnlyConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Cip68TokenService cip68TokenService(MetadataReferenceNftRepository metadataReferenceNftRepository) {
        return new Cip68TokenService(metadataReferenceNftRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public Cip26StorageReader cip26StorageReader(TokenMetadataRepository tokenMetadataRepository,
                                                  TokenLogoRepository tokenLogoRepository) {
        return new Cip26StorageReaderImpl(tokenMetadataRepository, tokenLogoRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public Cip68StorageReader cip68StorageReader(Cip68TokenService cip68TokenService) {
        return new Cip68StorageReaderImpl(cip68TokenService);
    }
}
