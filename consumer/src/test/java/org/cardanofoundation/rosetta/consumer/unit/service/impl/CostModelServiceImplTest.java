package org.cardanofoundation.rosetta.consumer.unit.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.bloxbean.cardano.client.transaction.spec.Language;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cardanofoundation.rosetta.common.entity.CostModel;
import org.cardanofoundation.rosetta.common.ledgersync.ProtocolParamUpdate;
import org.cardanofoundation.rosetta.common.ledgersync.Update;
import org.cardanofoundation.rosetta.common.ledgersync.mdl.CostModels;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.CostModelRepository;
import org.cardanofoundation.rosetta.consumer.service.impl.CostModelServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CostModelServiceImplTest {

  @Test
  void getGenesisCostModelDoNotSave() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);
    CostModel costModel = Mockito.mock(CostModel.class);
    CostModel costModelFinded = Mockito.mock(CostModel.class);
    Optional<CostModel> costModelOption = Optional.of(costModelFinded);

    Mockito.when(costModel.getHash()).thenReturn("thx00");
    Mockito.when(costModelRepository.findByHash("thx00")).thenReturn(costModelOption);
    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);

    costModelService.setGenesisCostModel(costModel);
    Mockito.verify(costModelRepository, Mockito.times(0)).save(Mockito.any());
    assertEquals(costModelFinded, costModelService.getGenesisCostModel());
  }

  @Test
  void getGenesisCostModelSave() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);
    CostModel costModel = Mockito.mock(CostModel.class);
    Optional<CostModel> costModelOption = Optional.ofNullable(null);

    Mockito.when(costModel.getHash()).thenReturn("thx00");
    Mockito.when(costModelRepository.findByHash("thx00")).thenReturn(costModelOption);
    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);

    costModelService.setGenesisCostModel(costModel);
    Mockito.verify(costModelRepository, Mockito.times(1)).save(costModel);
    //Genesis cost = null?
    assertNull(costModelService.getGenesisCostModel());
  }


  @Test
  void handleCostModelNoUpdates() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);
    AggregatedTx tx = Mockito.mock(AggregatedTx.class);
    Update update = Mockito.mock(Update.class);

    Mockito.when(tx.getUpdate()).thenReturn(update);
    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);
    costModelService.handleCostModel(tx);
  }

  @Test
  void handleCostModelPlutusV1() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);
    AggregatedTx tx = Mockito.mock(AggregatedTx.class);
    Update update = Mockito.mock(Update.class);
    ProtocolParamUpdate protocolParamUpdate = Mockito.mock(ProtocolParamUpdate.class);
    Map<String, ProtocolParamUpdate> protocolParamUpdates = new HashMap<>();
    Map<Language, List<BigInteger>> languageListMap = new HashMap<>();
    Language languageCostModel = Language.PLUTUS_V1;
    List<BigInteger> languageList = Arrays.asList(BigInteger.valueOf(0));
    languageListMap.put(languageCostModel, languageList);
    protocolParamUpdates.put("0", protocolParamUpdate);
    CostModels costModel = Mockito.mock(CostModels.class);

    Mockito.when(tx.getUpdate()).thenReturn(update);
    Mockito.when(update.getProtocolParamUpdates()).thenReturn(protocolParamUpdates);
    Mockito.when(protocolParamUpdate.getCostModels()).thenReturn(costModel);
    Mockito.when(costModel.getLanguages()).thenReturn(languageListMap);
    Mockito.when(costModel.getHash()).thenReturn("X0ksd");

    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);
    costModelService.handleCostModel(tx);
    Mockito.verify(costModelRepository,Mockito.times(1)).saveAll(Mockito.anyCollection());
  }

  @Test
  void handleCostModelPlutusV2() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);
    AggregatedTx tx = Mockito.mock(AggregatedTx.class);
    Update update = Mockito.mock(Update.class);
    ProtocolParamUpdate protocolParamUpdate = Mockito.mock(ProtocolParamUpdate.class);
    Map<String, ProtocolParamUpdate> protocolParamUpdates = new HashMap<>();
    Map<Language, List<BigInteger>> languageListMap = new HashMap<>();
    Language languageCostModel = Language.PLUTUS_V2;
    List<BigInteger> languageList = Arrays.asList(BigInteger.valueOf(1));
    languageListMap.put(languageCostModel, languageList);
    protocolParamUpdates.put("0", protocolParamUpdate);
    CostModels costModel = Mockito.mock(CostModels.class);

    Mockito.when(tx.getUpdate()).thenReturn(update);
    Mockito.when(update.getProtocolParamUpdates()).thenReturn(protocolParamUpdates);
    Mockito.when(protocolParamUpdate.getCostModels()).thenReturn(costModel);
    Mockito.when(costModel.getLanguages()).thenReturn(languageListMap);
    Mockito.when(costModel.getHash()).thenReturn("X0ksd");

    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);
    costModelService.handleCostModel(tx);
    Mockito.verify(costModelRepository,Mockito.times(1)).saveAll(Mockito.anyCollection());
  }

  @Test
  void findCostModelByHashReturnNull() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);

    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);

    assertNull(costModelService.findCostModelByHash("thx00"));
  }

  @Test
  void findCostModelByHashReturnCostModel() {
    CostModelRepository costModelRepository = Mockito.mock(CostModelRepository.class);
    CostModel costModel = Mockito.mock(CostModel.class);
    CostModel costModelFinded = Mockito.mock(CostModel.class);
    Optional<CostModel> costModelOption = Optional.of(costModelFinded);

    Mockito.when(costModel.getHash()).thenReturn("thx00");
    Mockito.when(costModelRepository.findByHash("thx00")).thenReturn(costModelOption);

    CostModelServiceImpl costModelService = new CostModelServiceImpl(costModelRepository);

    assertEquals(costModelFinded, costModelService.findCostModelByHash("thx00"));
  }

}
