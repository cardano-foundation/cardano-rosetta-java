package org.cardanofoundation.rosetta.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;
import org.cardanofoundation.rosetta.api.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountApiDelegateImplementation implements AccountApiDelegate {

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
