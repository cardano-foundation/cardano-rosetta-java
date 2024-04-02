package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.BLOCK_NOT_FOUND;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.TRANSACTION_NOT_FOUND;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

  @Mock
  private LedgerDataProviderService ledgerDataProviderService;
  @InjectMocks
  private BlockServiceImpl blockService;
  private final String blockTxHash = "txHash1";


  @Test
  void getBlockByBlockRequest_OK() {

    //given
    long index = 1;
    String hash = "hash1";

    String genesisPath = "../config/preprod/shelley-genesis.json";
    ReflectionTestUtils.setField(blockService, "genesisPath", genesisPath);

    Block expected = newBlock();
    when(ledgerDataProviderService.findBlock(index, hash))
        .thenReturn(expected);

    //when
    Block block = blockService.findBlock(index, hash);

    //then
    assertThat(block).isEqualTo(expected);

  }

  @Test
  void getBlockByBlockRequest_OK_emptyTransactions() {

    //given
    long index = 1;
    String hash = "hash1";

    String genesisPath = "../config/preprod/shelley-genesis.json";
    ReflectionTestUtils.setField(blockService, "genesisPath", genesisPath);

    when(ledgerDataProviderService.findBlock(index, hash))
        .thenReturn(newBlock());

    //when
    Block block = blockService.findBlock(index, hash);

    //then
    assertThat(block.getTransactions()).isNull();


  }

  @Test
  void getBlockByBlockRequest_blockNotFoundException() {

    //given
    String genesisPath = "../config/preprod/shelley-genesis.json";
    ReflectionTestUtils.setField(blockService, "genesisPath", genesisPath);

    when(ledgerDataProviderService.findBlock(anyLong(), anyString()))
        .thenReturn(newBlock());

    //when
    try {
      blockService.findBlock(1L, "hash");
    } catch (ApiException e) {
      //then
      assertThat(e.getError().getCode()).isEqualTo(BLOCK_NOT_FOUND.getCode());
    }


  }

  @Test
  void getBlockByBlockRequest_canNotReadGenesis() {

    //given
    long index = 1;
    String hash = "hash1";

    String genesisPath = "badPath";
    ReflectionTestUtils.setField(blockService, "genesisPath", genesisPath);

    when(ledgerDataProviderService.findBlock(index, hash))
        .thenReturn(newBlock());

    //when
    try {
      blockService.findBlock(index, hash);
    } catch (ApiException e) {
      //then
      assertThat(e.getMessage())
          .isEqualTo("Could not read genesis file path");
    }


  }


  private Block newBlock() {
    return new Block(
        "hash1",
        1L,
        2L,
        "prevHashBlock1",
        21L,
        3L,
        "createdAt1",
        4, 5,
        6L, null,
        "poolDeposit1");
  }

  @Test
  void getBlockTransaction_Test_OK() {

    //given
    Tran tx = newTran();
    long blockId = 1L;
    String blockHash = "hash1";
    when(ledgerDataProviderService.findTransactionsByBlock(blockId, blockHash))
        .thenReturn(List.of(tx));

    //when
    Tran blockTransaction = blockService.getBlockTransaction(blockId, blockHash, blockTxHash);

    //then
    assertThat(blockTransaction).isEqualTo(tx);
  }


  @Test
  void getBlockTransaction_Test_notFoundTransaction() {

    //given
    Tran tx = newTran();
    long blockId = 1L;
    String blockHash = "hash1";
    when(ledgerDataProviderService.findTransactionsByBlock(blockId, blockHash))
        .thenReturn(List.of(Tran.builder().hash("any").build()));

    try {
      //when
      blockService.getBlockTransaction(blockId, blockHash, blockTxHash);

    } catch (ApiException e) {
      //then
      assertThat(e.getError().getCode()).isEqualTo(TRANSACTION_NOT_FOUND.getCode());
    }
  }

  private Tran newTran() {
    return Tran.builder()
        .hash(blockTxHash)
        .build();
  }


}