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

@RestController
@RequiredArgsConstructor
public class AccountApiImplementation implements AccountApi {

  private final AccountService accountService;

  @Override
  public ResponseEntity<AccountBalanceResponse> accountBalance(
      @Valid @RequestBody AccountBalanceRequest accountBalanceRequest) {
    return ResponseEntity.ok(accountService.getAccountBalance(accountBalanceRequest));
  }

  @Override
  public ResponseEntity<AccountCoinsResponse> accountCoins(
      @Valid @RequestBody AccountCoinsRequest accountCoinsRequest) {
    return ResponseEntity.ok(accountService.getAccountCoins(accountCoinsRequest));
  }
}
