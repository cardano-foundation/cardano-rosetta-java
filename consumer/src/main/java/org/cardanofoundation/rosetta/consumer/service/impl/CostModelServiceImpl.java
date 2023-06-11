package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.transaction.spec.Language;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.CostModel;
import org.cardanofoundation.rosetta.common.ledgersync.ProtocolParamUpdate;
import org.cardanofoundation.rosetta.common.ledgersync.mdl.PlutusV1Keys;
import org.cardanofoundation.rosetta.common.ledgersync.mdl.PlutusV2Keys;
import org.cardanofoundation.rosetta.common.util.JsonUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.CostModelRepository;
import org.cardanofoundation.rosetta.consumer.service.CostModelService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CostModelServiceImpl implements CostModelService {

  final CostModelRepository costModelRepository;
  CostModel genesisCostModel;


  @Override
  public CostModel getGenesisCostModel() {
    return this.genesisCostModel;
  }

  @Override
  public void setGenesisCostModel(CostModel costModel) {
    setup(costModel);
  }


  @Override
  public void handleCostModel(AggregatedTx tx) {
    if (CollectionUtils.isEmpty(tx.getUpdate().getProtocolParamUpdates())) {
      return;
    }

    Map<String, CostModel> costModels = tx.getUpdate().getProtocolParamUpdates().values().stream()
        .map(ProtocolParamUpdate::getCostModels)
        .filter(costMdl -> !CollectionUtils.isEmpty(costMdl.getLanguages()))
        .map(costModelMessage -> {
          var languageMap = costModelMessage.getLanguages()
              .keySet()
              .stream()
              .collect(Collectors.toMap(this::getPlutusKey,
                  language -> getPlutusValue(language,
                      costModelMessage.getLanguages()
                          .get(language))));

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

  public void setup(CostModel costModel) {
    costModelRepository.findByHash(costModel.getHash())
        .ifPresentOrElse(cm -> genesisCostModel = cm, () ->
            costModelRepository.save(costModel)
        );
  }
}
