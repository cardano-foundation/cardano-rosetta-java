package org.cardanofoundation.rosetta.api.block.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.BLOCK_NOT_FOUND;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.TRANSACTION_NOT_FOUND;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {
  @Mock
  private LedgerBlockService ledgerBlockService;
  @InjectMocks
  private BlockServiceImpl blockService;
  @Mock
  private ProtocolParamService protocolParamService;
  @Mock
  private ProtocolParams protocolParams;

  public void givenProtocolParam() {
    when(protocolParamService.getProtocolParameters()).thenReturn(protocolParams);
    when(protocolParams.getPoolDeposit()).thenReturn(BigInteger.TEN);
  }

  @Test
  void getBlockByBlockRequest_OK() {
    //given
    givenProtocolParam();
    long index = 1;
    String hash = "hash1";
    Optional<Block> expected = newBlock();
    when(ledgerBlockService.findBlock(index, hash)).thenReturn(expected);
    //when
    Block block = blockService.findBlock(index, hash);
    //then
    assertThat(block).isEqualTo(expected.orElse(null)); //idea complains on .get()
  }

  @Test
  void getBlockByBlockRequest_OK_emptyTransactions() {
    //given
    givenProtocolParam();
    long index = 1;
    String hash = "hash1";
    when(ledgerBlockService.findBlock(index, hash)).thenReturn(newBlock());
    //when
    Block block = blockService.findBlock(index, hash);
    //then
    assertThat(block.getTransactions()).isNull();
  }

  @Test
  void getBlockByBlockRequest_blockNotFoundException() {
    //given
    givenProtocolParam();
    when(ledgerBlockService.findBlock(anyLong(), anyString())).thenReturn(newBlock());
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
    givenProtocolParam();
    long index = 1;
    String hash = "hash1";
    when(ledgerBlockService.findBlock(index, hash)).thenReturn(newBlock());
    try {
      //when
      blockService.findBlock(index, hash);
    } catch (ApiException e) {
      //then
      assertThat(e.getMessage())
          .isEqualTo("Could not read genesis file path");
    }
  }


  private Optional<Block> newBlock() {
    return Optional.of(new Block(
        "hash1",
        1L,
        2L,
        "prevHashBlock1",
        21L,
        3L,
        4,
        "createdAt1",
        4,
        6L, null,
        "poolDeposit1"
        ));
  }

  @Test
  void getBlockTransaction_Test_OK() {
    //given
    String txHash = "txHash1";
    BlockTx tx = newTran(txHash);
    long blockId = 1L;
    String blockHash = "hash1";
    when(ledgerBlockService.findTransactionsByBlock(blockId, blockHash))
        .thenReturn(List.of(tx));
    //when
    BlockTx blockTransaction = blockService.getBlockTransaction(blockId, blockHash, txHash);
    //then
    assertThat(blockTransaction).isEqualTo(tx);
  }


  @Test
  void getBlockTransaction_Test_notFoundTransaction() {
    //given
    long blockId = 1L;
    String blockHash = "hash1";
    when(ledgerBlockService.findTransactionsByBlock(blockId, blockHash))
        .thenReturn(List.of(newTran("any")));
    try {
      //when
      blockService.getBlockTransaction(blockId, blockHash, "differentFromAny");
    } catch (ApiException e) {
      //then
      assertThat(e.getError().getCode()).isEqualTo(TRANSACTION_NOT_FOUND.getCode());
    }
  }

  private BlockTx newTran(String hash) {
    return BlockTx.builder().hash(hash).build();
  }


}
