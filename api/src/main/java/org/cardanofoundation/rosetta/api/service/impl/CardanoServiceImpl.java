package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.util.CardanoAddressUtils;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class CardanoServiceImpl implements CardanoService {

    @Override
    public String getHashOfSignedTransaction(String signedTransaction) {
        try {
            log.info("[getHashOfSignedTransaction] About to hash signed transaction {}",
                    signedTransaction);
            byte[] signedTransactionBytes = HexUtil.decodeHexString(signedTransaction);
            log.info(
                    "[getHashOfSignedTransaction] About to parse transaction from signed transaction bytes");
            Transaction parsed = Transaction.deserialize(signedTransactionBytes);
            log.info("[getHashOfSignedTransaction] Returning transaction hash");
            TransactionBody body = parsed.getBody();
            byte[] hashBuffer;
            if (body == null ||
                    CborSerializationUtil.serialize(body.serialize())
                            == null) {
                hashBuffer = null;
            } else {
                hashBuffer = Blake2bUtil.blake2bHash256(
                        com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                                body.serialize()));
            }
            return CardanoAddressUtils.hexFormatter(hashBuffer);
        } catch (Exception error) {
            log.error(error.getMessage()
                    + "[getHashOfSignedTransaction] There was an error parsing signed transaction");
            throw ExceptionFactory.parseSignedTransactionError();
        }
    }

    @Override
    public Array decodeExtraData(String encoded) {
        try {
            DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
                    HexUtil.decodeHexString(encoded));
            return (Array) dataItem;
        } catch (Exception e) {
            throw ExceptionFactory.cantBuildSignedTransaction();
        }
    }
}
