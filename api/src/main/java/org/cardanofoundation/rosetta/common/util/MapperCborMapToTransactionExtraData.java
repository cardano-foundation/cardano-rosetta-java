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
import java.util.Optional;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.jetbrains.annotations.NotNull;
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

public class MapperCborMapToTransactionExtraData {

  public static TransactionExtraData convertCborMapToTransactionExtraData(Map map) {
    //separator
    String transactionMetadataHex = getTransactionMetadataHexFromMap(map);

    List<Operation> operations = new ArrayList<>();
    List<DataItem> operationsListMap = ((Array) map.get(
        new UnicodeString(Constants.OPERATIONS))).getDataItems();
    operationsListMap.forEach(oDataItem -> {
      Map operationMap = (Map) oDataItem;
      Operation operation = cborMapToOperation(operationMap);
      operations.add(operation);
    });
    return new TransactionExtraData(operations, transactionMetadataHex);
  }

  @NotNull
  private static Operation cborMapToOperation(Map operationMap) {
    Operation operation = new Operation();
    Optional.ofNullable(operationMap.get(new UnicodeString(Constants.OPERATION_IDENTIFIER)))
        .ifPresent(o -> {
          Map operationIdentifierMap = (Map) operationMap.get(
              new UnicodeString(Constants.OPERATION_IDENTIFIER));
          OperationIdentifier operationIdentifier = new OperationIdentifier();
          addOperationIdentifier(operationIdentifierMap, operationIdentifier);
          operation.setOperationIdentifier(operationIdentifier);
        });
    Optional.ofNullable(operationMap.get(new UnicodeString(Constants.RELATED_OPERATION)))
        .ifPresent(o -> {
          List<OperationIdentifier> relatedOperations = new ArrayList<>();
          List<DataItem> relatedOperationsDI = ((Array) operationMap.get(
              new UnicodeString(Constants.RELATED_OPERATION))).getDataItems();
          relatedOperationsDI.forEach(rDI -> {
            Map operationIdentifierMap2 = (Map) rDI;

            OperationIdentifier operationIdentifier2 = new OperationIdentifier();
            addOperationIdentifier(operationIdentifierMap2, operationIdentifier2);
            relatedOperations.add(operationIdentifier2);
          });
          operation.setRelatedOperations(relatedOperations);
        });

    addTypeToOperation(operationMap, operation, Constants.TYPE);
    addStatusToOperation(operationMap, operation, Constants.STATUS);
    addAccountToOperation(operationMap, operation);
    addAmount(operationMap, operation);
    addCoinChange(operationMap, operation);
    addMetadata(operationMap, operation);
    return operation;
  }

  private static void addMetadata(Map operationMap, Operation operation) {
    Optional.ofNullable(operationMap.get(new UnicodeString(Constants.METADATA))).ifPresent(o -> {
      Map metadataMap = (Map) operationMap.get(new UnicodeString(Constants.METADATA));
      OperationMetadata operationMetadata = new OperationMetadata();
      addWithDrawalAmount(metadataMap, operationMetadata);
      addDepositAmount(metadataMap, operationMetadata);
      addRefundAmount(metadataMap, operationMetadata);
      addStakingCredential(metadataMap, operationMetadata);
      addPoolKeyHash(metadataMap, operationMetadata);
      addEpoch(metadataMap, operationMetadata);
      addTokenBundle(metadataMap, operationMetadata);
      addPoolRegistrationCert(metadataMap, operationMetadata);
      addPoolRegistrationParams(metadataMap, operationMetadata);
      addVoteRegistrationMetadata(metadataMap, operationMetadata);
      operation.setMetadata(operationMetadata);
    });
  }

