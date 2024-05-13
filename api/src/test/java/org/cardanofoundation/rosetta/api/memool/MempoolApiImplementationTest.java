package org.cardanofoundation.rosetta.api.memool;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.apache.commons.lang3.NotImplementedException;
import org.openapitools.client.model.MempoolResponse;
import org.openapitools.client.model.MempoolTransactionRequest;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.TransactionIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.mempool.controller.MempoolApiImplementation;
import org.cardanofoundation.rosetta.api.mempool.service.MempoolService;

import static org.cardanofoundation.rosetta.EntityGenerator.givenNetworkRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenTransactionIdentifierResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MempoolApiImplementationTest {

    private MempoolService mempoolService = mock(MempoolService.class);
    private final MempoolApiImplementation mempoolApiImplementation = new MempoolApiImplementation(mempoolService);

    @Test
    void mempool_Test() {
        //given
        NetworkRequest networkRequest = givenNetworkRequest();
        List<TransactionIdentifier> transactionIdentifierList = List.of(givenTransactionIdentifierResponse().getTransactionIdentifier());
        when(mempoolService.getCurrentTransactionIdentifiers(networkRequest.getNetworkIdentifier().getNetwork()))
                .thenReturn(transactionIdentifierList);
        //when
        ResponseEntity<MempoolResponse> mempool = mempoolApiImplementation.mempool(networkRequest);
        //then
        assertNotNull(mempool);
        verify(mempoolService, times(1)).getCurrentTransactionIdentifiers(networkRequest.getNetworkIdentifier().getNetwork());
        assertEquals(transactionIdentifierList, mempool.getBody().getTransactionIdentifiers());
    }

    @Test
    void mempoolTransaction_Test() {
        //given
        MempoolTransactionRequest mempoolTransactionRequest = MempoolTransactionRequest.builder().build();
        //when
        NotImplementedException exception = assertThrows(NotImplementedException.class,
                () -> mempoolApiImplementation.mempoolTransaction(mempoolTransactionRequest));
        //then
        assertEquals("Not implemented yet", exception.getMessage());
    }
}
