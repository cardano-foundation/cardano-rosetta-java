package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConstructionApiDelegateImplCombineTests extends IntegrationTest{

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/combine");
  }

  @Test
  void test_should_return_signed_transaction_when_providing_valid_unsigned_transaction_and_signatures() {

  }

  @Test
  void test_should_return_signed_transaction_with_metadata() {

  }

  @Test
  void test_should_return_signed_transaction_with_byron_address() {

  }

  @Test
  void test_should_return_error_when_providing_valid_unsigned_transaction_but_invalid_signatures() {

  }

  @Test
  void test_should_return_error_when_providing_valid_signatures_but_invalid_transactions() {

  }

  @Test
  void test_should_throw_an_error_when_trying_to_sign_a_tx_with_missing_chaincode_for_byron_address() {

  }



}
