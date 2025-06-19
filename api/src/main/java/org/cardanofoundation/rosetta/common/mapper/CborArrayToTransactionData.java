package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.util.HexUtil;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;

public class CborArrayToTransactionData {

  public static TransactionData convert(Array decodedTransaction, boolean signed)
      throws CborException, CborDeserializationException {
    TransactionExtraData extraData = CborMapToTransactionExtraData.convertCborMapToTransactionExtraData(
        (Map) decodedTransaction.getDataItems().get(1));

    byte[] transactionBytes = HexUtil.decodeHexString(
        ((UnicodeString) decodedTransaction.getDataItems().get(0)).getString());

    if (signed) {
      return processSignedTransaction(transactionBytes, extraData);
    }

    return processUnsignedTransaction(transactionBytes, extraData);
  }

  private static TransactionData processSignedTransaction(byte[] bytes, TransactionExtraData extraData)
          throws CborException, CborDeserializationException {
    List<DataItem> decode = CborDecoder.decode(bytes);
    if (decode.get(0) instanceof Array array) {
      if (decode.size() >= 2 && array.getDataItems().size() == 3) {
        array.add(decode.get(1));
      }
      Transaction parsed = Transaction.deserialize(
              com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array));
      return new TransactionData(parsed.getBody(), extraData);
    } else {
      throw ExceptionFactory.cantCreateSignedTransactionFromBytes();
    }
  }

  private static TransactionData processUnsignedTransaction(byte[] bytes, TransactionExtraData extraData)
          throws CborDeserializationException {
    DataItem deserializedTrBody = CborSerializationUtil.deserialize(bytes);
    if (deserializedTrBody instanceof Map deserializedMap) {
      TransactionBody body = TransactionBody.deserialize(deserializedMap);
      return new TransactionData(body, extraData);
    } else {
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
  }

  private CborArrayToTransactionData() {
  }
}
