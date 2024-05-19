package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.mapstruct.Named;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.TokenBundleItem;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Constants;

@Component
@RequiredArgsConstructor
public class OperationMapperUtils {

  final ProtocolParamService protocolParamService;

  @Named("mapAmountsToOperationMetadataInput")
  public OperationMetadata mapToOperationMetaDataInput(List<Amt> amounts) {
    return mapToOperationMetaData(true, amounts);
  }

  @Named("mapAmountsToOperationMetadataOutput")
  public OperationMetadata mapToOperationMetaDataOutput(List<Amt> amounts) {
    return mapToOperationMetaData(false, amounts);
  }

  // TODO Map Tokens to one PolicyID
  public OperationMetadata mapToOperationMetaData(boolean spent, List<Amt> amounts) {
    OperationMetadata operationMetadata = new OperationMetadata();
    Optional.ofNullable(amounts)
        .stream()
        .flatMap(List::stream)
        .forEach(amount -> {
          if (!amount.getAssetName().equals(Constants.LOVELACE)) {
            TokenBundleItem tokenBundleItem = new TokenBundleItem();
            tokenBundleItem.setPolicyId(amount.getPolicyId());
            Amount amt = new Amount();
            amt.setValue(DataMapper.mapValue(amount.getQuantity().toString(), spent));
            String hexAssetName = amount.getUnit().replace(amount.getPolicyId(), "");
            amt.setCurrency(Currency.builder()
                .symbol(hexAssetName)
                .decimals(0)
                .build());
            tokenBundleItem.setTokens(List.of(amt));
            operationMetadata.addTokenBundleItem(tokenBundleItem);
          }
        });

    return operationMetadata.getTokenBundle() == null ? null : operationMetadata;
  }

  @Named("getAdaAmountInput")
  public String getAdaAmountInput(Utxo f) {
    return getAdaAmount(f, true);
  }

  @Named("getAdaAmountOutput")
  public String getAdaAmountOutput(Utxo f) {
    return getAdaAmount(f, false);
  }

  public String getAdaAmount(Utxo f, boolean input) {
    BigInteger adaAmount = Optional.ofNullable(f.getAmounts())
        .map(amts -> amts.stream().filter(amt -> amt.getAssetName().equals(
            Constants.LOVELACE)).findFirst().map(Amt::getQuantity).orElse(BigInteger.ZERO))
        .orElse(BigInteger.ZERO);
    return input ? adaAmount.negate().toString() : adaAmount.toString();
  }

  public Amount getDepositAmountPool() {
    String deposit = String.valueOf(protocolParamService.getProtocolParameters().getPoolDeposit());
    return DataMapper.mapAmount(deposit, Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("getDepositAmountStake")
  public Amount getDepositAmountStake(StakeRegistration model) {
    CertificateType type = model.getType();
    BigInteger keyDeposit = Optional.ofNullable(protocolParamService.getProtocolParameters().getKeyDeposit()).orElse(BigInteger.ZERO);
    if (type == CertificateType.STAKE_DEREGISTRATION) {
      keyDeposit = keyDeposit.negate();
    }
    return DataMapper.mapAmount(keyDeposit.toString(), Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("OperationIdentifier")
  public OperationIdentifier getOperationIdentifier(long index) {
    return new OperationIdentifier().index(index);
  }

  @Named("getUtxoName")
  public String getUtxoName(Utxo model) {
    return model.getTxHash() + ":" + model.getOutputIndex();
  }

  @Named("updateDepositAmountNegate")
  public Amount updateDepositAmountNegate(BigInteger amount) {
    BigInteger bigInteger = Optional.ofNullable(amount)
        .map(BigInteger::negate)
        .orElse(BigInteger.ZERO);
    return DataMapper.mapAmount(bigInteger.toString(), Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("convertCertificateType")
  public String convert(CertificateType model) {
    if (model == null) {
      return null;
    } else {
      return switch (model) {
        case CertificateType.STAKE_REGISTRATION -> OperationType.STAKE_KEY_REGISTRATION.getValue();
        case CertificateType.STAKE_DEREGISTRATION -> OperationType.STAKE_KEY_DEREGISTRATION.getValue();
        default -> null;
      };
    }
  }

  @Named("getCoinSpentAction")
  public CoinAction getCoinSpentAction(Utxo model) {
    return CoinAction.SPENT;
  }

  @Named("getCoinCreatedAction")
  public CoinAction getCoinCreatedAction(Utxo model) {
    return CoinAction.CREATED;
  }

}
