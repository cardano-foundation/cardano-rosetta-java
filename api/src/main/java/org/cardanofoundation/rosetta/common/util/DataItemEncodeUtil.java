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
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
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

public class DataItemEncodeUtil {

  private DataItemEncodeUtil() {

  }
  public static String encodeExtraData(String transaction, TransactionExtraData extraData)
      throws CborException {
    TransactionExtraData toEncode=getTxExtraData(extraData);
    co.nstant.in.cbor.model.Map transactionExtraDataMap = new co.nstant.in.cbor.model.Map();
    Array operationArray = new Array();
    toEncode.getOperations().forEach(operation -> {
      co.nstant.in.cbor.model.Map operationIdentifierMap = getOperationIdentifierMap(operation.getOperationIdentifier());

      Array rOperationArray = new Array();
      if (!ObjectUtils.isEmpty(operation.getRelatedOperations())) {
        operation.getRelatedOperations().forEach(rOperationIdentifier -> {
          co.nstant.in.cbor.model.Map operationIdentifierMapnew = getOperationIdentifierMap(rOperationIdentifier);
          rOperationArray.add(operationIdentifierMapnew);
        });
      }
      co.nstant.in.cbor.model.Map accountIdentifierMap = getAccMap(operation);
      co.nstant.in.cbor.model.Map amountMap = getAmountMap(operation.getAmount());
      co.nstant.in.cbor.model.Map coinChangeMap = getCoinchangeMap(operation);

      co.nstant.in.cbor.model.Map operationMap = new co.nstant.in.cbor.model.Map();
      addOperationIdentifier(operation,operationMap,operationIdentifierMap);
      if (!ObjectUtils.isEmpty(operation.getRelatedOperations())) {
        operationMap.put(new UnicodeString(Constants.RELATED_OPERATION), rOperationArray);
      }
      if (!ObjectUtils.isEmpty(operation.getType())) {
        operationMap.put(new UnicodeString(Constants.TYPE), new UnicodeString(operation.getType()));
      }
      if (!ObjectUtils.isEmpty(operation.getStatus())) {
        operationMap.put(new UnicodeString(Constants.STATUS),
            new UnicodeString(operation.getStatus()));
      }
      if (!ObjectUtils.isEmpty(operation.getAccount())) {
        operationMap.put(new UnicodeString(Constants.ACCOUNT), accountIdentifierMap);
      }
      if (!ObjectUtils.isEmpty(operation.getAmount())) {
        operationMap.put(new UnicodeString(Constants.AMOUNT), amountMap);
      }
      if (!ObjectUtils.isEmpty(operation.getCoinChange())) {
        operationMap.put(new UnicodeString(Constants.COIN_CHANGE), coinChangeMap);
      }
      addOperationMetadataMap(operation,operationMap);
      operationArray.add(operationMap);
    });
    transactionExtraDataMap.put(new UnicodeString(Constants.OPERATIONS), operationArray);
    if (toEncode.getTransactionMetadataHex() != null) {
      transactionExtraDataMap.put(new UnicodeString(Constants.TRANSACTIONMETADATAHEX),
          new UnicodeString(toEncode.getTransactionMetadataHex()));
    }
    Array outputArray = new Array();
    outputArray.add(new UnicodeString(transaction));
    outputArray.add(transactionExtraDataMap);
    return HexUtil.encodeHexString(
        com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(outputArray,
            false));
  }

