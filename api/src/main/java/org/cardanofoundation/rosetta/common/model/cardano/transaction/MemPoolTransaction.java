package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import org.openapitools.client.model.TransactionIdentifier;

public record MemPoolTransaction(TransactionIdentifier identifier, byte[] txBytes) {}
