package org.cardanofoundation.rosetta.api.constructionApiService.impl;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.SigningPayload;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class ConstructionApiDelegateImplPayloadTests extends IntegrationTest {


  private static final String MISSING_POOL_KEY_HASH_MESSAGE = "Pool key hash is required to operate";
  private static final String BASE_DIRECTORY = "src/test/resources/files/construction/payload";
  private static final String INVALID_HEXADECIMAL_CHARACTER_MESSAGE = "Invalid Hexadecimal Character: I";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/payloads");
  }

  @Test
  void test_send_valid_input_and_output_operations() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payloads_request_valid.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().get(0).getAccountIdentifier().getAddress(),
        "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795");
  }

  @Test
  void test_send_a_input_with_Byron_Address() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payloads_request_with_byron_input.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().get(0).getAccountIdentifier().getAddress(),
        "Ae2tdPwUPEZC6WJfVQxTNN2tWw4skGrN6zRVukvxJmTFy1nYkVGQBuURU3L");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795");
  }

  @Test
  void test_receive_single_payload_for_each_input_address() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payload_multiple_inputs.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().size(), 1);

  }

  @Test
  void test_return_an_error_when_operations_with_invalid_types() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payloads_invalid_operation_type.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_key_registration()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_key_registration.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().get(0).getAccountIdentifier().getAddress(),
        "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "ec6bb1091d68dcb3e4f4889329e143fbb6090b8e78c74e7c8d0903d9eec4eed1");
  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_delegation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().size(), 2);
    String address1 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address2 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";

    String hexBytes = "ca12b42830eb7b53cf73c9f8b35875619a47e3e7569ebd13c3c309396ffc47d8";
    // Check for address1 and hexBytes
    boolean found1 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address1) &&
          payload.getHexBytes().equals(hexBytes)) {
        found1 = true;
        break;
      }
    }
    assertThat(found1).isTrue();

    // Check for address2 and hexBytes
    boolean found2 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address2) &&
          payload.getHexBytes().equals(hexBytes)) {
        found2 = true;
        break;
      }
    }
    assertThat(found2).isTrue();

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_key_registration_stake_delegation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_key_registration_and_stake_delegation.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);
    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String hexBytes = "dbf6479409a59e3e99c79b9c46b6af714de7c8264094b1d38c373b7454acf33d";
    assertEquals(response.getPayloads().size(), 2);
    boolean found1 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address1) &&
          payload.getHexBytes().equals(hexBytes)) {
        found1 = true;
        break;
      }
    }
    assertThat(found1).isTrue();

    // Check for address2 and hexBytes
    boolean found2 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address2) &&
          payload.getHexBytes().equals(hexBytes)) {
        found2 = true;
        break;
      }
    }
    assertThat(found2).isTrue();

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_withdraw()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payload_valid_operations_including_withdrawal.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String hexBytes = "da2eb0d62aee9313fc68df0827bd176b55168bc9129aedce92f4e29b1d52de38";
    assertEquals(response.getPayloads().size(), 2);
    boolean found1 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address1) &&
          payload.getHexBytes().equals(hexBytes)) {
        found1 = true;
        break;
      }
    }
    assertThat(found1).isTrue();

    // Check for address2 and hexBytes
    boolean found2 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address2) &&
          payload.getHexBytes().equals(hexBytes)) {
        found2 = true;
        break;
      }
    }
    assertThat(found2).isTrue();

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_withdraw_and_stake_registration()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_withdrwal_and_stake_registration.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String hexBytes = "8b47f0f3690167b596f1e7623e1869148f6bea78ebceaa08fe890a2e3e9e4d89";
    assertEquals(response.getPayloads().size(), 2);
    boolean found1 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address1) &&
          payload.getHexBytes().equals(hexBytes)) {
        found1 = true;
        break;
      }
    }
    assertThat(found1).isTrue();

    // Check for address2 and hexBytes
    boolean found2 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address2) &&
          payload.getHexBytes().equals(hexBytes)) {
        found2 = true;
        break;
      }
    }
    assertThat(found2).isTrue();

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_key_deregistration()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_deregistration.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);
    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String hexBytes = "9c0f4e7fa746738d3df3665fc7cd11b2e3115e3268a047e0435f2454ed41fdc5";
    assertEquals(response.getPayloads().size(), 2);
    boolean found1 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address1) &&
          payload.getHexBytes().equals(hexBytes)) {
        found1 = true;
        break;
      }
    }
    assertThat(found1).isTrue();

    // Check for address2 and hexBytes
    boolean found2 = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address2) &&
          payload.getHexBytes().equals(hexBytes)) {
        found2 = true;
        break;
      }
    }
    assertThat(found2).isTrue();

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_retirement()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_pool_retirement.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().size(), 2);
    assertEquals(response.getPayloads().get(0).getAccountIdentifier().getAddress(),
        "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "ec44114edfb063ce344797f95328ccfd8bc1c92f71816803803110cfebbb8360");
    assertEquals(response.getPayloads().get(1).getAccountIdentifier().getAddress(),
        "153806dbcd134ddee69a8c5204e38ac80448f62342f8c23cfe4b7edf");
    assertEquals(response.getPayloads().get(1).getHexBytes(),
        "ec44114edfb063ce344797f95328ccfd8bc1c92f71816803803110cfebbb8360");

  }

  @Test
  void test_should_throw_an_error_when_no_epoch_was_sent_on_pool_retirement_operation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_no_epoch_was_sent_on_pool_retirement_operation.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_an_error_when_no_staking_key_is_provided_in_staking_key_registration_operation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_no_staking_key_is_provided_in_staking_key_registration.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_an_error_when_staking_key_in_one_operation_has_invalid_format()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_staking_key_in_one_operation_has_invalid_format.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_an_error_when_staking_key_in_one_operation_has_a_bigger_length_than_32()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_staking_key_in_one_operation_has_a_bigger_length_than_32.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_an_error_when_staking_key_in_one_operation_has_a_smaller_length_than_32()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_staking_key_in_one_operation_has_a_smaller_length_than_32.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_an_error_when_no_pool_key_hash_is_provided_for_stake_delegation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_no_pool_key_hash_is_provided_for_stake_delegation.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(MISSING_POOL_KEY_HASH_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_an_error_when_an_invalid_pool_key_hash_is_provided_for_stake_delegation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_an_invalid_pool_key_hash_is_provided_for_stake_delegation.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(INVALID_HEXADECIMAL_CHARACTER_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operation_including_ma_amount()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operation_including_ma_amount.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String hexBytes = "3a4e241fe0c56f8001cb2e71ffdf10e2804437b4159930c32d59e3b4469203d6";
    boolean found = false;
    for (SigningPayload payload : response.getPayloads()) {
      if (payload.getAccountIdentifier().getAddress().equals(address) &&
          payload.getHexBytes().equals(hexBytes)) {
        found = true;
        break;
      }
    }
    assertThat(found).isTrue();
  }


  @Test
  void test_should_fail_if_MultiAsset_policy_id_is_shorter_than_expected() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_MultiAsset_policy_id_is_shorter_than_expected.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testPolicyResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_MultiAsset_policy_id_is_not_a_hex_string() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_MultiAsset_policy_id_is_not_a_hex_string.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testPolicyResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_MultiAsset_policy_id_is_longer_than_expected() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_MultiAsset_policy_id_is_longer_than_expected.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testPolicyResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  private boolean testPolicyResponseFailedMessage(String responseBody) {
    Pattern pattern = Pattern.compile("[\\s\\S]PolicyId[\\s\\S]*is not valid");
    Matcher matcher = pattern.matcher(responseBody);
    return matcher.find();
  }

  @Test
  void test_should_fail_if_MultiAsset_symbol_is_not_a_hex_string() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_MultiAsset_symbol_is_not_a_hex_string.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testTokenNameResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_MultiAsset_symbol_longer_than_expected() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_MultiAsset_symbol_longer_than_expected.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testTokenNameResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  private boolean testTokenNameResponseFailedMessage(String responseBody) {
    Pattern pattern = Pattern.compile("Token name[\\s\\S]*is not valid");
    Matcher matcher = pattern.matcher(responseBody);
    return matcher.find();
  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_with_pool_registration_with_pledge()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_transactions_with_pool_registrations_with_pledge.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "19d2feaca112ef7df5b703509a2ae37a743fcc174b8aa3898e0e9f4f577b4c80";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_Single_Host_Addr_relay()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_transaction_hash_when_sending_valid_operations_including_pool_registration_with_Single_Host_Addr_relay.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "53f456cca6b8a6869301509cbd8c91f24dfef5b924b8c1e46934396cb3e47479";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_Single_Host_Name_relay()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_transaction_hash_when_sending_valid_operations_including_pool_registration_with_Single_Host_Name_relay.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "ad36f2e07fb6d0a0864bcda0deea6c081770ddb3390eac85c7696479930be608";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }


  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_multi_host_name_relay()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_transaction_hash_when_sending_valid_operations_including_pool_registration_with_multi_host_name_relay.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "bd874d7253af650d7d265a5dd259a16d838593e99252d3ed5409b84a2d3e864e";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }


  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_no_pool_metadata()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_transaction_hash_when_sending_valid_operations_including_pool_registration_with_no_pool_metadata.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "f7eb90cf4a22f0f214f37feaa00000683857e4216999ca089bab94cc012d83a9";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_multiple_relay()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_transaction_hash_when_sending_valid_operations_including_pool_registration_with_multiple_relay.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1uxly0q2cnpxrjrqm9vpnr9dwkr0j945gulhhgs3dx33l47sweg9er";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "c1939edfd1cfb1293ea9069dbd8bfd4ca0bb7ac92743b0243b27e5128da76909";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }

  private static boolean isAddressFoundInPayloads(List<SigningPayload> payloads, String address,
      String hexBytes) {
    for (SigningPayload payload : payloads) {
      if (payload.getAccountIdentifier().getAddress().equals(address) &&
          payload.getHexBytes().equals(hexBytes)) {
        return true;
      }
    }
    return false;
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_invalid_code_key_hash()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_invalid_code_key_hash.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_error_when_operations_include_pool_registration_with_missing_cold_key_hash()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_include_pool_registration_with_missing_cold_key_hash.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_empty_pool_relays()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_empty_pool_relays.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_pool_relay_type()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_invalid_pool_relay_type.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_missing_pool_relay_type()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_missing_pool_relay_type.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_pool_relays_with_invalid_ipv4()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_invalid_pool_relays_with_invalid_ipv4.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_pool_relays_with_invalid_port()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_invalid_pool_relays_with_invalid_port.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_invalid_pool_metadata_hash()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_pool_relays_with_invalid_pool_metadata_hash.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertTrue(error.isRetriable());
//      assertEquals(5000,error.getCode());
//      assertEquals("Invalid public key format",error.getDetails());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_invalid_pool_owners()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_pool_relays_with_invalid_pool_owners.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_negative_cost()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_negative_cost.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_negative_pledge()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_pool_relays_with_negative_pledge.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_negative_denominator()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_pool_relays_with_negative_denominator.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_alphabetical_numerator()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_pool_relays_with_alphabetical_numerator.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_no_margin()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_no_margin.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_reward_address()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_including_pool_registration_with_invalid_reward_address.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_with_pool_registration_with_cert()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_with_pool_registration_with_cert.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address1 = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String address2 = "stake1u9af5n26dtr6nkrs9qv05049x0jkcncau9k6vyd8xrhr7qq8tez5p";
    String address3 = "stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5";
    String address4 = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";
    String hexBytes = "36939bdede6c9170adea85911197806bca6a25bb56ef2d09ed7c407a31789eb8";

    boolean found1 = isAddressFoundInPayloads(response.getPayloads(), address1, hexBytes);
    boolean found2 = isAddressFoundInPayloads(response.getPayloads(), address2, hexBytes);
    boolean found3 = isAddressFoundInPayloads(response.getPayloads(), address3, hexBytes);
    boolean found4 = isAddressFoundInPayloads(response.getPayloads(), address4, hexBytes);

    assertThat(found1).isTrue();
    assertThat(found2).isTrue();
    assertThat(found3).isTrue();
    assertThat(found4).isTrue();
  }

  @Test
  void test_should_throw_an_error_when_sending_operations_with_pool_registration_with_invalid_cert()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_with_pool_registration_with_invalid_cert.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_sending_operations_with_pool_registration_with_invalid_cert_type()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_operations_with_pool_registration_with_invalid_cert_type.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }


  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_with_a_vote_registration()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_with_pool_registration_with_cert.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    String address = "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx";
    String hexBytes = "91dd32dbf59dc3295f53f1dc0ac02d317e139b1c4a26fef4c4172b4b9b0a2de1";

    boolean found = isAddressFoundInPayloads(response.getPayloads(), address, hexBytes);
  }

  @Test
  void test_should_throw_an_error_when_the_voting_key_is_empty() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_the_voting_key_is_empty.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_voting_key_is_not_valid() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_the_voting_key_is_not_valid.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_reward_address_is_empty() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_reward_address_is_empty.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_reward_address_is_not_valid() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_reward_address_is_not_valid.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_stake_key_is_empty() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_stake_key_is_empty.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_stake_key_is_not_valid() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_stake_key_is_not_valid.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_voting_nonce_is_not_greater_than_zero()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_voting_nonce_is_not_greater_than_zero.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_voting_signature_is_empty() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_voting_signature_is_empty.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_voting_signature_is_not_valid() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_voting_signature_is_not_valid.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_the_transaction_has_no_metadata() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_the_transaction_has_no_metadata.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_throw_an_error_when_there_is_no_vote_registration_metadata() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_there_is_no_vote_registration_metadata.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionPayloadsResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_return_valid_unsigned_transaction_when_including_related_operation()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_including_related_operation.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionPayloadsResponse.class);
    assertEquals(response.getUnsignedTransaction(),
        "8279013a6135303038313832353832303266323366643863636138333561663231663361633337356261633630316639376561643735663265373931343362646637316665326334626530343365386630313031383238323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032316130303661306337303033313930336538303438313832303038323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a7746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e646578007272656c617465645f6f7065726174696f6e7381a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a26761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b6370786b7375625f6163636f756e74a16761646472657373782a30783662313735343734653839303934633434646139386239353465656465616334393532373164306666616d6f756e74a26576616c7565682d393030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a5746f7065726174696f6e5f6964656e746966696572a165696e646578036474797065747374616b654b6579526567697374726174696f6e667374617475736773756363657373676163636f756e74a16761646472657373783b7374616b653175786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e376361656b376135686d65746164617461a1727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c656477617264733235353139");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "ec6bb1091d68dcb3e4f4889329e143fbb6090b8e78c74e7c8d0903d9eec4eed1");
  }
  @Test
  void test_should_return_valid_unsigned_transaction_when_including_amount_withdrawal_metadata()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payloads_when_including_amount_withdraw_metadata.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionPayloadsResponse.class);
    assertEquals(response.getUnsignedTransaction(),
        "8279013a6135303038313832353832303266323366643863636138333561663231663361633337356261633630316639376561643735663265373931343362646637316665326334626530343365386630313031383238323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032316130303661306337303033313930336538303438313832303038323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26576616c7565682d393030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a5746f7065726174696f6e5f6964656e746966696572a165696e646578036474797065747374616b654b6579526567697374726174696f6e667374617475736773756363657373676163636f756e74a16761646472657373783b7374616b653175786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e376361656b376135686d65746164617461a2727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c656477617264733235353139707769746864726177616c416d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c75656a2d313835333530343530");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "ec6bb1091d68dcb3e4f4889329e143fbb6090b8e78c74e7c8d0903d9eec4eed1");
  }

  @Test
  void test_should_return_valid_unsigned_transaction_when_including_amount_refund_metadata()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payloads_when_including_amount_refund_metadata.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionPayloadsResponse.class);
    assertEquals(response.getUnsignedTransaction(),
        "82790132613530303831383235383230326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663031303138323832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666230313832353831643631626234306631613634376263383863316264366237333864623865623636333537643932363437346561356666643662616137366339666230343032316130353762636566623033313930336538303438313832303138323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26576616c7565692d39303030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a5746f7065726174696f6e5f6964656e746966696572a165696e646578036474797065767374616b654b65794465726567697374726174696f6e667374617475736773756363657373676163636f756e74a16761646472657373783b7374616b653175387a666e6b687034673676686e6565746d763271656e3766356e64726b6c716a7138653973326e636b3968333063667a36716d70686d65746164617461a2727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c6564776172647332353531396c726566756e64416d6f756e74a26576616c7565682d323030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c7306");
    assertEquals(response.getPayloads().get(1).getHexBytes(),
        "7e07d9708cd2b4bfe091f97e7108f58b8b06d33113b71a2bb463a1a66d2d377e");
  }

  @Test
  void test_should_return_valid_unsigned_transaction_when_including_amount_deposit_metadata()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payloads_when_including_amount_deposit_metadata.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionPayloadsResponse.class);
    assertEquals(response.getUnsignedTransaction(),
        "8279013a6135303038313832353832303266323366643863636138333561663231663361633337356261633630316639376561643735663265373931343362646637316665326334626530343365386630313031383238323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032316130303661306337303033313930336538303438313832303038323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26576616c7565682d393030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a5746f7065726174696f6e5f6964656e746966696572a165696e646578036474797065747374616b654b6579526567697374726174696f6e667374617475736773756363657373676163636f756e74a16761646472657373783b7374616b653175786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e376361656b376135686d65746164617461a2727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c6564776172647332353531396d6465706f736974416d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c75656732303030303030");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "ec6bb1091d68dcb3e4f4889329e143fbb6090b8e78c74e7c8d0903d9eec4eed1");
  }
  @Test
  void test_should_return_valid_unsigned_transaction_when_including_sub_account()
      throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_when_including_sub_account.json"))),
        ConstructionPayloadsRequest.class);
    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl, request,
        ConstructionPayloadsResponse.class);
    assertEquals(response.getUnsignedTransaction(),
        "8279013a6135303038313832353832303266323366643863636138333561663231663361633337356261633630316639376561643735663265373931343362646637316665326334626530343365386630313031383238323538316436316262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662313932373130383235383164363162623430663161363437626338386331626436623733386462386562363633353764393236343734656135666664366261613736633966623139396334303032316130303661306337303033313930336538303438313832303038323030353831636262343066316136343762633838633162643662373338646238656236363335376439323634373465613566666436626161373663396662a16a6f7065726174696f6e7382a7746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e646578007272656c617465645f6f7065726174696f6e7381a265696e646578006d6e6574776f726b5f696e64657800647479706565696e707574667374617475736773756363657373676163636f756e74a16761646472657373783a616464723176786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e3763706e6b63707866616d6f756e74a26576616c7565682d393030303030306863757272656e6379a26673796d626f6c6341444168646563696d616c73066b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842326632336664386363613833356166323166336163333735626163363031663937656164373566326537393134336264663731666532633462653034336538663a316b636f696e5f616374696f6e6a636f696e5f7370656e74a5746f7065726174696f6e5f6964656e746966696572a165696e646578036474797065747374616b654b6579526567697374726174696f6e667374617475736773756363657373676163636f756e74a26761646472657373783b7374616b653175786135707564786737376733736461646465636d773874766336686d796e79776e34396c6c747434666d766e376361656b3761356b7375625f6163636f756e74a16761646472657373782a307836623137353437346538393039346334346461393862393534656564656163343935323731643066686d65746164617461a1727374616b696e675f63726564656e7469616ca2696865785f62797465737840314234303044363041414633344541463644434241423942424134363030314132333439373838364346313130363646373834363933334433304535414433466a63757276655f747970656c656477617264733235353139");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "ec6bb1091d68dcb3e4f4889329e143fbb6090b8e78c74e7c8d0903d9eec4eed1");
  }


}


