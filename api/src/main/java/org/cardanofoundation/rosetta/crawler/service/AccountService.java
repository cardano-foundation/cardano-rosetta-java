package org.cardanofoundation.rosetta.crawler.service;


import org.cardanofoundation.rosetta.crawler.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountCoinsResponse;

public interface AccountService {
    AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest);
    AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsResponse);

}
