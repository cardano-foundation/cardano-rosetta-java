package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.exception.ApiException;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;
import org.cardanofoundation.rosetta.api.model.CurveType;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.impl.ConstructionApiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class ConstructionApiDelegateImplDeriveTests extends IntegrationTest {

  public final String INVALID_PUBLIC_KEY_FORMAT_MESSAGE = "invalidPublicKeyFormat";
  public final String INVALID_STAKING_KEY_FORMAT_MESSAGE = "invalidStakingKeyFormat";

  public final String INVALID_ADDRESS_TYPE_ERROR = "invalidAddressTypeError";

  public final String MISSING_KEY_ERROR_MESSAGE = "missingStakingKeyError";
  private ConstructionApiServiceImpl constructionApiServiceImplUnderTest;

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(String.valueOf(serverPort)).concat("/construction/derive");
    constructionApiServiceImplUnderTest = new ConstructionApiServiceImpl();
    constructionApiServiceImplUnderTest.setCardanoService(mock(CardanoService.class)) ;
  }

  private ConstructionDeriveRequestMetadata generateMetadata(String addressType, String stakingKey,
      CurveType curveType) {
    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata();
    if (addressType != null) {
      metadata.setAddressType(addressType);
    }

    if (stakingKey != null) {
      PublicKey publicKey = new PublicKey();
      publicKey.setHexBytes(stakingKey);
      publicKey.setCurveType(
          curveType != null ? curveType.getValue() : CurveType.EDWARDS25519.getValue());
      metadata.setStakingCredential(publicKey);
    }
    return metadata;
  }

  private ConstructionDeriveRequest generatePayload(String blockchain, String network, String key,
      CurveType curveType, String type, String stakingKey) {
    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();

    NetworkIdentifier networkIdentifier = new NetworkIdentifier();
    networkIdentifier.setBlockchain(blockchain);
    networkIdentifier.setNetwork(network);

    PublicKey publicKey = new PublicKey();
    publicKey.setHexBytes(
        key != null ? key : "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");

    publicKey.setCurveType(
        curveType != null ? curveType.getValue() : CurveType.EDWARDS25519.getValue());
    ConstructionDeriveRequestMetadata metadata = generateMetadata(type, stakingKey, curveType);

    constructionDeriveRequest.setMetadata(metadata);
    constructionDeriveRequest.setPublicKey(publicKey);
    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
    return constructionDeriveRequest;
  }

  @Test
  void test_construction_derive_ok() {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null, null,
        null);

    ConstructionDeriveResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionDeriveResponse.class);

    assertEquals(response.getAccountIdentifier().getAddress(),
        "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx");
  }

  @Test
  void test_short_key_length() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", "smallPublicKey",
        null, null, null);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4007, error.getCode());
      assertEquals("Invalid public key format", error.getMessage());
    }
  }

  @Test
  void test_long_key_length() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet",
        "ThisIsABiggerPublicKeyForTestingPurposesThisIsABiggerPublicKeyForTestingPurposes", null,
        null, null);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4007, error.getCode());
      assertEquals("Invalid public key format", error.getMessage());
    }
  }

  @Test
  void test_staking_key_invalid_format() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null,
        "Enterprise", "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F__");

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4017, error.getCode());
      assertEquals("Invalid staking key format", error.getMessage());
    }
  }

  @Test
  void test_address_type_invalid_format() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null, "Invalid",
        "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");
    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4016, error.getCode());
      assertEquals("Provided address type is invalid", error.getMessage());
    }

  }

  @Test
  void test_should_return_base_address_when_providing_valid_keys_and_network() {
    ConstructionDeriveRequest request = generatePayload("cardano",
        "mainnet",
        "159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
        null, "Base",
        "964774728c8306a42252adbfb07ccd6ef42399f427ade25a5933ce190c5a8760");

    ConstructionDeriveResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionDeriveResponse.class);

    assertEquals(response.getAccountIdentifier().getAddress(),
        "addr1q9dhy809valxaer3nlvg2h5nudd62pxp6lu0cs36zczhfr98y6pah6lvppk8xft57nef6yexqh6rr204yemcmm3emhzsgg4fg0");
  }

  @Test
  void test_staking_key_missing() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload("cardano",
        "mainnet",
        "159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
        null,
        "Base",
        null);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4018, error.getCode());
      assertEquals("Staking key is required for this type of address", error.getMessage());
    }
  }

  @Test
  void test_should_return_the_reward_address() {
    ConstructionDeriveRequest request = generatePayload("cardano",
        "mainnet",
        "964774728c8306a42252adbfb07ccd6ef42399f427ade25a5933ce190c5a8760",
        null,
        "Reward",
        null);

    ConstructionDeriveResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionDeriveResponse.class);

    assertEquals(response.getAccountIdentifier().getAddress(),
        "stake1uxnjdq7ma0kqsmrny460fu5azvnqtap3486jvaudacuam3g3yc4nu");
  }

  @Test
  void test_short_staking_key() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload(
        "cardano",
        "mainnet",
        null,
        null,
        null,
        "smallPublicKey");

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4017, error.getCode());
      assertEquals("Invalid staking key format", error.getMessage());
    }
  }

  @Test
  void test_long_staking_key() throws JsonProcessingException {
    ConstructionDeriveRequest request = generatePayload(
        "cardano",
        "mainnet",
        null,
        null,
        null,
        "ThisIsABiggerPublicKeyForTestingPurposesThisIsABiggerPublicKeyForTestingPurposes");

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertFalse(error.isRetriable());
      assertEquals(4017, error.getCode());
      assertEquals("Invalid staking key format", error.getMessage());
    }
  }

  @Test
  void testConstructionDeriveService_CardanoServiceGenerateAddressReturnsNull() throws Exception {
    // Setup
    final ConstructionDeriveRequest request = ConstructionDeriveRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder().blockchain("cardano")
            .network("mainnet")
            .build())
        .publicKey(new PublicKey("159abeeecdf167ccc0ea60b30f9522154a0d74161aeb159fb43b6b0695f057b3",
            "edwards25519"))
        .metadata(new ConstructionDeriveRequestMetadata(null, null))
        .build();

    // Configure CardanoService.getNetworkIdentifierByRequestParameters(...).
    when(constructionApiServiceImplUnderTest.getCardanoService().getNetworkIdentifierByRequestParameters(
        any())).thenReturn(NetworkIdentifierType.CARDANO_MAINNET_NETWORK);
    when(constructionApiServiceImplUnderTest.getCardanoService().isKeyValid(anyString(),
        anyString())).thenReturn(true);
    when(constructionApiServiceImplUnderTest.getCardanoService().isAddressTypeValid(anyString()))
        .thenReturn(true);
    when(constructionApiServiceImplUnderTest.getCardanoService().generateAddress(
        eq(NetworkIdentifierType.CARDANO_MAINNET_NETWORK), anyString(), anyString(),
        eq(AddressType.ENTERPRISE))).thenReturn(null);
    try {
      ConstructionDeriveResponse constructionDeriveResponse = constructionApiServiceImplUnderTest.constructionDeriveService(
          request);
    } catch (ApiException e) {
      Error error = e.getError();
      assertFalse(error.isRetriable());
      assertEquals(5002, error.getCode());
      assertEquals("Address generation error", error.getMessage());
    }
  }
}
