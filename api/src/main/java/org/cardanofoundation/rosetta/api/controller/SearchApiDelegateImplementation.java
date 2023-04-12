package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.SearchTransactionsRequest;
import org.cardanofoundation.rosetta.api.model.rest.SearchTransactionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SearchApiDelegateImplementation implements SearchApiDelegate {
    @Override
    public ResponseEntity<SearchTransactionsResponse> searchTransactions(SearchTransactionsRequest searchTransactionsRequest) {
        return null;
    }
}
