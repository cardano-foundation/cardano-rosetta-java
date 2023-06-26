package org.cardanofoundation.rosetta.api.util;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import java.util.ArrayList;
import java.util.List;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.AccountIdentifierMetadata;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.CoinChange;
import org.cardanofoundation.rosetta.api.model.CoinIdentifier;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.Metadata;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationIdentifier;
import org.cardanofoundation.rosetta.api.model.OperationMetadata;
import org.cardanofoundation.rosetta.api.model.PoolMargin;
import org.cardanofoundation.rosetta.api.model.PoolMetadata;
import org.cardanofoundation.rosetta.api.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Relay;
import org.cardanofoundation.rosetta.api.model.SubAccountIdentifier;
import org.cardanofoundation.rosetta.api.model.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.VoteRegistrationMetadata;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;

public class DataItemDecodeUtil {

  private DataItemDecodeUtil() {

  }
  public static TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map) {
    //separator
    TransactionExtraData transactionExtraData = new TransactionExtraData();
    addTransactionMetadataHex(transactionExtraData,map);
    List<Operation> operations = new ArrayList<>();
    List<DataItem> operationsListMap = ((Array) map.get(
        new UnicodeString(Constants.OPERATIONS))).getDataItems();
    operationsListMap.forEach(oDataItem -> {
      co.nstant.in.cbor.model.Map operationMap = (co.nstant.in.cbor.model.Map) oDataItem;
      Operation operation = new Operation();
      if (operationMap.get(new UnicodeString(Constants.OPERATION_IDENTIFIER)) != null) {
        co.nstant.in.cbor.model.Map operationIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(
            new UnicodeString(Constants.OPERATION_IDENTIFIER));
        OperationIdentifier operationIdentifier = new OperationIdentifier();
        addOperationIdentifier(operationIdentifierMap,operationIdentifier);
        operation.setOperationIdentifier(operationIdentifier);
      }
      if (operationMap.get(new UnicodeString(Constants.RELATED_OPERATION)) != null) {
        List<OperationIdentifier> relatedOperations = new ArrayList<>();
        List<DataItem> relatedOperationsDI = ((Array) operationMap.get(
            new UnicodeString(Constants.RELATED_OPERATION))).getDataItems();
        relatedOperationsDI.forEach(rDI -> {
          co.nstant.in.cbor.model.Map operationIdentifierMap2 = (co.nstant.in.cbor.model.Map) rDI;

          OperationIdentifier operationIdentifier2 = new OperationIdentifier();
          addOperationIdentifier(operationIdentifierMap2,operationIdentifier2);
          relatedOperations.add(operationIdentifier2);
        });
        operation.setRelatedOperations(relatedOperations);
      }
      addTypeToOperation(operationMap,operation,Constants.TYPE);
      addStatusToOperation(operationMap,operation,Constants.STATUS);
      addAccountToOperation(operationMap,operation);
      addAmount(operationMap,operation);
      addCoinChange(operationMap,operation);
      addMetadata(operationMap,operation);
      operations.add(operation);
    });
    transactionExtraData.setOperations(operations);
    return transactionExtraData;
  }

  public static void addMetadata(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.METADATA)) != null) {
      co.nstant.in.cbor.model.Map metadataMap = (co.nstant.in.cbor.model.Map) operationMap.get(
          new UnicodeString(Constants.METADATA));
      OperationMetadata operationMetadata = new OperationMetadata();
      addWithDrawalAmount(metadataMap,operationMetadata);
      addDepositAmount(metadataMap,operationMetadata);
      addRefundAmount(metadataMap,operationMetadata);
      addStakingCredential(metadataMap,operationMetadata);
      addPoolKeyHash(metadataMap,operationMetadata);
      addEpoch(metadataMap,operationMetadata);
      addTokenBundle(metadataMap,operationMetadata);
      addPoolRegistrationCert(metadataMap,operationMetadata);
      addPoolRegistrationParams(metadataMap,operationMetadata);
      addVoteRegistrationMetadata(metadataMap,operationMetadata);
      operation.setMetadata(operationMetadata);
    }
  }
  public static void addVoteRegistrationMetadata(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA)) != null) {
      VoteRegistrationMetadata voteRegistrationMetadata = new VoteRegistrationMetadata();
      co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
          new UnicodeString(Constants.VOTEREGISTRATIONMETADATA));
      if (voteRegistrationMetadataMap.get(new UnicodeString(Constants.STAKE_KEY)) != null) {
        co.nstant.in.cbor.model.Map stakeKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.STAKE_KEY));
        PublicKey publicKey1 = getPublicKeyFromMap(stakeKeyMap);
        voteRegistrationMetadata.setStakeKey(publicKey1);
      }
      if (voteRegistrationMetadataMap.get(
          new UnicodeString(Constants.VOTING_KEY)) != null) {
        co.nstant.in.cbor.model.Map votingKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.VOTING_KEY));
        PublicKey publicKey2 = getPublicKeyFromMap(votingKeyMap);
        voteRegistrationMetadata.setVotingKey(publicKey2);
      }
      if (voteRegistrationMetadataMap.get(
          new UnicodeString(Constants.REWARD_ADDRESS)) != null) {
        String rewardAddress2 = ((UnicodeString) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.REWARD_ADDRESS))).getString();
        voteRegistrationMetadata.setRewardAddress(rewardAddress2);
      }
      if (voteRegistrationMetadataMap.get(
          new UnicodeString(Constants.VOTING_SIGNATURE)) != null) {
        String votingSignature = ((UnicodeString) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.VOTING_SIGNATURE))).getString();
        voteRegistrationMetadata.setVotingSignature(votingSignature);
      }
      if (voteRegistrationMetadataMap.get(
          new UnicodeString(Constants.VOTING_NONCE)) != null) {
        int votingNonce = ((UnsignedInteger) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.VOTING_NONCE))).getValue().intValue();
        voteRegistrationMetadata.setVotingNonce(votingNonce);
      }
      operationMetadata.setVoteRegistrationMetadata(voteRegistrationMetadata);
    }
  }
  public static void addPoolRegistrationParams(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.POOLREGISTRATIONPARAMS)) != null) {
      co.nstant.in.cbor.model.Map poolRegistrationParamsMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
          new UnicodeString(Constants.POOLREGISTRATIONPARAMS));
      PoolRegistrationParams poolRegistrationParams = new PoolRegistrationParams();
      addVrfKeyHash(poolRegistrationParamsMap,poolRegistrationParams);
      addRewardAddress(poolRegistrationParamsMap,poolRegistrationParams);
      addPledge(poolRegistrationParamsMap,poolRegistrationParams);
      addCost(poolRegistrationParamsMap,poolRegistrationParams);
      addPoolOwners(poolRegistrationParamsMap,poolRegistrationParams);
      addRelays(poolRegistrationParamsMap,poolRegistrationParams);
      addMargins(poolRegistrationParamsMap,poolRegistrationParams);
      if (poolRegistrationParamsMap.get(
          new UnicodeString(Constants.MARGIN_PERCENTAGE)) != null) {
        String marginPercentage = ((UnicodeString) poolRegistrationParamsMap.get(
            new UnicodeString(Constants.MARGIN_PERCENTAGE))).getString();
        poolRegistrationParams.setMarginPercentage(marginPercentage);
      }
      if (poolRegistrationParamsMap.get(
          new UnicodeString(Constants.POOLMETADATA)) != null) {
        PoolMetadata poolMetadata = new PoolMetadata();
        co.nstant.in.cbor.model.Map poolMetadataMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(
            new UnicodeString(Constants.POOLMETADATA));
        if (poolMetadataMap.get(
            new UnicodeString(Constants.URL)) != null) {
          String url = ((UnicodeString) poolMetadataMap.get(
              new UnicodeString(Constants.URL))).getString();
          poolMetadata.setUrl(url);
        }
        if (poolMetadataMap.get(
            new UnicodeString(Constants.HASH)) != null) {
          String hash = ((UnicodeString) poolMetadataMap.get(
              new UnicodeString(Constants.HASH))).getString();
          poolMetadata.setHash(hash);
        }
        poolRegistrationParams.setPoolMetadata(poolMetadata);
      }
      operationMetadata.setPoolRegistrationParams(poolRegistrationParams);
    }
  }
