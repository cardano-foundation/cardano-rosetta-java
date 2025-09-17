package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.model.*;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.cardanofoundation.rosetta.common.util.Constants.*;
import static org.cardanofoundation.rosetta.common.util.Formatters.key;

public class CborMapToOperation {

    private CborMapToOperation() {}

    public static Operation cborMapToOperation(Map operationMap) {
        Operation operation = new Operation();

        addOperationIdentifierToOperation(operation, operationMap);
        addRelatedOperationToOperation(operation, operationMap);

        addTypeToOperation(operationMap, operation);
        addStatusToOperation(operationMap, operation);
        addAccountIdentifierToOperation(operationMap, operation);

        addAmount(operationMap, operation);
        addCoinChange(operationMap, operation);
        addMetadata(operationMap, operation);

        return operation;
    }

    /**
     * Adding an OperationIdentifier object to the Operation object populated from the cbor MAP if not null.
     * Accessed through {@value Constants#OPERATION_IDENTIFIER}
     * @param operation The Operation object to fill
     * @param operationMap The map containing the operation identifier field
     */
    private static void addOperationIdentifierToOperation(Operation operation, Map operationMap) {
        Optional.ofNullable(operationMap.get(key(Constants.OPERATION_IDENTIFIER))).ifPresent(map -> {
            Map operationIdentifierMap = (Map) map;
            OperationIdentifier operationIdentifier = new OperationIdentifier();
            fillOperationIdentifier(operationIdentifierMap, operationIdentifier);
            operation.setOperationIdentifier(operationIdentifier);
        });
    }

    /**
     * Adding a List of RelatedOperations to operation populated from cbor Map if not null.
     * Accessed through {@value Constants#RELATED_OPERATION}
     * @param operation The Operation object to fill
     * @param operationMap The map containing the related operations field
     */
    private static void addRelatedOperationToOperation(Operation operation, Map operationMap) {
        Optional.ofNullable(operationMap.get(new UnicodeString(Constants.RELATED_OPERATION)))
                .ifPresent(o -> {
                    List<OperationIdentifier> relatedOperations = new ArrayList<>();
                    List<DataItem> relatedOperationsDataItems = ((Array) o).getDataItems();
                    relatedOperationsDataItems.forEach(rDI -> {
                        Map relatedOperationMap = (Map) rDI;

                        OperationIdentifier relatedOperationIdentifier = new OperationIdentifier();
                        fillOperationIdentifier(relatedOperationMap, relatedOperationIdentifier);
                        relatedOperations.add(relatedOperationIdentifier);
                    });
                    operation.setRelatedOperations(relatedOperations);
                });
    }

    /**
     * Adding Index and NetworkIndex to OperationIdentifier from cbor MAP if not null.
     * Access through {@value Constants#INDEX} and {@value Constants#NETWORK_INDEX}
     * @param operationIdentifierMap The map containing the operation identifier field
     * @param operationIdentifier The OperationIdentifier object to fill
     */
    private static void fillOperationIdentifier(Map operationIdentifierMap,
                                                OperationIdentifier operationIdentifier) {
        Optional.ofNullable(operationIdentifierMap.get(key(Constants.INDEX)))
                .ifPresent(index -> operationIdentifier.setIndex(((UnsignedInteger) index).getValue().longValue()));

        Optional.ofNullable(operationIdentifierMap.get(key(Constants.NETWORK_INDEX)))
                .ifPresent(index -> operationIdentifier.setNetworkIndex(((UnsignedInteger) index).getValue().longValue()));
    }

    /**
     * Filling the type field of the Operation object from the cbor MAP  if not null.
     * Accessed through {@value Constants#TYPE}
     * @param operationMap The map containing the type field
     * @param operation The Operation object to fill
     */
    private static void addTypeToOperation(Map operationMap, Operation operation) {
        Optional.ofNullable(operationMap.get(key(Constants.TYPE))).ifPresent(o -> {
            String status = ((UnicodeString) o).getString();
            operation.setType(status);
        });
    }

    /**
     * Filling the status field of the Operation object from the cbor MAP  if not null.
     * Accessed through {@value Constants#STATUS}
     * @param operationMap The map containing the status field
     * @param operation The Operation object to fill
     */
    private static void addStatusToOperation(Map operationMap, Operation operation) {
        Optional.ofNullable(operationMap.get(key(Constants.STATUS))).ifPresent(o -> {
            String status = ((UnicodeString) o).getString();
            operation.setStatus(status);
        });
    }

