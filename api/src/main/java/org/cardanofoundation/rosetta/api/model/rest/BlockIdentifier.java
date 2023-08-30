package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockIdentifier {
  @JsonProperty("index")
  private Long index;
  @JsonProperty("hash")
  private String hash;
}
