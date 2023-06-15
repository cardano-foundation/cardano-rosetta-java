package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @JsonProperty("transaction_identifier")
    private TransactionIdentifier transactionIdentifier;

    @JsonProperty("operations")
    private List<Operation> operations = new ArrayList<>();
}
