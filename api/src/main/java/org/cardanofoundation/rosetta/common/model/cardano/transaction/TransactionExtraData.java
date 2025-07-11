package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import org.openapitools.client.model.Operation;

import java.util.List;

public record TransactionExtraData (List<Operation> operations) {
}
