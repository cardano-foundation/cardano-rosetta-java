package org.cardanofoundation.rosetta.crawler.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.crawler.projection.dto.BlockDto;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceAtBlock {

  BlockDto block;
  String balance;
}
