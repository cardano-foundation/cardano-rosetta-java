package org.cardanofoundation.rosetta.crawler.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindMaBalance {

  private String name;
  private String policy;
  private String value;
}
