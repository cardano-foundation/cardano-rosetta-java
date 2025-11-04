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
public class PeerSnapshotConfig {
  @JsonProperty("bigLedgerPools")
  private List<BigLedgerPool> bigLedgerPools;
}
