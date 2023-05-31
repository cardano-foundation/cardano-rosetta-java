package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.annotation.Generated;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;

/**
 * ConstructionMetadataResponseMetadata
 */

@JsonTypeName("ConstructionMetadataResponse_metadata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
public class ConstructionMetadataResponseMetadata {

  @JsonProperty("ttl")
  private String ttl;

  @JsonProperty("protocol_parameters")
  private ProtocolParameters protocolParameters;


  public ConstructionMetadataResponseMetadata ttl(String ttl) {
    this.ttl = ttl;
    return this;
  }
}

