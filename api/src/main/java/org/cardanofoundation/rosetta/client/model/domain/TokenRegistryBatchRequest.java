package org.cardanofoundation.rosetta.client.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRegistryBatchRequest {

    @JsonProperty("subjects")
    @Valid
    private List<String> subjects;

    @JsonProperty("properties")
    @Valid
    private List<String> properties;

}