  public static void addOperationIdentifier(Operation operation, Map operationMap,Map operationIdentifierMap){
    if (operation.getOperationIdentifier() != null) {
      operationMap.put(new UnicodeString(Constants.OPERATION_IDENTIFIER), operationIdentifierMap);
    }
  }
  public static void addOperationMetadataMap(Operation operation,Map operationMap){
    boolean operationMetadataCheck = ObjectUtils.isEmpty(operation.getMetadata());
    if (!operationMetadataCheck) {
      co.nstant.in.cbor.model.Map oMetadataMap = new co.nstant.in.cbor.model.Map();
      OperationMetadata operationMetadata =
          ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata();
      addStakingCretoOperationMetadataMap(operationMetadata,oMetadataMap,Constants.STAKING_CREDENTIAL);
      addWithdrawal(operationMetadata,oMetadataMap,Constants.WITHDRAWALAMOUNT);
      addDeposit(operationMetadata,oMetadataMap,Constants.DEPOSITAMOUNT);
      addRefund(operationMetadata,oMetadataMap,Constants.REFUNDAMOUNT);
      addPoolKeyHash(operationMetadata,oMetadataMap,Constants.POOL_KEY_HASH);
      addEpoch(operationMetadata,oMetadataMap,Constants.EPOCH);
      addTokenBundle(operation,oMetadataMap);
      addPoolRegistrationCert(operationMetadata,oMetadataMap,Constants.POOLREGISTRATIONCERT);
      addPoolRegistrationParams(operationMetadata,operation,oMetadataMap);
      addVoteRegistrationMetadata(operationMetadata,oMetadataMap);
      if (!ObjectUtils.isEmpty(operation.getMetadata())) {
        operationMap.put(new UnicodeString(Constants.METADATA), oMetadataMap);
      }
    }
  }
  public static void addVoteRegistrationMetadata(OperationMetadata operationMetadata,Map oMetadataMap){
    if (operationMetadata != null && operationMetadata.getVoteRegistrationMetadata() != null) {
      co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = new co.nstant.in.cbor.model.Map();
      VoteRegistrationMetadata voteRegistrationMetadata =
          operationMetadata.getVoteRegistrationMetadata();
      addStakeKey(voteRegistrationMetadata,voteRegistrationMetadataMap);
      try{
        co.nstant.in.cbor.model.Map votingKeyMap = getPublicKeymap(
            ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                : voteRegistrationMetadata.getVotingkey());
        voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_KEY), votingKeyMap);
      }catch (Exception e){
        throw ExceptionFactory.unspecifiedError(e.getMessage());
      }
      Integer unsignedIntegerNumber;
      unsignedIntegerNumber = ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
          : voteRegistrationMetadata.getVotingNonce();
      UnsignedInteger unsignedInteger = null;
      if (unsignedIntegerNumber != null) {
        unsignedInteger = new UnsignedInteger(
            unsignedIntegerNumber);
      }
      if (unsignedInteger != null) {
        voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_NONCE),
            unsignedInteger);
      }
      voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_SIGNATURE),
          new UnicodeString(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getVotingSignature()));
      oMetadataMap.put(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA),
          voteRegistrationMetadataMap);
    }
  }
  public static void addStakeKey(VoteRegistrationMetadata voteRegistrationMetadata,Map voteRegistrationMetadataMap){
    try{
      co.nstant.in.cbor.model.Map stakeKeyMap = getPublicKeymap(
          ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
              : voteRegistrationMetadata.getStakeKey());
      voteRegistrationMetadataMap.put(new UnicodeString(Constants.REWARD_ADDRESS),
          new UnicodeString(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getRewardAddress()));
      voteRegistrationMetadataMap.put(new UnicodeString(Constants.STAKE_KEY), stakeKeyMap);
    }catch (Exception e){
      throw ExceptionFactory.unspecifiedError(e.getMessage());
    }
  }
  public static void addPoolRegistrationParams(OperationMetadata operationMetadata,Operation operation,Map oMetadataMap){
    if (operationMetadata != null && operationMetadata.getPoolRegistrationParams() != null) {
      co.nstant.in.cbor.model.Map poolRegistrationParamsMap = new co.nstant.in.cbor.model.Map();
      PoolRegistrationParams poolRegistrationParams = operationMetadata.getPoolRegistrationParams();
      addVrfKeyHash(poolRegistrationParams,poolRegistrationParamsMap);
      addRewardAddress(poolRegistrationParams,poolRegistrationParamsMap);
      addPledge(poolRegistrationParams,poolRegistrationParamsMap);
      addCost(poolRegistrationParams,poolRegistrationParamsMap);
      addOwners(operation,operationMetadata,poolRegistrationParamsMap);
      addRelays(operation,operationMetadata,poolRegistrationParamsMap);
      addPoolMargin(poolRegistrationParams,poolRegistrationParamsMap);
      if (poolRegistrationParams.getMarginPercentage() != null) {
        poolRegistrationParamsMap.put(new UnicodeString(Constants.MARGIN_PERCENTAGE),
            new UnicodeString(poolRegistrationParams.getMarginPercentage()));
      }
      co.nstant.in.cbor.model.Map poolMetadataMap = new co.nstant.in.cbor.model.Map();
      PoolMetadata poolMetadata = poolRegistrationParams.getPoolMetadata();
      if (poolMetadata != null) {
        if (!ObjectUtils.isEmpty(poolMetadata.getUrl())) {
          poolMetadataMap.put(new UnicodeString(Constants.URL),
              new UnicodeString(poolMetadata.getUrl()));
        }
        if (!ObjectUtils.isEmpty(poolMetadata.getHash())) {
          poolMetadataMap.put(new UnicodeString(Constants.HASH),
              new UnicodeString(poolMetadata.getHash()));
        }
        poolRegistrationParamsMap.put(new UnicodeString(Constants.POOLMETADATA),
            poolMetadataMap);
      }
      oMetadataMap.put(new UnicodeString(Constants.POOLREGISTRATIONPARAMS),
          poolRegistrationParamsMap);
    }
  }

  public static void addPoolMargin(PoolRegistrationParams poolRegistrationParams,Map poolRegistrationParamsMap){
    PoolMargin poolMargin = poolRegistrationParams.getMargin();
    if (poolMargin != null) {
      co.nstant.in.cbor.model.Map marginMap = new co.nstant.in.cbor.model.Map();
      if (poolMargin.getNumerator() != null) {
        marginMap.put(new UnicodeString(Constants.NUMERATOR),
            new UnicodeString(poolMargin.getNumerator()));
      }
      if (poolMargin.getDenominator() != null) {
        marginMap.put(new UnicodeString(Constants.DENOMINATOR),
            new UnicodeString(poolMargin.getDenominator()));
      }
      poolRegistrationParamsMap.put(new UnicodeString(Constants.MARGIN), marginMap);
    }
  }
  public static void addRelays(Operation operation,OperationMetadata operationMetadata,Map poolRegistrationParamsMap){
    Array relaysArray = new Array();
    if (!ObjectUtils.isEmpty(operationMetadata.getPoolRegistrationParams())
        && operationMetadata.getPoolRegistrationParams().getRelays() != null) {
      operation.getMetadata().getPoolRegistrationParams().getRelays()
          .forEach(r -> {
            if (r != null) {
              co.nstant.in.cbor.model.Map relayMap = new co.nstant.in.cbor.model.Map();
              addStringDataItemToRelays(relayMap,Constants.TYPE,r.getType());
              addStringDataItemToRelays(relayMap,Constants.IPV4,r.getIpv4());
              addStringDataItemToRelays(relayMap,Constants.IPV6,r.getIpv6());
              addStringDataItemToRelays(relayMap,Constants.DNSNAME,r.getDnsName());
              addStringDataItemToRelays(relayMap,Constants.PORT,r.getPort().toString());
              relaysArray.add(relayMap);
            }
          });
      poolRegistrationParamsMap.put(new UnicodeString(Constants.RELAYS), relaysArray);
    }
  }
  public static void addStringDataItemToRelays(Map relayMap,String dataItemName,String dataItemValue){
    if (dataItemValue != null) {
      relayMap.put(new UnicodeString(dataItemName),
          new UnicodeString(dataItemValue));
    }
  }
  public static void addOwners(Operation operation,OperationMetadata operationMetadata,Map poolRegistrationParamsMap){
    Array poolOwnersArray = new Array();
    if (!ObjectUtils.isEmpty(operationMetadata.getPoolRegistrationParams())
        && operation.getMetadata().getPoolRegistrationParams().getPoolOwners() != null) {
      operation.getMetadata().getPoolRegistrationParams().getPoolOwners()
          .forEach(o -> {
            DataItem dataItem = new UnicodeString(o);
            poolOwnersArray.add(dataItem);
          });
    }
    poolRegistrationParamsMap.put(new UnicodeString(Constants.POOLOWNERS), poolOwnersArray);
  }
  public static void addCost(PoolRegistrationParams poolRegistrationParams,Map poolRegistrationParamsMap){
    if (poolRegistrationParams.getCost() != null) {
      poolRegistrationParamsMap.put(new UnicodeString(Constants.COST),
          new UnicodeString(poolRegistrationParams.getCost()));
    }
  }
  public static void addPledge(PoolRegistrationParams poolRegistrationParams,Map poolRegistrationParamsMap){
    if (poolRegistrationParams.getPledge() != null) {
      poolRegistrationParamsMap.put(new UnicodeString(Constants.PLEDGE),
          new UnicodeString(poolRegistrationParams.getPledge()));
    }
  }
  public static void addRewardAddress(PoolRegistrationParams poolRegistrationParams,Map poolRegistrationParamsMap){
    if (poolRegistrationParams.getRewardAddress() != null) {
      poolRegistrationParamsMap.put(new UnicodeString(Constants.REWARD_ADDRESS),
          new UnicodeString(poolRegistrationParams.getRewardAddress()));
    }
  }
  public static void addVrfKeyHash(PoolRegistrationParams poolRegistrationParams,Map poolRegistrationParamsMap){
    if (poolRegistrationParams.getVrfKeyHash() != null) {
      poolRegistrationParamsMap.put(new UnicodeString(Constants.VRFKEYHASH),
          new UnicodeString(poolRegistrationParams.getVrfKeyHash()));
    }
  }
  public static void addTokenBundle(Operation operation,Map oMetadataMap){
    if (operation.getMetadata() != null && operation.getMetadata().getTokenBundle() != null) {
      Array tokenBundleArray = new Array();
      operation.getMetadata().getTokenBundle().forEach(tokenbundle -> {
        if (tokenbundle != null) {
          co.nstant.in.cbor.model.Map tokenBundleItemMap = new co.nstant.in.cbor.model.Map();
          if (tokenbundle.getPolicyId() != null) {
            tokenBundleItemMap.put(new UnicodeString(Constants.POLICYID),
                new UnicodeString(tokenbundle.getPolicyId()));
          }
          addTokensTomap(tokenbundle,tokenBundleItemMap);
          tokenBundleArray.add(tokenBundleItemMap);
        }
      });
      oMetadataMap.put(new UnicodeString(Constants.TOKENBUNDLE), tokenBundleArray);
    }
  }

  public static void addTokensTomap(TokenBundleItem tokenbundle,Map tokenBundleItemMap){
    if (tokenbundle.getTokens() != null) {
      Array tokensArray = new Array();
      tokenbundle.getTokens().forEach(amount -> {
        if (amount != null) {
          co.nstant.in.cbor.model.Map amountMapNext = getAmountMap(amount);
          tokensArray.add(amountMapNext);
        }
      });
      tokenBundleItemMap.put(new UnicodeString(Constants.TOKENS), tokensArray);
    }
  }
  public static void addPoolRegistrationCert(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getPoolRegistrationCert() != null) {
      oMetadataMap.put(new UnicodeString(dataItemName),
          new UnicodeString(operationMetadata.getPoolRegistrationCert()));
    }
  }
  public static void addEpoch(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getEpoch() != null) {
      oMetadataMap.put(new UnicodeString(dataItemName),
          new UnsignedInteger(operationMetadata.getEpoch()));
    }
  }
  public static void addPoolKeyHash(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getPoolKeyHash() != null) {
      oMetadataMap.put(new UnicodeString(dataItemName),
          new UnicodeString(operationMetadata.getPoolKeyHash()));
    }
  }
  public static void addRefund(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getRefundAmount() != null) {
      co.nstant.in.cbor.model.Map refundAmount = getAmountMap(
          operationMetadata.getRefundAmount());
      oMetadataMap.put(new UnicodeString(dataItemName), refundAmount);
    }
  }
  public static void addWithdrawal(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getWithdrawalAmount() != null) {
      co.nstant.in.cbor.model.Map amount = getAmountMapV2(
          operationMetadata.getWithdrawalAmount());
      oMetadataMap.put(new UnicodeString(dataItemName), amount);
    }
  }
  public static void addDeposit(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getDepositAmount() != null) {
      co.nstant.in.cbor.model.Map amount = getAmountMapV2(
          operationMetadata.getDepositAmount());
      oMetadataMap.put(new UnicodeString(dataItemName), amount);
    }
  }
  public static void addStakingCretoOperationMetadataMap(OperationMetadata operationMetadata,Map oMetadataMap,String dataItemName){
    if (operationMetadata != null && operationMetadata.getStakingCredential() != null) {
      co.nstant.in.cbor.model.Map stakingCredentialMap = getPublicKeymap(
          operationMetadata.getStakingCredential());
      oMetadataMap.put(new UnicodeString(dataItemName), stakingCredentialMap);
    }
  }
  public static Map getCoinchangeMap(Operation operation){
    co.nstant.in.cbor.model.Map coinChangeMap = new co.nstant.in.cbor.model.Map();
    co.nstant.in.cbor.model.Map coinIdentifierMap = new co.nstant.in.cbor.model.Map();
    CoinChange coinChange =
        ObjectUtils.isEmpty(operation.getCoinChange()) ? null : operation.getCoinChange();
    CoinIdentifier coinIdentifier =
        coinChange==null ? null : coinChange.getCoinIdentifier();
    coinIdentifierMap.put(new UnicodeString(Constants.IDENTIFIER),
        new UnicodeString(
            coinIdentifier==null ? null : coinIdentifier.getIdentifier()));
    coinChangeMap.put(new UnicodeString(Constants.COIN_IDENTIFIER), coinIdentifierMap);
    coinChangeMap.put(new UnicodeString(Constants.COIN_ACTION),
        new UnicodeString(coinChange==null ? null : coinChange.getCoinAction().toString()));
    return coinChangeMap;
  }
  public static Map getAccMap(Operation operation){
    co.nstant.in.cbor.model.Map accountIdentifierMap = new co.nstant.in.cbor.model.Map();
    if (operation.getAccount() != null) {
      addAddressDataItem(operation.getAccount().getAddress(),accountIdentifierMap);
      if (operation.getAccount().getSubAccount() != null) {
        co.nstant.in.cbor.model.Map subAccountIdentifierMap = new co.nstant.in.cbor.model.Map();
        addAddressDataItem(operation.getAccount().getSubAccount().getAddress(),subAccountIdentifierMap);
        if (operation.getAccount().getSubAccount().getMetadata() != null) {
          subAccountIdentifierMap.put(new UnicodeString(Constants.METADATA),
              objectToDataItem(operation.getAccount().getSubAccount().getMetadata()));
        }
        accountIdentifierMap.put(new UnicodeString(Constants.SUB_ACCOUNT),
            subAccountIdentifierMap);
      }
      if (operation.getAccount().getMetadata() != null) {
        co.nstant.in.cbor.model.Map accIdMetadataMap = new co.nstant.in.cbor.model.Map();
        accIdMetadataMap.put(new UnicodeString(Constants.CHAIN_CODE),
            new UnicodeString(ObjectUtils.isEmpty(operation.getAccount().getMetadata()) ? null
                : operation.getAccount().getMetadata().getChainCode()));
        accountIdentifierMap.put(new UnicodeString(Constants.METADATA), accIdMetadataMap);
      }
    }
    return accountIdentifierMap;
  }

  // TODO get rid of this method
  private static DataItem objectToDataItem(Object metadata) {
    Map map = new Map();
    HashMap<String, Object> metadataMap = (HashMap<String, Object>) metadata;
    metadataMap.forEach((key, value) -> {
      if (value instanceof String) {
        map.put(new UnicodeString(key), new UnicodeString((String) value));
      } else {
        map.put(new UnicodeString(key), objectToDataItem(value));
      }
    });
    return map;
  }

  public static void addAddressDataItem(String address, Map map){
    if (address != null) {
      map.put(new UnicodeString(Constants.ADDRESS),
          new UnicodeString(address));
    }
  }
  public static Map getOperationIdentifierMap(OperationIdentifier operationIdentifier){
    co.nstant.in.cbor.model.Map operationIdentifierMap = new co.nstant.in.cbor.model.Map();
    Long index = operationIdentifier.getIndex();
    if (index != null) {
      operationIdentifierMap.put(new UnicodeString(Constants.INDEX),
          new UnsignedInteger(operationIdentifier.getIndex()));
    }
    Long networkIndex = operationIdentifier.getNetworkIndex();
    if (networkIndex != null) {
      operationIdentifierMap.put(new UnicodeString(Constants.NETWORK_INDEX),
          new UnsignedInteger(networkIndex));
    }
    return  operationIdentifierMap;
  }
  public static TransactionExtraData getTxExtraData(TransactionExtraData extraData){
    List<Operation> extraOperations = extraData.getOperations().stream()
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
    TransactionExtraData toEncode = new TransactionExtraData();
    toEncode.setOperations(extraOperations);
    if (!ObjectUtils.isEmpty(extraData.getTransactionMetadataHex())) {
      toEncode.setTransactionMetadataHex(extraData.getTransactionMetadataHex());
    }
    return toEncode;
  }

  public static co.nstant.in.cbor.model.Map getAmountMap(Amount amount) {
    co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
    if (!ObjectUtils.isEmpty(amount)) {
      if (amount.getValue() != null) {
        amountMap.put(new UnicodeString(Constants.VALUE), new UnicodeString(amount.getValue()));
      }
      getCurrencyMap(amount, amountMap);
      if (amount.getMetadata() != null) {
        amountMap.put(new UnicodeString(Constants.METADATA), objectToDataItem(amount.getMetadata()));
      }
    }
    return amountMap;
  }

  public static co.nstant.in.cbor.model.Map getAmountMapV2(Amount amount) {
    co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
    if (!ObjectUtils.isEmpty(amount)) {
      getCurrencyMap(amount, amountMap);
      if (amount.getValue() != null) {
        amountMap.put(new UnicodeString(Constants.VALUE), new UnicodeString(amount.getValue()));
      }
      if (amount.getMetadata() != null) {
        amountMap.put(new UnicodeString(Constants.METADATA), objectToDataItem(amount.getMetadata()));
      }
    }
    return amountMap;
  }

  public static void getCurrencyMap(Amount amount, co.nstant.in.cbor.model.Map amountMap) {
    if (amount.getCurrency() != null) {
      co.nstant.in.cbor.model.Map currencyMap = new co.nstant.in.cbor.model.Map();
      if (amount.getCurrency().getSymbol() != null) {
        currencyMap.put(new UnicodeString(Constants.SYMBOL),
            new UnicodeString(amount.getCurrency().getSymbol()));
      }
      if (amount.getCurrency().getDecimals() != null) {
        currencyMap.put(new UnicodeString(Constants.DECIMALS),
            new UnsignedInteger(amount.getCurrency().getDecimals()));
      }
      co.nstant.in.cbor.model.Map addedMetadataMap = new co.nstant.in.cbor.model.Map();
      addedMetadataMap.put(new UnicodeString(Constants.METADATA),
          new UnicodeString(ObjectUtils.isEmpty(amount.getCurrency().getMetadata()) ? null
              : amount.getCurrency().getMetadata().getPolicyId()));
      if (amount.getCurrency().getMetadata() != null) {
        currencyMap.put(new UnicodeString(Constants.METADATA), addedMetadataMap);
      }
      amountMap.put(new UnicodeString(Constants.CURRENCY), currencyMap);
    }
  }

  public static co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey) {
    co.nstant.in.cbor.model.Map stakingCredentialMap = new co.nstant.in.cbor.model.Map();
    stakingCredentialMap.put(new UnicodeString(Constants.HEX_BYTES),
        new UnicodeString(ObjectUtils.isEmpty(publicKey) ? null : publicKey.getHexBytes()));
    stakingCredentialMap.put(new UnicodeString(Constants.CURVE_TYPE),
        new UnicodeString(ObjectUtils.isEmpty(publicKey) ? null : publicKey.getCurveType().toString()));
    return stakingCredentialMap;
  }

}
