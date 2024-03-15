package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.Builder;
import lombok.Data;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;

import org.cardanofoundation.rosetta.api.block.model.entity.StakeRegistrationEntity;

@Data
@Builder
public class StakeRegistration {

  private String txHash;

  private long certIndex;

  private String credential;

  private CertificateType type;

  private String address;

  private Integer epoch;

  private Long slot;

  private String blockHash;

  public static StakeRegistration fromEntity(StakeRegistrationEntity entity) {
    return StakeRegistration.builder()
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
