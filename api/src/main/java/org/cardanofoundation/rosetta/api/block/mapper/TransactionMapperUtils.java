package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.DRepDelegation;
import org.cardanofoundation.rosetta.api.block.model.domain.GovernancePoolVote;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.mapstruct.Named;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Component
@RequiredArgsConstructor
public class TransactionMapperUtils {

  final ProtocolParamService protocolParamService;
  final TokenRegistryHttpGateway tokenRegistryHttpGateway;

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

  @Named("mapAmountsToOperationMetadataInput")
  public OperationMetadata mapToOperationMetaDataInput(List<Amt> amounts) {
    return mapToOperationMetaData(true, amounts);
  }

  @Named("mapAmountsToOperationMetadataOutput")
  public OperationMetadata mapToOperationMetaDataOutput(List<Amt> amounts) {
    return mapToOperationMetaData(false, amounts);
  }

  @Nullable
  public OperationMetadata mapToOperationMetaData(boolean spent, List<Amt> amounts) {
    OperationMetadata operationMetadata = new OperationMetadata();

    if (amounts == null || amounts.isEmpty()) {
      return null;
    }

    // Filter out ADA amounts and collect subjects (policyId + assetName)
    List<Amt> nonAdaAmounts = amounts.stream()
            .filter(amount -> !amount.getAssetName().equals(LOVELACE))
            .toList();

    // token bundle is only for native assets
    if (nonAdaAmounts.isEmpty()) {
      return null;
    }

    // Collect all subjects (policyId + assetName concatenated) for batch fetching
    Set<String> subjects = nonAdaAmounts.stream()
            .map(amount -> amount.getPolicyId() + HexUtil.encodeHexString(amount.getAssetName().getBytes(UTF_8)))
            .collect(Collectors.toSet());

    // Fetch token metadata for all subjects in batch
    Map<String, Optional<TokenSubject>> tokenMetadataMap = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

    // Group amounts by policyId and create token bundles
    nonAdaAmounts.stream()
            .collect(Collectors.groupingBy(Amt::getPolicyId))
            .forEach((policyId, policyIdAmounts) ->
                    operationMetadata.addTokenBundleItem(
                            TokenBundleItem.builder()
                                    .policyId(policyId)
                                    .tokens(policyIdAmounts.stream()
                                            .map(amount -> extractAmount(spent, amount, tokenMetadataMap))
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

    return DataMapper.mapAmount(deposit, Constants.ADA, Constants.ADA_DECIMALS, null);
  }

  @Named("getDepositAmountStake")
  public Amount getDepositAmountStake(StakeRegistration model) {
    CertificateType type = model.getType();
    BigInteger keyDeposit = Optional.ofNullable(protocolParamService.findProtocolParameters()
            .getKeyDeposit()).orElse(BigInteger.ZERO);

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
    return "%s:%d".formatted(model.getTxHash(), model.getOutputIndex());
  }

  @Named("updateDepositAmountNegate")
  public Amount updateDepositAmountNegate(BigInteger amount) {
    BigInteger bigInteger = Optional.ofNullable(amount)
            .map(BigInteger::negate)
            .orElse(BigInteger.ZERO);

    return DataMapper.mapAmount(bigInteger.toString(), Constants.ADA, Constants.ADA_DECIMALS, null);
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

  private static Amount extractAmount(boolean spent,
                                      Amt amount,
                                      Map<String, Optional<TokenSubject>> tokenMetadataMap) {
    // Create subject for this token
    String subject = amount.getPolicyId() + HexUtil.encodeHexString(amount.getAssetName().getBytes(UTF_8));

    // Get metadata if available
    Optional<TokenSubject> tokenMetadata = tokenMetadataMap.getOrDefault(subject, Optional.empty());

    CurrencyResponse c = CurrencyResponse.builder()
            .symbol(amount.getAssetName())
            .decimals(extractTokenDecimals(tokenMetadata))
            .build();

    CurrencyMetadataResponse currencyMetadataResponse = extractTokenMetadata(amount.getPolicyId(), tokenMetadata);
    c.metadata(currencyMetadataResponse);

    return Amount.builder()
            .value(DataMapper.mapValue(amount.getQuantity().toString(), spent))
            .currency(c)
            .build();
  }

  private static int extractTokenDecimals(Optional<TokenSubject> tokenMetadata) {
    return tokenMetadata
            .map(TokenSubject::getMetadata)
            .map(TokenMetadata::getDecimals)
            .map(TokenPropertyNumber::getValue)
            .map(Long::intValue)
            .orElse(0);
  }

  private static CurrencyMetadataResponse extractTokenMetadata(String policyId,
                                                               Optional<TokenSubject> tokenMetadata) {
    CurrencyMetadataResponse.CurrencyMetadataResponseBuilder builder = CurrencyMetadataResponse.builder()
            .policyId(policyId);

    tokenMetadata.ifPresent(t -> {
      TokenMetadata tokenMeta = t.getMetadata();

      // Mandatory fields from registry API
      builder.subject(t.getSubject());
      builder.name(tokenMeta.getName().getValue());
      builder.description(tokenMeta.getDescription().getValue());

      // Optional fields
      Optional.ofNullable(tokenMeta.getTicker()).ifPresent(ticker -> builder.ticker(ticker.getValue()));
      Optional.ofNullable(tokenMeta.getUrl()).ifPresent(url -> builder.url(url.getValue()));
      Optional.ofNullable(tokenMeta.getLogo()).ifPresent(logo -> builder.logo(logo.getValue()));
      Optional.ofNullable(tokenMeta.getVersion()).ifPresent(version -> builder.version(BigDecimal.valueOf(version.getValue())));
    });

    return builder.build();
  }

}
