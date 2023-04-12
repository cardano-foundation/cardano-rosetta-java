package org.cardanofoundation.rosetta.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.cardanofoundation.rosetta.api.config.NetworkConfig;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.util.RosettaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class DefaultNetworkService implements NetworkService {

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

    @Override
    public NetworkListResponse getNetworkList(MetadataRequest metadataRequest) {
        final NetworkListResponse networkListResponse = new NetworkListResponse();
        rosettaConfig.getNetworks().forEach((networkConfig -> {
            final NetworkIdentifier identifier = new NetworkIdentifier();
            identifier.setBlockchain(RosettaConstants.BLOCKCHAIN_NAME);
            identifier.setNetwork(networkConfig.getSanitizedNetworkId());
//            networkListResponse.addNetworkIdentifiersItem(identifier);
        }));
        return networkListResponse;
    }

    @Override
    public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest) {
        final NetworkConfig requestedNetworkConfig = rosettaConfig.networkConfigFromNetworkRequest(networkRequest).orElseThrow();
        final NetworkOptionsResponse networkOptionsResponse = new NetworkOptionsResponse();
//        final Version version = new Version();
//        version.setRosettaVersion(rosettaConfig.getVersion());
//        version.setMiddlewareVersion(rosettaConfig.getImplementationVersion());
//        version.setNodeVersion(requestedNetworkConfig.getNodeVersion());
//        networkOptionsResponse.setVersion(version);
//
//        final Allow allow = new Allow();
//        allow.setOperationStatuses(RosettaConstants.ROSETTA_OPERATION_STATUSES);
//        allow.setOperationTypes(RosettaConstants.ROSETTA_OPERATION_TYPES);
//        allow.setErrors(RosettaConstants.ROSETTA_ERRORS);
//        allow.setHistoricalBalanceLookup(true);
//        allow.setCallMethods(List.of());
//        allow.setMempoolCoins(false);
//        allow.setBalanceExemptions(balanceExemptions);
//        networkOptionsResponse.setAllow(allow);
        return networkOptionsResponse;
    }

    @Override
    public NetworkStatusResponse getNetworkStatus(NetworkRequest networkRequest) {
        final NetworkConfig requestedNetworkConfig = rosettaConfig.networkConfigFromNetworkRequest(networkRequest).orElseThrow();
        final NetworkStatusResponse networkStatusResponse = new NetworkStatusResponse();
        // TODO fetch data via indexer or node bridge service
        return networkStatusResponse;
    }
}
