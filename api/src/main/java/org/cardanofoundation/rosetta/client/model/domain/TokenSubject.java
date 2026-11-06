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

    /** Null for e.g. a CIP-113 programmable token without CIP-26/CIP-68 metadata (registry 2.0.0+). */
    @JsonProperty("metadata")
    @Nullable
    private TokenMetadata metadata;

}
