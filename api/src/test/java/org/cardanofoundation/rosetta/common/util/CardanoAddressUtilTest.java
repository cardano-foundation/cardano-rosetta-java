package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
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
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardanoAddressUtilTest {

  private final String testMnemonic = "clog book honey force cricket stamp until seed minimum margin denial kind volume undo simple federal then jealous solid legal crucial crazy acoustic thank";

  @Test
  public void isStakeAddressTest() {
    String validStakeAddress = "stake_test1uz89q57sawqdmfgeapdl3cypgjldk4y9lzqg2q422wzywucvsxnhw";
    assertTrue(CardanoAddressUtils.isStakeAddress(validStakeAddress));

    String invalidStakeAddress = "addr_test1uz89q57sawqdmfgeapdl3cypgjldk4y9lzqg2q422wzywucvsxn";
    assertFalse(CardanoAddressUtils.isStakeAddress(invalidStakeAddress));
  }

  @Test
  void getAddressTest() {
    Account account = new Account(testMnemonic);
    HdKeyPair stakeHdKeyPair = account.stakeHdKeyPair();
    HdKeyPair paymentHdKeyPair = account.hdKeyPair();

    Address address = CardanoAddressUtils.getAddress(paymentHdKeyPair.getPublicKey().getKeyHash(),
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
    assertTrue(CardanoAddressUtils.isKeyValid(hexPublicKeyData, Constants.VALID_CURVE_TYPE));
    // key hash will result in an invalid Key
    String hexPublicKeyHash = HexUtil.encodeHexString(paymentKeyPair.getPublicKey().getKeyHash());
    assertFalse(CardanoAddressUtils.isKeyValid(hexPublicKeyHash, Constants.VALID_CURVE_TYPE));
  }

  @Test
  void generateSpecificRelayTest() {
    Relay relay = new Relay("127.0.0.1", "2001:0db8:3c4d:0015:0000:0000:1a2f:1a2b", "relay.io", 3001, Constants.SINGLE_HOST_ADDR);
    SingleHostAddr singleHostAddr = (SingleHostAddr) CardanoAddressUtils.generateSpecificRelay(
        relay);
    assertEquals("2001:db8:3c4d:15:0:0:1a2f:1a2b", singleHostAddr.getIpv6().getHostAddress());
    assertEquals(3001, singleHostAddr.getPort());

    relay.setType(Constants.SINGLE_HOST_NAME);
    SingleHostName singleHostName = (SingleHostName) CardanoAddressUtils.generateSpecificRelay(
        relay);
    assertEquals("relay.io", singleHostName.getDnsName());

    relay.setType(Constants.MULTI_HOST_NAME);
    MultiHostName multiHostName = (MultiHostName) CardanoAddressUtils.generateSpecificRelay(relay);
    assertEquals("relay.io", multiHostName.getDnsName());
  }

  @Test
  void generateAddressTest() {
    Account account = new Account(testMnemonic);
    Address address = (Address) CardanoAddressUtils.generateAddress(
        account.getBaseAddress().getAddress());
    assertEquals(account.getBaseAddress().getAddress(), address.getAddress());
  }

  @Test
  void getEraAddressTypeTest() {
    Account account = new Account(Networks.preprod(), testMnemonic);
    String address = account.getBaseAddress().getAddress();
    System.out.println(address);
    EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressType(
        account.getBaseAddress().getAddress());
    assertEquals(EraAddressType.SHELLEY, eraAddressType);
  }

  @Test
  void isPolicyIdValidTest() {
    String validPolicyID = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114";
    assertTrue(CardanoAddressUtils.isPolicyIdValid(validPolicyID));

    String invalidPolicyID = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415";
    assertFalse(CardanoAddressUtils.isPolicyIdValid(invalidPolicyID));
  }

  @Test
  void isTokenNameValidTest() {
    String validTokenName = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d1149307a4b6";
    assertTrue(CardanoAddressUtils.isTokenNameValid(validTokenName));
  }

  @Test
  void isEmptyHexStringTest() {
    String emptyHex = "\\x";
    assertTrue(CardanoAddressUtils.isEmptyHexString(emptyHex));
    String notEmptyHEx = "1234abcd";
    assertFalse(CardanoAddressUtils.isEmptyHexString(notEmptyHEx));
  }

  @Test
  void generateRewardAddress() {
    Account account = new Account(testMnemonic);
    String rewardAddress = CardanoAddressUtils.generateRewardAddress(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, account.hdKeyPair().getPublicKey());
    assertEquals("stake1ux5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxgfvzpaw", rewardAddress);
  }

  @Test
  void generateBaseAddressTest() {
    Account account = new Account(testMnemonic);
    String baseAddress = CardanoAddressUtils.generateBaseAddress(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, account.hdKeyPair().getPublicKey(), account.stakeHdKeyPair().getPublicKey());
    assertEquals(account.baseAddress(), baseAddress);
  }

  @Test
  void generateEnterpriseAddressTest() {
    Account account = new Account(testMnemonic);
    String enterpriseAddress = CardanoAddressUtils.generateEnterpriseAddress(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, account.hdKeyPair().getPublicKey());
    assertEquals(account.enterpriseAddress(), enterpriseAddress);
  }

  @Test
  void isEd25519KeyHashTest() {
    String validKeyHash = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114";
    assertTrue(CardanoAddressUtils.isEd25519KeyHash(validKeyHash));
    String invalidKeyHash = "5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef06441gg";
    assertFalse(CardanoAddressUtils.isEd25519KeyHash(invalidKeyHash));
  }

  @Test
  void getStakingCredentialFromStakeKeyTest() {
    Account account = new Account(testMnemonic);
    PublicKey publicKey = PublicKey.builder()
        .hexBytes(HexUtil.encodeHexString(account.stakeHdKeyPair().getPublicKey().getKeyData())).curveType(
            CurveType.EDWARDS25519).build();
    StakeCredential stakingCredentialFromStakeKey = CardanoAddressUtils.getStakingCredentialFromStakeKey(
        publicKey);
    assertEquals(HexUtil.encodeHexString(account.stakeHdKeyPair().getPublicKey().getKeyHash()), HexUtil.encodeHexString(stakingCredentialFromStakeKey.getHash()));
  }
}
