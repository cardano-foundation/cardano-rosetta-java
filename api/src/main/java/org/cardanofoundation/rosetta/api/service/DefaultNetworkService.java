package org.cardanofoundation.rosetta.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.model.rest.BalanceExemption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.model.rest.BalanceExemption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DefaultNetworkService {

    @Autowired
    private RosettaConfig rosettaConfig;

    @Autowired
    private NodeBridgeService nodeBridgeService;

    @Autowired
    private LedgerDataProviderService ledgerDataProviderService;

    private List<BalanceExemption> balanceExemptions;

    @PostConstruct
    void loadExemptionsFile() throws IOException {
        if (rosettaConfig.getExemptionsFile() != null) {
            final ObjectMapper objectMapper = new ObjectMapper();
            balanceExemptions = objectMapper.readValue(
                    new File(rosettaConfig.getExemptionsFile()),
                    new TypeReference<List<BalanceExemption>>() {
                    });
        } else {
            balanceExemptions = List.of();
        }
    }

}
