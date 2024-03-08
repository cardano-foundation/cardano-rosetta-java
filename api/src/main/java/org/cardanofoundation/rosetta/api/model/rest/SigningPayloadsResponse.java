package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.cardanofoundation.rosetta.api.model.cardano.Signature;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SigningPayloadsResponse {
    @JsonProperty("signatures")
    List<Signature> signatures;
}
