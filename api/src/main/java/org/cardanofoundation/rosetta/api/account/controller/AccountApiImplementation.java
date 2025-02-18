package org.cardanofoundation.rosetta.api.account.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.AccountApi;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;

import org.cardanofoundation.rosetta.api.account.service.AccountService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@RestController
@RequiredArgsConstructor
public class AccountApiImplementation implements AccountApi {

  private final AccountService accountService;
  private final NetworkService networkService;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;

  @Override
  public ResponseEntity<AccountBalanceResponse> accountBalance(
      @Valid @RequestBody AccountBalanceRequest accountBalanceRequest) {
    if(offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }
    networkService.verifyNetworkRequest(accountBalanceRequest.getNetworkIdentifier());

    return ResponseEntity.ok(accountService.getAccountBalance(accountBalanceRequest));
  }

  @Override
  public ResponseEntity<AccountCoinsResponse> accountCoins(
      @Valid @RequestBody AccountCoinsRequest accountCoinsRequest) {
    if(offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }
    networkService.verifyNetworkRequest(accountCoinsRequest.getNetworkIdentifier());

    return ResponseEntity.ok(accountService.getAccountCoins(accountCoinsRequest));
  }
}
