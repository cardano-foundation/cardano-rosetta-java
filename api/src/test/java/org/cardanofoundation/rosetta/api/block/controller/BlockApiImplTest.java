package org.cardanofoundation.rosetta.api.block.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.SpringMvcTest;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockResponse;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.service.BlockService;

import static org.mockito.ArgumentMatchers.any;
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
  BlockToBlockResponse mapperToBlockResponse;


  @Test
  void block_Test() throws Exception {

    //given
    BlockRequest blockRequest = BlockRequest
        .builder()
        .blockIdentifier(
            PartialBlockIdentifier
                .builder()
                .index(123L)
                .hash("hash1")
                .build())
        .networkIdentifier(NetworkIdentifier
            .builder()
            .blockchain("cardano")
            .network("devkit")
            .build())
        .build();

    BlockResponse blockResp = BlockResponse
        .builder()
        .block(
            org.openapitools.client.model.Block
                .builder()
                .blockIdentifier(
                    BlockIdentifier
                        .builder()
                        .index(123L)
                        .hash("hash1")
                        .build())
                .build())
        .build();

    when(blockService.findBlock(123L, "hash1")).thenReturn(new Block());
    when(mapperToBlockResponse.toDto(any(Block.class))).thenReturn(blockResp);

    String jsonRequestBody = objectMapper.writeValueAsString(blockRequest);

    //when
    //then
    Long index = blockRequest.getBlockIdentifier().getIndex();
    String hash = blockRequest.getBlockIdentifier().getHash();
    mockMvc.perform(post("/block")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequestBody))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.block.block_identifier.index").value(index))
        .andExpect(jsonPath("$.block.block_identifier.hash").value(hash));


  }

  @Test
  void blockTransaction_Test() {

    //TODO implement this test

  }


}