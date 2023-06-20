package org.cardanofoundation.rosetta.consumer.unit.service.impl.block;

import java.util.List;
import java.util.Optional;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedSlotLeader;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.EpochParamService;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.cardanofoundation.rosetta.consumer.service.SlotLeaderService;
import org.cardanofoundation.rosetta.consumer.service.TransactionService;
import org.cardanofoundation.rosetta.consumer.service.impl.block.BlockSyncServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

@ExtendWith(MockitoExtension.class)
public class BlockSyncServiceImplTest {

  @Mock
  BlockRepository blockRepository;

  @Mock
  TxRepository txRepository;

  @Mock
  TransactionService transactionService;

  @Mock
  BlockDataService blockDataService;

  @Mock
  SlotLeaderService slotLeaderService;

  @Mock
  EpochService epochService;

  @Mock
  EpochParamService epochParamService;



  @Test
  @DisplayName("Should skip block syncing on empty block batch")
  void shouldSkipBlockSyncWithNoBlocksTest() {
    Mockito.when(blockDataService.getBlockSize()).thenReturn(0);

    BlockSyncServiceImpl victim = new BlockSyncServiceImpl(
        blockRepository, txRepository, transactionService, blockDataService,
        slotLeaderService, epochService, epochParamService
    );
    victim.startBlockSyncing();
    Mockito.verifyNoInteractions(blockRepository);
    Mockito.verifyNoInteractions(txRepository);
    Mockito.verifyNoInteractions(transactionService);
    Mockito.verify(blockDataService, Mockito.times(1)).getBlockSize();
    Mockito.verifyNoMoreInteractions(blockDataService);
    Mockito.verifyNoInteractions(slotLeaderService);
    Mockito.verifyNoInteractions(epochService);
    Mockito.verifyNoInteractions(epochParamService);

  }

  @Test
  @DisplayName("Block sync should fail if previous block not found")
  void shouldFailOnNoPreviousBlockTest() {
    AggregatedBlock aggregatedBlock = Mockito.mock(AggregatedBlock.class);

    // Prev hash from block 46 preprod
    Mockito.when(aggregatedBlock.getPrevBlockHash())
        .thenReturn("45899e8002b27df291e09188bfe3aeb5397ac03546a7d0ead93aa2500860f1af");
    Mockito.when(blockDataService.getBlockSize()).thenReturn(1);
    Mockito.when(blockDataService.getFirstAndLastBlock())
        .thenReturn(Pair.of(aggregatedBlock, aggregatedBlock));
    Mockito.when(blockDataService.getAllAggregatedBlocks()).thenReturn(List.of(aggregatedBlock));

    BlockSyncServiceImpl victim = new BlockSyncServiceImpl(
        blockRepository, txRepository, transactionService, blockDataService,
        slotLeaderService, epochService, epochParamService
    );
    Assertions.assertThrows(IllegalStateException.class, victim::startBlockSyncing);

    Mockito.verify(blockRepository, Mockito.times(1)).findBlockByHash(Mockito.anyString());
    Mockito.verifyNoInteractions(txRepository);
    Mockito.verifyNoInteractions(transactionService);
    Mockito.verify(blockDataService, Mockito.times(1)).getBlockSize();
    Mockito.verify(blockDataService, Mockito.times(1)).getFirstAndLastBlock();
    Mockito.verify(blockDataService, Mockito.times(1)).getAllAggregatedBlocks();
    Mockito.verifyNoMoreInteractions(blockDataService);
    Mockito.verifyNoInteractions(slotLeaderService);
    Mockito.verifyNoInteractions(epochService);
    Mockito.verifyNoInteractions(epochParamService);
  }

