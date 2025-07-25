package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionMetadataRequest;
import org.openapitools.client.model.ConstructionMetadataResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetadataApiTest extends IntegrationTest {

    @Autowired
    private ConstructionApiService constructionApiService;

    private ConstructionMetadataRequest getCombineRequest() throws IOException {
        File file = new File(this.getClass().getClassLoader()
                .getResource("testdata/construction/metadata/metadata_request.json")
                .getFile());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, ConstructionMetadataRequest.class);
    }

    @Test
    void combineWithMetadataTest() throws IOException {
        //given
        ConstructionMetadataRequest constructionMetadataRequest = getCombineRequest();
        //when
        ConstructionMetadataResponse constructionMetadataResponse = constructionApiService.constructionMetadataService(constructionMetadataRequest);
        //then
        assertNotNull(constructionMetadataResponse);
        assertEquals("157141", constructionMetadataResponse.getSuggestedFee().getFirst().getValue());
        assertEquals(ADA, constructionMetadataResponse.getSuggestedFee().getFirst().getCurrency().getSymbol());
        assertEquals(ADA_DECIMALS, constructionMetadataResponse.getSuggestedFee().getFirst().getCurrency().getDecimals());

        assertEquals(BigDecimal.valueOf(695), constructionMetadataResponse.getMetadata().getTtl());
        assertEquals("4310", constructionMetadataResponse.getMetadata().getProtocolParameters().getCoinsPerUtxoSize());
        assertEquals("2000000", constructionMetadataResponse.getMetadata().getProtocolParameters().getKeyDeposit());
        assertEquals("500000000", constructionMetadataResponse.getMetadata().getProtocolParameters().getPoolDeposit());
    }

}
