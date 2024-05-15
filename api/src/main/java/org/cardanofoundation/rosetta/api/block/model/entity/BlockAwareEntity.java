package org.cardanofoundation.rosetta.api.block.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.UpdateTimestamp;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@MappedSuperclass
public class BlockAwareEntity {

  @Column(name = "block")
  private Long blockNumber;

  @Column(name = "block_time")
  private Long blockTime;

  @UpdateTimestamp
  @Column(name = "update_datetime")
  private LocalDateTime updateDateTime;

}
