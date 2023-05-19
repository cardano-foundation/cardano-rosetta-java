package org.cardanofoundation.rosetta.crawler.model.rest;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

  private String symbol;
  private Integer decimals;
  private Map<String, Object> metadata;

}
