package org.cardanofoundation.rosetta.api.account.model.entity;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address_utxo")
@IdClass(UtxoId.class)
public class AddressUtxoEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "output_index")
  private Integer outputIndex;

  @Column(name = "owner_addr")
  private String ownerAddr;

  @Column(name = "owner_stake_addr")
  private String ownerStakeAddr;

  @Type(JsonType.class)
  @Column(columnDefinition = "TEXT") // Use TEXT for H2
  private List<Amt> amounts;

  @Column(name = "block")
  private Long blockNumber;
}
