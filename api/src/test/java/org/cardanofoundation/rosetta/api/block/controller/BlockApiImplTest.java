package org.cardanofoundation.rosetta.api.block.controller;

import java.lang.reflect.Field;
import java.math.BigInteger;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static org.cardanofoundation.rosetta.EntityGenerator.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlockApiImplTest extends BaseSpringMvcSetup {

  @MockitoBean
  private BlockService blockService;

  @MockitoBean
  private ProtocolParamService protocolParamService;

  @MockitoBean
  BlockMapper blockMapper;

  @Mock
  private ProtocolParams protocolParams;

  @InjectMocks
  private BlockApiImpl blockApi;

  @Test
  void blockInvalidIndex() {
    BlockRequest blockRequest = newBlockRequest();
    PartialBlockIdentifier partialBlockIdentifier = new PartialBlockIdentifier(-1L, blockRequest.getBlockIdentifier().getHash());

    blockRequest.setBlockIdentifier(partialBlockIdentifier);
    assertThrows(ExceptionFactory.invalidBlockIdentifier(-1L).getClass(), () -> blockApi.block(blockRequest));
  }

  @Test
  void blockOfflineModeTest() throws NoSuchFieldException, IllegalAccessException {
    //given
    BlockRequest blockRequest = newBlockRequest();
    //when
    Field field = BlockApiImpl.class.getDeclaredField("offlineMode");
    field.setAccessible(true);
    field.set(blockApi, true);
    //then
    assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), () -> blockApi.block(blockRequest));
  }

  @Test
  void blockTransactionInvalidIndex() {
    BlockTransactionRequest blockTransactionRequest = newBlockTransactionRequest();
    blockTransactionRequest.setBlockIdentifier(new BlockIdentifier(-1L, blockTransactionRequest.getBlockIdentifier().getHash()));

    assertThrows(ExceptionFactory.invalidBlockIdentifier(-1L).getClass(), () -> blockApi.blockTransaction(blockTransactionRequest));
  }

  @Test
  void blockTransactionOfflineModeTest() throws NoSuchFieldException, IllegalAccessException {
    //given
    BlockTransactionRequest blockTransactionRequest = newBlockTransactionRequest();
    //when
    Field field = BlockApiImpl.class.getDeclaredField("offlineMode");
    field.setAccessible(true);
    field.set(blockApi, true);
    //then
    assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), () -> blockApi.blockTransaction(blockTransactionRequest));
  }

  @Test
  void block_Test() throws Exception {
    //given
    BlockRequest blockRequest = givenBlockRequest();
    when(blockService.findBlock(123L, "hash1")).thenReturn(new Block());
    //when
    //then
    Long index = blockRequest.getBlockIdentifier().getIndex();
    String hash = blockRequest.getBlockIdentifier().getHash();

    mockMvc.perform(post("/block")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(blockRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.block.block_identifier.index").value(index))
        .andExpect(jsonPath("$.block.block_identifier.hash").value(hash));
  }

  @Test
  void blockNotFound_Test() throws Exception {
    //given
    BlockRequest blockRequest = givenBlockRequest();
    when(blockService.findBlock(123L, "hash1")).thenThrow(ExceptionFactory.blockNotFoundException());
    //when
    //then
    mockMvc.perform(post("/block")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(blockRequest)))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.code").value(4001))
        .andExpect(jsonPath("$.message").value("Block not found"))
        .andExpect(jsonPath("$.retriable").value(false));
  }

  @Test
  void blockTransaction_Test() throws Exception {
    //given
    BlockTransactionResponse resp = newBlockTransactionResponse();
    BlockTransactionRequest req = newBlockTransactionRequest();
    when(blockService.getBlockTransaction(anyLong(), anyString(), anyString())).thenReturn(
        new BlockTx());
    when(protocolParamService.findProtocolParameters()).thenReturn(protocolParams);
    when(protocolParams.getPoolDeposit()).thenReturn(new BigInteger("1000"));
    //any string
    when(blockMapper.mapToBlockTransactionResponse(any(BlockTx.class))).thenReturn(resp);
    //when
    //then
    String txHash = resp.getTransaction().getTransactionIdentifier().getHash();
    mockMvc.perform(post("/block/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transaction.transaction_identifier.hash").value(txHash));
  }

  @Test
  void blockTransaction_notFound_Test() throws Exception {
    //given
    BlockTransactionRequest req = newBlockTransactionRequest();
    when(blockService.getBlockTransaction(anyLong(), anyString(), anyString()))
        .thenThrow(ExceptionFactory.transactionNotFound());
    //when
    //then
    mockMvc.perform(post("/block/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.code").value(4006))
        .andExpect(jsonPath("$.message").value("Transaction not found"))
        .andExpect(jsonPath("$.retriable").value(false));
  }

  private BlockRequest givenBlockRequest() {
    BlockRequest blockRequest = newBlockRequest();
    BlockResponse blockResp = newBlockResponse();
    when(blockMapper.mapToBlockResponse(any(Block.class))).thenReturn(blockResp);

    return blockRequest;
  }

}
