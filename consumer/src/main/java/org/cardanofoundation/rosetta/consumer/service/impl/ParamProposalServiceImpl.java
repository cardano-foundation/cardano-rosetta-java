package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.common.entity.CostModel;
import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.ProtocolParamUpdate;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedParamProposalRepository;
import org.cardanofoundation.rosetta.consumer.service.CostModelService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.rosetta.consumer.service.ParamProposalService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParamProposalServiceImpl implements ParamProposalService {

  CachedParamProposalRepository cachedParamProposalRepository;
  CostModelService costModelService;

  @Override
  public List<ParamProposal> handleParamProposals(
      Collection<AggregatedTx> successTxs, Map<String, Tx> txMap) {

    successTxs.stream()
        .filter(aggregatedTx -> Objects.nonNull(aggregatedTx.getUpdate()))
        .forEach(costModelService::handleCostModel);

    List<ParamProposal> paramProposals = successTxs.stream()
        .filter(aggregatedTx -> Objects.nonNull(aggregatedTx.getUpdate()))
        .flatMap(aggregatedTx ->
            handleParamProposal(aggregatedTx, txMap.get(aggregatedTx.getHash())).stream())
        .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(paramProposals)) {
      return Collections.emptyList();
    }

    return cachedParamProposalRepository.saveAll(paramProposals);
  }

  private List<ParamProposal> handleParamProposal(AggregatedTx aggregatedTx, Tx tx) {
    int epochNo = (int) aggregatedTx.getUpdate().getEpoch();

    Set<ParamProposal> paramProposals = aggregatedTx
        .getUpdate()
        .getProtocolParamUpdates()
        .entrySet()
        .stream()
        .map(entrySet -> {
          ProtocolParamUpdate protocolParamUpdate = entrySet.getValue();

          var minFreeA = toBigInteger(protocolParamUpdate.getMinFeeA());
          var minFeeB = toBigInteger(protocolParamUpdate.getMinFeeB());
          var maxBlockSize = toBigInteger(protocolParamUpdate.getMaxBlockSize());
          var maxTxSize = toBigInteger(protocolParamUpdate.getMaxTxSize());
          var maxBhSize = toBigInteger(protocolParamUpdate.getMaxBlockHeaderSize());
          var optimalPoolCount = toBigInteger(protocolParamUpdate.getOptimalPoolCount());
          var influence = toDouble(protocolParamUpdate.getPoolPledgeInfluence());
          var monetaryExpandRate = toDouble(protocolParamUpdate.getExpansionRate());
          var poolDeposit = protocolParamUpdate.getPoolDeposit();
          var treasuryGrowthRate = toDouble(protocolParamUpdate.getTreasuryGrowthRate());
          var decentralisation = toDouble(protocolParamUpdate.getDecentralisationParam());

          var cborEntropy = protocolParamUpdate.getExtraEntropy();
          String entropy = null;

          if (!CollectionUtils.isEmpty(cborEntropy) &&
              (int) cborEntropy.get(0) == BigInteger.ONE.intValue()) {
            entropy = cborEntropy.get(BigInteger.ONE.intValue()).toString();
          }

          var protocolMajor = protocolParamUpdate.getProtocolMajorVer();
          var protocolMinor = protocolParamUpdate.getProtocolMinorVer();
          var minUtxoValue = protocolParamUpdate.getMinUtxo();
          var minPoolCost = protocolParamUpdate.getMinPoolCost();
          var coinsPerUtxoSize = protocolParamUpdate.getAdaPerUtxoByte();
          var costModelRaw = protocolParamUpdate.getCostModels();
          CostModel costModel = null;
          if (Objects.nonNull(costModelRaw)) {
            costModel = costModelService.findCostModelByHash(costModelRaw.getHash());
          }
          var priceMem = toDouble(protocolParamUpdate.getPriceMem());
          var priceStep = toDouble(protocolParamUpdate.getPriceStep());
          var maxTxExMem = protocolParamUpdate.getMaxTxExMem();
          var maxTxExSteps = protocolParamUpdate.getMaxTxExSteps();
          var maxBlockExMem = protocolParamUpdate.getMaxBlockExMem();
          var maxBlockExSteps = protocolParamUpdate.getMaxBlockExSteps();
          var maxValSize = toBigInteger(protocolParamUpdate.getMaxValSize());

          var collateralPercent = protocolParamUpdate.getCollateralPercent();
          var maxCollateralInputs = protocolParamUpdate.getMaxCollateralInputs();

          return ParamProposal.builder()
              .key(entrySet.getKey())
              .epochNo(epochNo)
              .minFeeA(minFreeA)
              .minFeeB(minFeeB)
              .maxBlockSize(maxBlockSize)
              .maxTxSize(maxTxSize)
              .maxBhSize(maxBhSize)
              .optimalPoolCount(optimalPoolCount)
              .influence(influence)
              .monetaryExpandRate(monetaryExpandRate)
              .poolDeposit(poolDeposit)
              .treasuryGrowthRate(treasuryGrowthRate)
              .decentralisation(decentralisation)
              .entropy(entropy)
              .protocolMajor(protocolMajor)
              .protocolMinor(protocolMinor)
              .minUtxoValue(minUtxoValue)
              .minPoolCost(minPoolCost)
              .coinsPerUtxoSize(coinsPerUtxoSize)
              .costModel(costModel)
              .priceMem(priceMem)
              .priceStep(priceStep)
              .maxTxExMem(maxTxExMem)
              .maxTxExSteps(maxTxExSteps)
              .maxBlockExMem(maxBlockExMem)
              .maxBlockExSteps(maxBlockExSteps)
              .maxValSize(maxValSize)
              .collateralPercent(collateralPercent)
              .maxCollateralInputs(maxCollateralInputs)
              .registeredTx(tx)
              .build();
        }).collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(paramProposals)) {
      return Collections.emptyList();
    }

    return new ArrayList<>(paramProposals);
  }


  private BigInteger toBigInteger(Integer integer) {
    return Objects.isNull(integer) ? null : new BigInteger(String.valueOf(integer));
  }

  private BigInteger toBigInteger(Long longVal) {
    return Objects.isNull(longVal) ? null : new BigInteger(String.valueOf(longVal));
  }

  private Double toDouble(BigDecimal decimal) {
    return Objects.isNull(decimal) ? null : decimal.doubleValue();
  }
}
