package org.cardanofoundation.rosetta.crawler.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MaBalance {

  private String name;
  private String policy;
  private String value;
}
