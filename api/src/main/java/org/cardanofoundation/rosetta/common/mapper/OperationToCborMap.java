package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.model.*;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.cardanofoundation.rosetta.common.util.Formatters.key;

public class OperationToCborMap {

    private OperationToCborMap() {}

    public static Map convertToCborMap(Operation operation) {
        Map operationMap = new Map();
        // fill with nested objects
        addOperationIdentifierToMap(operation.getOperationIdentifier(), operationMap);
        addRelatedOperationToMap(operation.getRelatedOperations(), operationMap);
        addAccountIdentifierToMap(operation.getAccount(), operationMap);
        addAmountToMap(operation.getAmount(), operationMap);
        addCoinChangeToMap(operation.getCoinChange(), operationMap);

        addOperationMetadataToMap(operation.getMetadata(), operationMap);
        // add simple values
        putStringDataItemToMap(operationMap, Constants.STATUS, operation.getStatus());
        putStringDataItemToMap(operationMap, Constants.TYPE, operation.getType());

        return operationMap;
    }

    /**
     * Add OperationMetadata to operationMap with the key {@value Constants#METADATA}
     * @param metadata metadata
     * @param operationMap operationMap
     */
    private static void addOperationMetadataToMap(OperationMetadata metadata, Map operationMap) {
        Optional.ofNullable(metadata)
                .ifPresent(operationMetadata -> {
                    Map metadataMap = new Map();
                    // fill with nested objects
                    addPublicKeyToMap(operationMetadata.getStakingCredential(), metadataMap, Constants.STAKING_CREDENTIAL);
                    addWithdrawalAmaountToMap(operationMetadata.getWithdrawalAmount(), metadataMap);
                    addDepositToMap(operationMetadata.getDepositAmount(), metadataMap);
                    addRefundToMap(operationMetadata.getRefundAmount(), metadataMap);
                    addTokenBundleToMap(operationMetadata.getTokenBundle(), metadataMap);
                    addPoolRegistrationPramsToMap(operationMetadata.getPoolRegistrationParams(), metadataMap);
                    addDrepDelegationMetadataToMap(operationMetadata.getDrep(), metadataMap, Constants.DREP);
                    addPoolGovernanceVoteMetadataToMap(operationMetadata, metadataMap);

                    // add simple values
                    putStringDataItemToMap(metadataMap, Constants.POOL_REGISTRATION_CERT, operationMetadata.getPoolRegistrationCert());
                    putStringDataItemToMap(metadataMap, Constants.POOL_KEY_HASH, operationMetadata.getPoolKeyHash());
                    putUnsignedIntegerToMap(metadataMap, Constants.EPOCH, operationMetadata.getEpoch());

                    operationMap.put(key(Constants.METADATA), metadataMap);
                });
    }

    private static void addDrepDelegationMetadataToMap(@Nullable DRepParams drep, Map metadataMap, String key) {
        Optional.ofNullable(drep).ifPresent(dRepParams -> {
            Map dRepMap = new Map();

            putStringDataItemToMap(dRepMap, Constants.ID, dRepParams.getId());
            putStringDataItemToMap(dRepMap, Constants.TYPE, dRepParams.getType().getValue());

            metadataMap.put(key(key), dRepMap);
        });
    }

