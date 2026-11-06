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
/**
 * The V2 {@code metadata} block of a token registry subject.
 *
 * <p>All properties are optional: since cf-token-metadata-registry 2.0.0 tokens without a
 * {@code name} or {@code description} are served too, with {@code validation_errors}.</p>
 */
public class TokenMetadata {

    @JsonProperty("name")
    @Nullable
    private TokenProperty name;

    @JsonProperty("description")
    @Nullable
    private TokenProperty description;

    @JsonProperty("ticker")
    @Nullable
    private TokenProperty ticker;

    @JsonProperty("decimals")
    @Nullable
    private TokenPropertyNumber decimals;

    @JsonProperty("logo")
    @Nullable
    private TokenProperty logo;

    @JsonProperty("url")
    @Nullable
    private TokenProperty url;

    @JsonProperty("version")
    @Nullable
    private TokenPropertyNumber version;

}
