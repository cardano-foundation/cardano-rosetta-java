package org.cardanofoundation.rosetta.api.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MaBalance {

  private String name;
  private String policy;
  private String value;
}