    private static void addPoolGovernanceVoteMetadataToMap(OperationMetadata operationMetadata, Map metadataMap) {
        Optional.ofNullable(operationMetadata.getPoolGovernanceVoteParams()).ifPresent(poolGovernanceVoteParams -> {
            Map poolGovernanceVoteParamsMap = new Map();

            Map poolCredentialMap = new Map();
            poolCredentialMap.put(key(Constants.HEX_BYTES), new UnicodeString(poolGovernanceVoteParams.getPoolCredential().getHexBytes()));
            poolCredentialMap.put(key(Constants.CURVE_TYPE), new UnicodeString(poolGovernanceVoteParams.getPoolCredential().getCurveType().getValue()));

            poolGovernanceVoteParamsMap.put(key(Constants.POOL_CREDENTIAL), poolCredentialMap);
            poolGovernanceVoteParamsMap.put(key(Constants.VOTE), new UnicodeString(poolGovernanceVoteParams.getVote().getValue()));

            Optional.ofNullable(poolGovernanceVoteParams.getVoteRationale()).ifPresent(voteRationale -> {
                Map voteRationaleMap = new Map();

                putStringDataItemToMap(voteRationaleMap, "url", voteRationale.getUrl());
                putStringDataItemToMap(voteRationaleMap, "data_hash", voteRationale.getDataHash());

                poolGovernanceVoteParamsMap.put(key(Constants.VOTE_RATIONALE), voteRationaleMap);
            });

            Map actionMap = new Map();
            // Parse the concatenated governance action string
            org.cardanofoundation.rosetta.common.util.GovActionParamsUtil.ParsedGovActionParams parsedGovAction = 
                    org.cardanofoundation.rosetta.common.util.GovActionParamsUtil.parseAndValidate(poolGovernanceVoteParams.getGovernanceAction());
            
            putStringDataItemToMap(actionMap, "tx_id", parsedGovAction.getTxId());
            putUnsignedIntegerToMap(actionMap, "index", parsedGovAction.getIndex());

            poolGovernanceVoteParamsMap.put(key(Constants.GOVERNANCE_ACTION), actionMap);

            metadataMap.put(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS), poolGovernanceVoteParamsMap);
        });
    }

  /**
   * Add PoolRegistrationParams to metadataMap with the key {@value Constants#POOL_REGISTRATION_PARAMS}
   * @param poolRegistrationParams poolRegistrationParams
   * @param metadataMap metadataMap
   */
  private static void addPoolRegistrationPramsToMap(PoolRegistrationParams poolRegistrationParams,
      Map metadataMap) {
    Optional.ofNullable(poolRegistrationParams)
        .ifPresent(poolRegistrationParams1 -> {
          Map poolRegistrationParamsMap = new Map();
          // fill map with nested Objects
          addPoolOwnersToMap(poolRegistrationParams1.getPoolOwners(), poolRegistrationParamsMap);
          addMarginToMap(poolRegistrationParams1.getMargin(), poolRegistrationParamsMap);
          addRelaysToMap(poolRegistrationParams1.getRelays(), poolRegistrationParamsMap);
          addPoolMetadataToMap(poolRegistrationParams1.getPoolMetadata(), poolRegistrationParamsMap);
          // fill simple values
          putStringDataItemToMap(poolRegistrationParamsMap, Constants.PLEDGE, poolRegistrationParams1.getPledge());
          putStringDataItemToMap(poolRegistrationParamsMap, Constants.REWARD_ADDRESS, poolRegistrationParams1.getRewardAddress());
          putStringDataItemToMap(poolRegistrationParamsMap, Constants.VRF_KEY_HASH, poolRegistrationParams1.getVrfKeyHash());
          putStringDataItemToMap(poolRegistrationParamsMap, Constants.COST, poolRegistrationParams1.getCost());
          putStringDataItemToMap(poolRegistrationParamsMap, Constants.MARGIN_PERCENTAGE, poolRegistrationParams1.getMarginPercentage());
          // write back
          metadataMap.put(key(Constants.POOL_REGISTRATION_PARAMS), poolRegistrationParamsMap);
        });
  }

  /**
   * Add PoolMetadata to poolRegistrationParamsMap with the key {@value Constants#POOL_METADATA}
   * @param poolMetadata poolMetadata
   * @param poolRegistrationParamsMap poolRegistrationParamsMap
   */
  private static void addPoolMetadataToMap(PoolMetadata poolMetadata,
      Map poolRegistrationParamsMap) {
    Optional.ofNullable(poolMetadata)
        .ifPresent(o -> {
          Map poolMetadataMap = new Map();
          putStringDataItemToMap(poolMetadataMap,Constants.HASH, poolMetadata.getHash());
          putStringDataItemToMap(poolMetadataMap,Constants.URL, poolMetadata.getUrl());
          poolRegistrationParamsMap.put(key(Constants.POOL_METADATA), poolMetadataMap);
        });
  }

  /**
   * Add Margin to poolRegistrationParamsMap with the key {@value Constants#MARGIN}
   * @param margin margin
   * @param poolRegistrationParamsMap poolRegistrationParamsMap
   */
  private static void addMarginToMap(PoolMargin margin, Map poolRegistrationParamsMap) {
    Optional.ofNullable(margin)
        .ifPresent(margin1 -> {
          Map marginMap = new Map();
          marginMap.put(key(Constants.NUMERATOR), new UnicodeString(margin1.getNumerator()));
          marginMap.put(key(Constants.DENOMINATOR), new UnicodeString(margin1.getDenominator()));
          poolRegistrationParamsMap.put(key(Constants.MARGIN), marginMap);
        });
  }

  /**
   * Add Relays to poolRegistrationParamsMap with the key {@value Constants#RELAYS}
   * @param relays relays
   * @param poolRegistrationParamsMap poolRegistrationParamsMap
   */
  private static void addRelaysToMap(List<Relay> relays, Map poolRegistrationParamsMap) {
    Optional.ofNullable(relays)
        .ifPresent(relayList -> {
          Array relaysArray = new Array();

          relayList.forEach(r -> {
            Map relayMap = new Map();
            putStringDataItemToMap(relayMap, Constants.TYPE, r.getType());
            putStringDataItemToMap(relayMap, Constants.IPV4, r.getIpv4());
            putStringDataItemToMap(relayMap, Constants.IPV6, r.getIpv6());
            putStringDataItemToMap(relayMap, Constants.DNSNAME, r.getDnsName());

            // port is mandatory in a relay object
            putStringDataItemToMap(relayMap, Constants.PORT, r.getPort().toString());

            relaysArray.add(relayMap);
          });
          poolRegistrationParamsMap.put(key(Constants.RELAYS), relaysArray);
        });
  }

  /**
   * Add PoolOwners to poolRegistrationParamsMap with the key {@value Constants#POOL_OWNERS}
   * @param poolOwners poolOwners
   * @param poolRegistrationParamsMap poolRegistrationParamsMap
   */
  private static void addPoolOwnersToMap(List<String> poolOwners, Map poolRegistrationParamsMap) {
    Optional.ofNullable(poolOwners)
        .ifPresent(poolOwners1 -> {
          Array poolOwnersArray = new Array();
          poolOwners1.forEach(poolOwner -> poolOwnersArray.add(new UnicodeString(poolOwner)));
          poolRegistrationParamsMap.put(key(Constants.POOL_OWNERS), poolOwnersArray);
        });
  }

  /**
   * Add TokenBundle to metadataMap with the key {@value Constants#TOKEN_BUNDLE}
   * @param tokenBundle tokenBundle
   * @param metadataMap metadataMap
   */
  private static void addTokenBundleToMap(List<TokenBundleItem> tokenBundle, Map metadataMap) {
    Optional.ofNullable(tokenBundle)
        .ifPresent(tokenBundleItems -> {
          Array tokenBundleArray = new Array();
          tokenBundleItems.forEach(tokenBundleItem -> {
            Map tokenBundleMap = new Map();

            addTokensToMap(tokenBundleItem.getTokens(), tokenBundleMap);
            putStringDataItemToMap(tokenBundleMap, Constants.POLICYID, tokenBundleItem.getPolicyId());
            tokenBundleArray.add(tokenBundleMap);
          });
          metadataMap.put(key(Constants.TOKEN_BUNDLE), tokenBundleArray);
        });
  }

  /**
   * Add Tokens to tokenBundleMap with the key {@value Constants#TOKENS}
   * @param tokens tokens
   * @param tokenBundleMap tokenBundleMap
   */
  private static void addTokensToMap(List<Amount> tokens, Map tokenBundleMap) {
    Optional.ofNullable(tokens)
        .ifPresent(tokenAmounts -> {
          Array tokensArray = new Array();
          tokenAmounts.forEach(tokenAmount -> {
            Map tokenAmountMap = new Map();
            addAmountToMap(tokenAmount, tokenAmountMap);
            tokensArray.add(tokenAmountMap);
          });
          tokenBundleMap.put(key(Constants.TOKENS), tokensArray);
        });
  }



  /**
   * Add RefundAmount to metadataMap with the key {@value Constants#REFUNDAMOUNT}
   * @param refundAmount refundAmount
   * @param metadataMap metadataMap
   */
  private static void addRefundToMap(Amount refundAmount, Map metadataMap) {
    Optional.ofNullable(refundAmount)
        .ifPresent(refundAmount1 -> {
          Map refundAmountMap = new Map();
          addAmountToMap(refundAmount1, refundAmountMap);
          metadataMap.put(key(Constants.REFUNDAMOUNT), refundAmountMap);
        });
  }

  /**
   * Add DepositAmount to metadataMap with the key {@value Constants#DEPOSITAMOUNT}
   * @param depositAmount depositAmount
   * @param metadataMap metadataMap
   */
  private static void addDepositToMap(Amount depositAmount, Map metadataMap) {
    Optional.ofNullable(depositAmount)
        .ifPresent(depositAmount1 -> {
          Map depositAmountMap = new Map();
          addAmountToMap(depositAmount1, depositAmountMap);
          metadataMap.put(key(Constants.DEPOSITAMOUNT), depositAmountMap);
        });
  }

  /**
   * Add WithdrawalAmount to metadataMap with the key {@value Constants#WITHDRAWALAMOUNT}
   * @param withdrawalAmount withdrawalAmount
   * @param metadataMap metadataMap
   */
  private static void addWithdrawalAmaountToMap(Amount withdrawalAmount, Map metadataMap) {
    Optional.ofNullable(withdrawalAmount)
        .ifPresent(withdrawalAmount1 -> {
          Map withdrawalAmountMap = new Map();
          addAmountToMap(withdrawalAmount1, withdrawalAmountMap);
          metadataMap.put(key(Constants.WITHDRAWALAMOUNT), withdrawalAmountMap);
        });
  }

  private static void addPublicKeyToMap(PublicKey publicKey, Map metadataMap, String key) {
    Optional.ofNullable(publicKey)
        .ifPresent(publicKey1 -> {
          Map publicKeyMap = new Map();
          publicKeyMap.put(key(Constants.HEX_BYTES), new UnicodeString(publicKey1.getHexBytes()));
          publicKeyMap.put(key(Constants.CURVE_TYPE), new UnicodeString(publicKey1.getCurveType().getValue()));
          metadataMap.put(key(key), publicKeyMap);
        });
  }

  private static void addCoinChangeToMap(CoinChange coinChange, Map operationMap) {
    Optional.ofNullable(coinChange)
        .ifPresent(coinChange1 -> {
          Map coinChangeMap = new Map();
          addCoinIdentifierToMap(coinChange1.getCoinIdentifier(), coinChangeMap);
          addCoinActionToMap(coinChange1.getCoinAction(), coinChangeMap);
          operationMap.put(key(Constants.COIN_CHANGE), coinChangeMap);
        });
  }

  /**
   * Add CoinAction to coinChangeMap with the key {@value Constants#COIN_ACTION}
   * @param coinAction coinAction
   * @param coinChangeMap coinChangeMap
   */
  private static void addCoinActionToMap(CoinAction coinAction, Map coinChangeMap) {
    Optional.ofNullable(coinAction).ifPresent(ca -> coinChangeMap.put(key(Constants.COIN_ACTION), new UnicodeString(ca.getValue())));
  }

  /**
   * Add CoinIdentifier to coinChangeMap with the key {@value Constants#COIN_IDENTIFIER}
   * @param coinIdentifier coinIdentifier
   * @param coinChangeMap coinChangeMap
   */
  private static void addCoinIdentifierToMap(CoinIdentifier coinIdentifier, Map coinChangeMap) {
    Optional.ofNullable(coinIdentifier).ifPresent(coinId -> {
      Map coinIdentifierMap = new Map();
      coinIdentifierMap.put(key(Constants.IDENTIFIER), new UnicodeString(coinId.getIdentifier()));
      coinChangeMap.put(key(Constants.COIN_IDENTIFIER), coinIdentifierMap);
    });
  }

  /**
   * Add an AmountObject to map with the key {@value Constants#AMOUNT}
   * @param amount amount
   * @param operationMap operationMap
   */
  private static void addAmountToMap(Amount amount, Map operationMap) {
    Optional.ofNullable(amount).ifPresent(am -> {
      Map amountMap = new Map();
      addCurrencyToAmountMap(am.getCurrency(), amountMap);
      putStringDataItemToMap(amountMap,Constants.VALUE,am.getValue());
      operationMap.put(key(Constants.AMOUNT), amountMap);
    });
  }

  /**
   * Add Currency to amountMap with the key {@value Constants#CURRENCY}
   * @param currency currency
   * @param amountMap amountMap
   */
  private static void addCurrencyToAmountMap(Currency currency, Map amountMap) {
    Optional.ofNullable(currency).ifPresent(cur -> {
      Map currencyMap = new Map();
      putStringDataItemToMap(currencyMap,Constants.SYMBOL,cur.getSymbol());
      putUnsignedIntegerToMap(currencyMap, Constants.DECIMALS, cur.getDecimals());
      addMetadataToCurrencyMap(cur.getMetadata(), currencyMap);
      amountMap.put(key(Constants.CURRENCY), currencyMap);
    });
  }

  /**
   * Add Metadata to currencyMap with the key {@value Constants#METADATA}
   * @param metadata metadata
   * @param currencyMap currencyMap
   */
  private static void addMetadataToCurrencyMap(CurrencyMetadata metadata, Map currencyMap) {
    Optional.ofNullable(metadata).ifPresent(m -> {
      Map metadataMap = new Map();
      Optional.ofNullable(metadata.getPolicyId()).ifPresent(policyId -> metadataMap.put(key(Constants.POLICYID), new UnicodeString(policyId)));
      currencyMap.put(key(Constants.METADATA), metadataMap);
    });
  }

  /**
   * Add AccountIdentifier to operationMap with the key {@value Constants#ACCOUNT}
   * @param account account
   * @param operationMap operationMap
   */
  private static void addAccountIdentifierToMap(AccountIdentifier account, Map operationMap) {
    Optional.ofNullable(account)
        .ifPresent(accountIdentifier -> {
          co.nstant.in.cbor.model.Map accountIdentifierMap = new co.nstant.in.cbor.model.Map();
          // fill map
          putStringDataItemToMap(accountIdentifierMap, Constants.ADDRESS, account.getAddress());
          addSubAccountToMap(account, accountIdentifierMap);
          operationMap.put(key(Constants.ACCOUNT), accountIdentifierMap);
        });
  }

  /**
   * Add SubAccount to accountIdentifierMap with the key {@value Constants#SUB_ACCOUNT}
   * @param account account
   * @param accountIdentifierMap accountIdentifierMap
   */
  private static void addSubAccountToMap(AccountIdentifier account, Map accountIdentifierMap) {
    Optional.ofNullable(account.getSubAccount())
        .ifPresent(subAccount -> {
          Map subAccountIdentifierMap = new Map();
          putStringDataItemToMap(subAccountIdentifierMap, Constants.ADDRESS, subAccount.getAddress());
          addMetaDataToAccountIdentifier(account, subAccountIdentifierMap);
          accountIdentifierMap.put(key(Constants.SUB_ACCOUNT),
              subAccountIdentifierMap);
        });
  }

  /**
   * Add Metadata to subAccountIdentifierMap with the key {@value Constants#METADATA}
   * @param account account
   * @param subAccountIdentifierMap subAccountIdentifierMap
   */
  private static void addMetaDataToAccountIdentifier(AccountIdentifier account, Map subAccountIdentifierMap) {
    Optional.ofNullable(account.getSubAccount().getMetadata())
        .ifPresent(metadata -> subAccountIdentifierMap.put(key(Constants.METADATA),
            objectToDataItem(metadata)));
  }

  /**
   * Add RelatedOperation to operationMap with the key {@value Constants#RELATED_OPERATION}
   * @param relatedOperations relatedOperations
   * @param operationMap operationMap
   */
  private static void addRelatedOperationToMap(List<OperationIdentifier> relatedOperations,
      Map operationMap) {
    Optional.ofNullable(relatedOperations)
        .ifPresent(relatedOperationIdentifiers -> {
          Array relatedOperationArray = new Array();
          relatedOperationIdentifiers.stream().map(OperationToCborMap::getOperationIdentifierMap)
              .forEach(relatedOperationArray::add);
          operationMap.put(key(Constants.RELATED_OPERATION), relatedOperationArray);
        });
  }

  /**
   * Add OperationIdentifier to operationMap with the key {@value Constants#OPERATION_IDENTIFIER}
   * @param operationIdentifier operationIdentifier
   * @param operationMap operationMap
   */
  private static void addOperationIdentifierToMap(OperationIdentifier operationIdentifier, Map operationMap) {
    Map operationIdentifierMap = getOperationIdentifierMap(operationIdentifier);
    operationMap.put(key(Constants.OPERATION_IDENTIFIER), operationIdentifierMap);
  }

  /**
   * Get OperationIdentifierMap from OperationIdentifier
   * @param operationIdentifier operationIdentifier
   * @return OperationIdentifierMap
   */
  private static Map getOperationIdentifierMap(OperationIdentifier operationIdentifier) {
    Map operationIdentifierMap = new Map();
    // fill map
    putUnsignedIntegerToMap(operationIdentifierMap, Constants.INDEX, operationIdentifier.getIndex());
    putUnsignedIntegerToMap(operationIdentifierMap, Constants.NETWORK_INDEX, operationIdentifier.getNetworkIndex());
    return operationIdentifierMap;
  }


  // TODO get rid of this method
  private static DataItem objectToDataItem(Object metadata) {
    Map map = new Map();
    HashMap<String, Object> metadataMap = (HashMap<String, Object>) metadata;

    metadataMap.forEach((key, value) -> {
      if (value instanceof String valueString) {
        map.put(new UnicodeString(key), new UnicodeString(valueString));
      } else {
        map.put(new UnicodeString(key), objectToDataItem(value));
      }
    });

    return map;
  }

  private static void putStringDataItemToMap(Map map,String key,String value) {
    Optional.ofNullable(value)
        .ifPresent(o -> map.put(new UnicodeString(key), new UnicodeString(value)));
  }

  private static void putUnsignedIntegerToMap(Map map, String key,
      Integer value) {
    Optional.ofNullable(value)
        .ifPresent(votingNonce -> map.put(key(key), new UnsignedInteger(value)));
  }

  private static void putUnsignedIntegerToMap(Map map, String key, Long value) {
    Optional.ofNullable(value)
        .ifPresent(votingNonce -> map.put(key(key), new UnsignedInteger(value)));
  }

}
