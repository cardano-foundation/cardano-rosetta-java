package org.cardanofoundation.rosetta.crawler.controller;

import org.cardanofoundation.rosetta.crawler.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountCoinsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountApiDelegateImplementation implements AccountApiDelegate {
    @Override
    public ResponseEntity<AccountBalanceResponse> accountBalance(
        AccountBalanceRequest accountBalanceRequest) {
        return null;
    }

    @Override
    public ResponseEntity<AccountCoinsResponse> accountCoins(AccountCoinsRequest accountCoinsRequest) {
        return null;
    }
}