public static void addMargins(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
  if (poolRegistrationParamsMap.get(new UnicodeString(Constants.MARGIN)) != null) {
    co.nstant.in.cbor.model.Map marginMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(
        new UnicodeString(Constants.MARGIN));
    PoolMargin poolMargin = new PoolMargin();
    if (marginMap.get(
        new UnicodeString(Constants.NUMERATOR)) != null) {
      String numerator = ((UnicodeString) marginMap.get(
          new UnicodeString(Constants.NUMERATOR))).getString();
      poolMargin.setNumerator(numerator);
    }
    if (marginMap.get(
        new UnicodeString(Constants.DENOMINATOR)) != null) {
      String denominator = ((UnicodeString) marginMap.get(
          new UnicodeString(Constants.DENOMINATOR))).getString();
      poolMargin.setDenominator(denominator);
    }
    poolRegistrationParams.setMargin(poolMargin);
  }
}
  public static void addRelays(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
    if (poolRegistrationParamsMap.get(new UnicodeString(Constants.RELAYS)) != null) {
      List<Relay> relayList = new ArrayList<>();
      List<DataItem> relaysArray = ((Array) poolRegistrationParamsMap.get(
          new UnicodeString(Constants.RELAYS))).getDataItems();
      relaysArray.forEach(rA -> {
        co.nstant.in.cbor.model.Map rAMap = (co.nstant.in.cbor.model.Map) rA;
        Relay relay = new Relay();
        addRelayType(rAMap,relay);
        addIpv4(rAMap,relay);
        addIpv6(rAMap,relay);
        if (rAMap.get(new UnicodeString(Constants.DNSNAME)) != null) {
          String dnsName = ((UnicodeString) rAMap.get(
              new UnicodeString(Constants.DNSNAME))).getString();
          relay.setDnsName(dnsName);
        }
        relayList.add(relay);
      });
      poolRegistrationParams.setRelays(relayList);
    }
  }
  public static void addIpv6(Map rAMap,Relay relay){
    if (rAMap.get(new UnicodeString(Constants.IPV6)) != null) {
      String ipv6 = ((UnicodeString) rAMap.get(
          new UnicodeString(Constants.IPV6))).getString();
      relay.setIpv6(ipv6);
    }
  }
  public static void addIpv4(Map rAMap,Relay relay){
    if (rAMap.get(new UnicodeString(Constants.IPV4)) != null) {
      String ipv4 = ((UnicodeString) rAMap.get(
          new UnicodeString(Constants.IPV4))).getString();
      relay.setIpv4(ipv4);
    }
  }
  public static void addRelayType(Map rAMap,Relay relay){
    if (rAMap.get(new UnicodeString(Constants.TYPE)) != null) {
      String typeR = ((UnicodeString) rAMap.get(
          new UnicodeString(Constants.TYPE))).getString();
      relay.setType(typeR);
    }
  }
  public static void addPoolOwners(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
    if (poolRegistrationParamsMap.get(
        new UnicodeString(Constants.POOLOWNERS)) != null) {
      List<String> stringList = new ArrayList<>();
      List<DataItem> poolOwners = ((Array) poolRegistrationParamsMap.get(
          new UnicodeString(Constants.POOLOWNERS))).getDataItems();
      poolOwners.forEach(p -> {
        if (p != null) {
          stringList.add(((UnicodeString) p).getString());
        }
      });
      poolRegistrationParams.setPoolOwners(stringList);
    }
  }
  public static void addCost(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
    if (poolRegistrationParamsMap.get(
        new UnicodeString(Constants.COST)) != null) {
      String cost = ((UnicodeString) poolRegistrationParamsMap.get(
          new UnicodeString(Constants.COST))).getString();
      poolRegistrationParams.setCost(cost);
    }
  }
  public static void addPledge(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
    if (poolRegistrationParamsMap.get(new UnicodeString(Constants.PLEDGE)) != null) {
      String pledge = ((UnicodeString) poolRegistrationParamsMap.get(
          new UnicodeString(Constants.PLEDGE))).getString();
      poolRegistrationParams.setPledge(pledge);
    }
  }
  public static void addRewardAddress(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
    if (poolRegistrationParamsMap.get(
        new UnicodeString(Constants.REWARD_ADDRESS)) != null) {
      String rewardAddress = ((UnicodeString) poolRegistrationParamsMap.get(
          new UnicodeString(Constants.REWARD_ADDRESS))).getString();
      poolRegistrationParams.setRewardAddress(rewardAddress);
    }
  }
  public static void addVrfKeyHash(Map poolRegistrationParamsMap,PoolRegistrationParams poolRegistrationParams){
    if (poolRegistrationParamsMap.get(
        new UnicodeString(Constants.VRFKEYHASH)) != null) {
      String vrfKeyHash = ((UnicodeString) poolRegistrationParamsMap.get(
          new UnicodeString(Constants.VRFKEYHASH))).getString();
      poolRegistrationParams.setVrfKeyHash(vrfKeyHash);
    }
  }
  public static void addPoolRegistrationCert(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(
        new UnicodeString(Constants.POOLREGISTRATIONCERT)) != null) {
      String poolRegistrationCert = ((UnicodeString) metadataMap.get(
          new UnicodeString(Constants.POOLREGISTRATIONCERT))).getString();
      operationMetadata.setPoolRegistrationCert(poolRegistrationCert);
    }
  }
  public static void addTokenBundle(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.TOKENBUNDLE)) != null) {
      List<DataItem> tokenBundleArray = ((Array) metadataMap.get(
          new UnicodeString(Constants.TOKENBUNDLE))).getDataItems();
      List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
      tokenBundleArray.forEach(t -> {
        co.nstant.in.cbor.model.Map tokenBundleMap = (co.nstant.in.cbor.model.Map) t;
        TokenBundleItem tokenBundleItem = new TokenBundleItem();
        if (tokenBundleMap.get(
            new UnicodeString(Constants.POLICYID)) != null) {
          String policyIdT = ((UnicodeString) tokenBundleMap.get(
              new UnicodeString(Constants.POLICYID))).getString();
          tokenBundleItem.setPolicyId(policyIdT);
        }

        List<Amount> tokenAList = new ArrayList<>();
        if (tokenBundleMap.get(
            new UnicodeString(Constants.TOKENS)) != null) {
          List<DataItem> tokensItem = ((Array) tokenBundleMap.get(
              new UnicodeString(Constants.TOKENS))).getDataItems();
          tokensItem.forEach(tk -> {
            co.nstant.in.cbor.model.Map tokenAmountMap = (co.nstant.in.cbor.model.Map) tk;
            Amount amount1 = getAmountFromMap(tokenAmountMap);
            tokenAList.add(amount1);
          });
        }
        tokenBundleItem.setTokens(tokenAList);
        tokenBundleItems.add(tokenBundleItem);
      });
      operationMetadata.setTokenBundle(tokenBundleItems);
    }
  }
  public static void addEpoch(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(
        new UnicodeString(Constants.EPOCH)) != null) {
      Long epoch = ((UnsignedInteger) metadataMap.get(
          new UnicodeString(Constants.EPOCH))).getValue().longValue();
      operationMetadata.setEpoch(epoch);
    }
  }
  public static void addPoolKeyHash(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(
        new UnicodeString(Constants.POOL_KEY_HASH)) != null) {
      String poolKeyHash = ((UnicodeString) metadataMap.get(
          new UnicodeString(Constants.POOL_KEY_HASH))).getString();
      operationMetadata.setPoolKeyHash(poolKeyHash);
    }
  }
  public static void addStakingCredential(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.STAKING_CREDENTIAL)) != null) {
      co.nstant.in.cbor.model.Map stakingCredentialMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
          new UnicodeString(Constants.STAKING_CREDENTIAL));
      PublicKey publicKey = getPublicKeyFromMap(stakingCredentialMap);
      operationMetadata.setStakingCredential(publicKey);
    }
  }
  public static void addRefundAmount(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(
        new UnicodeString(Constants.REFUNDAMOUNT)) != null) {
      co.nstant.in.cbor.model.Map refundAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
          new UnicodeString(Constants.REFUNDAMOUNT));
      Amount amountR = getAmountFromMap(refundAmountMap);
      operationMetadata.setRefundAmount(amountR);
    }
  }
  public static void addDepositAmount(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.DEPOSITAMOUNT)) != null) {
      co.nstant.in.cbor.model.Map depositAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
          new UnicodeString(Constants.DEPOSITAMOUNT));
      Amount amountD = getAmountFromMap(depositAmountMap);
      operationMetadata.setDepositAmount(amountD);
    }
  }
  public static void addWithDrawalAmount(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.WITHDRAWALAMOUNT)) != null) {
      co.nstant.in.cbor.model.Map withdrawalAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
          new UnicodeString(Constants.WITHDRAWALAMOUNT));
      Amount amountW = getAmountFromMap(withdrawalAmountMap);
      operationMetadata.setWithdrawalAmount(amountW);
    }
  }
  public static void addCoinChange(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.COIN_CHANGE)) != null) {
      co.nstant.in.cbor.model.Map coinChangeMap = (co.nstant.in.cbor.model.Map) operationMap.get(
          new UnicodeString(Constants.COIN_CHANGE));
      CoinChange coinChange = new CoinChange();
      if (coinChangeMap.get(new UnicodeString(Constants.COIN_ACTION)) != null) {
        String coinAction = ((UnicodeString) coinChangeMap.get(
            new UnicodeString(Constants.COIN_ACTION))).getString();
        coinChange.setCoinAction(coinAction);
      }
      if (coinChangeMap.get(new UnicodeString(Constants.COIN_IDENTIFIER)) != null) {
        CoinIdentifier coinIdentifier = new CoinIdentifier();
        co.nstant.in.cbor.model.Map coinIdentifierMap = (co.nstant.in.cbor.model.Map) coinChangeMap.get(
            new UnicodeString(Constants.COIN_IDENTIFIER));
        String identifier = ((UnicodeString) coinIdentifierMap.get(
            new UnicodeString(Constants.IDENTIFIER))).getString();
        coinIdentifier.setIdentifier(identifier);
        coinChange.setCoinIdentifier(coinIdentifier);
      }
      operation.setCoinChange(coinChange);
    }
  }
  public static void addAmount(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.AMOUNT)) != null) {
      co.nstant.in.cbor.model.Map amountMap = (co.nstant.in.cbor.model.Map) operationMap.get(
          new UnicodeString(Constants.AMOUNT));
      Amount amount = getAmountFromMap(amountMap);
      operation.setAmount(amount);
    }
  }
  public static void addAccountToOperation(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.ACCOUNT)) != null) {
      AccountIdentifier accountIdentifier = new AccountIdentifier();
      co.nstant.in.cbor.model.Map accountIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(
          new UnicodeString(Constants.ACCOUNT));
      addAddress(accountIdentifierMap,accountIdentifier);
      addSubAccount(accountIdentifierMap,accountIdentifier);
      if (accountIdentifierMap.get(new UnicodeString(Constants.METADATA)) != null) {
        co.nstant.in.cbor.model.Map accountIdentifierMetadataMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
            new UnicodeString(Constants.METADATA));
        AccountIdentifierMetadata accountIdentifierMetadata = new AccountIdentifierMetadata();
        if (accountIdentifierMetadataMap.get(new UnicodeString(Constants.CHAIN_CODE)) != null) {
          String chainCode = null;

          if (accountIdentifierMetadataMap.get(new UnicodeString(Constants.CHAIN_CODE))
              .getMajorType().getValue() == MajorType.UNICODE_STRING.getValue()) {
            chainCode = ((UnicodeString) (accountIdentifierMetadataMap.get(
                new UnicodeString(Constants.CHAIN_CODE)))).getString();
          }

          accountIdentifierMetadata.setChainCode(chainCode);
        }
        accountIdentifier.setMetadata(accountIdentifierMetadata);
      }
      operation.setAccount(accountIdentifier);
    }
  }
  public static void addSubAccount(Map accountIdentifierMap,AccountIdentifier accountIdentifier){
    if (accountIdentifierMap.get(new UnicodeString(Constants.SUB_ACCOUNT)) != null) {
      co.nstant.in.cbor.model.Map subAccountIdentifierMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
          new UnicodeString(Constants.SUB_ACCOUNT));
      SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
      if (subAccountIdentifierMap.get(new UnicodeString(Constants.ADDRESS)) != null) {
        String addressSub = ((UnicodeString) (subAccountIdentifierMap.get(
            new UnicodeString(Constants.ADDRESS)))).getString();
        subAccountIdentifier.setAddress(addressSub);
      }
      if (subAccountIdentifierMap.get(new UnicodeString(Constants.METADATA)) != null) {
        co.nstant.in.cbor.model.Map metadataSub = (co.nstant.in.cbor.model.Map) (subAccountIdentifierMap.get(
            new UnicodeString(Constants.METADATA)));
        if (!metadataSub.getValues().isEmpty()) {
          subAccountIdentifier.setMetadata(metadataSub);
        }
      }
      accountIdentifier.setSubAccount(subAccountIdentifier);
    }
  }
  public static void addAddress(Map accountIdentifierMap,AccountIdentifier accountIdentifier){
    if (accountIdentifierMap.get(
        new UnicodeString(Constants.ADDRESS)) != null) {
      String address = ((UnicodeString) accountIdentifierMap.get(
          new UnicodeString(Constants.ADDRESS))).getString();
      accountIdentifier.setAddress(address);
    }
  }
  public static void addStatusToOperation(Map operationMap,Operation operation,String dataName){
    if (operationMap.get(new UnicodeString(dataName)) != null) {
      String status = ((UnicodeString) (operationMap.get(
          new UnicodeString(dataName)))).getString();
      operation.setStatus(status);
    }
  }
  public static void addTypeToOperation(Map operationMap,Operation operation,String dataName){
    if (operationMap.get(new UnicodeString(dataName)) != null) {
      String type = ((UnicodeString) (operationMap.get(
          new UnicodeString(dataName)))).getString();
      operation.setType(type);
    }
  }
  public static void addOperationIdentifier(Map operationIdentifierMap,OperationIdentifier operationIdentifier){
    if (operationIdentifierMap.get(new UnicodeString(Constants.INDEX)) != null) {
      operationIdentifier.setIndex(((UnsignedInteger) operationIdentifierMap.get(
          new UnicodeString(Constants.INDEX))).getValue()
          .longValue());
    }
    if (operationIdentifierMap.get(new UnicodeString(Constants.NETWORK_INDEX)) != null) {
      operationIdentifier.setNetworkIndex(((UnsignedInteger) operationIdentifierMap.get(
          new UnicodeString(Constants.NETWORK_INDEX))).getValue()
          .longValue());
    }
  }
  public static void addTransactionMetadataHex(TransactionExtraData transactionExtraData,Map map){
    if (map.get(
        new UnicodeString(Constants.TRANSACTIONMETADATAHEX)) != null) {
      String transactionMetadataHex = ((UnicodeString) map.get(
          new UnicodeString(Constants.TRANSACTIONMETADATAHEX))).getString();
      transactionExtraData.setTransactionMetadataHex(transactionMetadataHex);
    }
  }

  public static PublicKey getPublicKeyFromMap(co.nstant.in.cbor.model.Map stakingCredentialMap) {
    PublicKey publicKey = new PublicKey();
    if (stakingCredentialMap.get(
        new UnicodeString(Constants.HEX_BYTES)) != null) {
      String hexBytes = ((UnicodeString) stakingCredentialMap.get(
          new UnicodeString(Constants.HEX_BYTES))).getString();
      publicKey.setHexBytes(hexBytes);
    }
    if (stakingCredentialMap.get(
        new UnicodeString(Constants.CURVE_TYPE)) != null) {
      String curveType = ((UnicodeString) stakingCredentialMap.get(
          new UnicodeString(Constants.CURVE_TYPE))).getString();
      publicKey.setCurveType(curveType);
    }
    return publicKey;
  }

  public static Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap) {
    Amount amount = new Amount();
    if (amountMap != null) {
      if (amountMap.get(new UnicodeString(Constants.VALUE)) != null) {
        String value = ((UnicodeString) amountMap.get(
            new UnicodeString(Constants.VALUE))).getString();
        amount.setValue(value);
      }
      if (amountMap.get(new UnicodeString(Constants.METADATA)) != null) {
        co.nstant.in.cbor.model.Map metadataAm = (co.nstant.in.cbor.model.Map) amountMap.get(
            new UnicodeString(Constants.METADATA));
        amount.setMetadata(metadataAm);
      }
      getCurrencyFromMap(amountMap, amount);
    }
    return amount;
  }

  public static void getCurrencyFromMap(co.nstant.in.cbor.model.Map amountMap, Amount amount) {
    if (amountMap.get(
        new UnicodeString(Constants.CURRENCY)) != null) {
      co.nstant.in.cbor.model.Map currencyMap = (co.nstant.in.cbor.model.Map) amountMap.get(
          new UnicodeString(Constants.CURRENCY));
      Currency currency = new Currency();
      if (currencyMap.get(new UnicodeString(Constants.SYMBOL)) != null) {
        String symbol = ((UnicodeString) currencyMap.get(
            new UnicodeString(Constants.SYMBOL))).getString();
        currency.setSymbol(symbol);
      }
      if (currencyMap.get(
          new UnicodeString(Constants.DECIMALS)) != null) {
        Integer decimals = ((UnsignedInteger) currencyMap.get(
            new UnicodeString(Constants.DECIMALS))).getValue()
            .intValue();
        currency.setDecimals(decimals);
      }

      if (currencyMap.get(new UnicodeString(Constants.METADATA)) != null) {
        Metadata metadata = new Metadata();
        co.nstant.in.cbor.model.Map addedMetadataMap = (co.nstant.in.cbor.model.Map) currencyMap.get(
            new UnicodeString(Constants.METADATA));
        if (addedMetadataMap.get(new UnicodeString(Constants.POLICYID)) != null) {
          String policyId = ((UnicodeString) addedMetadataMap.get(
              new UnicodeString(Constants.POLICYID))).getString();
          metadata.setPolicyId(policyId);
        }
        currency.setMetadata(metadata);
      }
      amount.setCurrency(currency);
    }
  }
}
