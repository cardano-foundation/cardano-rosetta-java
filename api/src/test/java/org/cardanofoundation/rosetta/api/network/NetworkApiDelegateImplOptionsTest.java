//package org.cardanofoundation.rosetta.api.network;
//
//
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Objects;
//import org.cardanofoundation.rosetta.api.IntegrationTest;
//
//import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
//import org.cardanofoundation.rosetta.api.common.enumeration.OperationTypeStatus;
//import org.cardanofoundation.rosetta.api.model.rest.NetworkOptionsResponse;
//import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
//import org.cardanofoundation.rosetta.api.network.utils.Common;
//import org.cardanofoundation.rosetta.api.util.RosettaConstants;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.openapitools.client.model.Allow;
//import org.openapitools.client.model.BalanceExemption;
//import org.openapitools.client.model.Error;
//import org.openapitools.client.model.ExemptionType;
//import org.openapitools.client.model.OperationStatus;
//import org.openapitools.client.model.Version;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//
//public class NetworkApiDelegateImplOptionsTest extends IntegrationTest {
//  @BeforeEach
//  public void setUp() {
//    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/network/options");
//  }
//
//  @Test
//  void test_when_request_with_valid_payload_should_return_object_containing_proper_version_information(){
//    NetworkRequest requestPayload = Common.generateNetworkPayload(RosettaConstants.BLOCKCHAIN_NAME,
//        RosettaConstants.MAINNET);
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
//
//    Version version = new Version().nodeVersion("cardano-node 8.0.0 - linux-x86_64 - ghc-8.10")
//        .rosettaVersion("1.4.13")
//        .middlewareVersion("1.0.0-SNAPSHOT")
//        .metadata(new LinkedHashMap<>());
//
//    HttpEntity<NetworkRequest> requestEntity = new HttpEntity<>(requestPayload , headers);
//    ResponseEntity<NetworkOptionsResponse> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity,
//        NetworkOptionsResponse.class);
//    assertEquals(HttpStatus.OK , responseEntity.getStatusCode() );
//    assertEquals(Objects.requireNonNull(responseEntity.getBody()).getVersion() , version);
//  }
//
//  @Test
//  void test_when_request_with_valid_payload_should_return_object_containing_proper_allow_information(){
//    NetworkRequest requestPayload = Common.generateNetworkPayload(RosettaConstants.BLOCKCHAIN_NAME,
//        RosettaConstants.MAINNET);
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
//
//    OperationStatus success = new OperationStatus().successful(true).status(OperationTypeStatus.SUCCESS.getValue());
//    OperationStatus invalid = new OperationStatus().successful(false).status(OperationTypeStatus.INVALID.getValue());
//    List<OperationStatus> operationStatuses = List.of(success,invalid);
//
//    BalanceExemption balanceExemption1 = new BalanceExemption().exemptionType(ExemptionType.GREATER_OR_EQUAL);
//    BalanceExemption balanceExemption2 = new BalanceExemption().exemptionType(ExemptionType.LESS_OR_EQUAL);
//    BalanceExemption balanceExemption3 = new BalanceExemption().exemptionType(ExemptionType.DYNAMIC);
//
//
//    Allow allow = new Allow().operationStatuses(operationStatuses)
//        .operationTypes(Arrays.stream(OperationType.values()).map(OperationType::getValue).toList())
//        .errors(RosettaConstants.ROSETTA_ERRORS.stream().map(error ->
//                    new Error().code(error.getCode())
//                        .message(error.getMessage())
//                        .retriable(error.isRetriable())
//                        .description(error.getDescription())
//                        .code(error.getCode())
//                )
//                .sorted(Comparator.comparingInt(Error::getCode))
//                .toList())
//        .historicalBalanceLookup(true)
//        .callMethods(new ArrayList<>())
//        .balanceExemptions(List.of(balanceExemption1 , balanceExemption2 ,balanceExemption3))
//        .mempoolCoins(false);
//
//    HttpEntity<NetworkRequest> requestEntity = new HttpEntity<>(requestPayload , headers);
//    ResponseEntity<NetworkOptionsResponse> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity,
//        NetworkOptionsResponse.class);
//    assertEquals(HttpStatus.OK , responseEntity.getStatusCode() );
//    assertEquals(Objects.requireNonNull(responseEntity.getBody()).getAllow() , allow);
//  }
//
//}