    /**
     * Adding an AccountIdentifier object populated from the cbor MAP  if not null.
     * Accessed through {@value Constants#ACCOUNT}
     * @param operationMap The map containing the account field
     * @param operation The Operation object to fill
     */
    private static void addAccountIdentifierToOperation(Map operationMap, Operation operation) {
        Optional.ofNullable(operationMap.get(key(Constants.ACCOUNT))).ifPresent(o -> {
            Map accountIdentifierMap = (Map) o;
            AccountIdentifier accountIdentifier = new AccountIdentifier();
            // fill object
            addAddressToAccountIdentifier(accountIdentifierMap, accountIdentifier);
            addSubAccountToAccountIdentifier(accountIdentifierMap, accountIdentifier);
            addMetaDataToAccountIdentifier(accountIdentifierMap, accountIdentifier);
            // write to Operation
            operation.setAccount(accountIdentifier);
        });
    }

    /**
     * Adding Metadata to the AccountIdentifier from the cbor MAP  if not null.
     * Accessed through {@value Constants#METADATA}
     * @param accountIdentifierMap The map containing the amount field
     * @param accountIdentifier The Operation object to fill
     */
    private static void addMetaDataToAccountIdentifier(Map accountIdentifierMap, AccountIdentifier accountIdentifier) {
        Optional.ofNullable(accountIdentifierMap.get(key(Constants.METADATA)))
                .ifPresent(o -> {
                    Map accountIdentifierMetadataMap = (Map) o;
                    AccountIdentifierMetadata accountIdentifierMetadata = new AccountIdentifierMetadata();
                    // fill object
                    addChainCodeToAccountIdentifierMetadata(accountIdentifierMetadataMap, accountIdentifierMetadata);
                    // write to AccountIdentifier
                    accountIdentifier.setMetadata(accountIdentifierMetadata);
                });
    }

    /**
     * Filling the ChainCode field of the AccountIdentifierMetadata object from the cbor MAP  if not null.
     * Accessed through {@value Constants#CHAIN_CODE}
     * @param accountIdentifierMetadataMap The map containing the chain code field
     * @param accountIdentifierMetadata The AccountIdentifierMetadata object to fill
     */
    private static void addChainCodeToAccountIdentifierMetadata(Map accountIdentifierMetadataMap,
                                                                AccountIdentifierMetadata accountIdentifierMetadata) {
        Optional.ofNullable(accountIdentifierMetadataMap.get(key(Constants.CHAIN_CODE)))
                .ifPresent(o -> {
                    String chainCode = ((UnicodeString) o).getString();

                    accountIdentifierMetadata.setChainCode(chainCode);
                });
    }

    /**
     * Filling the address field of the AccountIdentifier object from the cbor MAP  if not null.
     * Accessed through {@value Constants#ADDRESS}
     * @param accountIdentifierMap The map containing the address field
     * @param accountIdentifier The AccountIdentifier object to fill
     */
    private static void addAddressToAccountIdentifier(Map accountIdentifierMap, AccountIdentifier accountIdentifier) {
        Optional.ofNullable(accountIdentifierMap.get(key(Constants.ADDRESS)))
                .ifPresent(o -> {
                    String address = ((UnicodeString) o).getString();
                    accountIdentifier.setAddress(address);
                });
    }

    /**
     * Adding a Sub accountIdentifier to the AccountIdentifier if not null.
     * Accessed through {@value Constants#SUB_ACCOUNT}
     * @param accountIdentifierMap The map containing the subAccount field
     * @param accountIdentifier The AccountIdentifier object to fill
     */
    private static void addSubAccountToAccountIdentifier(Map accountIdentifierMap, AccountIdentifier accountIdentifier) {
        Optional.ofNullable(accountIdentifierMap.get(key(Constants.SUB_ACCOUNT)))
                .ifPresent(o -> {
                    Map subAccountIdentifierMap = (Map) o;
                    SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
                    // fill object
                    addAddressToSubAccountIdentifier(subAccountIdentifierMap, subAccountIdentifier);
                    // write to AccountIdentifier
                    accountIdentifier.setSubAccount(subAccountIdentifier);
                });
    }

    /**
     * Filling the address field of the SubAccountIdentifier object from the cbor MAP  if not null.
     * Accessed through {@value Constants#ADDRESS}
     * @param subAccountIdentifierMap The map containing the address field
     * @param subAccountIdentifier The SubAccountIdentifier object to fill
     */
    private static void addAddressToSubAccountIdentifier(Map subAccountIdentifierMap,
                                                         SubAccountIdentifier subAccountIdentifier) {
        Optional.ofNullable(subAccountIdentifierMap.get(key(Constants.ADDRESS)))
                .ifPresent(o1 -> {
                    String addressSub = ((UnicodeString) o1).getString();
                    subAccountIdentifier.setAddress(addressSub);
                });
    }

    /**
     * Add an Amount object to the Operation object from the cbor MAP  if not null.
     * Accessed through {@value Constants#AMOUNT}
     * @param operationMap The map containing the amount field
     * @param operation The Operation object to fill
     */
    private static void addAmount(Map operationMap, Operation operation) {
        Optional.ofNullable(operationMap.get(key(Constants.AMOUNT))).ifPresent(o -> {
            Map amountMap = (Map) o;
            Amount amount = getAmountFromMap(amountMap);
            operation.setAmount(amount);
        });
    }

