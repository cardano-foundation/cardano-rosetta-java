package org.cardanofoundation.rosetta.api.block.model.entity;

import com.bloxbean.cardano.yaci.core.model.certs.StakeCredType;
import com.bloxbean.cardano.yaci.core.model.governance.DrepType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "delegation_vote")
@IdClass(DrepVoteDelegationId.class)
public class DrepVoteDelegationEntity {

  @NotNull
  @jakarta.persistence.Id
  @Column(name = "tx_hash", nullable = false)
  private String txHash;

  @NotNull
  @jakarta.persistence.Id
  @Column(name = "cert_index", nullable = false)
  private long certIndex;

  @NotNull
  @Column(name = "tx_index", nullable = false)
  private int txIndex;

  @Nullable
  @Column(name = "slot", nullable = true)
  private Long slot;

  @Nullable
  @Column(name = "block", nullable = true)
  private Long blockNumber;

  @Nullable
  @Column(name = "block_time", nullable = true)
  private Long blockTime;

  @Nullable
  @Column(name = "update_datetime", nullable = true)
  private LocalDateTime updateDateTime;

  @Nullable
  @Column(name = "address", nullable = true)
  private String address;

  @Nullable
  @Column(name = "drep_hash", nullable = true)
  private String drepHash;

  @Nullable
  @Column(name = "drep_id", nullable = true)
  private String drepId;

  @Nullable
  @Column(name = "drep_type", nullable = true)
  @Enumerated(EnumType.STRING)
  private DrepType drepType;

  @Nullable
  @Column(name = "credential", nullable = true)
  private String credential;

  @Nullable
  @Column(name = "cred_type", nullable = true)
  @Enumerated(EnumType.STRING)
  private StakeCredType credType;

  @Nullable
  @Column(name = "epoch", nullable = true)
  private Integer epoch;

}
