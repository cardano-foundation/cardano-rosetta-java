package org.cardanofoundation.rosetta.api.block.model.entity;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "block")
public class BlockEntity {

  @Id
  @Column(name = "hash")
  private String hash;

  @Column(name = "number")
  private Long number;

  @Column(name = "slot")
  private Long slot;

  @Column(name = "block_time")
  private Long blockTimeInSeconds;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_hash",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  private BlockEntity prev;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "block")
  private List<TxnEntity> transactions;

  @Column(name = "body_size")
  private Long blockBodySize;

  @Column(name = "no_of_txs")
  private Long noOfTxs;

  @Column(name = "slot_leader")
  private String slotLeader;

  @Column(name = "epoch")
  private Integer epochNumber;
}
