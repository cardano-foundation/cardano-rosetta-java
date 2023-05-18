package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.RosettaApiApplication;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpServerErrorException;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RosettaApiApplication.class)
@Slf4j
class ConstructionApiDelegateImplPreprocessTests extends IntegrationTest{

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/preprocess");
  }

  private final String BASE_DIRECTORY = "src/test/resources/files/construction/preprocess";

  int sizeInBytes (String hex) {
    return hex.length() / 2;
  }

  @Test
   void test_should_return_a_valid_ttl_when_the_parameters_are_valid() throws IOException {

    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_request_ttl_valid.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(TestFixedData.TRANSACTION_SIZE_IN_BYTES, constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_an_input_with_a_byron_address() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_request_ttl_valid.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);
    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(TestFixedData.TRANSACTION_SIZE_IN_BYTES, constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_throw_error_when_invalid_outputs_are_sent_as_parameters() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_request_invalid_outputs.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
          baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains("ThisIsAnInvalidAddressaddr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpxInvalid"));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_throw_an_error_when_invalid_outputs_are_sent_as_parameters() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_throw_an_error_when_invalid_inputs_are_sent_as_parameters.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
          baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains("Invalid Hexadecimal Character"));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_return_a_valid_TTL_when_the_operations_include_stake_key_registration() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_return_a_valid_ttl_when_the_operations_include_stake_key_registration.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);
    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_STAKE_KEY_REGISTRATION), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_pool_retirement() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_pool_retirement.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_POOL_RETIREMENT), constructionPreprocessResponse.getOptions().getTransactionSize());

  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_stake_key_deregistration() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_stake_key_deregistration.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_STAKE_KEY_DEREGISTRATION), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_stake_delegation() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_stake_delegation.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_STAKE_DELEGATION), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_stake_key_registration_and_stake_delegation() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_stake_key_and_stake_delegation.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_STAKE_KEY_REGISTRATION_AND_STAKE_DELEGATION), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_withdrawal() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_withdrawal.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_WITHDRAWAL), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_two_withdrawals() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_two_withdrawals.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_TWO_WITHDRAWALS), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_two_withdrawal_and_stake_key_registration() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_operations_include_two_withdrawal_and_stake_key_registration.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_STAKE_KEY_REGISTRATION_AND_WITHDRAWAL), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_return_a_valid_ttl_when_using_default_relative_ttl() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_using_default_relative_ttl.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);


    assertEquals(1000, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(TestFixedData.TRANSACTION_SIZE_IN_BYTES, constructionPreprocessResponse.getOptions()
        .getTransactionSize());
  }

  @Test
  void test_should_properly_process_multiassets_transactions() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_assets_transaction.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_MA), constructionPreprocessResponse
        .getOptions().getTransactionSize());
  }

  @Test
  void test_should_properly_process_multiassets_transactions_with_several_tokens() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_assets_transaction_with_several_tokens.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_MULTIPLE_MA), constructionPreprocessResponse
        .getOptions().getTransactionSize());
  }

  @Test
  void test_should_properly_process_multiassets_transactions_with_tokens_without_name()
      throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_assets_transaction_with_tokens_without_name.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_MA_WITHOUT_NAME), constructionPreprocessResponse.getOptions().getTransactionSize());
  }

  @Test
  void test_should_fail_if_multi_asset_policy_id_is_shorter_than_expected() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_assets_policy_id_is_shorter_than_expected.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testPolicyResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }



  @Test
  void test_should_fail_if_multi_asset_policy_id_is_longer_than_expected() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_assets_policy_id_is_longer_than_expected.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testPolicyResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_multi_asset_policy_id_is_not_a_hex_string() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_assets_policy_id_is_not_the_hex_string.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testPolicyResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_multi_asset_symbol_longer_than_expected() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_asset_symbol_longer_than_expected.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testTokenNameResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_multi_asset_symbol_is_not_a_hex_string() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_asset_symbol_is_not_a_hex_string.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testTokenNameResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_multi_asset_value_for_output_operation_is_negative() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_asset_value_for_output_operation_is_negative.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testAssetValueResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_should_fail_if_multi_asset_value_is_not_number() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_multi_asset_value_is_not_number.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate
          .postForObject(baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(testAssetValueResponseFailedMessage(responseBody));
      assertEquals(500, e.getRawStatusCode());
    }
  }



  private boolean testPolicyResponseFailedMessage(String responseBody) {
    Pattern pattern = Pattern.compile("PolicyId[\\s\\S]*is not valid");
    Matcher matcher = pattern.matcher(responseBody);
    return matcher.find();
  }

  private boolean testTokenNameResponseFailedMessage(String responseBody) {
    Pattern pattern = Pattern.compile("Token name[\\s\\S]*is not valid");
    Matcher matcher = pattern.matcher(responseBody);
    return matcher.find();
  }

  private boolean testAssetValueResponseFailedMessage(String responseBody) {
    Pattern pattern = Pattern.compile("Asset[\\s\\S]*has negative or invalid value[\\s\\S]");
    Matcher matcher = pattern.matcher(responseBody);
    return matcher.find();
  }

  @Test
  void test_should_process_properly_process_transactions_with_pool_registrations_with_pledge() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_transactions_with_pool_registrations_with_pledge.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(100, constructionPreprocessResponse.getOptions().getRelativeTtl());
    assertEquals(sizeInBytes(TestFixedData.SIGNED_TX_WITH_POOL_REGISTRATION_AND_PLEDGE), constructionPreprocessResponse
        .getOptions().getTransactionSize());
  }

}
