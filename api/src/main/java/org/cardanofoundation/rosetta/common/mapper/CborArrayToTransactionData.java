package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.util.HexUtil;
import java.util.List;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;

public class CborArrayToTransactionData {

  public static TransactionData convert(Array decodedTransaction, boolean signed)
      throws CborException, CborDeserializationException {
    TransactionExtraData extraData = CborMapToTransactionExtraData.convertCborMapToTransactionExtraData(
        (Map) decodedTransaction.getDataItems().get(1));

    byte[] bytes = HexUtil.decodeHexString(
        ((UnicodeString) decodedTransaction.getDataItems().get(0)).getString());
    TransactionBody body;
    if (signed) {
      List<DataItem> decode = CborDecoder.decode(bytes);
      Array array = (Array) decode.get(0);
      if (decode.size() >= 2 && array.getDataItems().size() == 3) {
        array.add(decode.get(1));
      }
      Transaction parsed = Transaction.deserialize(
          com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array));
      body = parsed.getBody();
    } else {
      body = TransactionBody.deserialize(
          (Map) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(bytes));
    }
    return new TransactionData(body, extraData);
  }

}
