package org.cardanofoundation.rosetta.common.model.cardano.network;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicRoot {
  private List<AccessPoint> accessPoints;
}
