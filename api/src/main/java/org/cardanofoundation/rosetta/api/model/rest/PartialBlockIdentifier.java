package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * When fetching data by BlockIdentifier, it may be possible to only specify the index or hash. If neither property is specified, it is assumed that the client is making a request at the current block.
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class PartialBlockIdentifier {

  @JsonProperty("index")
  private Long index;

  @JsonProperty("hash")
  private String hash;

}

