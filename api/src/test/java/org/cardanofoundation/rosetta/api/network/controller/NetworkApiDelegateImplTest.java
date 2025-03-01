package org.cardanofoundation.rosetta.api.network.controller;

import java.lang.reflect.Field;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.mockito.InjectMocks;
import org.openapitools.client.model.MetadataRequest;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkOptionsResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.NetworkStatusResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import static org.cardanofoundation.rosetta.EntityGenerator.givenMetadataRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenNetworkListResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenNetworkOptionsResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenNetworkRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenNetworkStatusResponse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NetworkApiDelegateImplTest extends BaseSpringMvcSetup {

    @MockBean
    private NetworkService networkService;

    @InjectMocks
    private NetworkApiImpl networkApi;

    @Test
    void statusOfflineModeTest() throws Exception {
        //given
        NetworkRequest networkRequest = givenNetworkRequest();
        Field field = NetworkApiImpl.class.getDeclaredField("offlineMode");
        field.setAccessible(true);
        field.set(networkApi, true);
        //when
        //then
        assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), () -> networkApi.networkStatus(networkRequest));
    }

    @Test
    void networkList_Test() throws Exception {
        //given
        MetadataRequest metadataRequest = givenMetadataRequest();
        NetworkListResponse networkListResponse = givenNetworkListResponse();
        when(networkService.getSupportedNetwork()).thenReturn(NetworkEnum.DEVNET.getNetwork());
        when(networkService.getNetworkList(any())).thenReturn(networkListResponse);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //when
        //then
        mockMvc.perform(post("/network/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metadataRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.network_identifiers[0].blockchain").value("cardano"))
                .andExpect(jsonPath("$.network_identifiers[0].network").value("devkit"));
    }

    @Test
    void networkStatus_Test() throws Exception {
        //given
        NetworkRequest networkRequest = givenNetworkRequest();
        NetworkStatusResponse networkStatusResponse = givenNetworkStatusResponse();
        when(networkService.getNetworkStatus(any())).thenReturn(networkStatusResponse);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //when
        //then
        mockMvc.perform(post("/network/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(networkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_block_identifier.index").value(123L))
                .andExpect(jsonPath("$.current_block_identifier.hash").value("123"));
    }

    @Test
    void networkOptions_Test() throws Exception {
        //given
        NetworkRequest networkRequest = givenNetworkRequest();
        NetworkOptionsResponse networkOptionsResponse = givenNetworkOptionsResponse();
        when(networkService.getNetworkOptions(any())).thenReturn(networkOptionsResponse);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //when
        //then
        mockMvc.perform(post("/network/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(networkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version.rosetta_version").value(networkOptionsResponse.getVersion().getRosettaVersion()))
                .andExpect(jsonPath("$.version.node_version").value(networkOptionsResponse.getVersion().getNodeVersion()))
                .andExpect(jsonPath("$.allow.operation_statuses[0].status").value(networkOptionsResponse.getAllow().getOperationStatuses().getFirst().getStatus()));
    }

}