    /**
     * Returns an Amount object populated from the cbor MAP  if not null.
     * Containing the value and metadata fields. Accessed through {@value Constants#VALUE} and {@value Constants#METADATA}
     * @param amountMap The map containing the amount field
     * @return The populated Amount object
     */
    private static Amount getAmountFromMap(Map amountMap) {
        Amount amount = new Amount();

        Optional.ofNullable(amountMap).ifPresent(am -> {
            addValueToAmount(am, amount);
            addMetadataToAmount(am, amount);
            addCurrencyToAmount(am, amount);
        });

        return amount;
    }

    /**
     * Add metadata to Amount object. The metadata object is accessed through {@value Constants#METADATA}.
     * @param am The map containing the metadata field
     * @param amount The Amount object to fill
     */
    private static void addMetadataToAmount(Map am, Amount amount) {
        Optional.ofNullable(am.get(key(Constants.METADATA))).ifPresent(o -> {
            Map metadataAm = (Map) o;
            amount.setMetadata(metadataAm);
        });
    }

    /**
     * Add value to Amount object. The value is accessed through {@value Constants#VALUE}.
     * @param am The map containing the value field
     * @param amount The Amount object to fill
     */
    private static void addValueToAmount(Map am, Amount amount) {
        Optional.ofNullable(am.get(key(Constants.VALUE))).ifPresent(o -> amount.setValue(((UnicodeString) o).getString()));
    }


    /**
     * Add currency to the Amount object from the cbor MAP  if not null.
     * Accessed through {@value Constants#CURRENCY}
     * @param amountMap The map containing the amount field
     * @param amount The Amount object to fill
     */
    private static void addCurrencyToAmount(Map amountMap, Amount amount) {
        Optional.ofNullable(amountMap.get(key(Constants.CURRENCY))).ifPresent(o -> {
            Map currencyMap = (Map) o;
            CurrencyResponse currency = getCurrencyMap(currencyMap);
            addMetadataToCurrency(currencyMap, currency);
            amount.setCurrency(currency);
        });
    }

    /**
     * Add Metadata to Currency populated from cbor MAP if not null. Metadata contains the policyID accessed through {@value Constants#POLICYID}
     * @param currencyMap The map containing the currency field
     * @param currency The Currency object to fill
     */
    private static void addMetadataToCurrency(Map currencyMap, CurrencyResponse currency) {
        Optional.ofNullable(currencyMap.get(key(Constants.POLICYID))).ifPresent(o -> {
            String policyId = ((UnicodeString) o).getString();
            CurrencyMetadataResponse metadata = CurrencyMetadataResponse.builder()
                .policyId(policyId)
                .build();

            currency.setMetadata(metadata);
        });
    }


    /**
     * Returns a Currency object populated from the cbor MAP  if not null.
     * Containing the symbol and decimals fields. Accessed through {@value Constants#SYMBOL} and {@value Constants#DECIMALS}
     * @param currencyMap The map containing the currency field
     * @return The populated Currency object
     */
    private static CurrencyResponse getCurrencyMap(Map currencyMap) {
        CurrencyResponse currency = new CurrencyResponse();
        Optional.ofNullable(currencyMap.get(key(Constants.SYMBOL))).ifPresent(o1 -> {
            currency.setSymbol(((UnicodeString) o1).getString());
        });

        Optional.ofNullable(currencyMap.get(key(Constants.DECIMALS))).ifPresent(o1 -> {
            currency.setDecimals(((UnsignedInteger) o1).getValue().intValue());
        });

        return currency;
    }

    /**
     * Add Coin change to Operation. The Coin change objects is accessed through {@value Constants#COIN_CHANGE}.
     * The Coin change object contains a CoinAction and a CoinIdentifier.
     * @param operationMap The map containing the Coin change field
     * @param operation The Operation object to fill
     */
    private static void addCoinChange(Map operationMap, Operation operation) {
        Optional.ofNullable(operationMap.get(key(Constants.COIN_CHANGE))).ifPresent(o -> {
            Map coinChangeMap = (Map) o;
            CoinChange coinChange = new CoinChange();
            addCoinActionToCoinChange(coinChangeMap, coinChange);
            addCoinIdentifierToCoinChange(coinChangeMap, coinChange);
            operation.setCoinChange(coinChange);
        });
    }

