package org.cardanofoundation.rosetta.api.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceAtBlock {

  BlockDto block;
  String balance;
}
