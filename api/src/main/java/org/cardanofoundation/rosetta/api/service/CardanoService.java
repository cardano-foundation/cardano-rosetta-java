package org.cardanofoundation.rosetta.api.service;


import co.nstant.in.cbor.model.Array;

public interface CardanoService {
    String getHashOfSignedTransaction(String signedTransaction);
    Array decodeExtraData(String encoded);
}
