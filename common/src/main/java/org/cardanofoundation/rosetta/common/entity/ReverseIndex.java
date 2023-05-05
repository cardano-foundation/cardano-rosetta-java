package org.cardanofoundation.rosetta.common.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reverse_index")
public class ReverseIndex extends BaseEntity{



  @NotNull
  @Column(name = "block_id", nullable = false)
  private Long blockId;

  @Lob
  @Column(name = "min_ids")
  private String minIds;


}