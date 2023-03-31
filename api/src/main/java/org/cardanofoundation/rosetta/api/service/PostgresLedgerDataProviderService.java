package org.cardanofoundation.rosetta.api.service;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PostgresLedgerDataProviderService implements LedgerDataProviderService {
    @Autowired
    private RosettaConfig rosettaConfig;

    private final Map<String, PostgresLedgerDataProviderClient> clients = new HashMap<>();

    @PostConstruct
    void init() {
        rosettaConfig.getNetworks().forEach(networkConfig -> {
            clients.put(networkConfig.getSanitizedNetworkId(), PostgresLedgerDataProviderClient.builder().networkId(networkConfig.getSanitizedNetworkId()).build());
        });
    }

    @Override
    public BlockIdentifier getTip(final String networkId) {
        if (clients.containsKey(networkId)) {
            return clients.get(networkId).getTip();
        }

        throw new IllegalArgumentException("Invalid network id specified.");
    }
}
