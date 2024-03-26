package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AllArgsConstructor;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Provider;
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
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.SubAccountIdentifier;
import org.openapitools.client.model.TokenBundleItem;
import org.openapitools.client.model.VoteRegistrationMetadata;

@OpenApiMapper
@AllArgsConstructor
public class CborMapToRosettaOperation {

  final ModelMapper modelMapper;

  public TransactionExtraData toTransactionExtraData(Map map) {

    List<DataItem> array = ((Array) map.get(new UnicodeString(Constants.OPERATIONS))).getDataItems();

    return new TransactionExtraData(array.stream().map(dataItem -> toOperation((Map) dataItem)).toList(),
        ((UnicodeString) map.get(new UnicodeString(Constants.TRANSACTIONMETADATAHEX))).getString());
  }

  public List<Operation> toOperationList(List<Map> dataItems) {
    return dataItems.stream().map(this::toOperation).toList();
  }

  public Operation toOperation(Map cborMap) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, Operation.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, Operation.class))
        .addMappings(
          mapper -> {
            // OperationIdentifier
            mapper.map(src -> toOperationIdentifier(
                src.get(new UnicodeString(Constants.OPERATION_IDENTIFIER))), Operation::setOperationIdentifier);
            // RelatedOperations
            mapper.map(src -> ((Array)src.get(new UnicodeString(Constants.RELATED_OPERATION)))
                .getDataItems().stream().map(dataItem -> toOperationIdentifier((Map) dataItem)).toList(), Operation::setRelatedOperations);
            // Type
            mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.TYPE))).getString(), Operation::setType);
            // Status
            mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.STATUS))).getString(), Operation::setStatus);
            // Account
            mapper.map(src -> toAccountIdentifier((Map) src.get(new UnicodeString(Constants.ACCOUNT))), Operation::setAccount);
            // Amount
            mapper.map(src -> toAmount((Map) src.get(new UnicodeString(Constants.AMOUNT))), Operation::setAmount);
            // to CoinChange
            mapper.map(src -> toCoinChange((Map) src.get(new UnicodeString(Constants.COIN_CHANGE))), Operation::setCoinChange);
            // to metadata
            mapper.map(src -> toOperationMetadata((Map) src.get(new UnicodeString(Constants.METADATA))), Operation::setMetadata);
          }

        ).map(cborMap);
  }

  private OperationMetadata toOperationMetadata(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, OperationMetadata.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, OperationMetadata.class))
        .addMappings(mapper -> {
              mapper.map(src -> toAmount((Map) src.get(new UnicodeString(Constants.WITHDRAWALAMOUNT))), OperationMetadata::setWithdrawalAmount);
              mapper.map(src -> toAmount((Map) src.get(new UnicodeString(Constants.DEPOSITAMOUNT))), OperationMetadata::setDepositAmount);
              mapper.map(src -> toAmount((Map) src.get(new UnicodeString(Constants.REFUNDAMOUNT))), OperationMetadata::setRefundAmount);
              mapper.map(src -> toPublicKey((Map) src.get(new UnicodeString(Constants.STAKING_CREDENTIAL))), OperationMetadata::setStakingCredential);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.POOL_KEY_HASH))).getString(), OperationMetadata::setPoolKeyHash);
              mapper.map(src -> ((UnsignedInteger)src.get(new UnicodeString(Constants.EPOCH))).getValue().intValue(), OperationMetadata::setEpoch);
              mapper.map(src -> toTokenBundleItemList((List<DataItem>) src.get(new UnicodeString(Constants.TOKENBUNDLE))), OperationMetadata::setTokenBundle);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.POOLREGISTRATIONCERT))).getString(), OperationMetadata::setPoolRegistrationCert);
              mapper.map(src -> toPoolRegistrationParams((Map)src.get(new UnicodeString(Constants.POOLREGISTRATIONPARAMS))), OperationMetadata::setPoolRegistrationParams);
              mapper.map(src -> toVoteRegistrationMetadata((Map) src.get(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA))), OperationMetadata::setVoteRegistrationMetadata);
            }
        ).map(map);
  }

  private VoteRegistrationMetadata toVoteRegistrationMetadata(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, VoteRegistrationMetadata.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, VoteRegistrationMetadata.class))
        .addMappings(mapper -> {
              mapper.map(src -> toPublicKey((Map) src.get(new UnicodeString(Constants.STAKE_KEY))), VoteRegistrationMetadata::setStakeKey);
              mapper.map(src -> toPublicKey((Map) src.get(new UnicodeString(Constants.VOTING_KEY))), VoteRegistrationMetadata::setVotingkey);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.REWARD_ADDRESS))).getString(), VoteRegistrationMetadata::setRewardAddress);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.VOTING_SIGNATURE))).getString(), VoteRegistrationMetadata::setVotingSignature);
              mapper.map(src -> ((UnsignedInteger) src.get(new UnicodeString(Constants.VOTING_SIGNATURE))).getValue().intValue(), VoteRegistrationMetadata::setVotingNonce);

            }
        ).map(map);
  }

  private PoolRegistrationParams toPoolRegistrationParams(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, PoolRegistrationParams.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, PoolRegistrationParams.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.VRFKEYHASH))).getString(), PoolRegistrationParams::setVrfKeyHash);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.REWARD_ADDRESS))).getString(), PoolRegistrationParams::setRewardAddress);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.PLEDGE))).getString(), PoolRegistrationParams::setPledge);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.COST))).getString(), PoolRegistrationParams::setCost);
              mapper.map(src -> ((List<UnicodeString>) src.get(new UnicodeString(Constants.POOLOWNERS))).stream().map(UnicodeString::getString).toList(), PoolRegistrationParams::setPoolOwners);
              mapper.map(src -> toRelays((Map) src.get(new UnicodeString(Constants.RELAYS))), PoolRegistrationParams::setRelays);
              mapper.map(src -> toPoolMargin((Map) src.get(new UnicodeString(Constants.MARGIN))), PoolRegistrationParams::setMargin);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.MARGIN_PERCENTAGE))).getString(), PoolRegistrationParams::setMarginPercentage);
              mapper.map(src -> toPoolMetadata((Map) src.get(new UnicodeString(Constants.POOLMETADATA))), PoolRegistrationParams::setPoolMetadata);
            }
        ).map(map);
  }

  private PoolMetadata toPoolMetadata(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, PoolMetadata.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, PoolMetadata.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.URL))).getString(), PoolMetadata::setUrl);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.HASH))).getString(), PoolMetadata::setHash);
            }
        ).map(map);
  }

  private PoolMargin toPoolMargin(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, PoolMargin.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, PoolMargin.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.NUMERATOR))).getString(), PoolMargin::setNumerator);
              mapper.map(src -> ((UnsignedInteger) src.get(new UnicodeString(Constants.DENOMINATOR))).getValue().intValue(), PoolMargin::setDenominator);
            }
        ).map(map);
  }

  private List<Relay> toRelays(Map map) {
    return ((List<Map>) map.get(new UnicodeString(Constants.RELAYS))).stream().map(this::toRelay).toList();
  }

  private Relay toRelay(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, Relay.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, Relay.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.TYPE))).getString(), Relay::setType);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.IPV4))).getString(), Relay::setIpv4);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.IPV6))).getString(), Relay::setIpv6);
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.DNSNAME))).getString(), Relay::setDnsName);
              mapper.map(src -> ((UnsignedInteger) src.get(new UnicodeString(Constants.PORT))).getValue().intValue(), Relay::setPort);
            }
        ).map(map);
  }

  private List<TokenBundleItem> toTokenBundleItemList(List<DataItem> array) {
    return array.stream().map(this::toTokenBundleItem).toList();
  }

  private TokenBundleItem toTokenBundleItem(DataItem item) {
    Map map = (Map) item;
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, TokenBundleItem.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, TokenBundleItem.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.POLICYID))).getString(), TokenBundleItem::setPolicyId);
              mapper.map(src -> toAmountList((List<Map>) src.get(new UnicodeString(Constants.TOKENS))), TokenBundleItem::setTokens);
            }
        ).map(map);
  }

  private List<Amount> toAmountList(List<Map> dataItems) {
    return dataItems.stream().map(this::toAmount).toList();
  }

  private PublicKey toPublicKey(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, PublicKey.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, PublicKey.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.HEX_BYTES))).getString(), PublicKey::setHexBytes);
              mapper.map(src -> CurveType.fromValue(((UnicodeString) src.get(new UnicodeString(Constants.CURVE_TYPE))).getString()), PublicKey::setCurveType);
            }
        ).map(map);
  }


  private CoinChange toCoinChange(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, CoinChange.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, CoinChange.class))
        .addMappings(mapper -> {
              mapper.map(src -> CoinAction.fromValue(((UnicodeString) src.get(new UnicodeString(Constants.COIN_ACTION))).getString()), CoinChange::setCoinAction);
              mapper.map(src -> toCoinIdentifier((Map) src.get(new UnicodeString(Constants.COIN_IDENTIFIER))), CoinChange::setCoinIdentifier);
            }
        ).map(map);
  }

  private CoinIdentifier toCoinIdentifier(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, CoinIdentifier.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, CoinIdentifier.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.INDEX))).getString(), CoinIdentifier::setIdentifier);
            }
        ).map(map);
  }

  private Amount toAmount(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, Amount.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, Amount.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnsignedInteger) src.get(new UnicodeString(Constants.VALUE))).getValue().longValue(), Amount::setValue);
              mapper.map(src -> src.get(new UnicodeString(Constants.METADATA)), Amount::setMetadata);
              mapper.map(src -> toCurrency((Map) src.get(new UnicodeString(Constants.CURRENCY))), Amount::setCurrency);
            }
        ).map(map);
  }

  private Currency toCurrency(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, Currency.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, Currency.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.SYMBOL))).getString(), Currency::setSymbol);
              mapper.map(src -> ((UnsignedInteger) src.get(new UnicodeString(Constants.DECIMALS))).getValue().intValue(), Currency::setDecimals);
              mapper.map(src -> toCurrencyMetadata((Map) src.get(new UnicodeString(Constants.METADATA))), Currency::setMetadata);
            }
        ).map(map);
  }

  private CurrencyMetadata toCurrencyMetadata(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, CurrencyMetadata.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, CurrencyMetadata.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.POLICYID))).getString(), CurrencyMetadata::setPolicyId);
            }
        ).map(map);
  }

  private AccountIdentifier toAccountIdentifier(Map dataitem) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, AccountIdentifier.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, AccountIdentifier.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.ADDRESS))).getString(), AccountIdentifier::setAddress);
              mapper.map(src -> toSubAccountIdentifier((Map) src.get(new UnicodeString(Constants.SUB_ACCOUNT))), AccountIdentifier::setSubAccount);
              mapper.map(src -> toAccountMetadata((Map) src.get(new UnicodeString(Constants.METADATA))), AccountIdentifier::setMetadata);
            }
        ).map(dataitem);
  }

  private AccountIdentifierMetadata toAccountMetadata(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, AccountIdentifierMetadata.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, AccountIdentifierMetadata.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.CHAIN_CODE))).getString(), AccountIdentifierMetadata::setChainCode);
            }
        ).map(map);
  }

  private SubAccountIdentifier toSubAccountIdentifier(Map map) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, SubAccountIdentifier.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, SubAccountIdentifier.class))
        .addMappings(mapper -> {
              mapper.map(src -> ((UnicodeString) src.get(new UnicodeString(Constants.ADDRESS))).getString(), SubAccountIdentifier::setAddress);
            }
        ).map(map);
  }



  private OperationIdentifier toOperationIdentifier(DataItem dataItem) {
    Converter<DataItem, Long> dataItemToLong = ctx -> dataItemToLong(ctx.getSource());
    return Optional
        .ofNullable(modelMapper.getTypeMap(Map.class, OperationIdentifier.class))
        .orElseGet(() -> modelMapper.createTypeMap(Map.class, OperationIdentifier.class))
        .addMappings(mapper -> {
              mapper.using(dataItemToLong).map(src -> src.get(new UnicodeString(Constants.INDEX)), OperationIdentifier::setIndex);
              mapper.using(dataItemToLong).map(src -> src.get(new UnicodeString(Constants.NETWORK_INDEX)), OperationIdentifier::setNetworkIndex);
            }
        ).map((Map) dataItem);
  }

  private long getLongFromDataMap(Map dataMap, String key) {
    return dataItemToLong(dataMap.get(new UnicodeString(key)));
  }

  // TODO refactor using Optional
  private long dataItemToLong(DataItem dataItem) {
    return dataItem.getMajorType() == null ? 0 : ((UnsignedInteger) dataItem).getValue().longValue();
  }

}
