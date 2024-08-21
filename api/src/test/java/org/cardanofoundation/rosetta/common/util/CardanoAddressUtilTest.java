package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.transaction.spec.cert.MultiHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostAddr;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.bloxbean.cardano.client.util.HexUtil;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Relay;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.cardanofoundation.rosetta.common.util.CardanoAddressUtils.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardanoAddressUtilTest {

  private final String testMnemonic = "clog book honey force cricket stamp until seed minimum margin denial kind volume undo simple federal then jealous solid legal crucial crazy acoustic thank";

  @Test
  void isStakeAddressTest() {
    String validStakeAddress = "stake_test1uz89q57sawqdmfgeapdl3cypgjldk4y9lzqg2q422wzywucvsxnhw";
    assertTrue(isStakeAddress(validStakeAddress));

    String invalidStakeAddress = "addr_test1uz89q57sawqdmfgeapdl3cypgjldk4y9lzqg2q422wzywucvsxn";
    assertFalse(isStakeAddress(invalidStakeAddress));
  }

  @Test
  void getAddressTest() {
    Account account = new Account(testMnemonic);
    HdKeyPair stakeHdKeyPair = account.stakeHdKeyPair();
    HdKeyPair paymentHdKeyPair = account.hdKeyPair();

    Address address = getAddress(paymentHdKeyPair.getPublicKey().getKeyHash(),
        stakeHdKeyPair.getPublicKey().getKeyHash(), (byte) 0x00,
        Networks.mainnet(), AddressType.Base);

    assertEquals(account.getBaseAddress().getAddress(), address.getAddress());
  }

  @Test
  void isKeyValidTest() {
    Account account = new Account(testMnemonic);
    HdKeyPair paymentKeyPair = account.hdKeyPair();
    // key data will result in a valid Key
    String hexPublicKeyData = HexUtil.encodeHexString(paymentKeyPair.getPublicKey().getKeyData());
    assertTrue(isKeyValid(hexPublicKeyData, Constants.VALID_CURVE_TYPE));
    // key hash will result in an invalid Key
    String hexPublicKeyHash = HexUtil.encodeHexString(paymentKeyPair.getPublicKey().getKeyHash());
    assertFalse(isKeyValid(hexPublicKeyHash, Constants.VALID_CURVE_TYPE));
  }

  @Test
  void generateSpecificRelayTest() {
    Relay relay = new Relay("127.0.0.1", "2001:0db8:3c4d:0015:0000:0000:1a2f:1a2b", "relay.io",
        3001, Constants.SINGLE_HOST_ADDR);
    SingleHostAddr singleHostAddr = (SingleHostAddr) generateSpecificRelay(
        relay);
    assertEquals("2001:db8:3c4d:15:0:0:1a2f:1a2b", singleHostAddr.getIpv6().getHostAddress());
    assertEquals(3001, singleHostAddr.getPort());

    relay.setType(Constants.SINGLE_HOST_NAME);
    SingleHostName singleHostName = (SingleHostName) generateSpecificRelay(
        relay);
    assertEquals("relay.io", singleHostName.getDnsName());

    relay.setType(Constants.MULTI_HOST_NAME);
    MultiHostName multiHostName = (MultiHostName) generateSpecificRelay(relay);
    assertEquals("relay.io", multiHostName.getDnsName());
  }

  @Test
  void generateAddressTest() {
    Account account = new Account(testMnemonic);
    Address address = (Address) generateAddress(
        account.getBaseAddress().getAddress());
    assertEquals(account.getBaseAddress().getAddress(), address.getAddress());
  }

  @Test
  void getEraAddressTypeTest() {
    Account account = new Account(Networks.preprod(), testMnemonic);
    String address = account.getBaseAddress().getAddress();
    System.out.println(address);
    EraAddressType eraAddressType = getEraAddressType(
        account.getBaseAddress().getAddress());
    assertEquals(EraAddressType.SHELLEY, eraAddressType);
  }

  @Test
  void isPolicyIdValidTest() {
    String validPolicyID = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114";
    assertTrue(isPolicyIdValid(validPolicyID));

    String invalidPolicyID = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415";
    assertFalse(isPolicyIdValid(invalidPolicyID));
  }

  @Test
  void isTokenNameValidTest() {
    String validTokenName = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d1149307a4b6";
    assertTrue(isTokenNameValid(validTokenName));
  }

  @Test
  void isEmptyHexStringTest() {
    String emptyHex = "\\x";
    assertTrue(isEmptyHexString(emptyHex));
    String notEmptyHEx = "1234abcd";
    assertFalse(isEmptyHexString(notEmptyHEx));
  }

  @Test
  void generateRewardAddress() {
    Account account = new Account(testMnemonic);
    String rewardAddress = CardanoAddressUtils.generateRewardAddress(NetworkEnum.MAINNET.getNetwork(), account.hdKeyPair().getPublicKey());
    assertEquals("stake1ux5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxgfvzpaw", rewardAddress);
  }

  @Test
  void generateBaseAddressTest() {
    Account account = new Account(testMnemonic);
    String baseAddress = generateBaseAddress(NetworkEnum.MAINNET.getNetwork(), account.hdKeyPair().getPublicKey(),
        account.stakeHdKeyPair().getPublicKey());
    assertEquals(account.baseAddress(), baseAddress);
  }

  @Test
  void generateEnterpriseAddressTest() {
    Account account = new Account(testMnemonic);
    String enterpriseAddress = generateEnterpriseAddress(
        NetworkEnum.MAINNET.getNetwork(), account.hdKeyPair().getPublicKey());
    assertEquals(account.enterpriseAddress(), enterpriseAddress);
  }

  @Test
  void isEd25519KeyHashTest() {
    String validKeyHash = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114";
    assertTrue(isEd25519KeyHash(validKeyHash));
    String invalidKeyHash = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef06441gg";
    assertFalse(isEd25519KeyHash(invalidKeyHash));
  }

  @Test
  void getStakingCredentialFromStakeKeyTest() {
    Account account = new Account(testMnemonic);
    PublicKey publicKey = PublicKey.builder()
        .hexBytes(HexUtil.encodeHexString(account.stakeHdKeyPair().getPublicKey().getKeyData()))
        .curveType(
            CurveType.EDWARDS25519).build();
    StakeCredential stakingCredentialFromStakeKey = getStakingCredentialFromStakeKey(
        publicKey);
    assertEquals(HexUtil.encodeHexString(account.stakeHdKeyPair().getPublicKey().getKeyHash()),
        HexUtil.encodeHexString(stakingCredentialFromStakeKey.getHash()));
  }

  @Test
  void multipleBytesTest() {
    byte[] input = {0x00, 0x0F, 0x10, 0x7F};
    String expected = "000f107f";
    String actual = hex(input);
    assertEquals(expected, actual);
  }

  @Test
  void validSignatureTest() {
    String input = "a".repeat(128);
    assertTrue(isEd25519Signature(input));
  }

  @Test
   void validHexStringTest() {
    String input = "0123456789abcdef";
    Address address = getAddressFromHexString(input);
    assertNotNull(address);
    assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef},
            address.getBytes());
  }

  @Test
  void publicKeyToHdPublicKeyTest() {
    Account account = new Account(testMnemonic);
    PublicKey publicKey = PublicKey.builder()
        .hexBytes(HexUtil.encodeHexString(account.stakeHdKeyPair().getPublicKey().getKeyData()))
        .curveType(CurveType.EDWARDS25519).build();

    HdPublicKey actual = publicKeyToHdPublicKey(publicKey);

    assertEquals(HexUtil.encodeHexString(account.stakeHdKeyPair().getPublicKey().getKeyData()),
        HexUtil.encodeHexString(actual.getKeyData()));
  }

  @Test
  void publicKeyToHdPublicKeyNullPublicKeyTest() {
    ApiException actualException = assertThrows(ApiException.class,
        () -> publicKeyToHdPublicKey(null));

    assertEquals(RosettaErrorType.STAKING_KEY_MISSING.getCode(),
        actualException.getError().getCode());
    assertEquals(RosettaErrorType.STAKING_KEY_MISSING.getMessage(),
        actualException.getError().getMessage());
  }

  @Test
  void publicKeyToHdPublicKeyEmptyPublicKeyTest() {
    PublicKey publicKey = PublicKey.builder()
        .hexBytes("")
        .curveType(CurveType.EDWARDS25519)
        .build();
    ApiException actualException = assertThrows(ApiException.class,
        () -> publicKeyToHdPublicKey(publicKey));

    assertEquals(RosettaErrorType.STAKING_KEY_MISSING.getCode(),
        actualException.getError().getCode());
    assertEquals(RosettaErrorType.STAKING_KEY_MISSING.getMessage(),
        actualException.getError().getMessage());
  }

  @Test
  void publicKeyToHdPublicKeyInvalidPublicKeyTest() {
    PublicKey publicKey = PublicKey.builder()
        .hexBytes("invalid")
        .curveType(CurveType.EDWARDS25519)
        .build();
    ApiException actualException = assertThrows(ApiException.class,
        () -> publicKeyToHdPublicKey(publicKey));

    assertEquals(RosettaErrorType.INVALID_STAKING_KEY_FORMAT.getCode(),
        actualException.getError().getCode());
    assertEquals(RosettaErrorType.INVALID_STAKING_KEY_FORMAT.getMessage(),
        actualException.getError().getMessage());
  }

  @Test
  void isAddressValidTest() {
    String validAddress = "addr1qyj9der2u29gcuwcxum7ylyfqrcg86d0l76khlazmn9cnxgqk2v9xpzd89c93yaszz2zgczqshqvf424cmpmswlr2lnqt6j3lu";
    // not expecting an exception
    CardanoAddressUtils.verifyAddress(validAddress);
    // checking null
    assertThrows(ApiException.class, () -> CardanoAddressUtils.verifyAddress(null));
    String invalidAddress = "addr1qyj9der2u29gcuwcxum7ylyfqrcg86d0l76khlazmn9cnxgqk2v9xpzd89c93yaszz2zgczqshqvf424cmpmswlr2lnqt6j3lb";
    assertThrows(RuntimeException.class, () -> CardanoAddressUtils.verifyAddress(invalidAddress));
    // wrong Casing
    String wrongCasingAddress = "addr1qyj9der2u29gcuwcxum7ylyfqrcg86d0l76khlazmn9cnxgqk2v9xpzd89c93yaszz2zgczqshqvf424cmpmswlr2lnqt6j3lU";
    assertThrows(ApiException.class, () -> CardanoAddressUtils.verifyAddress(wrongCasingAddress));

  }
}
