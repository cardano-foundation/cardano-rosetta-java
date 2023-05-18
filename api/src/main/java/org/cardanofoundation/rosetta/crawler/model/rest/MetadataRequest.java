package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:33
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class MetadataRequest {
  @JsonProperty("metadata")
  @SerializedName("metadata")
  private Object metadata;
}
