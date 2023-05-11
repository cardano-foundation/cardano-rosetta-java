package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;
import org.cardanofoundation.rosetta.api.model.CurveType;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

public class ConstructionApiDelegateImplDeriveTests extends IntegrationTest{
  public final String INVALID_PUBLIC_KEY_FORMAT_MESSAGE = "invalidPublicKeyFormat";
  public final String INVALID_STAKING_KEY_FORMAT_MESSAGE = "invalidStakingKeyFormat";

  public final String INVALID_ADDRESS_TYPE_ERROR = "invalidAddressTypeError";

  public final String MISSING_KEY_ERROR_MESSAGE  = "missingStakingKeyError";


  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/derive");
  }

  private ConstructionDeriveRequestMetadata generateMetadata(String addressType, String stakingKey, CurveType curveType) {
    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata();
    if(addressType != null) {
      metadata.setAddressType(addressType);
    }

    if(stakingKey != null) {
      PublicKey publicKey = new PublicKey();
      publicKey.setHexBytes(stakingKey);
      publicKey.setCurveType(curveType != null ? curveType.getValue() : CurveType.EDWARDS25519.getValue());
      metadata.setStakingCredential(publicKey);
    }
    return metadata;
  }

  private ConstructionDeriveRequest generatePayload(String blockchain, String network, String key, CurveType curveType, String type, String stakingKey) {
    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();

    NetworkIdentifier networkIdentifier = new NetworkIdentifier();
    networkIdentifier.setBlockchain(blockchain);
    networkIdentifier.setNetwork(network);

    PublicKey publicKey = new PublicKey();
    publicKey.setHexBytes(key != null ? key : "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");

    publicKey.setCurveType(curveType != null ? curveType.getValue() : CurveType.EDWARDS25519.getValue());
    ConstructionDeriveRequestMetadata metadata = generateMetadata(type, stakingKey, curveType);

    constructionDeriveRequest.setMetadata(metadata);
    constructionDeriveRequest.setPublicKey(publicKey);
    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
    return constructionDeriveRequest;
  }

  @Test
  void test_construction_derive_ok() {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null, null, null);

    ConstructionDeriveResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionDeriveResponse.class);

    assertEquals(response.getAccountIdentifier().getAddress(), "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx");
  }

  @Test
  void test_short_key_length() {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", "smallPublicKey", null, null, null);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(INVALID_PUBLIC_KEY_FORMAT_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }

  }

  @Test
  void test_long_key_length() {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", "ThisIsABiggerPublicKeyForTestingPurposesThisIsABiggerPublicKeyForTestingPurposes", null, null, null);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(INVALID_PUBLIC_KEY_FORMAT_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_staking_key_invalid_format() {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null, "Enterprise", "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F__");

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(INVALID_STAKING_KEY_FORMAT_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_address_type_invalid_format() {
    ConstructionDeriveRequest request = generatePayload("cardano", "mainnet", null, null, "Invalid", "1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");
    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(INVALID_ADDRESS_TYPE_ERROR));
      assertEquals(500, e.getRawStatusCode());
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


    assertEquals(response.getAccountIdentifier().getAddress(), "addr1q9dhy809valxaer3nlvg2h5nudd62pxp6lu0cs36zczhfr98y6pah6lvppk8xft57nef6yexqh6rr204yemcmm3emhzsgg4fg0");
  }

  @Test
  void test_staking_key_missing() {
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
      assertTrue(responseBody.contains(MISSING_KEY_ERROR_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
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


    assertEquals(response.getAccountIdentifier().getAddress(), "stake1uxnjdq7ma0kqsmrny460fu5azvnqtap3486jvaudacuam3g3yc4nu");
  }

  @Test
  void test_short_staking_key() {
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
      assertTrue(responseBody.contains(INVALID_STAKING_KEY_FORMAT_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_long_staking_key() {
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
      assertTrue(responseBody.contains(INVALID_STAKING_KEY_FORMAT_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

}
