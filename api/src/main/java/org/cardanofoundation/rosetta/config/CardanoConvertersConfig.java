package org.cardanofoundation.rosetta.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import jakarta.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.cardanofoundation.conversions.CardanoConverters;
import org.cardanofoundation.conversions.ClasspathConversionsFactory;
import org.cardanofoundation.conversions.domain.NetworkType;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.util.Constants.MAINNET_NETWORK_MAGIC;

@Configuration
@Slf4j
public class CardanoConvertersConfig {

    @Bean
    @Nullable
    public CardanoConverters cardanoConverters(NetworkService networkService) {
         if (networkService.getSupportedNetwork() == null) { // mostly helper for unit tests and integration tests
             return null;
         }

        long protocolMagic = networkService.getSupportedNetwork().getProtocolMagic();

        log.info("Creating CardanoConverters for network magic: {}", protocolMagic);

        if (protocolMagic == MAINNET_NETWORK_MAGIC) {
            return ClasspathConversionsFactory.createConverters(NetworkType.MAINNET);
        }

        if (protocolMagic == Constants.PREPROD_NETWORK_MAGIC) {
            return ClasspathConversionsFactory.createConverters(NetworkType.PREPROD);
        }

        if (protocolMagic == Constants.PREVIEW_NETWORK_MAGIC) {
            return ClasspathConversionsFactory.createConverters(NetworkType.PREVIEW);
        }

        if (protocolMagic == Constants.SANCHONET_NETWORK_MAGIC) {
            return ClasspathConversionsFactory.createConverters(NetworkType.SANCHONET);
        }

        if (protocolMagic == Constants.DEVKIT_NETWORK_MAGIC) {
            // Cardano Converters for DevKit is not supported but we still need to return something sensible
            return null;
        }

        throw new IllegalArgumentException("Unsupported network magic: " + protocolMagic);
    }

    @Bean
    @Qualifier("shellyStartTime")
    @SuppressWarnings("java:S6831")
    public Instant shellyStartTime(@Nullable CardanoConverters cardanoConverters, ZoneId zoneId) {
        if (cardanoConverters == null) {
            return Instant.ofEpochMilli(0L);
        }

        ZoneOffset offset = ZonedDateTime.now(zoneId).getOffset();

        return cardanoConverters.genesisConfig().getShelleyStartTime().toInstant(offset);
    }

    @Bean
    @Qualifier("shelleyStartSlot")
    @SuppressWarnings("java:S6831")
    public Long shelleyStartSlot(@Nullable CardanoConverters cardanoConverters) {
        if (cardanoConverters == null) {
            return 0L;
        }

        return cardanoConverters.genesisConfig().firstShelleySlot();
    }

}
