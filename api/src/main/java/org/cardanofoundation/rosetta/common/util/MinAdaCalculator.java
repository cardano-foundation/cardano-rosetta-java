package org.cardanofoundation.rosetta.common.util;

import java.math.BigInteger;

import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

public class MinAdaCalculator {
    private MinAdaCalculator() {}

    public static BigInteger calculateMinAda(TransactionOutput output, BigInteger coinsPerUtxoSize) {
        try {
            byte[] outputBytes = CborSerializationUtil.serialize(output.serialize());
            return BigInteger.valueOf(outputBytes.length).multiply(coinsPerUtxoSize);
        } catch (Exception e) {
            throw ExceptionFactory.unspecifiedError("Failed to serialize output for min ADA check");
        }
    }
}