    /**
     * Add CoinIdentifier to CoinChange object. The coin identifier object is accessed through {@value Constants#COIN_IDENTIFIER}.
     * @param coinChangeMap The map containing the coin identifier field
     * @param coinChange The CoinChange object to fill
     */
    private static void addCoinIdentifierToCoinChange(Map coinChangeMap, CoinChange coinChange) {
        Optional.ofNullable(coinChangeMap.get(key(Constants.COIN_IDENTIFIER)))
                .ifPresent(o -> {
                    CoinIdentifier coinIdentifier = new CoinIdentifier();
                    Map coinIdentifierMap = (Map) o;
                    addIdentifierToCoinIdentifier(coinIdentifierMap, coinIdentifier);
                    coinChange.setCoinIdentifier(coinIdentifier);
                });
    }

    /**
     * Add Identifier to CoinIdentifier object. The identifier is accessed through {@value Constants#IDENTIFIER}.
     * @param coinIdentifierMap The map containing the identifier field
     * @param coinIdentifier The CoinIdentifier object to fill
     */
    private static void addIdentifierToCoinIdentifier(Map coinIdentifierMap, CoinIdentifier coinIdentifier) {
        Optional.ofNullable(coinIdentifierMap.get(key(Constants.IDENTIFIER)))
                .ifPresent(o -> {
                    String identifier = ((UnicodeString) o).getString();
                    coinIdentifier.setIdentifier(identifier);
                });
    }

    /**
     * Add CoinAction to CoinChange object. The coin action is accessed through {@value Constants#COIN_ACTION}.
     * @param coinChangeMap The map containing the coin action field
     * @param coinChange The CoinChange object to fill
     */
    private static void addCoinActionToCoinChange(Map coinChangeMap, CoinChange coinChange) {
        Optional.ofNullable(coinChangeMap.get(key(Constants.COIN_ACTION)))
                .ifPresent(o -> {
                    String coinAction = ((UnicodeString) o).getString();
                    coinChange.setCoinAction(CoinAction.fromValue(coinAction));
                });
    }

    /**
     * Add metadata to Operation. The metadata object is accessed through {@value Constants#METADATA}.
     * The metadata object contains a list of different metadata objects.
     * @param operationMap The map containing the metadata field
     * @param operation The Operation object to fill
     */
    private static void addMetadata(Map operationMap, Operation operation) {
        Optional.ofNullable(operationMap.get(new UnicodeString(Constants.METADATA))).ifPresent(o -> {
            Map metadataMap = (Map) operationMap.get(new UnicodeString(Constants.METADATA));

            OperationMetadata operationMetadata = new OperationMetadata();
            addWithdrawalAmount(metadataMap, operationMetadata);
            addDepositAmount(metadataMap, operationMetadata);
            addRefundAmount(metadataMap, operationMetadata);
            addStakingCredential(metadataMap, operationMetadata);
            addPoolKeyHash(metadataMap, operationMetadata);
            addEpoch(metadataMap, operationMetadata);
            addTokenBundle(metadataMap, operationMetadata);
            addPoolRegistrationCert(metadataMap, operationMetadata);
            addPoolRegistrationParams(metadataMap, operationMetadata);
            addDrepVoteDelegationParams(metadataMap, operationMetadata);
            addPoolGovernanceVoteParams(metadataMap, operationMetadata);

            operation.setMetadata(operationMetadata);
        });
    }

