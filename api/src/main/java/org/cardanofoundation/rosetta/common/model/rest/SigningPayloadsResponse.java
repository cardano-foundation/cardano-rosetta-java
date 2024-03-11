package org.cardanofoundation.rosetta.common.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.cardanofoundation.rosetta.common.model.cardano.Signature;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SigningPayloadsResponse {
    @JsonProperty("signatures")
    List<Signature> signatures;
}
