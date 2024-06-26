package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StakeRegistration {

  private String txHash;

  private long certIndex;

  private CertificateType type;

  private String address;

}
