package org.cardanofoundation.rosetta.api.block.model.entity;

import java.util.List;
import javax.annotation.Nullable;
import jakarta.persistence.*;

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
  @OrderBy("txIndex")
  private List<TxnEntity> transactions;

  @Column(name = "body_size")
  private Long blockBodySize;

  // NULL for Byron blocks: yaci-store sets it only for Shelley and later
  @Nullable
  @Column(name = "no_of_txs")
  private Long noOfTxs;

  @Column(name = "slot_leader")
  private String slotLeader;

  @Column(name = "epoch")
  private Integer epochNumber;

}
