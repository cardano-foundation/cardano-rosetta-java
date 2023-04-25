package org.cardanofoundation.rosetta.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionParsed {
    private List<Operation> operations;
    private List<AccountIdentifier> account_identifier_signers;
}
