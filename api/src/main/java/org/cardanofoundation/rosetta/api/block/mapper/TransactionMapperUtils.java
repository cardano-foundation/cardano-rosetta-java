package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.DRepDelegation;
import org.cardanofoundation.rosetta.api.block.model.domain.GovernancePoolVote;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Component
@RequiredArgsConstructor
public class TransactionMapperUtils {

  private final ProtocolParamService protocolParamService;
  private final DataMapper dataMapper;

  @Named("convertGovAnchorFromRosetta")
  public GovVoteRationaleParams convertGovAnchorFromRosetta(Anchor anchor) {
    return GovernancePoolVote.convertFromRosetta(anchor);
  }

  @Named("convertGovVoteRationaleToRosetta")
  public Anchor convertGovVoteRationaleToRosetta(GovVoteRationaleParams params) {
    return GovernancePoolVote.convertToRosetta(params);
  }

  @Named("convertGovVoteToRosetta")
  public GovVoteParams convertGovVoteFromRosetta(Vote vote) {
    return GovernancePoolVote.convertFromRosetta(vote);
  }

  @Named("convertGovVoteToRosetta")
  public Vote convertGovVoteToRosetta(GovVoteParams voteParams) {
    return GovernancePoolVote.convertToRosetta(voteParams);
  }

  @Named("convertGovActionIdToRosetta")
  public String convertGovActionIdFromRosetta(GovActionId govActionId) {
    return GovernancePoolVote.convertFromRosetta(govActionId);
  }

  @Named("convertGovActionIdToRosetta")
  public GovActionId convertGovActionIdToRosetta(String govActionParamsString) {
    return GovernancePoolVote.convertGovActionIdToRosetta(govActionParamsString);
  }

  @Named("convertPoolCredentialToRosetta")
  public PublicKey convertPoolCredentialToRosetta(String poolCredentialHex) {
    return GovernancePoolVote.convertToRosetta(poolCredentialHex);
  }

  @Named("convertDRepToRosetta")
  public DRepDelegation.DRep convertDRepToRosetta(DRepParams dRepParams) {
    return DRepDelegation.DRep.convertDRepToRosetta(dRepParams);
  }

  @Named("convertDRepFromRosetta")
  public DRepParams convertDRepFromRosetta(DRepDelegation.DRep drep) {
    return DRepDelegation.DRep.convertDRepFromRosetta(drep);
  }

  @Named("mapAmountsToOperationMetadataInputWithCache")
  public OperationMetadata mapToOperationMetaDataInputWithCache(List<Amt> amounts,
                                                                @Context Map<Asset, TokenRegistryCurrencyData> metadataMap) {
    return mapToOperationMetaDataWithCache(true, amounts, metadataMap);
  }

  @Named("mapAmountsToOperationMetadataOutputWithCache")
  public OperationMetadata mapToOperationMetaDataOutputWithCache(List<Amt> amounts,
                                                                 @Context Map<Asset, TokenRegistryCurrencyData> metadataMap) {
    return mapToOperationMetaDataWithCache(false, amounts, metadataMap);
  }

  @Nullable
  public OperationMetadata mapToOperationMetaDataWithCache(boolean spent,
                                                           List<Amt> amounts,
                                                           Map<Asset, TokenRegistryCurrencyData> metadataMap) {
    OperationMetadata operationMetadata = new OperationMetadata();

    if (amounts == null || amounts.isEmpty()) {
      return null;
    }

    // Filter out ADA amounts
    List<Amt> nonAdaAmounts = amounts.stream()
            .filter(amount -> !amount.getAssetName().equals(LOVELACE))
            .toList();

    // token bundle is only for ada, no native assets present
    if (nonAdaAmounts.isEmpty()) {
      return null;
    }

    // Group amounts by policyId and create token bundles using the pre-fetched metadata
    nonAdaAmounts.stream()
            .collect(Collectors.groupingBy(Amt::getPolicyId))
            .forEach((policyId, policyIdAmounts) ->
                    operationMetadata.addTokenBundleItem(
                            TokenBundleItem.builder()
                                    .policyId(policyId)
                                    .tokens(policyIdAmounts.stream()
                                            .map(amount -> extractAmountWithCache(spent, amount, metadataMap))
                                            .toList())
                                    .build()
                    )
            );

    return Objects.isNull(operationMetadata.getTokenBundle()) ? null : operationMetadata;
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
                    LOVELACE)).findFirst().map(Amt::getQuantity).orElse(BigInteger.ZERO))
            .orElse(BigInteger.ZERO);
    return input ? adaAmount.negate().toString() : adaAmount.toString();
  }

  public Amount getDepositAmountPool() {
    String deposit = String.valueOf(protocolParamService.findProtocolParameters().getPoolDeposit());

    return dataMapper.mapAmount(deposit, Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("getDepositAmountStake")
  public Amount getDepositAmountStake(StakeRegistration model) {
    CertificateType type = model.getType();
    BigInteger keyDeposit = Optional.ofNullable(protocolParamService.findProtocolParameters()
            .getKeyDeposit()).orElse(BigInteger.ZERO);

    if (type == CertificateType.STAKE_DEREGISTRATION) {
      keyDeposit = keyDeposit.negate();
    }

    return dataMapper.mapAmount(keyDeposit.toString(), Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("OperationIdentifier")
  public OperationIdentifier getOperationIdentifier(long index) {
    return new OperationIdentifier().index(index);
  }

  @Named("getUtxoName")
  public String getUtxoName(Utxo model) {
    return "%s:%d".formatted(model.getTxHash(), model.getOutputIndex());
  }

  @Named("updateDepositAmountNegate")
  public Amount updateDepositAmountNegate(BigInteger amount) {
    BigInteger bigInteger = Optional.ofNullable(amount)
            .map(BigInteger::negate)
            .orElse(BigInteger.ZERO);

    return dataMapper.mapAmount(bigInteger.toString(), Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("convertStakeCertificateType")
  @Nullable
  public String convertCertificateType(CertificateType model) {
    if (model == null) {
      return null;
    }

    return switch (model) {
      case CertificateType.STAKE_REGISTRATION -> OperationType.STAKE_KEY_REGISTRATION.getValue();
      case CertificateType.STAKE_DEREGISTRATION -> OperationType.STAKE_KEY_DEREGISTRATION.getValue();
      default -> null;
    };
  }

  @Named("getCoinSpentAction")
  public CoinAction getCoinSpentAction(Utxo model) {
    return CoinAction.SPENT;
  }

  @Named("getCoinCreatedAction")
  public CoinAction getCoinCreatedAction(Utxo model) {
    return CoinAction.CREATED;
  }

  private Amount extractAmountWithCache(boolean spent,
                                        Amt amount,
                                        Map<Asset, TokenRegistryCurrencyData> metadataMap) {
    Asset asset = Asset.builder()
            .policyId(amount.getPolicyId())
            .assetName(amount.getAssetName())
            .build();

    TokenRegistryCurrencyData metadata = metadataMap.get(asset);

    String assetHex = amount.getUnit().replace(amount.getPolicyId(), "");

    return dataMapper.mapAmount(
            dataMapper.mapValue(amount.getQuantity().toString(), spent),
            assetHex,
            getDecimalsWithFallback(metadata),
            metadata
    );
  }

  private static int getDecimalsWithFallback(@NotNull TokenRegistryCurrencyData metadata) {
    return Optional.ofNullable(metadata.getDecimals())
            .orElse(0);
  }

}
