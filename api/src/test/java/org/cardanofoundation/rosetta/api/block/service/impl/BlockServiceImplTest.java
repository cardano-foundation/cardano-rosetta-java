package org.cardanofoundation.rosetta.api.block.service.impl;

import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.BlockRequest;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.PartialBlockIdentifier;
import org.openapitools.client.model.SubNetworkIdentifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

    @Mock
    private LedgerDataProviderService ledgerDataProviderService;
    private BlockServiceImpl blockService = new BlockServiceImpl(ledgerDataProviderService);

    @Test
    void getBlockByBlockRequest() {

        //given
        BlockRequest blockRequest = new BlockRequest();
        blockRequest.setBlockIdentifier(newPartialBlockIdentifier());
        blockRequest.setNetworkIdentifier(newNetworkIdentifier());
        //when

//        blockService.findBlock(blockRequest); TODO saa: impl
        //then

        String s = "dd";



    }

    private NetworkIdentifier newNetworkIdentifier() {
        NetworkIdentifier nid = new NetworkIdentifier();
        return NetworkIdentifier
                .builder()
                .network("network1")
                .blockchain("cardano-dev")
                .subNetworkIdentifier(newSubNetworkIdentifier())
                .build();
    }

    private SubNetworkIdentifier newSubNetworkIdentifier() {
        return SubNetworkIdentifier
                .builder()
                .network("network")
                .metadata("metadata")
                .build();
    }

    private PartialBlockIdentifier newPartialBlockIdentifier() {
        return null;
    }


    @Test
    void findTransactionsByBlock() {
    }

    @Test
    void getBlockTransaction() {
    }

    @Test
    void findBlock() {
    }
}