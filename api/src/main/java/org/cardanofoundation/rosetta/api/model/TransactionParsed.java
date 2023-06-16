package org.cardanofoundation.rosetta.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionParsed {
    private List<Operation> operations;
    private List<AccountIdentifier> account_identifier_signers;
}
