package org.cardanofoundation.rosetta.api.construction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.openapitools.client.model.ConstructionSubmitRequest;
import org.openapitools.client.model.TransactionIdentifierResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmitApiTest extends IntegrationTest {

    @Autowired
    private ConstructionApiServiceImpl constructionApiService;

    @MockBean
    private CardanoConstructionService cardanoService;

    @Test
    void submitTest() {
        //given
        ConstructionSubmitRequest constructionSubmitRequest = ConstructionSubmitRequest
                .builder()
                .signedTransaction("signedTransaction")
                .build();

        when(cardanoService.extractTransactionIfNeeded(constructionSubmitRequest.getSignedTransaction())).thenReturn("transaction");
        when(cardanoService.submitTransaction("transaction")).thenReturn("{}");
        //when
        TransactionIdentifierResponse transactionIdentifierResponse = constructionApiService.constructionSubmitService(constructionSubmitRequest);
        //then
        verify(cardanoService, times(1)).extractTransactionIfNeeded(any());
        verify(cardanoService, times(1)).submitTransaction(any());
        assertEquals("{}", transactionIdentifierResponse.getTransactionIdentifier().getHash());
    }

}
