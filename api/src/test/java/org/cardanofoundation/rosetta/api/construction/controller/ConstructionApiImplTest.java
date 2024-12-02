package org.cardanofoundation.rosetta.api.construction.controller;

import java.lang.reflect.Field;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.mockito.InjectMocks;
import org.openapitools.client.model.ConstructionCombineRequest;
import org.openapitools.client.model.ConstructionCombineResponse;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;
import org.openapitools.client.model.ConstructionHashRequest;
import org.openapitools.client.model.ConstructionMetadataRequest;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ConstructionParseRequest;
import org.openapitools.client.model.ConstructionParseResponse;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsResponse;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;
import org.openapitools.client.model.ConstructionSubmitRequest;
import org.openapitools.client.model.TransactionIdentifierResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionCombineRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionCombineResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionDeriveRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionDeriveResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionHashRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionMetadataRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionMetadataResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionParseRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionPayloadsRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionPayloadsResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionPreprocessRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionPreprocessResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionSubmitRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenTransactionIdentifierResponse;
import static org.cardanofoundation.rosetta.EntityGenerator.givenTransactionMetadataResponse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConstructionApiImplTest extends BaseSpringMvcSetup {

    @MockBean
    private ConstructionApiService constructionApiService;

    @InjectMocks
    private ConstructionApiImplementation constructionApiImplementation;

    @BeforeEach
    void init() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    @Test
    void metadataOfflineModeTest() throws Exception {
        //given
        ConstructionMetadataRequest constructionMetadataRequest = givenConstructionMetadataRequest();
        Field field = ConstructionApiImplementation.class.getDeclaredField("offlineMode");
        field.setAccessible(true);
        field.set(constructionApiImplementation, true);
        //when
        //then
        assertThrows(ExceptionFactory.NotSupportedInOfflineMode().getClass(), () -> constructionApiImplementation.constructionMetadata(constructionMetadataRequest));
    }



    @Test
    void submitOfflineModeTest() throws Exception {
        //given
        ConstructionSubmitRequest constructionSubmitRequest = givenConstructionSubmitRequest();
        Field field = ConstructionApiImplementation.class.getDeclaredField("offlineMode");
        field.setAccessible(true);
        field.set(constructionApiImplementation, true);
        //when
        //then
        assertThrows(ExceptionFactory.NotSupportedInOfflineMode().getClass(), () -> constructionApiImplementation.constructionSubmit(constructionSubmitRequest));
    }

    @Test
    void combine_Test() throws Exception {
        //given
        ConstructionCombineRequest constructionCombineRequest = givenConstructionCombineRequest();
        ConstructionCombineResponse constructionCombineResponse = givenConstructionCombineResponse();
        when(constructionApiService.constructionCombineService(constructionCombineRequest)).thenReturn(constructionCombineResponse);
        //when
        //then
        mockMvc.perform(post("/construction/combine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionCombineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signed_transaction").value(constructionCombineResponse.getSignedTransaction()));
    }

    @Test
    void derive_Test() throws Exception {
        //given
        ConstructionDeriveRequest constructionDeriveRequest = givenConstructionDeriveRequest();
        ConstructionDeriveResponse constructionCombineResponse = givenConstructionDeriveResponse();
        when(constructionApiService.constructionDeriveService(constructionDeriveRequest)).thenReturn(constructionCombineResponse);
        //when
        //then
        mockMvc.perform(post("/construction/derive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionDeriveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_identifier.address").value(constructionCombineResponse.getAccountIdentifier().getAddress()));
    }

    @Test
    void hash_Test() throws Exception {
        //given
        ConstructionHashRequest constructionHashRequest = givenConstructionHashRequest();
        TransactionIdentifierResponse transactionIdentifierResponse = givenTransactionIdentifierResponse();
        when(constructionApiService.constructionHashService(constructionHashRequest)).thenReturn(transactionIdentifierResponse);
        //when
        //then
        mockMvc.perform(post("/construction/hash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionHashRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_identifier.hash").value(transactionIdentifierResponse.getTransactionIdentifier().getHash()));
    }

    @Test
    void metadata_Test() throws Exception {
        //given
        ConstructionMetadataRequest constructionMetadataRequest = givenConstructionMetadataRequest();
        ConstructionMetadataResponse constructionMetadataResponse = givenTransactionMetadataResponse();
        when(constructionApiService.constructionMetadataService(constructionMetadataRequest)).thenReturn(constructionMetadataResponse);
        //when
        //then
        mockMvc.perform(post("/construction/metadata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionMetadataRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.ttl").value(constructionMetadataResponse.getMetadata().getTtl()));
    }

    @Test
    void parse_Test() throws Exception {
        //given
        ConstructionParseRequest constructionParseRequest = givenConstructionParseRequest();
        ConstructionParseResponse constructionMetadataResponse = givenConstructionMetadataResponse();
        when(constructionApiService.constructionParseService(constructionParseRequest)).thenReturn(constructionMetadataResponse);
        //when
        //then
        mockMvc.perform(post("/construction/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionParseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations[0].type").value(constructionMetadataResponse.getOperations().getFirst().getType()));
    }

    @Test
    void payloads_Test() throws Exception {
        //given
        ConstructionPayloadsRequest constructionPayloadsRequest = givenConstructionPayloadsRequest();
        ConstructionPayloadsResponse constructionPayloadsResponse = givenConstructionPayloadsResponse();
        when(constructionApiService.constructionPayloadsService(constructionPayloadsRequest)).thenReturn(constructionPayloadsResponse);
        //when
        //then
        mockMvc.perform(post("/construction/payloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionPayloadsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unsigned_transaction").value(constructionPayloadsResponse.getUnsignedTransaction()));
    }

    @Test
    void preprocess_Test() throws Exception {
        //given
        ConstructionPreprocessRequest constructionPreprocessRequest = givenConstructionPreprocessRequest();
        ConstructionPreprocessResponse constructionPreprocessResponse = givenConstructionPreprocessResponse();
        when(constructionApiService.constructionPreprocessService(constructionPreprocessRequest)).thenReturn(constructionPreprocessResponse);
        //when
        //then
        mockMvc.perform(post("/construction/preprocess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionPreprocessRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.required_public_keys[0].address").value(constructionPreprocessResponse.getRequiredPublicKeys().getFirst().getAddress()));
    }

    @Test
    void submit_Test() throws Exception {
        //given
        ConstructionSubmitRequest constructionSubmitRequest = givenConstructionSubmitRequest();
        TransactionIdentifierResponse transactionIdentifierResponse = givenTransactionIdentifierResponse();
        when(constructionApiService.constructionSubmitService(constructionSubmitRequest)).thenReturn(transactionIdentifierResponse);
        //when
        //then
        mockMvc.perform(post("/construction/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(constructionSubmitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_identifier.hash").value(transactionIdentifierResponse.getTransactionIdentifier().getHash()));
    }
}
