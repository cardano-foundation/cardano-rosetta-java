package org.cardanofoundation.rosetta.api.account.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

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
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;

@RestController
@RequiredArgsConstructor
public class AccountApiImplementation implements AccountApi {

  private final AccountService accountService;
  private final NetworkService networkService;

  @Override
  public ResponseEntity<AccountBalanceResponse> accountBalance(
      @Valid @RequestBody AccountBalanceRequest accountBalanceRequest) {

    networkService.verifyNetworkRequest(accountBalanceRequest.getNetworkIdentifier());
    CardanoAddressUtils.verifyAddress(accountBalanceRequest.getAccountIdentifier().getAddress());

    return ResponseEntity.ok(accountService.getAccountBalance(accountBalanceRequest));
  }

  @Override
  public ResponseEntity<AccountCoinsResponse> accountCoins(
      @Valid @RequestBody AccountCoinsRequest accountCoinsRequest) {

    networkService.verifyNetworkRequest(accountCoinsRequest.getNetworkIdentifier());
    CardanoAddressUtils.verifyAddress(accountCoinsRequest.getAccountIdentifier().getAddress());

    return ResponseEntity.ok(accountService.getAccountCoins(accountCoinsRequest));
  }
}
