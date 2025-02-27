package org.cardanofoundation.rosetta.api.search.controller;

import java.lang.reflect.Field;
import java.util.Collections;

import lombok.SneakyThrows;

import org.springframework.http.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.SearchTransactionsRequest;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.api.search.service.SearchService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchControllerTest extends BaseSpringMvcSetup {

  @Mock
  SearchService service;
  @Mock
  NetworkService networkService;

  @InjectMocks
  SearchApiImpl searchApi;

  @Test
  void searchOfflineModeTest() throws NoSuchFieldException, IllegalAccessException {
    SearchTransactionsRequest request = new SearchTransactionsRequest();
    Field field = SearchApiImpl.class.getDeclaredField("offlineMode");
    field.setAccessible(true);
    field.set(searchApi, true);
    assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), () -> searchApi.searchTransactions(request));
  }

  @SneakyThrows
  @Test
  void searchControllerNetworkIsMissing() {
    SearchTransactionsRequest request = new SearchTransactionsRequest();
    Mockito.when(service.searchTransaction(any(), any(), any())).thenReturn(Collections.emptyList());
    mockMvc.perform(post("/search/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is5xxServerError());
  }

  @SneakyThrows
  @Test
  void searchControllerOK() {
    SearchTransactionsRequest request = SearchTransactionsRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .build();
    Mockito.when(service.searchTransaction(any(), any(), any())).thenReturn(Collections.emptyList());

    mockMvc.perform(post("/search/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total_count").value(10))
        .andExpect(jsonPath("$.next_offset").value(10));
  }
}
