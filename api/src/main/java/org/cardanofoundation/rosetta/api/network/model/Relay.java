package org.cardanofoundation.rosetta.api.network.model;

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
public class Relay {
  @JsonProperty("domain")
  private String domain;

  @JsonProperty("port")
  private Integer port;

  @JsonProperty("address")
  private String address;
}
