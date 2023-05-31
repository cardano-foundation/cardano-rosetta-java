package org.cardanofoundation.rosetta.api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionParsed {
    private List<Operation> operations;
    private List<AccountIdentifier> account_identifier_signers;
}
