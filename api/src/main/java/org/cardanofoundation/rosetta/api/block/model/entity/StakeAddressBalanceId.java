package org.cardanofoundation.rosetta.api.block.model.entity;

import java.io.Serializable;
import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class StakeAddressBalanceId implements Serializable {

  @Column(name = "address")
  private String address;
  @Column(name = "slot")
  private Long slot;
}
