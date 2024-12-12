package org.cardanofoundation.rosetta.api.block.model.entity;

import java.math.BigInteger;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class TxnEntity {

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "block_hash",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  private BlockEntity block;

  @Type(JsonType.class)
  @Column(name = "inputs", columnDefinition = "TEXT")
  private List<UtxoKey> inputKeys;

  @Type(JsonType.class)
  @Column(name = "outputs", columnDefinition = "TEXT")
  private List<UtxoKey> outputKeys;

  @Column(name = "fee")
  private BigInteger fee;

  @OneToMany(mappedBy = "txHash")
  private List<TxScriptEntity> script;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tx_hash",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  private TransactionSizeEntity sizeEntity;

}
