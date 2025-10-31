package org.cardanofoundation.rosetta.api.network.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BigLedgerPool {
  @JsonProperty("accumulatedStake")
  private Double accumulatedStake;

  @JsonProperty("relativeStake")
  private Double relativeStake;

  @JsonProperty("relays")
  private List<Relay> relays;
}
