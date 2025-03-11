package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stake_registration")
@IdClass(StakeRegistrationId.class)
public class StakeRegistrationEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "cert_index")
  private long certIndex;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private CertificateType type;

  @Column(name = "address")
  private String address;

}
