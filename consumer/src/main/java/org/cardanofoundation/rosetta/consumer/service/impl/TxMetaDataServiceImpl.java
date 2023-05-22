package org.cardanofoundation.rosetta.consumer.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxMetadata;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.common.ledgersync.AuxData;
import org.cardanofoundation.rosetta.common.util.JsonUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxMetadataRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.TxMetaDataService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TxMetaDataServiceImpl implements TxMetaDataService {

  private final CachedTxMetadataRepository cachedTxMetadataRepository;
  private final BlockDataService blockDataService;
  private final ObjectMapper mapper;

  @Override
  public List<TxMetadata> handleAuxiliaryDataMaps(Map<String, Tx> txMap) {
    List<TxMetadata> txMetadataList = new ArrayList<>();

    blockDataService.forEachAggregatedBlock(aggregatedBlock -> {
      if (CollectionUtils.isEmpty(aggregatedBlock.getAuxiliaryDataMap())) {
        return;
      }

      Map<Integer, AuxData> auxiliaryDataMap = aggregatedBlock.getAuxiliaryDataMap();
      aggregatedBlock.getTxList().stream()
          .filter(AggregatedTx::isValidContract)
          .filter(aggregatedTx -> auxiliaryDataMap.containsKey((int) aggregatedTx.getBlockIndex()))
          .flatMap(aggregatedTx -> {
            int txIndex = (int) aggregatedTx.getBlockIndex();
            AuxData auxData = auxiliaryDataMap.get(txIndex);
            String txHash = aggregatedTx.getHash();
            Tx tx = txMap.get(txHash);

            return handleAuxiliaryData(auxData, tx).stream();
          })
          .forEach(txMetadataList::add);
    });

    return cachedTxMetadataRepository.saveAll(txMetadataList);
  }

  private List<TxMetadata> handleAuxiliaryData(AuxData auxiliaryData, Tx tx) {
    if (!ObjectUtils.isEmpty(auxiliaryData.getMetadataCbor())) {
      try {
        Map<BigDecimal, Object> json = mapper.readValue(auxiliaryData.getMetadataJson(),
            new TypeReference<>() {
            });
        return json.entrySet().stream().map(entry -> {
          String metadataJson = null;

          if (Objects.nonNull(entry.getValue()) &&
              !entry.getValue().toString().contains(ConsumerConstant.BYTE_NULL)) {
            metadataJson = JsonUtil.getPrettyJson(entry.getValue());
          }

          return TxMetadata.builder()
              .tx(tx)
              .json(metadataJson)
              .key(entry.getKey().toBigInteger())
              .bytes(auxiliaryData.getMetadataCbor()
                  .get(entry.getKey())
                  .getBytes())
              .build();
        }).distinct().collect(Collectors.toList());
      } catch (Exception ex) {
        log.error("Tx hash: {}, meta data json: {}, mess: {}", tx.getHash(),auxiliaryData.getMetadataJson(), ex.getMessage());
        throw new IllegalStateException();
      }
    }

    return Collections.emptyList();
  }
}
