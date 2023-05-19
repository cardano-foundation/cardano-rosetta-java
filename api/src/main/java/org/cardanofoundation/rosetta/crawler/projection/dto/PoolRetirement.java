package org.cardanofoundation.rosetta.crawler.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoolRetirement {

  private long epoch;
  private String address;
}