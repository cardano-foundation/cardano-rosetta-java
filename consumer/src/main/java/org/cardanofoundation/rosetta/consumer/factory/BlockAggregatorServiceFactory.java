package org.cardanofoundation.rosetta.consumer.factory;

import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.service.BlockAggregatorService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class BlockAggregatorServiceFactory extends
    AbstractServiceFactory<BlockAggregatorService<? extends CommonBlock>, BlockAggregatorService> { // NOSONAR

  public BlockAggregatorServiceFactory(
      List<BlockAggregatorService<? extends CommonBlock>> blockAggregatorServices) {
    super(blockAggregatorServices);
  }

  @Override
  void init() {
    serviceMap = services.stream()
        .collect(
            Collectors.toMap(
                BlockAggregatorService::supports,
                Function.identity()));
  }

  @SuppressWarnings({"unchecked"})
  public AggregatedBlock aggregateBlock(CommonBlock block) {
    return serviceMap.get(block.getClass()).aggregateBlock(block);
  }
}
