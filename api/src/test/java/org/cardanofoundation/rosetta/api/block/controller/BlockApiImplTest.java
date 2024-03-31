package org.cardanofoundation.rosetta.api.block.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.PartialBlockIdentifier;
import org.openapitools.client.model.TransactionIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.SpringMvcTest;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockResponse;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockTransactionResponse;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.api.block.service.BlockService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class BlockApiImplTest extends SpringMvcTest {

  @MockBean
  @SuppressWarnings("unused") //used in when
  private BlockService blockService;

  @MockBean
  BlockToBlockResponse blockToBlockResponse;

  @MockBean
  BlockToBlockTransactionResponse mapperToBlockTxResponse;


  @Test
  void block_Test() throws Exception {

    //given
    BlockRequest blockRequest = newBlockRequest();
    BlockResponse blockResp = newBlockResponse();
    when(blockService.findBlock(123L, "hash1")).thenReturn(new Block());
    when(blockToBlockResponse.toDto(any(Block.class))).thenReturn(blockResp);

    //when
    //then
    Long index = blockRequest.getBlockIdentifier().getIndex();
    String hash = blockRequest.getBlockIdentifier().getHash();
    mockMvc.perform(post("/block")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(blockRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.block.block_identifier.index").value(index))
        .andExpect(jsonPath("$.block.block_identifier.hash").value(hash));


  }

  @Test
  void blockTransaction_Test() throws Exception {

    //given
    BlockTransactionResponse resp = newBlockTransactionResponse();
    BlockTransactionRequest req = newBlockTransactionRequest();

    when(blockService.getBlockTransaction(anyLong(), anyString(), anyString())).thenReturn(
        new Tran());
    when(blockService.getPoolDeposit()).thenReturn("1000"); //any string
    when(mapperToBlockTxResponse.toDto(any(Tran.class), anyString())).thenReturn(resp);

    //when
    //then
    String txHash = resp.getTransaction().getTransactionIdentifier().getHash();
    mockMvc.perform(post("/block/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transaction.transaction_identifier.hash").value(txHash));

  }

  private BlockTransactionResponse newBlockTransactionResponse() {
    return BlockTransactionResponse
        .builder()
        .transaction(
            org.openapitools.client.model.Transaction
                .builder()
                .transactionIdentifier(
                    TransactionIdentifier
                        .builder()
                        .hash("hash1")
                        .build())
                .build())
        .build();
  }

  private static BlockIdentifier newBlockId() {
    return BlockIdentifier
        .builder()
        .index(123L)
        .hash("hash1")
        .build();
  }


  private static BlockResponse newBlockResponse() {
    return BlockResponse
        .builder()
        .block(
            org.openapitools.client.model.Block
                .builder()
                .blockIdentifier(
                    newBlockId())
                .build())
        .build();
  }

  private static BlockTransactionRequest newBlockTransactionRequest() {
    return BlockTransactionRequest
        .builder()
        .blockIdentifier(newBlockId())
        .networkIdentifier(newNetworkId())
        .transactionIdentifier(TransactionIdentifier
            .builder()
            .hash("txHash1")
            .build())
        .build();
  }

  private static BlockRequest newBlockRequest() {
    return BlockRequest
        .builder()
        .blockIdentifier(
            PartialBlockIdentifier
                .builder()
                .index(123L)
                .hash("hash1")
                .build())
        .networkIdentifier(newNetworkId())
        .build();
  }

  private static NetworkIdentifier newNetworkId() {
    return NetworkIdentifier
        .builder()
        .blockchain("cardano")
        .network("devkit")
        .build();
  }


}