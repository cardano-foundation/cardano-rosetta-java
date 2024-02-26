package org.cardanofoundation.rosetta.api.model.dto;

import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.model.Relay;
import org.cardanofoundation.rosetta.common.model.PoolRegistrationEnity;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class PoolRegistrationDTO {
    private String txHash;
    private long certIndex;
    private String poolId;
    private String vrfKeyHash;
    private String pledge;
    private String margin;
    private String cost;
    private String rewardAccount;
    private Set<String> owners;
    private List<Relay> relays;
    private Integer epoch;
    private Long slot;
    private String blockHash;

    public static PoolRegistrationDTO fromEntity(PoolRegistrationEnity entity) {
        return PoolRegistrationDTO.builder()
                .txHash(entity.getTxHash())
                .certIndex(entity.getCertIndex())
                .poolId(entity.getPoolId())
                .vrfKeyHash(entity.getVrfKeyHash())
                .pledge(entity.getPledge().toString())
                .margin(entity.getMargin().toString())
                .cost(entity.getCost().toString())
                .rewardAccount(entity.getRewardAccount())
                .relays(entity.getRelays().stream().map(relay -> new Relay("", relay.getIpv4(), relay.getIpv6(), relay.getDnsName(), relay.getPort().toString())).toList()) // TODO check type
                .owners(entity.getPoolOwners())
                .epoch(entity.getEpoch())
                .slot(entity.getSlot())
                .blockHash(entity.getBlockHash())
                .build();
    }
}
