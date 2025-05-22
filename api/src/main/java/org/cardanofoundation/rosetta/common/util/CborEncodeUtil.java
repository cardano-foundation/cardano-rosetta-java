package org.cardanofoundation.rosetta.common.util;

import java.util.List;

import lombok.experimental.UtilityClass;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.util.HexUtil;
import org.openapitools.client.model.Operation;

import org.cardanofoundation.rosetta.common.mapper.OperationToCborMap;

@UtilityClass
public class CborEncodeUtil {

  /**
   * Serialized extra Operations of type coin_spent, staking, pool, vote
   *
   * @param transaction serialized transaction
   * @param operations full list of operations
   * @param transactionMetadataHex transaction metadata in hex
   * @return serialized extra Operations of type coin_spent, staking, pool, vote
   * @throws CborException if serialization fails
   */
  public String encodeExtraData(String transaction,
                                List<Operation> operations,
                                String transactionMetadataHex) throws CborException {
    co.nstant.in.cbor.model.Map transactionExtraDataMap = new co.nstant.in.cbor.model.Map();
    Array operationArray = new Array();

    operations.forEach(operation -> operationArray.add(OperationToCborMap.convertToCborMap(operation)));

    transactionExtraDataMap.put(new UnicodeString(Constants.OPERATIONS), operationArray);
    if (transactionMetadataHex != null) {
      transactionExtraDataMap.put(new UnicodeString(Constants.TRANSACTIONMETADATAHEX),
          new UnicodeString(transactionMetadataHex));
    }
    Array outputArray = new Array();
    outputArray.add(new UnicodeString(transaction));
    outputArray.add(transactionExtraDataMap);

    return HexUtil.encodeHexString(
        com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(outputArray,
            false));
  }

}
