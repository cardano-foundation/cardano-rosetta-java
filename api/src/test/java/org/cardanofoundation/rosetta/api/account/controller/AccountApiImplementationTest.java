package org.cardanofoundation.rosetta.api.account.controller;

import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.NetworkIdentifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.service.AccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountApiImplementationTest {

  private MockMvc mockMvc;

  @Mock
  AccountService accountService;

  @Mock
  NetworkService networkService;

  @Spy
  @InjectMocks
  AccountApiImplementation accountController;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
  }

  @Test
  void accountBalancePositiveTest() {
    AccountBalanceRequest request = Mockito.mock(AccountBalanceRequest.class);
    AccountBalanceResponse response = Mockito.mock(AccountBalanceResponse.class);
    request.setNetworkIdentifier(NetworkIdentifier.builder().blockchain(Constants.CARDANO_BLOCKCHAIN).network(Constants.DEVKIT).build());

    Mockito.when(accountService.getAccountBalance(request)).thenReturn(response);

    ResponseEntity<AccountBalanceResponse> actual = accountController.accountBalance(request);

    assertEquals(response, actual.getBody());
    assertEquals(HttpStatusCode.valueOf(200), actual.getStatusCode());
    verify(accountService).getAccountBalance(request);
    verifyNoMoreInteractions(accountService);
  }

  @Test
  void accountCoinsPositiveTest() {
    AccountCoinsRequest request = Mockito.mock(AccountCoinsRequest.class);
    AccountCoinsResponse response = Mockito.mock(AccountCoinsResponse.class);
    request.setNetworkIdentifier(NetworkIdentifier.builder().blockchain(Constants.CARDANO_BLOCKCHAIN).network(Constants.DEVKIT).build());

    Mockito.when(accountService.getAccountCoins(request)).thenReturn(response);
    ResponseEntity<AccountCoinsResponse> actual = accountController.accountCoins(request);

    assertEquals(response, actual.getBody());
    assertEquals(HttpStatusCode.valueOf(200), actual.getStatusCode());
    verify(accountService).getAccountCoins(request);
    verifyNoMoreInteractions(accountService);
  }

  @Test
  void accountBalanceNegativeTest() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/account/balance")
        .param("request", "null");
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(415)).andReturn();
  }

  @Test
  void accountCoinsNegativeTest() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/account/coins")
        .param("request", "null");
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(415)).andReturn();
  }
}
