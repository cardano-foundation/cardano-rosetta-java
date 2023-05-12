package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.transaction.spec.Language;
import jakarta.annotation.PostConstruct;
import org.cardanofoundation.rosetta.common.entity.CostModel;
import org.cardanofoundation.rosetta.common.ledgersync.ProtocolParamUpdate;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.common.ledgersync.mdl.PlutusV1Keys;
import org.cardanofoundation.rosetta.common.ledgersync.mdl.PlutusV2Keys;
import org.cardanofoundation.rosetta.common.util.JsonUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.CostModelRepository;
import org.cardanofoundation.rosetta.consumer.service.CostModelService;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CostModelServiceImpl implements CostModelService {

  final BlockRepository blockRepository;
  final CostModelRepository costModelRepository;
  CostModel genesisCostModel;
  @Value("${genesis.network}")
  Integer network;

  @PostConstruct
  public void setup() {
    var blockHeight = blockRepository.getBlockHeight();
    var costModelId = costModelRepository.findCostModeMaxId();

    switch (network){
      case Constant.MAINNET:
      case Constant.PREPROD_TESTNET:
        genesisCostModel = CostModel.builder()
            .costs("{\"PlutusV1\":mainnet{\"bData-cpu-arguments\":mainnet150000,mainnet\"iData-cpu-arguments\":mainnet150000,mainnet\"trace-cpu-arguments\":mainnet150000,mainnet\"mkCons-cpu-arguments\":mainnet150000,mainnet\"fstPair-cpu-arguments\":mainnet150000,mainnet\"mapData-cpu-arguments\":mainnet150000,mainnet\"sndPair-cpu-arguments\":mainnet150000,mainnet\"unBData-cpu-arguments\":mainnet150000,mainnet\"unIData-cpu-arguments\":mainnet150000,mainnet\"bData-memory-arguments\":mainnet32,mainnet\"cekLamCost-exBudgetCPU\":mainnet29773,mainnet\"cekVarCost-exBudgetCPU\":mainnet29773,mainnet\"headList-cpu-arguments\":mainnet150000,mainnet\"iData-memory-arguments\":mainnet32,mainnet\"listData-cpu-arguments\":mainnet150000,mainnet\"nullList-cpu-arguments\":mainnet150000,mainnet\"tailList-cpu-arguments\":mainnet150000,mainnet\"trace-memory-arguments\":mainnet32,mainnet\"mkCons-memory-arguments\":mainnet32,mainnet\"mkNilData-cpu-arguments\":mainnet150000,mainnet\"unMapData-cpu-arguments\":mainnet150000,mainnet\"cekApplyCost-exBudgetCPU\":mainnet29773,mainnet\"cekConstCost-exBudgetCPU\":mainnet29773,mainnet\"cekDelayCost-exBudgetCPU\":mainnet29773,mainnet\"cekForceCost-exBudgetCPU\":mainnet29773,mainnet\"chooseData-cpu-arguments\":mainnet150000,mainnet\"chooseList-cpu-arguments\":mainnet150000,mainnet\"chooseUnit-cpu-arguments\":mainnet150000,mainnet\"constrData-cpu-arguments\":mainnet150000,mainnet\"fstPair-memory-arguments\":mainnet32,mainnet\"ifThenElse-cpu-arguments\":mainnet1,mainnet\"mapData-memory-arguments\":mainnet32,mainnet\"mkPairData-cpu-arguments\":mainnet150000,mainnet\"sndPair-memory-arguments\":mainnet32,mainnet\"unBData-memory-arguments\":mainnet32,mainnet\"unIData-memory-arguments\":mainnet32,mainnet\"unListData-cpu-arguments\":mainnet150000,mainnet\"cekLamCost-exBudgetMemory\":mainnet100,mainnet\"cekVarCost-exBudgetMemory\":mainnet100,mainnet\"headList-memory-arguments\":mainnet32,mainnet\"listData-memory-arguments\":mainnet32,mainnet\"nullList-memory-arguments\":mainnet32,mainnet\"sha2_256-memory-arguments\":mainnet4,mainnet\"sha3_256-memory-arguments\":mainnet4,mainnet\"tailList-memory-arguments\":mainnet32,mainnet\"cekBuiltinCost-exBudgetCPU\":mainnet29773,mainnet\"cekStartupCost-exBudgetCPU\":mainnet100,mainnet\"mkNilData-memory-arguments\":mainnet32,mainnet\"unConstrData-cpu-arguments\":mainnet150000,mainnet\"unMapData-memory-arguments\":mainnet32,mainnet\"cekApplyCost-exBudgetMemory\":mainnet100,mainnet\"cekConstCost-exBudgetMemory\":mainnet100,mainnet\"cekDelayCost-exBudgetMemory\":mainnet100,mainnet\"cekForceCost-exBudgetMemory\":mainnet100,mainnet\"chooseData-memory-arguments\":mainnet32,mainnet\"chooseList-memory-arguments\":mainnet32,mainnet\"chooseUnit-memory-arguments\":mainnet32,mainnet\"constrData-memory-arguments\":mainnet32,mainnet\"equalsData-memory-arguments\":mainnet1,mainnet\"ifThenElse-memory-arguments\":mainnet1,mainnet\"mkNilPairData-cpu-arguments\":mainnet150000,mainnet\"mkPairData-memory-arguments\":mainnet32,mainnet\"unListData-memory-arguments\":mainnet32,mainnet\"blake2b_256-memory-arguments\":mainnet4,mainnet\"sha2_256-cpu-arguments-slope\":mainnet29175,mainnet\"sha3_256-cpu-arguments-slope\":mainnet82363,mainnet\"cekBuiltinCost-exBudgetMemory\":mainnet100,mainnet\"cekStartupCost-exBudgetMemory\":mainnet100,mainnet\"equalsString-memory-arguments\":mainnet1,mainnet\"indexByteString-cpu-arguments\":mainnet150000,mainnet\"unConstrData-memory-arguments\":mainnet32,mainnet\"addInteger-cpu-arguments-slope\":mainnet0,mainnet\"decodeUtf8-cpu-arguments-slope\":mainnet1000,mainnet\"encodeUtf8-cpu-arguments-slope\":mainnet1000,mainnet\"equalsData-cpu-arguments-slope\":mainnet10000,mainnet\"equalsInteger-memory-arguments\":mainnet1,mainnet\"mkNilPairData-memory-arguments\":mainnet32,mainnet\"blake2b_256-cpu-arguments-slope\":mainnet29175,mainnet\"appendString-cpu-arguments-slope\":mainnet1000,mainnet\"equalsString-cpu-arguments-slope\":mainnet1000,mainnet\"indexByteString-memory-arguments\":mainnet1,mainnet\"lengthOfByteString-cpu-arguments\":mainnet150000,mainnet\"lessThanInteger-memory-arguments\":mainnet1,mainnet\"sha2_256-cpu-arguments-intercept\":mainnet2477736,mainnet\"sha3_256-cpu-arguments-intercept\":mainnet0,mainnet\"addInteger-memory-arguments-slope\":mainnet1,mainnet\"decodeUtf8-memory-arguments-slope\":mainnet8,mainnet\"encodeUtf8-memory-arguments-slope\":mainnet8,mainnet\"equalsByteString-memory-arguments\":mainnet1,mainnet\"equalsInteger-cpu-arguments-slope\":mainnet1326,mainnet\"modInteger-cpu-arguments-constant\":mainnet148000,mainnet\"modInteger-memory-arguments-slope\":mainnet1,mainnet\"addInteger-cpu-arguments-intercept\":mainnet197209,mainnet\"consByteString-cpu-arguments-slope\":mainnet1000,mainnet\"decodeUtf8-cpu-arguments-intercept\":mainnet150000, \"encodeUtf8-cpu-arguments-intercept\": 150000, \"equalsData-cpu-arguments-intercept\": 150000, \"appendString-memory-arguments-slope\": 1, \"blake2b_256-cpu-arguments-intercept\": 2477736, \"equalsString-cpu-arguments-constant\": 1000, \"lengthOfByteString-memory-arguments\": 4, \"lessThanByteString-memory-arguments\": 1, \"lessThanInteger-cpu-arguments-slope\": 497, \"modInteger-memory-arguments-minimum\": 1, \"multiplyInteger-cpu-arguments-slope\": 11218, \"sliceByteString-cpu-arguments-slope\": 5000, \"subtractInteger-cpu-arguments-slope\": 0, \"appendByteString-cpu-arguments-slope\": 621, \"appendString-cpu-arguments-intercept\": 150000, \"divideInteger-cpu-arguments-constant\": 148000, \"divideInteger-memory-arguments-slope\": 1, \"equalsByteString-cpu-arguments-slope\": 247, \"equalsString-cpu-arguments-intercept\": 150000, \"addInteger-memory-arguments-intercept\": 1, \"consByteString-memory-arguments-slope\": 1, \"decodeUtf8-memory-arguments-intercept\": 0, \"encodeUtf8-memory-arguments-intercept\": 0, \"equalsInteger-cpu-arguments-intercept\": 136542, \"modInteger-memory-arguments-intercept\": 0, \"consByteString-cpu-arguments-intercept\": 150000, \"divideInteger-memory-arguments-minimum\": 1, \"lessThanByteString-cpu-arguments-slope\": 248, \"lessThanEqualsInteger-memory-arguments\": 1, \"multiplyInteger-memory-arguments-slope\": 1, \"quotientInteger-cpu-arguments-constant\": 148000, \"quotientInteger-memory-arguments-slope\": 1, \"sliceByteString-memory-arguments-slope\": 1, \"subtractInteger-memory-arguments-slope\": 1, \"appendByteString-memory-arguments-slope\": 1, \"appendString-memory-arguments-intercept\": 0, \"equalsByteString-cpu-arguments-constant\": 150000, \"lessThanInteger-cpu-arguments-intercept\": 179690, \"multiplyInteger-cpu-arguments-intercept\": 61516, \"remainderInteger-cpu-arguments-constant\": 148000, \"remainderInteger-memory-arguments-slope\": 1, \"sliceByteString-cpu-arguments-intercept\": 150000, \"subtractInteger-cpu-arguments-intercept\": 197209, \"verifyEd25519Signature-memory-arguments\": 1, \"appendByteString-cpu-arguments-intercept\": 396231, \"divideInteger-memory-arguments-intercept\": 0, \"equalsByteString-cpu-arguments-intercept\": 112536, \"quotientInteger-memory-arguments-minimum\": 1, \"consByteString-memory-arguments-intercept\": 0, \"lessThanEqualsByteString-memory-arguments\": 1, \"lessThanEqualsInteger-cpu-arguments-slope\": 1366, \"remainderInteger-memory-arguments-minimum\": 1, \"lessThanByteString-cpu-arguments-intercept\": 103599, \"multiplyInteger-memory-arguments-intercept\": 0, \"quotientInteger-memory-arguments-intercept\": 0, \"sliceByteString-memory-arguments-intercept\": 0, \"subtractInteger-memory-arguments-intercept\": 1, \"verifyEd25519Signature-cpu-arguments-slope\": 1, \"appendByteString-memory-arguments-intercept\": 0, \"remainderInteger-memory-arguments-intercept\": 0, \"lessThanEqualsByteString-cpu-arguments-slope\": 248, \"lessThanEqualsInteger-cpu-arguments-intercept\": 145276, \"modInteger-cpu-arguments-model-arguments-slope\": 118, \"verifyEd25519Signature-cpu-arguments-intercept\": 3345831, \"lessThanEqualsByteString-cpu-arguments-intercept\": 103599, \"divideInteger-cpu-arguments-model-arguments-slope\": 118, \"modInteger-cpu-arguments-model-arguments-intercept\": 425507, \"quotientInteger-cpu-arguments-model-arguments-slope\": 118, \"remainderInteger-cpu-arguments-model-arguments-slope\": 118, \"divideInteger-cpu-arguments-model-arguments-intercept\": 425507, \"quotientInteger-cpu-arguments-model-arguments-intercept\": 425507, \"remainderInteger-cpu-arguments-model-arguments-intercept\": 425507}}")
            .hash("144099AE3A42F67FA87A8B1A0144A7F243E6B87D8C6F080FC21C67FDD55D6478")
            .build();
        break;
      default:
        throw new RuntimeException("not support yet");
    }

    if (blockHeight.isEmpty() && costModelId.isEmpty()) {
      costModelRepository.save(genesisCostModel);
      return;
    }

    costModelRepository.findByHash(genesisCostModel.getHash())
        .ifPresentOrElse(cm -> genesisCostModel = cm,
            () -> {
              log.error("No genesis cost model");
              System.exit(0);
            });


  }

  @Override
  public CostModel getGenesisCostModel() {
    return this.genesisCostModel;
  }

  @Override
  public void handleCostModel(AggregatedTx tx) {
    if (CollectionUtils.isEmpty(tx.getUpdate().getProtocolParamUpdates())) {
      return;
    }

    Map<String, CostModel> costModels = tx
        .getUpdate()
        .getProtocolParamUpdates()
        .values()
        .stream()
        .map(ProtocolParamUpdate::getCostModels)
        .filter(costMdl -> !CollectionUtils.isEmpty(costMdl.getLanguages()))
        .map(costModelMessage -> {
          var languageMap = costModelMessage.getLanguages()
              .keySet()
              .stream()
              .collect(Collectors.toMap(this::getPlutusKey,
                  language -> getPlutusValue(language,
                      costModelMessage.getLanguages().get(language))));

          var json = JsonUtil.getPrettyJson(languageMap);

          return CostModel.builder()
              .costs(json)
              .hash(costModelMessage.getHash())
              .build();
        }).collect(Collectors.toConcurrentMap(CostModel::getHash, Function.identity()
            , (past, future) -> future));

    if (!ObjectUtils.isEmpty(costModels)) {
      costModelRepository.existHash(
              costModels.keySet())
          .forEach(costModels::remove);
      costModelRepository.saveAll(costModels.values());
    }
  }

  @Override
  public CostModel findCostModelByHash(String hash) {
    var costModelOptional = costModelRepository.findByHash(hash);
    return costModelOptional.orElse(null);
  }

  private String getPlutusKey(Language language) {
    switch (language) {
      case PLUTUS_V1:
        return PLUTUS_V1_KEY;
      case PLUTUS_V2:
        return PLUTUS_V2_KEY;
      default:
        log.error("Un handle language {}", language);
        System.exit(1);
    }
    return null;
  }

  private Map<String, BigInteger> getPlutusValue(Language language, List<BigInteger> values) {
    switch (language) {
      case PLUTUS_V1:
        return new PlutusV1Keys().getCostModelMap(values);
      case PLUTUS_V2:
        return new PlutusV2Keys().getCostModelMap(values);
      default:
        log.error("Un handle language {}", language);
        System.exit(1);
    }
    return Collections.emptyMap();
  }
}
