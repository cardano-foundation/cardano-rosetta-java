package org.cardanofoundation.rosetta.api.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.cardano.PoolMargin;
import org.cardanofoundation.rosetta.api.model.cardano.PoolMetadata;
import org.cardanofoundation.rosetta.api.model.cardano.Relay;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PoolRegistrationParams {

    private String vrfKeyHash;
    private String rewardAddress;
    private String pledge;
    private String cost;
    private List<String> poolOwners;
    private List<Relay> relays;
    private PoolMargin margin;
    private String marginPercentage;
    private PoolMetadata poolMetadata;
}