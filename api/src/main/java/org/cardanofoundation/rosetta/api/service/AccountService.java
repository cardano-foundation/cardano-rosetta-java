package org.cardanofoundation.rosetta.api.service;

import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;

public interface AccountService {

  AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest);

  AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsResponse);

}