  private static void addVoteRegistrationMetadata(Map metadataMap,
      OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA)))
        .ifPresent(voteregMetadata -> {
          VoteRegistrationMetadata voteRegistrationMetadata = new VoteRegistrationMetadata();
          Map voteRegistrationMetadataMap = (Map) metadataMap.get(
              new UnicodeString(Constants.VOTEREGISTRATIONMETADATA));
          Optional.ofNullable(
                  voteRegistrationMetadataMap.get(new UnicodeString(Constants.STAKE_KEY)))
              .ifPresent(stakeKey -> {
                Map stakeKeyMap = (Map) voteRegistrationMetadataMap.get(
                    new UnicodeString(Constants.STAKE_KEY));
                PublicKey publicKey1 = getPublicKeyFromMap(stakeKeyMap);
                voteRegistrationMetadata.setStakeKey(publicKey1);
              });
          Optional.ofNullable(
                  voteRegistrationMetadataMap.get(new UnicodeString(Constants.VOTING_KEY)))
              .ifPresent(votingKey -> {
                Map votingKeyMap = (Map) voteRegistrationMetadataMap.get(
                    new UnicodeString(Constants.VOTING_KEY));
                PublicKey publicKey2 = getPublicKeyFromMap(votingKeyMap);
                voteRegistrationMetadata.setVotingkey(publicKey2);
              });
          Optional.ofNullable(
                  voteRegistrationMetadataMap.get(new UnicodeString(Constants.REWARD_ADDRESS)))
              .ifPresent(rewardAddress -> {
                String rewardAddress2 = ((UnicodeString) voteRegistrationMetadataMap.get(
                    new UnicodeString(Constants.REWARD_ADDRESS))).getString();
                voteRegistrationMetadata.setRewardAddress(rewardAddress2);
              });
          Optional.ofNullable(
                  voteRegistrationMetadataMap.get(new UnicodeString(Constants.VOTING_SIGNATURE)))
              .ifPresent(votingSignature -> {
                String votingSignatureStr = ((UnicodeString) voteRegistrationMetadataMap.get(
                    new UnicodeString(Constants.VOTING_SIGNATURE))).getString();
                voteRegistrationMetadata.setVotingSignature(votingSignatureStr);
              });
          Optional.ofNullable(
                  voteRegistrationMetadataMap.get(new UnicodeString(Constants.VOTING_NONCE)))
              .ifPresent(votingNonce -> {
                int votingNonceInt = ((UnsignedInteger) voteRegistrationMetadataMap.get(
                    new UnicodeString(Constants.VOTING_NONCE))).getValue().intValue();
                voteRegistrationMetadata.setVotingNonce(votingNonceInt);
              });
          operationMetadata.setVoteRegistrationMetadata(voteRegistrationMetadata);
        });
  }

  private static void addPoolRegistrationParams(Map metadataMap,
      OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.POOLREGISTRATIONPARAMS)))
        .ifPresent(o -> {
          Map poolRegistrationParamsMap = (Map) metadataMap.get(
              new UnicodeString(Constants.POOLREGISTRATIONPARAMS));
          PoolRegistrationParams poolRegistrationParams = new PoolRegistrationParams();
          addVrfKeyHash(poolRegistrationParamsMap, poolRegistrationParams);
          addRewardAddress(poolRegistrationParamsMap, poolRegistrationParams);
          addPledge(poolRegistrationParamsMap, poolRegistrationParams);
          addCost(poolRegistrationParamsMap, poolRegistrationParams);
          addPoolOwners(poolRegistrationParamsMap, poolRegistrationParams);
          addRelays(poolRegistrationParamsMap, poolRegistrationParams);
          addMargins(poolRegistrationParamsMap, poolRegistrationParams);
          if (poolRegistrationParamsMap.get(new UnicodeString(Constants.MARGIN_PERCENTAGE))
              != null) {
            String marginPercentage = ((UnicodeString) poolRegistrationParamsMap.get(
                new UnicodeString(Constants.MARGIN_PERCENTAGE))).getString();
            poolRegistrationParams.setMarginPercentage(marginPercentage);
          }
          if (poolRegistrationParamsMap.get(new UnicodeString(Constants.POOLMETADATA)) != null) {
            PoolMetadata poolMetadata = new PoolMetadata();
            Map poolMetadataMap = (Map) poolRegistrationParamsMap.get(
                new UnicodeString(Constants.POOLMETADATA));
            if (poolMetadataMap.get(new UnicodeString(Constants.URL)) != null) {
              String url = ((UnicodeString) poolMetadataMap.get(
                  new UnicodeString(Constants.URL))).getString();
              poolMetadata.setUrl(url);
            }
            if (poolMetadataMap.get(new UnicodeString(Constants.HASH)) != null) {
              String hash = ((UnicodeString) poolMetadataMap.get(
                  new UnicodeString(Constants.HASH))).getString();
              poolMetadata.setHash(hash);
            }
            poolRegistrationParams.setPoolMetadata(poolMetadata);
          }
          operationMetadata.setPoolRegistrationParams(poolRegistrationParams);
        });
  }

  private static void addMargins(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.MARGIN)))
        .ifPresent(o -> {
          Map marginMap = (Map) poolRegistrationParamsMap.get(new UnicodeString(Constants.MARGIN));
          PoolMargin poolMargin = new PoolMargin();
          Optional.ofNullable(marginMap.get(new UnicodeString(Constants.NUMERATOR)))
              .ifPresent(o1 -> {
                String numerator = ((UnicodeString) marginMap.get(
                    new UnicodeString(Constants.NUMERATOR))).getString();
                poolMargin.setNumerator(numerator);
              });
          Optional.ofNullable(marginMap.get(new UnicodeString(Constants.DENOMINATOR)))
              .ifPresent(o1 -> {
                String denominator = ((UnicodeString) marginMap.get(
                    new UnicodeString(Constants.DENOMINATOR))).getString();
                poolMargin.setDenominator(denominator);
              });
          poolRegistrationParams.setMargin(poolMargin);
        });
  }

  private static void addRelays(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(
            ((Array) poolRegistrationParamsMap.get(new UnicodeString(Constants.RELAYS))).getDataItems())
        .ifPresent(o -> {
          List<Relay> relayList = new ArrayList<>();
          List<DataItem> relaysArray = ((Array) poolRegistrationParamsMap.get(
              new UnicodeString(Constants.RELAYS))).getDataItems();
          relaysArray.forEach(rA -> {
            Map rAMap = (Map) rA;
            Relay relay = new Relay();
            addRelayType(rAMap, relay);
            addIpv4(rAMap, relay);
            addIpv6(rAMap, relay);
            if (rAMap.get(new UnicodeString(Constants.DNSNAME)) != null) {
              String dnsName = ((UnicodeString) rAMap.get(
                  new UnicodeString(Constants.DNSNAME))).getString();
              relay.setDnsName(dnsName);
            }
            relayList.add(relay);
          });
          poolRegistrationParams.setRelays(relayList);
        });
  }

  private static void addIpv6(Map rAMap, Relay relay) {
    Optional.ofNullable(rAMap.get(new UnicodeString(Constants.IPV6))).ifPresent(o -> {
      String ipv6 = ((UnicodeString) rAMap.get(new UnicodeString(Constants.IPV6))).getString();
      relay.setIpv6(ipv6);
    });
  }

  private static void addIpv4(Map rAMap, Relay relay) {
    Optional.ofNullable(rAMap.get(new UnicodeString(Constants.IPV4))).ifPresent(o -> {
      String ipv4 = ((UnicodeString) rAMap.get(new UnicodeString(Constants.IPV4))).getString();
      relay.setIpv4(ipv4);
    });
  }

  private static void addRelayType(Map rAMap, Relay relay) {
    Optional.ofNullable(rAMap.get(new UnicodeString(Constants.TYPE))).ifPresent(o -> {
      String typeR = ((UnicodeString) rAMap.get(new UnicodeString(Constants.TYPE))).getString();
      relay.setType(typeR);
    });
  }

  private static void addPoolOwners(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.POOLOWNERS)))
        .ifPresent(o -> {
          List<String> stringList = new ArrayList<>();
          List<DataItem> poolOwners = ((Array) poolRegistrationParamsMap.get(
              new UnicodeString(Constants.POOLOWNERS))).getDataItems();
          poolOwners.forEach(p -> {
            if (p != null) {
              stringList.add(((UnicodeString) p).getString());
            }
          });
          poolRegistrationParams.setPoolOwners(stringList);
        });
  }

  private static void addCost(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.COST)))
        .ifPresent(o -> {
          String cost = ((UnicodeString) poolRegistrationParamsMap.get(
              new UnicodeString(Constants.COST))).getString();
          poolRegistrationParams.setCost(cost);
        });
  }

  private static void addPledge(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.PLEDGE)))
        .ifPresent(o -> {
          String pledge = ((UnicodeString) poolRegistrationParamsMap.get(
              new UnicodeString(Constants.PLEDGE))).getString();
          poolRegistrationParams.setPledge(pledge);
        });
  }

  private static void addRewardAddress(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.REWARD_ADDRESS)))
        .ifPresent(o -> {
          String rewardAddress = ((UnicodeString) poolRegistrationParamsMap.get(
              new UnicodeString(Constants.REWARD_ADDRESS))).getString();
          poolRegistrationParams.setRewardAddress(rewardAddress);
        });
  }

  public static void addVrfKeyHash(Map poolRegistrationParamsMap,
      PoolRegistrationParams poolRegistrationParams) {
    Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.VRFKEYHASH)))
        .ifPresent(o -> {
          String vrfKeyHash = ((UnicodeString) poolRegistrationParamsMap.get(
              new UnicodeString(Constants.VRFKEYHASH))).getString();
          poolRegistrationParams.setVrfKeyHash(vrfKeyHash);
        });
  }

  private static void addPoolRegistrationCert(Map metadataMap,
      OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.POOLREGISTRATIONCERT)))
        .ifPresent(o -> {
          String poolRegistrationCert = ((UnicodeString) metadataMap.get(
              new UnicodeString(Constants.POOLREGISTRATIONCERT))).getString();
          operationMetadata.setPoolRegistrationCert(poolRegistrationCert);
        });
  }

  private static void addTokenBundle(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.TOKENBUNDLE))).ifPresent(o -> {
      List<DataItem> tokenBundleArray = ((Array) metadataMap.get(
          new UnicodeString(Constants.TOKENBUNDLE))).getDataItems();
      List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
      tokenBundleArray.forEach(t -> {
        Map tokenBundleMap = (Map) t;
        TokenBundleItem tokenBundleItem = new TokenBundleItem();
        if (tokenBundleMap.get(new UnicodeString(Constants.POLICYID)) != null) {
          String policyIdT = ((UnicodeString) tokenBundleMap.get(
              new UnicodeString(Constants.POLICYID))).getString();
          tokenBundleItem.setPolicyId(policyIdT);
        }

        List<Amount> tokenAList = new ArrayList<>();
        if (tokenBundleMap.get(new UnicodeString(Constants.TOKENS)) != null) {
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
    });
  }

  private static void addEpoch(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.EPOCH))).ifPresent(o -> {
      BigInteger value = ((UnsignedInteger) metadataMap.get(
          new UnicodeString(Constants.EPOCH))).getValue();
      operationMetadata.setEpoch(value.intValue());
    });
  }

  private static void addPoolKeyHash(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.POOL_KEY_HASH)))
        .ifPresent(o -> {
          operationMetadata.setPoolKeyHash(((UnicodeString) o).getString());
        });
  }

  private static void addStakingCredential(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.STAKING_CREDENTIAL)))
        .ifPresent(o -> {

          Map stakingCredentialMap = (Map) o;
          operationMetadata.setStakingCredential(getPublicKeyFromMap(stakingCredentialMap));
        });
  }

  private static void addRefundAmount(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.REFUNDAMOUNT))).ifPresent(o -> {
      Map refundAmountMap = (Map) o;
      operationMetadata.setRefundAmount(getAmountFromMap(refundAmountMap));

    });
  }

  private static void addDepositAmount(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.DEPOSITAMOUNT)))
        .ifPresent(o -> {
          Map depositAmountMap = (Map) o;
          Amount amountD = getAmountFromMap(depositAmountMap);
          operationMetadata.setDepositAmount(amountD);
        });
  }

  private static void addWithDrawalAmount(Map metadataMap, OperationMetadata operationMetadata) {
    Optional.ofNullable(metadataMap.get(new UnicodeString(Constants.WITHDRAWALAMOUNT)))
        .ifPresent(o -> {
          Map withdrawalAmountMap = (Map) o;
          Amount amountW = getAmountFromMap(withdrawalAmountMap);
          operationMetadata.setWithdrawalAmount(amountW);
        });
  }

  private static void addCoinChange(Map operationMap, Operation operation) {
    Optional.ofNullable(operationMap.get(new UnicodeString(Constants.COIN_CHANGE))).ifPresent(o -> {
      Map coinChangeMap = (Map) o;
      CoinChange coinChange = new CoinChange();
      Optional.ofNullable(coinChangeMap.get(new UnicodeString(Constants.COIN_ACTION)))
          .ifPresent(o1 -> {
            String coinAction = ((UnicodeString) o1).getString();
            coinChange.setCoinAction(CoinAction.fromValue(coinAction));
          });
      Optional.ofNullable(coinChangeMap.get(new UnicodeString(Constants.COIN_IDENTIFIER)))
          .ifPresent(o1 -> {
            CoinIdentifier coinIdentifier = new CoinIdentifier();
            Map coinIdentifierMap = (Map) o1;
            Optional.ofNullable(coinIdentifierMap.get(new UnicodeString(Constants.IDENTIFIER)))
                .ifPresent(o2 -> {
                  String identifier = ((UnicodeString) o2).getString();
                  coinIdentifier.setIdentifier(identifier);
                });
            coinChange.setCoinIdentifier(coinIdentifier);
          });
      operation.setCoinChange(coinChange);
    });
  }

  private static void addAmount(Map operationMap, Operation operation) {
    Optional.ofNullable(operationMap.get(new UnicodeString(Constants.AMOUNT))).ifPresent(o -> {
      Map amountMap = (Map) o;
      Amount amount = getAmountFromMap(amountMap);
      operation.setAmount(amount);
    });
  }

  private static void addAccountToOperation(Map operationMap, Operation operation) {
    Optional.ofNullable(operationMap.get(new UnicodeString(Constants.ACCOUNT))).ifPresent(o -> {
      Map accountIdentifierMap = (Map) o;
      AccountIdentifier accountIdentifier = new AccountIdentifier();
      addAddress(accountIdentifierMap, accountIdentifier);
      addSubAccount(accountIdentifierMap, accountIdentifier);
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
    });
  }

  private static void addSubAccount(Map accountIdentifierMap, AccountIdentifier accountIdentifier) {
    Optional.ofNullable(accountIdentifierMap.get(new UnicodeString(Constants.SUB_ACCOUNT)))
        .ifPresent(o -> {
          Map subAccountIdentifierMap = (Map) o;
          SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
          Optional.ofNullable(subAccountIdentifierMap.get(new UnicodeString(Constants.ADDRESS)))
              .ifPresent(o1 -> {
                String addressSub = ((UnicodeString) o1).getString();
                subAccountIdentifier.setAddress(addressSub);
              });
          accountIdentifier.setSubAccount(subAccountIdentifier);
        });
  }

  private static void addAddress(Map accountIdentifierMap, AccountIdentifier accountIdentifier) {
    Optional.ofNullable(accountIdentifierMap.get(new UnicodeString(Constants.ADDRESS)))
        .ifPresent(o -> {
          String address = ((UnicodeString) o).getString();
          accountIdentifier.setAddress(address);
        });
  }

  private static void addStatusToOperation(Map operationMap, Operation operation, String dataName) {
    Optional.ofNullable(operationMap.get(new UnicodeString(dataName))).ifPresent(o -> {
      String status = ((UnicodeString) o).getString();
      operation.setStatus(status);
    });
  }

  private static void addTypeToOperation(Map operationMap, Operation operation, String dataName) {
    Optional.ofNullable(operationMap.get(new UnicodeString(dataName))).ifPresent(o -> {
      String status = ((UnicodeString) o).getString();
      operation.setType(status);
    });
  }

  private static void addOperationIdentifier(Map operationIdentifierMap,
      OperationIdentifier operationIdentifier) {
    Optional.ofNullable(operationIdentifierMap.get(new UnicodeString(Constants.INDEX)))
        .ifPresent(o -> {
          operationIdentifier.setIndex(((UnsignedInteger) o).getValue().longValue());
        });
    Optional.ofNullable(operationIdentifierMap.get(new UnicodeString(Constants.NETWORK_INDEX)))
        .ifPresent(o -> {
          operationIdentifier.setNetworkIndex(((UnsignedInteger) o).getValue().longValue());
        });
  }

  private static String getTransactionMetadataHexFromMap(Map map) {
    DataItem transactionMetadataHex = map.get(new UnicodeString(Constants.TRANSACTIONMETADATAHEX));
    return transactionMetadataHex == null ? ""
        : ((UnicodeString) transactionMetadataHex).getString();
  }

  private static PublicKey getPublicKeyFromMap(Map stakingCredentialMap) {
    PublicKey publicKey = new PublicKey();
    Optional.ofNullable(stakingCredentialMap.get(new UnicodeString(Constants.HEX_BYTES)))
        .ifPresent(o -> {
          publicKey.setHexBytes(((UnicodeString) o).getString());
        });
    Optional.ofNullable(stakingCredentialMap.get(new UnicodeString(Constants.CURVE_TYPE)))
        .ifPresent(o -> {
          publicKey.setCurveType(CurveType.fromValue(((UnicodeString) o).getString()));
        });
    return publicKey;
  }

  private static Amount getAmountFromMap(Map amountMap) {
    Amount amount = new Amount();
    Optional.ofNullable(amountMap).ifPresent(am -> {
      Optional.ofNullable(am.get(new UnicodeString(Constants.VALUE))).ifPresent(o -> {
        amount.setValue(((UnicodeString) o).getString());
      });
      Optional.ofNullable(am.get(new UnicodeString(Constants.METADATA))).ifPresent(o -> {
        Map metadataAm = (Map) o;
        amount.setMetadata(metadataAm);
      });
      getCurrencyFromMap(amountMap, amount);
    });
    return amount;
  }

  private static void getCurrencyFromMap(Map amountMap, Amount amount) {
    Optional.ofNullable(amountMap.get(new UnicodeString(Constants.CURRENCY))).ifPresent(o -> {
      Map currencyMap = (Map) o;
      Currency currency = new Currency();
      Optional.ofNullable(currencyMap.get(new UnicodeString(Constants.SYMBOL))).ifPresent(o1 -> {
        currency.setSymbol(((UnicodeString) o1).getString());
      });
      Optional.ofNullable(currencyMap.get(new UnicodeString(Constants.DECIMALS))).ifPresent(o1 -> {
        currency.setDecimals(((UnsignedInteger) o1).getValue().intValue());
      });
      Optional.ofNullable(currencyMap.get(new UnicodeString(Constants.METADATA))).ifPresent(o1 -> {
        CurrencyMetadata metadata = new CurrencyMetadata();
        Map addedMetadataMap = (Map) o1;
        Optional.ofNullable(addedMetadataMap.get(new UnicodeString(Constants.POLICYID)))
            .ifPresent(o2 -> {
              metadata.setPolicyId(((UnicodeString) o2).getString());
            });
        currency.setMetadata(metadata);
      });
      amount.setCurrency(currency);
    });
  }
}
