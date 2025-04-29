package org.cardanofoundation.rosetta.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import jakarta.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.cardanofoundation.conversions.CardanoConverters;
import org.cardanofoundation.conversions.ClasspathConversionsFactory;
import org.cardanofoundation.rosetta.api.network.service.GenesisDataProvider;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.conversions.domain.NetworkType.*;
import static org.cardanofoundation.rosetta.common.util.Constants.MAINNET_NETWORK_MAGIC;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CardanoConvertersConfig {

    private final GenesisDataProvider genesisDataProvider;

    @Bean
    @Nullable
    public CardanoConverters cardanoConverters() {
        int protocolMagic = genesisDataProvider.getProtocolMagic();

        log.info("Creating CardanoConverters for network magic: {}", protocolMagic);

        if (protocolMagic == MAINNET_NETWORK_MAGIC) {
            log.info("Creating CardanoConverters for mainnet");
            return ClasspathConversionsFactory.createConverters(MAINNET);
        }

        if (protocolMagic == Constants.PREPROD_NETWORK_MAGIC) {
            log.info("Creating CardanoConverters for preprod");
            return ClasspathConversionsFactory.createConverters(PREPROD);
        }

        if (protocolMagic == Constants.PREVIEW_NETWORK_MAGIC) {
            log.info("Creating CardanoConverters for preview");
            return ClasspathConversionsFactory.createConverters(PREVIEW);
        }

        if (protocolMagic == Constants.SANCHONET_NETWORK_MAGIC) {
            log.info("Creating CardanoConverters for sanchonet");
            return ClasspathConversionsFactory.createConverters(SANCHONET);
        }

        if (protocolMagic == Constants.DEVKIT_NETWORK_MAGIC) {
            log.info("Creating CardanoConverters for devkit, no converters available");
            // Cardano Converters for DevKit is not supported but we still need to return something sensible
            return null;
        }

        throw new IllegalArgumentException("Unsupported network magic: %d".formatted(protocolMagic));
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
