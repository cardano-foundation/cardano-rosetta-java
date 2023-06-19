package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:13
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionIdentifierResponse {
    @JsonProperty("transaction_identifier")
    private TransactionIdentifier transactionIdentifier;

    @JsonProperty("metadata")
    private Object metadata;

    public TransactionIdentifierResponse(TransactionIdentifier transactionIdentifier) {
        this.transactionIdentifier = transactionIdentifier;
    }
}
