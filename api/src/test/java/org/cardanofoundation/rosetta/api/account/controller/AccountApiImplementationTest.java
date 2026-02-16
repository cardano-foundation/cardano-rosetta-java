package org.cardanofoundation.rosetta.api.account.controller;

import java.lang.reflect.Field;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.NetworkIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.api.account.service.AccountService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountApiImplementationTest extends BaseSpringMvcSetup {

  @Mock
  AccountService accountService;

  @Mock
  NetworkService networkService;

  @Spy
  @InjectMocks
  AccountApiImplementation accountController;

  @Test
  void accountBalanceOfflineModeTest() throws NoSuchFieldException, IllegalAccessException {
    AccountBalanceRequest request = Mockito.mock(AccountBalanceRequest.class);
    Field field = AccountApiImplementation.class.getDeclaredField("offlineMode");
    field.setAccessible(true);
    field.set(accountController, true);
    assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), () -> accountController.accountBalance(request));
  }

  @Test
  void accountCoinsOfflineModeTest() throws NoSuchFieldException, IllegalAccessException {
    AccountCoinsRequest request = Mockito.mock(AccountCoinsRequest.class);
    Field field = AccountApiImplementation.class.getDeclaredField("offlineMode");
    field.setAccessible(true);
    field.set(accountController, true);
    assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), () -> accountController.accountCoins(request));
  }

  @Test
  void accountBalancePositiveTest() {
    AccountBalanceRequest request = Mockito.mock(AccountBalanceRequest.class);
    AccountBalanceResponse response = Mockito.mock(AccountBalanceResponse.class);
    request.setNetworkIdentifier(
        NetworkIdentifier.builder()
            .blockchain(Constants.CARDANO_BLOCKCHAIN)
            .network(Constants.DEVKIT)
            .build());

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
    request.setNetworkIdentifier(
        NetworkIdentifier.builder().blockchain(Constants.CARDANO_BLOCKCHAIN)
            .network(Constants.DEVKIT).build());

    Mockito.when(accountService.getAccountCoins(request)).thenReturn(response);
    ResponseEntity<AccountCoinsResponse> actual = accountController.accountCoins(request);

    assertEquals(response, actual.getBody());
    assertEquals(HttpStatusCode.valueOf(200), actual.getStatusCode());
    verify(accountService).getAccountCoins(request);
    verifyNoMoreInteractions(accountService);
  }

  @Test
  void accountBalanceNegativeTest() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/account/balance")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}");
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(400)).andReturn();
  }

  @Test
  void accountCoinsNegativeTest() throws Exception {
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/account/coins")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}");
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(400)).andReturn();
  }

  @Test
  void accountBalanceInvalidPayloadTest() throws Exception {
    AccountBalanceRequest request = givenAccountBalanceRequest();
    request.setNetworkIdentifier(null);
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/account/balance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request));
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.code").value(RosettaErrorType.UNSPECIFIED_ERROR.getCode()))
        .andExpect(jsonPath("$.details.message", containsString("An error occurred for request")))
        .andReturn();
  }

  @Test
  void accountCoinsInvalidPayloadTest() throws Exception {
    AccountCoinsRequest request = givenAccountCoinsRequest();
    request.setAccountIdentifier(null);
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/account/coins")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request));
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.code").value(RosettaErrorType.UNSPECIFIED_ERROR.getCode()))
        .andExpect(jsonPath("$.details.message", containsString("An error occurred for request")))
        .andReturn();
  }

  @Test
  void accountBalanceInvalidRequestTest() throws Exception {
    AccountBalanceRequest request = givenAccountBalanceRequest();
    request.getNetworkIdentifier().setBlockchain("Invalid");
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/account/balance")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request));
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.code").value(RosettaErrorType.INVALID_BLOCKCHAIN.getCode()))
        .andExpect(jsonPath("$.message").value(RosettaErrorType.INVALID_BLOCKCHAIN.getMessage()))
        .andReturn();
  }

  @Test
  void accountCoinsInvalidRequestTest() throws Exception {
    AccountCoinsRequest request = givenAccountCoinsRequest();
    request.getNetworkIdentifier().setBlockchain("Invalid");
    RequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/account/coins")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request));
    mockMvc.perform(requestBuilder)
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.code").value(RosettaErrorType.INVALID_BLOCKCHAIN.getCode()))
        .andExpect(jsonPath("$.message").value(RosettaErrorType.INVALID_BLOCKCHAIN.getMessage()))
        .andReturn();
  }

  private AccountBalanceRequest givenAccountBalanceRequest() {
    return AccountBalanceRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(Constants.CARDANO_BLOCKCHAIN)
            .network(Constants.DEVKIT)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(Constants.ADDRESS)
            .build())
        .build();
  }

  private AccountCoinsRequest givenAccountCoinsRequest() {
    return AccountCoinsRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(Constants.CARDANO_BLOCKCHAIN)
            .network(Constants.DEVKIT)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(Constants.ADDRESS)
            .build())
        .build();
  }

}