    private static void addDrepVoteDelegationParams(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.DREP))).ifPresent(o -> {
            Map drepVoteDelegationMap = (Map) o;
            DRepParams drepParams = new DRepParams();

            Optional.ofNullable(drepVoteDelegationMap.get(new UnicodeString(Constants.ID)))
                    .ifPresent(di -> {
                        drepParams.setId(((UnicodeString) di).getString());
                    });

            Optional.ofNullable(drepVoteDelegationMap.get(new UnicodeString(Constants.TYPE)))
                    .ifPresent(di -> {
                        drepParams.setType(DRepTypeParams.fromValue(((UnicodeString) di).getString()));
                    });

            operationMetadata.setDrep(drepParams);
        });
    }

    private static void addPoolGovernanceVoteParams(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key("poolGovernanceVoteParams"))).ifPresent(poolGovernanceVoteParamsDi -> {
            Map poolGovernanceVoteParamsMap = (Map) poolGovernanceVoteParamsDi;
            PoolGovernanceVoteParams poolGovernanceVoteParams = new PoolGovernanceVoteParams();
            addGovActionId(poolGovernanceVoteParamsMap, poolGovernanceVoteParams);
            addGovPoolCredential(poolGovernanceVoteParamsMap, poolGovernanceVoteParams);
            addGovVote(poolGovernanceVoteParamsMap, poolGovernanceVoteParams);
            addGovPoolGovernanceVoteParams(poolGovernanceVoteParamsMap, poolGovernanceVoteParams);

            operationMetadata.setPoolGovernanceVoteParams(poolGovernanceVoteParams);
        });
    }

    private static void addGovPoolGovernanceVoteParams(Map metadataMap, PoolGovernanceVoteParams operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.VOTE_RATIONALE)))
                .ifPresent(o -> {
                    Map voterMap = (Map) o;
                    UnicodeString url = (UnicodeString) voterMap.get(new UnicodeString("url"));
                    UnicodeString dataHash = (UnicodeString) voterMap.get(new UnicodeString("data_hash"));

                    operationMetadata.setVoteRationale(new GovVoteRationaleParams(url.getString(), dataHash.getString()));
                });
    }

    static void addGovActionId(Map metadataMap, PoolGovernanceVoteParams operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.GOVERNANCE_ACTION_HASH)))
                .ifPresent(governanceActionHash -> {
                    String govActionString = ((UnicodeString) governanceActionHash).getString();
                    
                    // Validate that the governance action hash is exactly 66 characters (64 + 2)
                    if (govActionString.length() != 66) {
                        // Invalid length, ignore the value
                        return;
                    }
                    
                    // Parse tx_id and index from the concatenated string
                    // Index is always the last 2 hex characters
                    String txId = govActionString.substring(0, 64);
                    String indexHex = govActionString.substring(64);
                    int index = Integer.parseInt(indexHex, 16);
                    
                    String concatenatedGovAction = org.cardanofoundation.rosetta.common.util.GovActionParamsUtil
                            .formatGovActionString(txId, index);

                    operationMetadata.setGovernanceActionHash(concatenatedGovAction);
                });
    }

    private static void addGovVote(Map metadataMap, PoolGovernanceVoteParams operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.VOTE)))
                .ifPresent(o -> {
                    UnicodeString voteValue = (UnicodeString) o;

                    operationMetadata.setVote(GovVoteParams.fromValue(voteValue.toString().toLowerCase()));
                });
    }

    private static void addGovPoolCredential(Map metadataMap, PoolGovernanceVoteParams operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(POOL_CREDENTIAL)))
                .ifPresent(poolCredentialDi -> {
                    Map poolCredentialMap = (Map) poolCredentialDi;

                    UnicodeString hexBytes = (UnicodeString) poolCredentialMap.get(key(HEX_BYTES));
                    UnicodeString curveType = (UnicodeString) poolCredentialMap.get(key(CURVE_TYPE));

                    operationMetadata.setPoolCredential(new PublicKey(hexBytes.getString(), CurveType.fromValue(curveType.getString())));
                });
    }


    /**
     * Add PoolRegistrationParams to Operation metadata object. The pool registration params object is accessed through {@value Constants#POOL_REGISTRATION_PARAMS}.
     * The pool registration params object contains a list of different metadata objects.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addPoolRegistrationParams(Map metadataMap,
                                                  OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.POOL_REGISTRATION_PARAMS)))
                .ifPresent(poolMap -> {
                    Map poolRegistrationParamsMap = (Map) poolMap;
                    PoolRegistrationParams poolRegistrationParams = new PoolRegistrationParams();
                    // filling the poolRegistrationParams object
                    addVrfKeyHash(poolRegistrationParamsMap, poolRegistrationParams);
                    addRewardAddress(poolRegistrationParamsMap, poolRegistrationParams);
                    addPledge(poolRegistrationParamsMap, poolRegistrationParams);
                    addCost(poolRegistrationParamsMap, poolRegistrationParams);
                    addPoolOwners(poolRegistrationParamsMap, poolRegistrationParams);
                    addRelays(poolRegistrationParamsMap, poolRegistrationParams);
                    addMargins(poolRegistrationParamsMap, poolRegistrationParams);
                    addMarginPercentage(poolRegistrationParamsMap, poolRegistrationParams);
                    addPoolMetadata(poolRegistrationParamsMap, poolRegistrationParams);
                    // write back to operation metadata
                    operationMetadata.setPoolRegistrationParams(poolRegistrationParams);
                });
    }

    /**
     * Add PoolMetadata to Evapotranspirations object. The pool metadata object is accessed through {@value Constants#POOL_METADATA}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addPoolMetadata(Map poolRegistrationParamsMap,
                                        PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.POOL_METADATA)))
                .ifPresent(pMap -> {
                    PoolMetadata poolMetadata = new PoolMetadata();
                    Map poolMetadataMap = (Map) pMap;
                    // filling the poolMetadata object
                    addUrlToPoolMetadata(poolMetadataMap, poolMetadata);
                    addHashToPoolMetadata(poolMetadataMap, poolMetadata);
                    // write back to poolRegistrationParams
                    poolRegistrationParams.setPoolMetadata(poolMetadata);
                });
    }

    /**
     * Add Hash to Pool Metadata Map object. The hash is accessed through {@value Constants#HASH}.
     * @param poolMetadataMap The map containing the pool metadata field
     * @param poolMetadata The PoolMetadata object to fill
     */
    private static void addHashToPoolMetadata(Map poolMetadataMap, PoolMetadata poolMetadata) {
        Optional.ofNullable(poolMetadataMap.get(key(Constants.HASH)))
                .ifPresent(hash -> {
                    String hashStr = ((UnicodeString) hash).getString();
                    poolMetadata.setHash(hashStr);
                });
    }

    /**
     * Add Url to Pool Metadata Object from the cbor MAP  if not null.
     * Accessed through {@value Constants#URL}
     * @param poolMetadataMap The map containing the pool metadata field
     * @param poolMetadata The PoolMetadata object to fill
     */
    private static void addUrlToPoolMetadata(Map poolMetadataMap, PoolMetadata poolMetadata) {
        Optional.ofNullable(poolMetadataMap.get(key(Constants.URL)))
                .ifPresent(url -> {
                    String urlStr = ((UnicodeString) url).getString();
                    poolMetadata.setUrl(urlStr);
                });
    }

    /**
     * Add Margin Percentage to Pool registration params object. The margin percentage is accessed through {@value Constants#MARGIN_PERCENTAGE}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addMarginPercentage(Map poolRegistrationParamsMap,
                                            PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.MARGIN_PERCENTAGE)))
                .ifPresent(percentage -> {
                    String marginPercentage = ((UnicodeString) percentage).getString();
                    poolRegistrationParams.setMarginPercentage(marginPercentage);
                });
    }

    /**
     * Add Margins to Pool registration params object. The margin object is accessed through {@value Constants#MARGIN}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addMargins(Map poolRegistrationParamsMap,
                                   PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(new UnicodeString(Constants.MARGIN)))
                .ifPresent(o -> {
                    Map marginMap = (Map) o;
                    PoolMargin poolMargin = new PoolMargin();
                    // filling the poolMargin object
                    addNumeratorToPoolMargin(marginMap, poolMargin);
                    addDenominatorToPoolMargin(marginMap, poolMargin);
                    // write back to poolRegistrationParams
                    poolRegistrationParams.setMargin(poolMargin);
                });
    }

    /**
     * Add Denominator to Pool margin object. The denominator is accessed through {@value Constants#DENOMINATOR}.
     * @param marginMap The map containing the margin field
     * @param poolMargin The PoolMargin object to fill
     */
    private static void addDenominatorToPoolMargin(Map marginMap, PoolMargin poolMargin) {
        Optional.ofNullable(marginMap.get(new UnicodeString(Constants.DENOMINATOR)))
                .ifPresent(o -> {
                    String denominator = ((UnicodeString) o).getString();
                    poolMargin.setDenominator(denominator);
                });
    }

    /**
     * Add Numerator to Pool margin object. The numerator is accessed through {@value Constants#NUMERATOR}.
     * @param marginMap The map containing the margin field
     * @param poolMargin The PoolMargin object to fill
     */
    private static void addNumeratorToPoolMargin(Map marginMap, PoolMargin poolMargin) {
        Optional.ofNullable(marginMap.get(new UnicodeString(Constants.NUMERATOR)))
                .ifPresent(o -> {
                    String numerator = ((UnicodeString) o).getString();
                    poolMargin.setNumerator(numerator);
                });
    }

    /**
     * Adding Relays to PoolRegistrationParams object. The relays object is accessed through {@value Constants#RELAYS}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
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
                        addDnsName(rAMap, relay);
                        addPort(rAMap, relay);

                        relayList.add(relay);
                    });
                    poolRegistrationParams.setRelays(relayList);
                });
    }

    /** Add DnsName to Relay object. The DnsName is accessed through {@value Constants#DNSNAME}.
     * @param rAMap The map containing the relay field
     * @param relay The Relay object to fill
     */
    private static void addDnsName(Map rAMap, Relay relay) {
        Optional.ofNullable(rAMap.get(new UnicodeString(Constants.DNSNAME))).ifPresent(o -> {
            String dnsName = ((UnicodeString) o).getString();
            relay.setDnsName(dnsName);
        });
    }

    /**
     * Add IPv6 to Relay object. The IPv6 is accessed through {@value Constants#IPV6}.
     * @param rAMap The map containing the relay field
     * @param relay The Relay object to fill
     */
    private static void addIpv6(Map rAMap, Relay relay) {
        Optional.ofNullable(rAMap.get(new UnicodeString(Constants.IPV6))).ifPresent(o -> {
            String ipv6 = ((UnicodeString) o).getString();
            relay.setIpv6(ipv6);
        });
    }

    /**
     * Add IPv4 to Relay object. The IPv4 is accessed through {@value Constants#IPV4}.
     * @param rAMap The map containing the relay field
     * @param relay The Relay object to fill
     */
    private static void addIpv4(Map rAMap, Relay relay) {
        Optional.ofNullable(rAMap.get(new UnicodeString(Constants.IPV4))).ifPresent(o -> {
            String ipv4 = ((UnicodeString) o).getString();
            relay.setIpv4(ipv4);
        });
    }

    /**
     * Add Type to Relay object. The type is accessed through {@value Constants#TYPE}.
     * @param rAMap The map containing the relay field
     * @param relay The Relay object to fill
     */
    private static void addRelayType(Map rAMap, Relay relay) {
        Optional.ofNullable(rAMap.get(new UnicodeString(Constants.TYPE))).ifPresent(o -> {
            String typeR = ((UnicodeString) rAMap.get(new UnicodeString(Constants.TYPE))).getString();
            relay.setType(typeR);
        });
    }

    private static void addPort(Map rAMap, Relay relay) {
        Optional.ofNullable(rAMap.get(new UnicodeString(PORT))).ifPresent(o -> {
            String port = ((UnicodeString) o).getString();

            relay.setPort(Integer.parseInt(port));
        });
    }

    /**
     * Add a List of PoolOwners to Pool registration params object. The pool owners are accessed through {@value Constants#POOL_OWNERS}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addPoolOwners(Map poolRegistrationParamsMap,
                                      PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.POOL_OWNERS)))
                .ifPresent(o -> {
                    List<String> stringList = new ArrayList<>();
                    List<DataItem> poolOwners = ((Array) o).getDataItems();
                    poolOwners.forEach(p -> {
                        if (p != null) {
                            stringList.add(((UnicodeString) p).getString());
                        }
                    });
                    poolRegistrationParams.setPoolOwners(stringList);
                });
    }

    /**
     * Add Cost to Pool registration params object. The cost is accessed through {@value Constants#COST}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addCost(Map poolRegistrationParamsMap,
                                PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.COST)))
                .ifPresent(o -> {
                    String cost = ((UnicodeString) o).getString();
                    poolRegistrationParams.setCost(cost);
                });
    }

    /**
     * Add Pledge to Pool registration params object. The pledge is accessed through {@value Constants#PLEDGE}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addPledge(Map poolRegistrationParamsMap,
                                  PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.PLEDGE)))
                .ifPresent(o -> {
                    String pledge = ((UnicodeString) o).getString();
                    poolRegistrationParams.setPledge(pledge);
                });
    }

    /**
     * Add RewardAddress to Pool registration params object. The reward address is accessed through {@value Constants#REWARD_ADDRESS}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    private static void addRewardAddress(Map poolRegistrationParamsMap,
                                         PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.REWARD_ADDRESS)))
                .ifPresent(o -> {
                    String rewardAddress = ((UnicodeString) o).getString();
                    poolRegistrationParams.setRewardAddress(rewardAddress);
                });
    }

    /**
     * Add VrfKeyHash to Pool registration params object. The vrf key hash is accessed through {@value Constants#VRF_KEY_HASH}.
     * @param poolRegistrationParamsMap The map containing the pool registration params field
     * @param poolRegistrationParams The PoolRegistrationParams object to fill
     */
    public static void addVrfKeyHash(Map poolRegistrationParamsMap,
                                     PoolRegistrationParams poolRegistrationParams) {
        Optional.ofNullable(poolRegistrationParamsMap.get(key(Constants.VRF_KEY_HASH)))
                .ifPresent(o -> {
                    String vrfKeyHash = ((UnicodeString) o).getString();
                    poolRegistrationParams.setVrfKeyHash(vrfKeyHash);
                });
    }

    /**
     * Add PoolRegistrationCert to Operation Metadata object. The pool registration cert is accessed through {@value Constants#POOL_REGISTRATION_CERT}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addPoolRegistrationCert(Map metadataMap,
                                                OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.POOL_REGISTRATION_CERT)))
                .ifPresent(o -> {
                    String poolRegistrationCert = ((UnicodeString) o).getString();
                    operationMetadata.setPoolRegistrationCert(poolRegistrationCert);
                });
    }

    /**
     * Add TokenBundle to Operation metadata object. The token bundle is accessed through {@value Constants#TOKEN_BUNDLE}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addTokenBundle(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.TOKEN_BUNDLE))).ifPresent(o -> {
            List<DataItem> tokenBundleArray = ((Array) o).getDataItems();
            List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
            tokenBundleArray.forEach(t -> {
                Map tokenBundleMap = (Map) t;
                TokenBundleItem tokenBundleItem = new TokenBundleItem();
                // fill object
                addPolicyIdToTokenBundleItem(tokenBundleMap, tokenBundleItem);
                addTokensToTokenBundleItem(tokenBundleMap, tokenBundleItem);
                // write back to tokenBundleItems
                tokenBundleItems.add(tokenBundleItem);
            });

            operationMetadata.setTokenBundle(tokenBundleItems);
        });
    }

    /**
     * Add Tokens to TokenBundleItem object. The tokens are accessed through {@value Constants#TOKENS}.
     * @param tokenBundleMap The map containing the token bundle field
     * @param tokenBundleItem The TokenBundleItem object to fill
     */
    private static void addTokensToTokenBundleItem(Map tokenBundleMap, TokenBundleItem tokenBundleItem) {
        List<Amount> tokenAList = new ArrayList<>();
        Optional.ofNullable(tokenBundleMap.get(key(Constants.TOKENS))).ifPresent(o -> {
            List<DataItem> tokensItem = ((Array) o).getDataItems();
            tokensItem.forEach(tk -> {
                Map tokenAmountMap = (Map) tk;

                Optional.ofNullable(tokenAmountMap.get(key(Constants.AMOUNT))).ifPresent(am -> {
                    Map amountMap = (Map) am;
                    Amount amount = getAmountFromMap(amountMap);

                    tokenAList.add(amount);
                });
            });
        });

        tokenBundleItem.setTokens(tokenAList);
    }

    /**
     * Add PolicyId to TokenBundleItem object. The policyId is accessed
     * @param tokenBundleMap The map containing the token bundle field
     * @param tokenBundleItem The TokenBundleItem object to fill
     */
    private static void addPolicyIdToTokenBundleItem(Map tokenBundleMap, TokenBundleItem tokenBundleItem) {
        Optional.ofNullable(tokenBundleMap.get(key(Constants.POLICYID))).ifPresent(o -> {
            String policyId = ((UnicodeString) o).getString();
            tokenBundleItem.setPolicyId(policyId);
        });
    }

    /**
     * Add Epoch to Operation metadata object. The epoch is accessed through {@value Constants#EPOCH}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addEpoch(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.EPOCH))).ifPresent(o -> {
            BigInteger value = ((UnsignedInteger) metadataMap.get(
                    new UnicodeString(Constants.EPOCH))).getValue();
            operationMetadata.setEpoch(value.intValue());
        });
    }

    /**
     * Add PoolKeyHash to Operation metadata object. The pool key hash is accessed through {@value Constants#POOL_KEY_HASH}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addPoolKeyHash(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.POOL_KEY_HASH)))
                .ifPresent(o -> operationMetadata.setPoolKeyHash(((UnicodeString) o).getString()));
    }

    /**
     * Add StakingCredential to Operation metadata object. The staking credential is accessed through {@value Constants#STAKING_CREDENTIAL}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addStakingCredential(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.STAKING_CREDENTIAL)))
                .ifPresent(o -> {
                    Map stakingCredentialMap = (Map) o;
                    operationMetadata.setStakingCredential(getPublicKeyFromMap(stakingCredentialMap));
                });
    }

    /**
     * Add RefundAmount to Operation metadata object. The refund amount is accessed through {@value Constants#REFUNDAMOUNT}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addRefundAmount(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.REFUNDAMOUNT))).ifPresent(o -> {
            Map refundAmountMap = (Map) o;
            operationMetadata.setRefundAmount(getAmountFromMap(refundAmountMap));
        });
    }

    /**
     * Add DepositAmount to Operation metadata object. The deposit amount is accessed through {@value Constants#DEPOSITAMOUNT}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addDepositAmount(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.DEPOSITAMOUNT)))
                .ifPresent(o -> {
                    Map depositAmountMap = (Map) o;
                    Amount amountD = getAmountFromMap(depositAmountMap);
                    operationMetadata.setDepositAmount(amountD);
                });
    }

    /**
     * Add WithdrawalAmount to Operation metadata object. The withdrawal amount is accessed through {@value Constants#WITHDRAWALAMOUNT}.
     * @param metadataMap The map containing the metadata field
     * @param operationMetadata The OperationMetadata object to fill
     */
    private static void addWithdrawalAmount(Map metadataMap, OperationMetadata operationMetadata) {
        Optional.ofNullable(metadataMap.get(key(Constants.WITHDRAWALAMOUNT)))
                .ifPresent(o -> {
                    Map withdrawalAmountMap = (Map) o;
                    Amount amountW = getAmountFromMap(withdrawalAmountMap);
                    operationMetadata.setWithdrawalAmount(amountW);
                });
    }

    /**
     * Returns a PublicKey object populated from the cbor MAP  if not null.
     * @param stakingCredentialMap The map containing the staking credential field
     * @return The populated PublicKey object
     */
    private static PublicKey getPublicKeyFromMap(Map stakingCredentialMap) {
        PublicKey publicKey = new PublicKey();

        Optional.ofNullable(stakingCredentialMap.get(new UnicodeString(HEX_BYTES)))
                .ifPresent(o -> publicKey.setHexBytes(((UnicodeString) o).getString()));

        Optional.ofNullable(stakingCredentialMap.get(new UnicodeString(CURVE_TYPE)))
                .ifPresent(o -> publicKey.setCurveType(CurveType.fromValue(((UnicodeString) o).getString())));

        return publicKey;
    }

}
