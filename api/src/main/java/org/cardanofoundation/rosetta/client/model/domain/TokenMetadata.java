package org.cardanofoundation.rosetta.client.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenMetadata {

    @JsonProperty("name")
    private TokenProperty name;

    @JsonProperty("description")
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
