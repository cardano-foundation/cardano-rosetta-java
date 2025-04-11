package org.cardanofoundation.rosetta.yaciindexer.service;

import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.yaci.helper.model.Transaction;

public interface TransactionScriptSizeCalculator {

    int calculateScriptSize(Transaction tx, Map signedTransaction);

}
