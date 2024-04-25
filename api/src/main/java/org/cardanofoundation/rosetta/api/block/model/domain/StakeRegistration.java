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

  private CertificateType type;

  private String address;

}
