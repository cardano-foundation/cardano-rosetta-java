package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signature;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.rest.SigningPayloadsRequest;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.rest.SigningPayloadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConstructionSigningPayloadsTest extends IntegrationTest {

  private final String BASE_DIRECTORY = "src/test/resources/files/construction/signingPayloads";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(String.valueOf(serverPort))
        .concat("/construction/signingPayloads");
  }
  @Test
  void test_should_return_valid_signatures()
      throws IOException {
    SigningPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/construction_signingPayloads.json"))),
        SigningPayloadsRequest.class);

    SigningPayloadsResponse signingPayloadsResponse = restTemplate.postForObject(
        baseUrl, request, SigningPayloadsResponse.class);
    Signature signature= signingPayloadsResponse.getSignatures().get(0);
    assertEquals(signature.getHexBytes(), "baf4e81506cf98426c79016564353cc932f6081b1b929fe750f42d958180e451bb8b22c8f34e08314eb635d2bbabb75f1e1a8a420edf35a646a9e11d155a7303");
    assertEquals(signature.getPublicKey().getHexBytes(), "74dabdc594506574a9b58f719787d36ea1af291d141d3e5e5ccfe076909ae106");
    assertEquals(signature.getSigningPayload().getHexBytes(), "59b6a510f88f1013e09ac2c07bfe227a7411b0dd71d1f6f3891c3d8d6d812488");
    assertEquals(signature.getSigningPayload().getAccountIdentifier().getAddress(), "addr_test1vpcv26kdu8hr9x939zktp275xhwz4478c8hcdt7l8wrl0ecjftnfa");
  }
}
