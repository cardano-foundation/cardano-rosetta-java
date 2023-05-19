package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class YaciNodeBridgeService implements NodeBridgeService {

    @Autowired
    private RosettaConfig rosettaConfig;

    private final Map<String, YaciNodeBridgeClient> clients = new HashMap<>();

    @PostConstruct
    void init() {
        rosettaConfig.getNetworks().forEach(networkConfig -> {
            clients.put(networkConfig.getSanitizedNetworkId(), YaciNodeBridgeClient.builder().networkId(networkConfig.getSanitizedNetworkId()).build());
        });
    }
}