  @Test
  @DisplayName("Should do block sync with aggregated block having null slot leader successfully")
  void shouldSyncBlockWithNullSlotLeaderSuccessfullyTest() {
    AggregatedBlock aggregatedBlock = Mockito.mock(AggregatedBlock.class);

    // Prev hash from block 0 preprod
    Mockito.when(aggregatedBlock.getPrevBlockHash())
        .thenReturn("d4b8de7a11d929a323373cbab6c1a9bdc931beffff11db111cf9d57356ee1937");
    Mockito.when(blockDataService.getBlockSize()).thenReturn(1);
    Mockito.when(blockDataService.getFirstAndLastBlock())
        .thenReturn(Pair.of(aggregatedBlock, aggregatedBlock));
    Mockito.when(blockDataService.getAllAggregatedBlocks()).thenReturn(List.of(aggregatedBlock));
    Mockito.when(blockRepository.findBlockByHash(Mockito.anyString()))
        .thenReturn(Optional.of(Mockito.mock(Block.class)));

    BlockSyncServiceImpl victim = new BlockSyncServiceImpl(
        blockRepository, txRepository, transactionService, blockDataService,
        slotLeaderService, epochService, epochParamService
    );
    victim.startBlockSyncing();

    Mockito.verify(blockRepository, Mockito.times(1)).findBlockByHash(Mockito.anyString());
    Mockito.verify(blockRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(blockRepository);
    Mockito.verify(txRepository, Mockito.times(1)).findFirstByOrderByIdDesc();
    Mockito.verifyNoMoreInteractions(txRepository);
    Mockito.verify(transactionService, Mockito.times(1))
        .prepareAndHandleTxs(Mockito.anyMap(), Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(transactionService);
    Mockito.verify(blockDataService, Mockito.times(1)).getBlockSize();
    Mockito.verify(blockDataService, Mockito.times(1)).getFirstAndLastBlock();
    Mockito.verify(blockDataService, Mockito.times(1)).getAllAggregatedBlocks();
    Mockito.verify(blockDataService, Mockito.times(1)).clearBatchBlockData();
    Mockito.verifyNoMoreInteractions(blockDataService);
    Mockito.verifyNoMoreInteractions(slotLeaderService);
    Mockito.verify(epochService, Mockito.times(1)).handleEpoch(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(epochService);
    Mockito.verify(epochParamService, Mockito.times(1)).handleEpochParams();
    Mockito.verifyNoMoreInteractions(epochParamService);

  }

  @Test
  @DisplayName("Should do block sync successfully")
  void shouldSyncSuccessfullyTest() {
    AggregatedBlock aggregatedBlock = Mockito.mock(AggregatedBlock.class);
    AggregatedSlotLeader slotLeader = Mockito.mock(AggregatedSlotLeader.class);

    // Prev hash, slot leader from block 46 preprod
    Mockito.when(aggregatedBlock.getPrevBlockHash())
        .thenReturn("45899e8002b27df291e09188bfe3aeb5397ac03546a7d0ead93aa2500860f1af");
    Mockito.when(aggregatedBlock.getSlotLeader()).thenReturn(slotLeader);
    Mockito.when(slotLeader.getHashRaw())
        .thenReturn("aae9293510344ddd636364c2673e34e03e79e3eefa8dbaa70e326f7d");
    Mockito.when(slotLeader.getPrefix()).thenReturn(ConsumerConstant.SHELLEY_SLOT_LEADER_PREFIX);
    Mockito.when(blockDataService.getBlockSize()).thenReturn(1);
    Mockito.when(blockDataService.getFirstAndLastBlock())
        .thenReturn(Pair.of(aggregatedBlock, aggregatedBlock));
    Mockito.when(blockDataService.getAllAggregatedBlocks()).thenReturn(List.of(aggregatedBlock));
    Mockito.when(blockRepository.findBlockByHash(Mockito.anyString()))
        .thenReturn(Optional.of(Mockito.mock(Block.class)));

    BlockSyncServiceImpl victim = new BlockSyncServiceImpl(
        blockRepository, txRepository, transactionService, blockDataService,
        slotLeaderService, epochService, epochParamService
    );
    victim.startBlockSyncing();

    Mockito.verify(blockRepository, Mockito.times(1)).findBlockByHash(Mockito.anyString());
    Mockito.verify(blockRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(blockRepository);
    Mockito.verify(txRepository, Mockito.times(1)).findFirstByOrderByIdDesc();
    Mockito.verifyNoMoreInteractions(txRepository);
    Mockito.verify(transactionService, Mockito.times(1))
        .prepareAndHandleTxs(Mockito.anyMap(), Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(transactionService);
    Mockito.verify(blockDataService, Mockito.times(1)).getBlockSize();
    Mockito.verify(blockDataService, Mockito.times(1)).getFirstAndLastBlock();
    Mockito.verify(blockDataService, Mockito.times(1)).getAllAggregatedBlocks();
    Mockito.verify(blockDataService, Mockito.times(1)).clearBatchBlockData();
    Mockito.verifyNoMoreInteractions(blockDataService);
    Mockito.verify(slotLeaderService, Mockito.times(1))
        .getSlotLeader(Mockito.anyString(), Mockito.anyString());
    Mockito.verifyNoMoreInteractions(slotLeaderService);
    Mockito.verify(epochService, Mockito.times(1)).handleEpoch(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(epochService);
    Mockito.verify(epochParamService, Mockito.times(1)).handleEpochParams();
    Mockito.verifyNoMoreInteractions(epochParamService);
  }

}
