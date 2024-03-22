package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import java.util.List;
import org.openapitools.client.model.Operation;

public record TransactionExtraData (List<Operation> operations, String transactionMetadataHex){}
