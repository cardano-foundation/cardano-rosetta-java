package org.cardanofoundation.rosetta.api.call.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class CallApiTest extends IntegrationTest {

  private static final String SMART_WALLET_USER = "addr_test1zqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6w9afxpncxpaxjsg9k3etntnk8ypdup8ryr6navxyz72c62qhg2h67";
  private static final String SMART_WALLET_STAKE = "addr_test1zqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6wqqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2srfn3an";

  @Autowired
  private CallService callService;

  private CallRequest getCallRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    return new ObjectMapper().readValue(file, CallRequest.class);
  }

  @SuppressWarnings("unchecked")
  private String resolvedAddress(CallResponse response) {
    Map<String, Object> result = (Map<String, Object>) response.getResult();
    Map<String, Object> accountIdentifier = (Map<String, Object>) result.get("account_identifier");
    return (String) accountIdentifier.get("address");
  }

  @Test
  void shouldAdvertiseResolveSmartWalletAddr() {
    assertThat(callService.getSupportedMethods()).contains("resolve_smart_wallet_addr");
  }

  @Nested
  class ResolveSmartWalletAddrTest {

    @Test
    void shouldResolveEnterpriseAddress() throws IOException {
      CallResponse response = callService.processCallRequest(
          getCallRequest("testdata/call/resolve_enterprise_request.json"));

      assertThat(resolvedAddress(response)).isEqualTo(SMART_WALLET_USER);
      assertThat(response.getIdempotent()).isTrue();
    }

    @Test
    void shouldResolveBaseAddressUsingStakeCredential() throws IOException {
      CallResponse response = callService.processCallRequest(
          getCallRequest("testdata/call/resolve_base_request.json"));

      assertThat(resolvedAddress(response)).isEqualTo(SMART_WALLET_STAKE);
    }

    @Test
    void shouldResolveEnterpriseAndBaseForSameUserToSameWallet() throws IOException {
      CallResponse fromEnterprise = callService.processCallRequest(
          getCallRequest("testdata/call/resolve_enterprise_request.json"));
      CallResponse fromBase = callService.processCallRequest(
          getCallRequest("testdata/call/resolve_base_same_user_request.json"));

      assertThat(resolvedAddress(fromBase)).isEqualTo(resolvedAddress(fromEnterprise));
    }

    @Test
    void shouldReturnSmartWalletUnchanged() throws IOException {
      CallResponse response = callService.processCallRequest(
          getCallRequest("testdata/call/resolve_smart_wallet_request.json"));

      assertThat(resolvedAddress(response)).isEqualTo(SMART_WALLET_USER);
    }
  }

  @Nested
  class ResolveSmartWalletAddrErrorsTest {

    @Test
    void shouldRejectScriptEnterpriseAddress() throws IOException {
      CallRequest request = getCallRequest("testdata/call/resolve_script_enterprise_request.json");

      ApiException exception = catchThrowableOfType(
          () -> callService.processCallRequest(request), ApiException.class);

      assertThat(exception.getError().getCode())
          .isEqualTo(RosettaErrorType.CIP113_ADDRESS_NOT_SMART_WALLET.getCode());
      assertThat(exception.getError().getDetails().getMessage()).isNotBlank();
    }

    @Test
    void shouldRejectRewardAddress() throws IOException {
      CallRequest request = getCallRequest("testdata/call/resolve_reward_request.json");

      ApiException exception = catchThrowableOfType(
          () -> callService.processCallRequest(request), ApiException.class);

      assertThat(exception.getError().getCode())
          .isEqualTo(RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED.getCode());
      assertThat(exception.getError().getDetails().getMessage()).isNotBlank();
    }

    @Test
    void shouldRejectMissingAddressParameter() throws IOException {
      CallRequest request = getCallRequest("testdata/call/resolve_missing_address_request.json");

      ApiException exception = catchThrowableOfType(
          () -> callService.processCallRequest(request), ApiException.class);

      assertThat(exception.getError().getCode())
          .isEqualTo(RosettaErrorType.CALL_PARAMETER_MISSING.getCode());
      assertThat(exception.getError().getDetails().getMessage()).contains("address");
    }
  }
}
