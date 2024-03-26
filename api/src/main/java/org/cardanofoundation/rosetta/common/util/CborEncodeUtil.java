package org.cardanofoundation.rosetta.common.util;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.util.HexUtil;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.OperationToCborMap;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolMargin;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.TokenBundleItem;
import org.openapitools.client.model.VoteRegistrationMetadata;

public class CborEncodeUtil {

  private CborEncodeUtil() {

  }

  /**
   * Serialized extra Operations of type coin_spent, staking, pool, vote
   * @param transaction serialized transaction
   * @param operations full list of operations
   * @param transactionMetadataHex transaction metadata in hex
   * @return serialized extra Operations of type coin_spent, staking, pool, vote
   * @throws CborException
   */
  public static String encodeExtraData(String transaction, List<Operation> operations, String transactionMetadataHex)
      throws CborException {

    List<Operation> filteredExtraOperations = getTxExtraData(operations);

    co.nstant.in.cbor.model.Map transactionExtraDataMap = new co.nstant.in.cbor.model.Map();
    Array operationArray = new Array();
    filteredExtraOperations.forEach(operation -> {
      operationArray.add(OperationToCborMap.convertToCborMap(operation));
    });
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

  /**
   * Get all Operations which are of the type coin_spent, staking, pool, vote
   * @param operations Operations to be filtered
   * @return List of Operations of type coin_spent, staking, pool, vote
   */
  private static List<Operation> getTxExtraData(List<Operation> operations){
    return operations.stream()
        .filter(operation -> {
              String coinAction = ObjectUtils.isEmpty(operation.getCoinChange()) ? null
                  : operation.getCoinChange().getCoinAction().toString();
              boolean coinActionStatement =
                  !ObjectUtils.isEmpty(coinAction) && coinAction.equals(Constants.COIN_SPENT_ACTION);
              return coinActionStatement ||
                  Constants.StakingOperations.contains(operation.getType()) ||
                  Constants.PoolOperations.contains(operation.getType()) ||
                  Constants.VoteOperations.contains(operation.getType());
            }
        ).toList();
  }

}
