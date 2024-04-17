package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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
