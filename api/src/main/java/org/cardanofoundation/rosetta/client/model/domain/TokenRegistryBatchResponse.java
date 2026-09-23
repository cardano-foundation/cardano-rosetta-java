package org.cardanofoundation.rosetta.client.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.lang.Nullable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRegistryBatchResponse {

    @JsonProperty("subjects")
    @Nullable
    private List<TokenSubject> subjects;

    @JsonProperty("queryPriority")
    @Nullable
    private List<String> queryPriority;

}
