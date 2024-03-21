package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;
import jakarta.persistence.Column;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class StakeRegistrationId implements Serializable {

  @Column(name = "tx_hash")
  private String txHash;
  @Column(name = "cert_index")
  private long certIndex;
}
