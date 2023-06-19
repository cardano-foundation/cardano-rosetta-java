package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConstructionCombineResponse {
    @JsonProperty("signed_transaction")
    private String signedTransaction;
}
