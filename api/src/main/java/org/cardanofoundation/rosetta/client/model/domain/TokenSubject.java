package org.cardanofoundation.rosetta.client.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.annotation.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenSubject {

    @JsonProperty("subject")
    private String subject;

    /** Required by the registry's V2 contract (empty for a token without metadata); nullable as it is external input. */
    @JsonProperty("metadata")
    @Nullable
    private TokenMetadata metadata;

}
