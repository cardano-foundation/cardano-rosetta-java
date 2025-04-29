package org.cardanofoundation.rosetta.api.construction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.openapitools.client.model.ConstructionSubmitRequest;
import org.openapitools.client.model.TransactionIdentifierResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubmitApiTest extends IntegrationTest {

    @Autowired
    private ConstructionApiServiceImpl constructionApiService;

    @MockitoBean
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
