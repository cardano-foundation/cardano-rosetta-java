package org.cardanofoundation.rosetta.common.util;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.AccountIdentifierMetadata;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolMargin;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.SubAccountIdentifier;
import org.openapitools.client.model.TokenBundleItem;
import org.openapitools.client.model.VoteRegistrationMetadata;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.PoolRegistrationParams;

public class DataItemDecodeUtil {
  public static TransactionExtraData changeFromCborMaptoTransactionExtraData(Map map) {
    //separator
    String transactionMetadataHex = addTransactionMetadataHex(map);
    List<Operation> operations = new ArrayList<>();
    List<DataItem> operationsListMap = ((Array) map.get(
        new UnicodeString(Constants.OPERATIONS))).getDataItems();
    operationsListMap.forEach(oDataItem -> {
      Map operationMap = (Map) oDataItem;
      Operation operation = new Operation();
      if (operationMap.get(new UnicodeString(Constants.OPERATION_IDENTIFIER)) != null) {
        Map operationIdentifierMap = (Map) operationMap.get(
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
          Map operationIdentifierMap2 = (Map) rDI;

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
    return new TransactionExtraData(operations,transactionMetadataHex);
  }

  public static void addMetadata(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.METADATA)) != null) {
      Map metadataMap = (Map) operationMap.get(
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
  public static void addVoteRegistrationMetadata(Map metadataMap,
      OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA)) != null) {
      VoteRegistrationMetadata voteRegistrationMetadata = new VoteRegistrationMetadata();
      Map voteRegistrationMetadataMap = (Map) metadataMap.get(
          new UnicodeString(Constants.VOTEREGISTRATIONMETADATA));
      if (voteRegistrationMetadataMap.get(new UnicodeString(Constants.STAKE_KEY)) != null) {
        Map stakeKeyMap = (Map) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.STAKE_KEY));
        PublicKey publicKey1 = getPublicKeyFromMap(stakeKeyMap);
        voteRegistrationMetadata.setStakeKey(publicKey1);
      }
      if (voteRegistrationMetadataMap.get(
          new UnicodeString(Constants.VOTING_KEY)) != null) {
        Map votingKeyMap = (Map) voteRegistrationMetadataMap.get(
            new UnicodeString(Constants.VOTING_KEY));
        PublicKey publicKey2 = getPublicKeyFromMap(votingKeyMap);
        voteRegistrationMetadata.setVotingkey(publicKey2);
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
      Map poolRegistrationParamsMap = (Map) metadataMap.get(
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
        Map poolMetadataMap = (Map) poolRegistrationParamsMap.get(
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
      Map marginMap = (Map) poolRegistrationParamsMap.get(
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
        Map rAMap = (Map) rA;
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
        Map tokenBundleMap = (Map) t;
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
            Map tokenAmountMap = (Map) tk;
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
      BigInteger value = ((UnsignedInteger) metadataMap.get(
          new UnicodeString(Constants.EPOCH))).getValue();
      operationMetadata.setEpoch(value.intValue());
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
      Map stakingCredentialMap = (Map) metadataMap.get(
          new UnicodeString(Constants.STAKING_CREDENTIAL));
      PublicKey publicKey = getPublicKeyFromMap(stakingCredentialMap);
      operationMetadata.setStakingCredential(publicKey);
    }
  }
  public static void addRefundAmount(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(
        new UnicodeString(Constants.REFUNDAMOUNT)) != null) {
      Map refundAmountMap = (Map) metadataMap.get(
          new UnicodeString(Constants.REFUNDAMOUNT));
      Amount amountR = getAmountFromMap(refundAmountMap);
      operationMetadata.setRefundAmount(amountR);
    }
  }
  public static void addDepositAmount(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.DEPOSITAMOUNT)) != null) {
      Map depositAmountMap = (Map) metadataMap.get(
          new UnicodeString(Constants.DEPOSITAMOUNT));
      Amount amountD = getAmountFromMap(depositAmountMap);
      operationMetadata.setDepositAmount(amountD);
    }
  }
  public static void addWithDrawalAmount(Map metadataMap,OperationMetadata operationMetadata){
    if (metadataMap.get(new UnicodeString(Constants.WITHDRAWALAMOUNT)) != null) {
      Map withdrawalAmountMap = (Map) metadataMap.get(
          new UnicodeString(Constants.WITHDRAWALAMOUNT));
      Amount amountW = getAmountFromMap(withdrawalAmountMap);
      operationMetadata.setWithdrawalAmount(amountW);
    }
  }
  public static void addCoinChange(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.COIN_CHANGE)) != null) {
      Map coinChangeMap = (Map) operationMap.get(
          new UnicodeString(Constants.COIN_CHANGE));
      CoinChange coinChange = new CoinChange();
      if (coinChangeMap.get(new UnicodeString(Constants.COIN_ACTION)) != null) {
        String coinAction = ((UnicodeString) coinChangeMap.get(
            new UnicodeString(Constants.COIN_ACTION))).getString();
        coinChange.setCoinAction(CoinAction.fromValue(coinAction));
      }
      if (coinChangeMap.get(new UnicodeString(Constants.COIN_IDENTIFIER)) != null) {
        CoinIdentifier coinIdentifier = new CoinIdentifier();
        Map coinIdentifierMap = (Map) coinChangeMap.get(
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
      Map amountMap = (Map) operationMap.get(
          new UnicodeString(Constants.AMOUNT));
      Amount amount = getAmountFromMap(amountMap);
      operation.setAmount(amount);
    }
  }
  public static void addAccountToOperation(Map operationMap,Operation operation){
    if (operationMap.get(new UnicodeString(Constants.ACCOUNT)) != null) {
      AccountIdentifier accountIdentifier = new AccountIdentifier();
      Map accountIdentifierMap = (Map) operationMap.get(
          new UnicodeString(Constants.ACCOUNT));
      addAddress(accountIdentifierMap,accountIdentifier);
      addSubAccount(accountIdentifierMap,accountIdentifier);
      if (accountIdentifierMap.get(new UnicodeString(Constants.METADATA)) != null) {
        Map accountIdentifierMetadataMap = (Map) accountIdentifierMap.get(
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
  public static void addSubAccount(Map accountIdentifierMap, AccountIdentifier accountIdentifier){
    if (accountIdentifierMap.get(new UnicodeString(Constants.SUB_ACCOUNT)) != null) {
      Map subAccountIdentifierMap = (Map) accountIdentifierMap.get(
          new UnicodeString(Constants.SUB_ACCOUNT));
      SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
      if (subAccountIdentifierMap.get(new UnicodeString(Constants.ADDRESS)) != null) {
        String addressSub = ((UnicodeString) (subAccountIdentifierMap.get(
            new UnicodeString(Constants.ADDRESS)))).getString();
        subAccountIdentifier.setAddress(addressSub);
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
  public static String addTransactionMetadataHex(Map map){
    String transactionMetadataHex = "";
    if (map.get(
        new UnicodeString(Constants.TRANSACTIONMETADATAHEX)) != null) {
      transactionMetadataHex = ((UnicodeString) map.get(
          new UnicodeString(Constants.TRANSACTIONMETADATAHEX))).getString();
    }
    return transactionMetadataHex;
  }

  public static PublicKey getPublicKeyFromMap(Map stakingCredentialMap) {
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
      publicKey.setCurveType(CurveType.fromValue(curveType));
    }
    return publicKey;
  }

  public static Amount getAmountFromMap(Map amountMap) {
    Amount amount = new Amount();
    if (amountMap != null) {
      if (amountMap.get(new UnicodeString(Constants.VALUE)) != null) {
        String value = ((UnicodeString) amountMap.get(
            new UnicodeString(Constants.VALUE))).getString();
        amount.setValue(value);
      }
      if (amountMap.get(new UnicodeString(Constants.METADATA)) != null) {
        Map metadataAm = (Map) amountMap.get(
            new UnicodeString(Constants.METADATA));
        amount.setMetadata(metadataAm);
      }
      getCurrencyFromMap(amountMap, amount);
    }
    return amount;
  }

  public static void getCurrencyFromMap(Map amountMap, Amount amount) {
    if (amountMap.get(
        new UnicodeString(Constants.CURRENCY)) != null) {
      Map currencyMap = (Map) amountMap.get(
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
        CurrencyMetadata metadata = new CurrencyMetadata();
        Map addedMetadataMap = (Map) currencyMap.get(
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
