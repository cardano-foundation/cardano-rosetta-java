package org.cardanofoundation.rosetta.api.service.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import org.cardanofoundation.rosetta.api.service.utils.Common;
import org.cardanofoundation.rosetta.crawler.controller.NetworkApiDelegateImplementation;
import org.cardanofoundation.rosetta.crawler.model.Peer;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.crawler.service.NetworkService;
import org.cardanofoundation.rosetta.crawler.util.RosettaConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NetworkApiDelegateImplementation.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class NetworkControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private NetworkService networkService;

  private final String NETWORK_STATUS_ENDPOINT = "/network/status";
  private final String NETWORK_LIST_ENDPOINT = "/network/list";


  @Test
  void whenCallNetworkList() throws Exception {
    MetadataRequest metadataRequest =MetadataRequest.builder()
        .metadata(new HashMap<>())
        .build();

    //mock data
    NetworkIdentifier identifier = new NetworkIdentifier();
    identifier.setBlockchain(RosettaConstants.BLOCKCHAIN_NAME);
    identifier.setNetwork("mainnet");
    NetworkListResponse response = new NetworkListResponse();
    response.addNetworkIdentifiersItem(identifier);

    String body = objectMapper.writeValueAsString(metadataRequest);

    given(networkService.getNetworkList(any())).willReturn(response);
    mockMvc.perform(post(NETWORK_LIST_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
            .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(objectMapper.writeValueAsString(response)))
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.network_identifiers").isArray())
        .andDo(print());


  }

  @Test
  void whenCallNetworkStatus() throws Exception {
    NetworkRequest request = Common.generateNetworkPayload("cardano", "preprod");

    // mock data
    BlockIdentifier genesisBlock = BlockIdentifier.builder().hash("d4b8de7a11d929a323373cbab6c1a9bdc931beffff11db111cf9d57356ee1937")
        .build();
    BlockIdentifier latestBlock = BlockIdentifier.builder().hash("57857bccde3793808d6d58d0f979d0178b00ba82b50fff9f6eec52a45dae4ad3").index(939262L)
        .build();
    Peer peer = new Peer("relays-new.cardano-mainnet.iohk.io");
    NetworkStatusResponse responseExpected =NetworkStatusResponse.builder()
            .genesisBlockIdentifier(genesisBlock)
            .currentBlockIdentifier(latestBlock)
            .peers(List.of(peer))
            .build();
    String body = objectMapper.writeValueAsString(request);

    given(networkService.getNetworkStatus(any(NetworkRequest.class))).willReturn(responseExpected);
    mockMvc.perform(post(NETWORK_STATUS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.genesis_block_identifier").isNotEmpty())
            .andExpect(jsonPath("$.genesis_block_identifier").value(genesisBlock))
            .andExpect(jsonPath("$.current_block_identifier").isNotEmpty())
            .andExpect(jsonPath("$.current_block_identifier").value(latestBlock))
            .andExpect(jsonPath("$.peers").isArray())
            .andExpect(jsonPath("$.peers[0].peer_id").value("relays-new.cardano-mainnet.iohk.io"))
            .andDo(print())
    ;
  }


}
