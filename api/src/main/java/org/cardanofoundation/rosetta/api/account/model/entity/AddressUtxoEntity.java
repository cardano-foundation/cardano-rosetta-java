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

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "address_utxo")
@IdClass(UtxoId.class)
@DynamicUpdate
public class AddressUtxoEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Id
  @Column(name = "output_index")
  private Integer outputIndex;

  @Column(name = "owner_addr")
  private String ownerAddr;

  @Type(JsonType.class)
  private List<Amt> amounts;
}
