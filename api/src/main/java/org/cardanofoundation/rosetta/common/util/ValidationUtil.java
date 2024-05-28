package org.cardanofoundation.rosetta.common.util;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.common.enumeration.CatalystSigIndexes;

/**
 * Utility class for validation methods. Will be used for common validation methods.
 */
public class ValidationUtil {

  private ValidationUtil() {
  }

  // [FutureUse] This method will be used to validate Transaction votes.
  public static boolean validateVoteDataFields(Map<String, Object> object) {
    List<CatalystDataIndexes> hexStringIndexes = Arrays.asList(
        CatalystDataIndexes.REWARD_ADDRESS,
        CatalystDataIndexes.STAKE_KEY,
        CatalystDataIndexes.VOTING_KEY
    );
    boolean isValidVotingNonce =
        object.containsKey(CatalystDataIndexes.VOTING_NONCE.getValue().toString())
            && object.get(CatalystDataIndexes.VOTING_NONCE.getValue().toString()) instanceof Number;

    return isValidVotingNonce
        && hexStringIndexes.stream().allMatch(index ->
        object.containsKey(index.getValue().toString()) && isHexString(
            object.get(index.getValue().toString()).toString()));
  }

  // [FutureUse] Votes-related code.
  public static boolean isVoteSignatureValid(Map<String, Object> mapJsonString) {

    List<Integer> dataIndexes = Arrays.stream(CatalystSigIndexes.values())
        .map(CatalystSigIndexes::getValue)
        .filter(value -> value > 0)
        .toList();
    return dataIndexes.stream().allMatch(index ->
        mapJsonString.containsKey(String.valueOf(index))
            && isHexString(mapJsonString.get(String.valueOf(index))));
  }

  // [FutureUse] Votes-related code.
  public static boolean isVoteDataValid(Map<String, Object> jsonObject) {
    boolean isObject = Objects.nonNull(jsonObject);

    return isObject && validateVoteDataFields(jsonObject);

  }

  // [FutureUse]
  public static boolean isHexString(Object value) {
    if (value instanceof String str) {
      return str.matches("^(0x)?[0-9a-fA-F]+$");
    }
    return false;
  }

  // [FutureUse]
  public static boolean areEqualUtxos(Utxo firstUtxo, Utxo secondUtxo) {
    return Objects.equals(firstUtxo.getOutputIndex(), secondUtxo.getOutputIndex())
        && Objects.equals(firstUtxo.getTxHash(), secondUtxo.getTxHash());
  }

}
