package org.cardanofoundation.rosetta.common.services;


import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;

public interface CardanoService {
    String getHashOfSignedTransaction(String signedTransaction);
    Array decodeExtraData(String encoded);
    Long calculateTtl(Long ttlOffset);
    Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborSerializationException, CborException;
    Long calculateTxMinimumFee(Long transactionSize, ProtocolParams protocolParameters);
}
