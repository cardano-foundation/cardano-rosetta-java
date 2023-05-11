//package org.openapitools;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.restassured.RestAssured;
//import io.restassured.builder.RequestSpecBuilder;
//import io.restassured.specification.RequestSpecification;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.Base64;
//import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;
//import org.cardanofoundation.rosetta.api.model.CurveType;
//import org.cardanofoundation.rosetta.api.model.PublicKey;
//import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
//import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
//import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.cache.CacheManager;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.web.client.RestTemplate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class IntegrationTest {
//  @LocalServerPort
//  protected int serverPort;
//
//  private String baseUrl = "http://localhost";
//
//  private static RestTemplate restTemplate;
//
//  @BeforeAll
//  public static void init() {
//    restTemplate = new RestTemplate();
//  }
//
//  @BeforeEach
//  public void setUp() {
//    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/derive");
//  }
//
//  private ConstructionDeriveRequestMetadata generateMetadata(String addressType, String stakingKey, CurveType curveType) {
//    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata();
//    if(addressType != null) {
//      metadata.setAddressType(addressType);
//    }
//
//    if(stakingKey != null) {
//      PublicKey publicKey = new PublicKey();
//      publicKey.setHexBytes(stakingKey);
//      publicKey.setCurveType(curveType != null ? curveType.getValue() : CurveType.EDWARDS25519.getValue());
//      metadata.setStakingCredential(publicKey);
//    }
//    return metadata;
//  }
//
//  private ConstructionDeriveRequest generatePayload(String blockchain, String network, String key, CurveType curveType, String type, String stakingKey) {
//    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();
//
//    NetworkIdentifier networkIdentifier = new NetworkIdentifier();
//    networkIdentifier.setBlockchain(blockchain);
//    networkIdentifier.setNetwork(network);
//
//    PublicKey publicKey = new PublicKey();
//    publicKey.setHexBytes(key != null ? key : "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");
//
//    publicKey.setCurveType(curveType != null ? curveType.getValue() : CurveType.EDWARDS25519.getValue());
//    ConstructionDeriveRequestMetadata metadata = generateMetadata(type, stakingKey, curveType);
//
//    constructionDeriveRequest.setMetadata(metadata);
//    constructionDeriveRequest.setPublicKey(publicKey);
//    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
//    return constructionDeriveRequest;
//  }
//
//  @Test
//  public void testConstructionDeriveOK() {
//    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null, null, null);
//
//    ConstructionDeriveResponse constructionDeriveResponse = restTemplate.postForObject(baseUrl,
//        request, ConstructionDeriveResponse.class);
//
//    System.out.println();
//
//  }
//
//}
