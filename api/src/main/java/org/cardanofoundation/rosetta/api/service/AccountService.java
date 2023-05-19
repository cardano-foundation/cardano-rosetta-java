package org.cardanofoundation.rosetta.api.service;


import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;

public interface AccountService {
    AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest);
    AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsResponse);

}
