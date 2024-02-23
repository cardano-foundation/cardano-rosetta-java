package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.cardanofoundation.rosetta.api.model.rest.TransactionMetadata;
import org.openapitools.client.model.RelatedTransaction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @JsonProperty("transaction_identifier")
    private TransactionIdentifier transactionIdentifier;

    @JsonProperty("operations")
    private List<Operation> operations = new ArrayList<>();

    @JsonProperty("related_transactions")
    @Valid
    private List<RelatedTransaction> relatedTransactions = null;

    @JsonProperty("amount")
    @Valid
    private Amount amount;

    @JsonProperty("metadata")
    private TransactionMetadata metadata;
}
