package org.cardanofoundation.rosetta.api.model.dto;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.model.entity.StakeRegistrationEntity;

@Data
@Builder
public class StakeRegistrationDTO {

    private String txHash;

    private long certIndex;

    private String credential;

    private CertificateType type;

    private String address;

    private Integer epoch;

    private Long slot;

    private String blockHash;

    public static StakeRegistrationDTO fromEntity(StakeRegistrationEntity entity) {
        return StakeRegistrationDTO.builder()
                .txHash(entity.getTxHash())
                .certIndex(entity.getCertIndex())
                .credential(entity.getCredential())
                .type(entity.getType())
                .address(entity.getAddress())
                .epoch(entity.getEpoch())
                .slot(entity.getSlot())
                .blockHash(entity.getBlockHash())
                .build();
    }
}
