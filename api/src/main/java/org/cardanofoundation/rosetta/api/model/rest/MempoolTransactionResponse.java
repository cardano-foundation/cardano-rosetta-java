package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.Transaction;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MempoolTransactionResponse {
    @JsonProperty("transaction")
    Transaction transaction;

}
