package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Operation;

public record TransactionParsed (List<Operation> operations, List<AccountIdentifier> account_identifier_signers) {}
