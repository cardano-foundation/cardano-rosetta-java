package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "block", uniqueConstraints = {
    @UniqueConstraint(name = "unique_block", columnNames = {"hash"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Block extends BaseEntity {


  @Column(name = "hash", nullable = false, length = 64)
  private String hash;

  @Column(name = "epoch_no")
  private Integer epochNo;

  @Column(name = "slot_no")
  private Long slotNo;

  @Column(name = "epoch_slot_no")
  private Integer epochSlotNo;

  @Column(name = "block_no")
  private Long blockNo;

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "previous_id")
  @EqualsAndHashCode.Exclude
  private Block previous;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "slot_leader_id")
  @EqualsAndHashCode.Exclude
  private SlotLeader slotLeader;

  @Column(name = "slot_leader_id", updatable = false, insertable = false)
  private Long slotLeaderId;

  @Column(name = "size")
  private Integer size;

  @Column(name = "time")
  private Timestamp time;

  @Column(name = "tx_count")
  private Long txCount;

  @Column(name = "proto_major")
  private Integer protoMajor;

  @Column(name = "proto_minor")
  private Integer protoMinor;

  @Column(name = "vrf_key", length = 65535)
  private String vrfKey;

  @Column(name = "op_cert", length = 64)
  private String opCert;

  @Column(name = "op_cert_counter")
  private Long opCertCounter;


  @OneToMany(mappedBy = "block")
  private List<Tx> txList;

//  @OneToOne
//  @JoinColumn(name = "epoch_no", referencedColumnName = "no", nullable=false, insertable=false, updatable=false)
//  private Epoch epoch;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Block block = (Block) o;
    return id != null && Objects.equals(id, block.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
