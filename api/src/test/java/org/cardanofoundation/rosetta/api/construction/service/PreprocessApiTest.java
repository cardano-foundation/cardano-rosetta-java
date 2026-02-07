package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PreprocessApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionPreprocessRequest getPreprocessRequest(String constructionPayloadFile)
      throws IOException {
    File file =
        new File(this.getClass().getClassLoader().getResource(constructionPayloadFile).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionPreprocessRequest request = mapper.readValue(file,
        ConstructionPreprocessRequest.class);

    return request;
  }

  @Test
  void simplePreprocessTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/simple_preprocess.json", 1000, 234);
  }

  @Test
  void twoWithdrawalsTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/two_withdrawals.json", 100, 409);
  }

  @Test
  void poolRegistrationTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/pool_registration.json", 100, 930);
  }

  @Test
  void dRepDelegationKeyHashTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/drep_vote_delegation-keyhash.json", 100, 405);
  }

  @Test
  void dRepDelegationAbstainTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/drep_vote_delegation-keyhash.json", 100, 405);
  }

  @Test
  void dRepDelegationNoConfidenceTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/drep_vote_delegation-keyhash.json", 100, 405);
  }

  @Test
  void govPoolActionVoteTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/pool_governance_vote.json", 100, 408);
  }

  @Nested
  class Cip129DRepTypeInference {

    @Test
    void shouldInferKeyHashType_whenCip129PrefixedIdWithoutType() throws IOException {
      // CIP-129 prefixed id with 0x22 header (key_hash), no type provided
      assertPreprocessRequest(
          "testdata/construction/preprocess/drep_vote_delegation-cip129_keyhash_no_type.json",
          100, 405);
    }

    @Test
    void shouldInferScriptHashType_whenCip129PrefixedIdWithoutType() throws IOException {
      // CIP-129 prefixed id with 0x23 header (script_hash), no type provided
      assertPreprocessRequest(
          "testdata/construction/preprocess/drep_vote_delegation-cip129_scripthash_no_type.json",
          100, 405);
    }

    @Test
    void shouldFail_whenRawIdWithoutType() throws IOException {
      // Raw 28-byte id without type should fail with MISSING_DREP_TYPE (5040)
      assertPreprocessFail(
          "testdata/construction/preprocess/drep_vote_delegation-raw_no_type_shouldFail.json",
          5040);
    }

    @Test
    void shouldFail_whenInvalidNonDRepHeader() throws IOException {
      // CIP-129 id with non-DRep header (0x12) should fail with INVALID_DREP_TYPE (5037)
      assertPreprocessFail(
          "testdata/construction/preprocess/drep_vote_delegation-invalid_header_shouldFail.json",
          5037);
    }
  }

  private void assertPreprocessRequest(String constructionPayloadFile, int expectedTtl,
      int expectedTransactionSize)
      throws IOException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(constructionPayloadFile);

    ConstructionPreprocessResponse constructionPreprocessResponse = constructionApiService.constructionPreprocessService(
        preprocessRequest);
    Map<String, Integer> options = (Map<String, Integer>) constructionPreprocessResponse.getOptions();

    assertEquals(expectedTtl, options.get("relative_ttl"), "relative_ttl is not as expected");
    assertEquals(expectedTransactionSize, options.get("transaction_size"), "transaction_size is not as expected");
  }

  private void assertPreprocessFail(String constructionPayloadFile, int expectedErrorCode)
      throws IOException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(constructionPayloadFile);

    ApiException exception = Assertions.assertThrows(
        ApiException.class,
        () -> constructionApiService.constructionPreprocessService(preprocessRequest));

    assertThat(exception.getError().getCode()).isEqualTo(expectedErrorCode);
  }

}
