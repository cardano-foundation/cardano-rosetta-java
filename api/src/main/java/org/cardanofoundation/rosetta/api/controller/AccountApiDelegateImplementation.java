package org.cardanofoundation.rosetta.api.controller;

import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountApiDelegateImplementation implements AccountApiDelegate {
  @Autowired
  AccountService accountService;

  @Override
  public ResponseEntity<AccountBalanceResponse> accountBalance(@Valid @RequestBody
  AccountBalanceRequest accountBalanceRequest) {
    AccountBalanceResponse response = accountService.getAccountBalance(accountBalanceRequest);
    return ResponseEntity.ok(response);

  }
    @Override
    public ResponseEntity<AccountCoinsResponse> accountCoins(AccountCoinsRequest accountCoinsRequest) {
        return null;
    }
}
