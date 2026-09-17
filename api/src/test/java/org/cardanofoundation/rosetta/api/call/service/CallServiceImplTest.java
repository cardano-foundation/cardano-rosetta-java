package org.cardanofoundation.rosetta.api.call.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.CallRequest;
import org.openapitools.client.model.CallResponse;
import org.openapitools.client.model.NetworkIdentifier;
import org.springframework.test.util.ReflectionTestUtils;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.util.HexUtil;

import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.Cip113AddressServiceImpl;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class CallServiceImplTest {

  private static final String METHOD = "resolve_smart_wallet_addr";
  private static final String PREPROD = "preprod";
  private static final String MAINNET = "mainnet";

  private static final String PLB = "198ec641705835b5e9664d0c8214a676f121e2b0d5ab8a1ecbc0ed38";
  private static final String OTHER_SCRIPT = "f2182b00a37bd746e20575c9af01ab31312213514cd31e872e0a2a3e";

  // Key hashes used to build the fixtures below.
  private static final String USER_KEY_HASH = "bd49833c183d34a082da395cd73b1c816f0271907a9f58620bcac694";
  private static final String STAKE_KEY_HASH = "0010421a43ff6f63f1da96f2cc4d591472ba9005375402e31a58f2d5";

  // Testnet inputs (network id 0).
  private static final String ENTERPRISE_USER = "addr_test1vz75nqeurq7nfgyzmgu4e4emrjqk7qn3jpaf7krzp09vd9qdzamqp";
  private static final String ENTERPRISE_OTHER_USER = "addr_test1vza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6mzywr";
  private static final String BASE_OTHER_PAY_STAKE = "addr_test1qza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2s52m4r0";
  private static final String BASE_USER_PAY_STAKE = "addr_test1qz75nqeurq7nfgyzmgu4e4emrjqk7qn3jpaf7krzp09vd9qqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2sfqqr44";
  private static final String BASE_OTHER_PAY_USER_STAKE = "addr_test1qza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7aafxpncxpaxjsg9k3etntnk8ypdup8ryr6navxyz72c62qqtznyz";
  private static final String BASE_SCRIPT_NOT_PLB = "addr_test1zreps2cq5daaw3hzq46untcp4vcnzgsn29xdx8589c9z50sqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2s70lru2";
  private static final String BASE_PLB_SCRIPT_STAKE = "addr_test1xqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6wqqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2sahgwgj";
  private static final String BASE_KEY_PAY_SCRIPT_STAKE = "addr_test1yza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2s25q2kw";
  private static final String ENTERPRISE_SCRIPT_PLB = "addr_test1wqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6wqpgrhsa";
  private static final String ENTERPRISE_SCRIPT_OTHER = "addr_test1wreps2cq5daaw3hzq46untcp4vcnzgsn29xdx8589c9z50szlr8vd";
  private static final String REWARD_KEY = "stake_test1uz75nqeurq7nfgyzmgu4e4emrjqk7qn3jpaf7krzp09vd9qd2rrht";
  private static final String REWARD_SCRIPT = "stake_test17qvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6wqpqa08h";
  private static final String POINTER = "addr_test1gza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cqqqqqd9a64v";
  private static final String MAINNET_HRP_TESTNET_HEADER = "addr1vza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cppxfpp";
  // Checksum-valid bech32 whose header nibble (0x8) is the Byron marker; no wallet emits this.
  private static final String BYRON_HEADER_UNDER_SHELLEY_HRP = "addr_test1sz75nqeurq7nfgyzmgu4e4emrjqk7qn3jpaf7krzp09vd9qgf3m4p";

  // Mainnet inputs (network id 1).
  private static final String MAINNET_ENTERPRISE_USER = "addr1vx75nqeurq7nfgyzmgu4e4emrjqk7qn3jpaf7krzp09vd9qk2f80y";

  // Expected smart wallets.
  private static final String SMART_WALLET_USER = "addr_test1zqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6w9afxpncxpaxjsg9k3etntnk8ypdup8ryr6navxyz72c62qhg2h67";
  private static final String SMART_WALLET_OTHER_USER = "addr_test1zqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6w9mgrc6v3au3rqm66mn3kuwke340kfxga82tl7kh2nke8asgpvgzg";
  private static final String SMART_WALLET_STAKE = "addr_test1zqvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6wqqzppp5sllda3lrk5k7txy6kg5w2afqpfh2spwxxjc7t2srfn3an";
  private static final String MAINNET_SMART_WALLET_USER = "addr1zyvca3jpwpvrtd0fvexseqs55em0zg0zkr26hzs7e0qw6w9afxpncxpaxjsg9k3etntnk8ypdup8ryr6navxyz72c62q57hhkp";

  private Cip113AddressServiceImpl cip113AddressService;
  private CallServiceImpl underTest;

  @BeforeEach
  void setUp() {
    cip113AddressService = new Cip113AddressServiceImpl();
    setPlb(PLB);
    underTest = new CallServiceImpl(null, cip113AddressService);
  }

  private void setPlb(String value) {
    ReflectionTestUtils.setField(cip113AddressService, "cip113BaseScriptHash", value);
  }

  private static CallRequest request(String network, Map<String, Object> parameters) {
    CallRequest request = new CallRequest();
    request.setNetworkIdentifier(NetworkIdentifier.builder().blockchain("cardano").network(network).build());
    request.setMethod(METHOD);
    request.setParameters(parameters);
    return request;
  }

  private static Map<String, Object> address(Object address) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("address", address);
    return parameters;
  }

  @SuppressWarnings("unchecked")
  private String resolve(String network, String inputAddress) {
    CallResponse response = underTest.resolveSmartWalletAddress(request(network, address(inputAddress)));
    Map<String, Object> result = (Map<String, Object>) response.getResult();
    Map<String, Object> accountIdentifier = (Map<String, Object>) result.get("account_identifier");
    return (String) accountIdentifier.get("address");
  }

  private ApiException resolveError(String network, Map<String, Object> parameters) {
    return catchThrowableOfType(
        () -> underTest.resolveSmartWalletAddress(request(network, parameters)), ApiException.class);
  }

  private void assertCode(ApiException exception, RosettaErrorType expected) {
    assertThat(exception).isNotNull();
    assertThat(exception.getError().getCode()).isEqualTo(expected.getCode());
    assertThat(exception.getError().isRetriable()).isFalse();
  }

  @Test
  void supportedMethodsIncludeResolveSmartWalletAddr() {
    assertThat(underTest.getSupportedMethods()).contains(METHOD);
  }

  @Test
  void processCallRequestRoutesToResolver() {
    CallResponse response = underTest.processCallRequest(request(PREPROD, address(ENTERPRISE_USER)));

    assertThat(response.getIdempotent()).isTrue();
    assertThat(response.getResult()).isNotNull();
  }

  @Nested
  class Resolution {

    @Test
    void enterpriseAddressUsesPaymentKeyHash() {
      assertThat(resolve(PREPROD, ENTERPRISE_USER)).isEqualTo(SMART_WALLET_USER);
    }

    @Test
    void baseAddressUsesStakeKeyHash() {
      assertThat(resolve(PREPROD, BASE_OTHER_PAY_STAKE)).isEqualTo(SMART_WALLET_STAKE);
    }

    @Test
    void smartWalletIsIdempotent() {
      assertThat(resolve(PREPROD, SMART_WALLET_USER)).isEqualTo(SMART_WALLET_USER);
    }

    @Test
    void enterpriseAndBaseForSameUserResolveToSameWallet() {
      assertThat(resolve(PREPROD, ENTERPRISE_USER))
          .isEqualTo(resolve(PREPROD, BASE_OTHER_PAY_USER_STAKE))
          .isEqualTo(SMART_WALLET_USER);
    }

    @Test
    void baseAddressesSharingStakeKeyResolveToSameWallet() {
      assertThat(resolve(PREPROD, BASE_OTHER_PAY_STAKE))
          .isEqualTo(resolve(PREPROD, BASE_USER_PAY_STAKE))
          .isEqualTo(SMART_WALLET_STAKE);
    }

    @Test
    void differentUsersResolveToDifferentWallets() {
      assertThat(resolve(PREPROD, ENTERPRISE_USER)).isNotEqualTo(resolve(PREPROD, ENTERPRISE_OTHER_USER));
      assertThat(resolve(PREPROD, ENTERPRISE_OTHER_USER)).isEqualTo(SMART_WALLET_OTHER_USER);
    }

    @Test
    void mainnetInputYieldsMainnetWallet() {
      assertThat(resolve(MAINNET, MAINNET_ENTERPRISE_USER)).isEqualTo(MAINNET_SMART_WALLET_USER);
    }

    @Test
    void resultIsIdempotent() {
      CallResponse response = underTest.resolveSmartWalletAddress(request(PREPROD, address(ENTERPRISE_USER)));

      assertThat(response.getIdempotent()).isTrue();
    }

    @Test
    void decodedStructureMatchesInputs() {
      Address wallet = new Address(resolve(PREPROD, ENTERPRISE_USER));

      assertThat(wallet.getAddressType()).isEqualTo(AddressType.Base);
      assertThat(wallet.getNetwork().getNetworkId()).isZero();
      assertThat(wallet.isScriptHashInPaymentPart()).isTrue();
      assertThat(wallet.isStakeKeyHashInDelegationPart()).isTrue();
      assertThat(wallet.getPaymentCredentialHash()).contains(HexUtil.decodeHexString(PLB));
      assertThat(wallet.getDelegationCredentialHash()).contains(HexUtil.decodeHexString(USER_KEY_HASH));
    }

    @Test
    void decodedMainnetStructureCarriesMainnetTag() {
      Address wallet = new Address(resolve(MAINNET, MAINNET_ENTERPRISE_USER));

      assertThat(wallet.getPrefix()).isEqualTo("addr");
      assertThat(wallet.getNetwork().getNetworkId()).isEqualTo(1);
      assertThat(wallet.getPaymentCredentialHash()).contains(HexUtil.decodeHexString(PLB));
      assertThat(wallet.getDelegationCredentialHash()).contains(HexUtil.decodeHexString(USER_KEY_HASH));
    }
  }

  @Nested
  class NotSmartWallet {

    @Test
    void baseWithForeignPaymentScriptIsRejected() {
      assertCode(resolveError(PREPROD, address(BASE_SCRIPT_NOT_PLB)),
          RosettaErrorType.CIP113_ADDRESS_NOT_SMART_WALLET);
    }

    @Test
    void enterpriseWithPlbScriptIsRejected() {
      assertCode(resolveError(PREPROD, address(ENTERPRISE_SCRIPT_PLB)),
          RosettaErrorType.CIP113_ADDRESS_NOT_SMART_WALLET);
    }

    @Test
    void enterpriseWithForeignScriptIsRejected() {
      assertCode(resolveError(PREPROD, address(ENTERPRISE_SCRIPT_OTHER)),
          RosettaErrorType.CIP113_ADDRESS_NOT_SMART_WALLET);
    }
  }

  @Nested
  class UnsupportedAddressType {

    @Test
    void baseWithScriptStakeIsRejected() {
      assertCode(resolveError(PREPROD, address(BASE_PLB_SCRIPT_STAKE)),
          RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED);
    }

    @Test
    void baseWithKeyPaymentAndScriptStakeIsRejected() {
      assertCode(resolveError(PREPROD, address(BASE_KEY_PAY_SCRIPT_STAKE)),
          RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED);
    }

    @Test
    void rewardKeyAddressIsRejected() {
      assertCode(resolveError(PREPROD, address(REWARD_KEY)),
          RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED);
    }

    @Test
    void rewardScriptAddressIsRejected() {
      assertCode(resolveError(PREPROD, address(REWARD_SCRIPT)),
          RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED);
    }

    @Test
    void pointerAddressIsRejected() {
      assertCode(resolveError(PREPROD, address(POINTER)),
          RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED);
    }

    @Test
    void byronHeaderUnderShelleyPrefixIsRejected() {
      assertCode(resolveError(PREPROD, address(BYRON_HEADER_UNDER_SHELLEY_HRP)),
          RosettaErrorType.CIP113_ADDRESS_TYPE_NOT_SUPPORTED);
    }
  }

  @Nested
  class InvalidAddress {

    @Test
    void malformedBech32IsRejected() {
      assertCode(resolveError(PREPROD, address("addr_test1notavalidaddressxyz")),
          RosettaErrorType.CIP113_INVALID_ADDRESS);
    }

    @Test
    void prefixDisagreeingWithHeaderIsRejected() {
      assertCode(resolveError(PREPROD, address(MAINNET_HRP_TESTNET_HEADER)),
          RosettaErrorType.CIP113_INVALID_ADDRESS);
    }

    @Test
    void mainnetAddressOnTestnetIsRejected() {
      assertCode(resolveError(PREPROD, address(MAINNET_ENTERPRISE_USER)),
          RosettaErrorType.CIP113_INVALID_ADDRESS);
    }

    @Test
    void testnetAddressOnMainnetIsRejected() {
      assertCode(resolveError(MAINNET, address(ENTERPRISE_USER)),
          RosettaErrorType.CIP113_INVALID_ADDRESS);
    }
  }

  @Nested
  class ParameterErrors {

    @Test
    void missingAddressIsRejected() {
      assertCode(resolveError(PREPROD, new HashMap<>()), RosettaErrorType.CALL_PARAMETER_MISSING);
    }

    @Test
    void blankAddressIsRejected() {
      assertCode(resolveError(PREPROD, address("   ")), RosettaErrorType.CALL_PARAMETER_MISSING);
    }

    @Test
    void nonStringAddressIsRejected() {
      assertCode(resolveError(PREPROD, address(123)), RosettaErrorType.CALL_PARAMETER_MISSING);
    }
  }

  @Nested
  class Configuration {

    @Test
    void missingPlbIsRejected() {
      setPlb("");

      assertCode(resolveError(PREPROD, address(ENTERPRISE_USER)),
          RosettaErrorType.CIP113_PLB_SCRIPT_HASH_NOT_CONFIGURED);
    }

    @Test
    void malformedPlbIsRejected() {
      setPlb("not-hex");

      assertCode(resolveError(PREPROD, address(ENTERPRISE_USER)),
          RosettaErrorType.CIP113_PLB_SCRIPT_HASH_INVALID);
    }

    @Test
    void wrongLengthPlbIsRejected() {
      setPlb(PLB + "00");

      assertCode(resolveError(PREPROD, address(ENTERPRISE_USER)),
          RosettaErrorType.CIP113_PLB_SCRIPT_HASH_INVALID);
    }

    @Test
    void configurationIsCheckedBeforeAddress() {
      setPlb("");

      assertCode(resolveError(PREPROD, address("addr_test1notavalidaddressxyz")),
          RosettaErrorType.CIP113_PLB_SCRIPT_HASH_NOT_CONFIGURED);
    }

    @Test
    void parameterIsCheckedBeforeConfiguration() {
      setPlb("");

      assertCode(resolveError(PREPROD, new HashMap<>()), RosettaErrorType.CALL_PARAMETER_MISSING);
    }

    @Test
    void differentPlbChangesPaymentCredential() {
      setPlb(OTHER_SCRIPT);

      Address wallet = new Address(resolve(PREPROD, ENTERPRISE_USER));

      assertThat(wallet.getPaymentCredentialHash()).contains(HexUtil.decodeHexString(OTHER_SCRIPT));
      assertThat(wallet.getDelegationCredentialHash()).contains(HexUtil.decodeHexString(USER_KEY_HASH));
    }
  }
}
