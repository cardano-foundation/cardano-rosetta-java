package org.cardanofoundation.rosetta.yaciindexer.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;

@Service
@Slf4j
public class TransactionSizeCalculatorImpl implements TransactionSizeCalculator {

    public int calculateSize(Map signedTransaction) {
        byte[] serialize = CborSerializationUtil.serialize(signedTransaction);

        return serialize.length;
    }

}
