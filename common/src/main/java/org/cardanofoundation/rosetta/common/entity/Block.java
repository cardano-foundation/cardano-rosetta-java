package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "block")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Block implements Serializable {

  @Id
  @Column(name = "hash", nullable = false)
  private String hash;

  @Column(name = "epoch")
  private Integer epoch;

  @Column(name = "slot")
  private Long slot;

  @Column(name = "epoch_slot")
  private Integer epochSlot;

  @Column(name = "number")
  private Long number;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_hash",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  @EqualsAndHashCode.Exclude
  private Block previous;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "slot_leader",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  @EqualsAndHashCode.Exclude
  private SlotLeader slotLeader;

  @Column(name = "body_size")
  private Integer bodySize;

  @Column(name = "body_hash")
  private String bodyHash;

  @Column(name = "create_datetime")
  private Timestamp createDatetime;

  @Column(name = "update_datetime")
  private Timestamp updateDatetime;

  @Column(name = "no_of_txs")
  private Long noOfTxs;

  @Column(name = "protocol_version")
  private String protocolVersion;

  @Column(name = "vrf_vkey", length = 65535)
  private String vrfVKey;

  @Column(name = "op_cert_hot_vkey")
  private String opCertHotVkey;

  @Column(name = "op_cert_seq_number")
  private Long opCertSeqNumber;

  @OneToMany(mappedBy = "block")
  private List<Tx> txList;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Block block = (Block) o;
    return hash != null && Objects.equals(hash, block.hash);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
