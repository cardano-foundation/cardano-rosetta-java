package org.cardanofoundation.rosetta.yaciindexer.service;

public interface TransactionSizeCalculator {

    int calculateSize(co.nstant.in.cbor.model.Map signedTransaction);

}
