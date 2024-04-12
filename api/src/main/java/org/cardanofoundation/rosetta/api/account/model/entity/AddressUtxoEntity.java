package org.cardanofoundation.rosetta.api.account.model.entity;

import java.math.BigInteger;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockAwareEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "address_utxo")
@IdClass(UtxoId.class)
@DynamicUpdate
public class AddressUtxoEntity extends BlockAwareEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "output_index")
  private Integer outputIndex;

  @Column(name = "block_hash")
  private String blockHash;

  @Column(name = "owner_addr")
  private String ownerAddr;

  @Column(name = "lovelace_amount")
  private BigInteger lovelaceAmount;

  @Type(JsonType.class)
  private List<Amt> amounts;
}
