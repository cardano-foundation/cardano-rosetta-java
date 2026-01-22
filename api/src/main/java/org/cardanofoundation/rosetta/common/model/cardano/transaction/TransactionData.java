package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import com.bloxbean.cardano.client.transaction.spec.TransactionBody;

public record TransactionData(TransactionBody transactionBody,
                              TransactionExtraData transactionExtraData) {

}
