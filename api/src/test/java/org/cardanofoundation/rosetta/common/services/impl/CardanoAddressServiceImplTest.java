package org.cardanofoundation.rosetta.common.services.impl;

import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.PublicKey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.cardanofoundation.rosetta.EntityGenerator.*;
import static org.cardanofoundation.rosetta.common.enumeration.AddressType.BASE;
import static org.cardanofoundation.rosetta.common.enumeration.AddressType.REWARD;
import static org.cardanofoundation.rosetta.common.enumeration.NetworkEnum.PREPROD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

@SuppressWarnings("java:S5778")
@ExtendWith(MockitoExtension.class)
class CardanoAddressServiceImplTest {

  CardanoAddressServiceImpl genesisService = new CardanoAddressServiceImpl();

  @Test
  void getCardanoBaseAddressTest() {
    PublicKey stakingCredential = givenPublicKey();
    PublicKey publicKey = givenPublicKey();

    String cardanoAddress = genesisService
            .getCardanoAddress(BASE, stakingCredential, publicKey, PREPROD);

    assertEquals("addr_test1qza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7amgrc6v3au3rqm66mn3kuwke340kfxga82tl7kh2nke8aslzyvu5",
            cardanoAddress);
  }

  @Test
  void getCardanoBaseAddressMissingPubKeyTest() {
    ApiException exception = assertThrows(ApiException.class,
    () -> genesisService.getCardanoAddress(BASE, null, null, PREPROD));

    assertEquals("Public key is missing", exception.getError().getMessage());
  }

  @Test
  void getCardanoBaseAddressMissingStakingTest()  {
    ApiException exception = assertThrows(ApiException.class,
            () -> genesisService.getCardanoAddress(BASE, null, new PublicKey(), PREPROD));

    assertEquals("Staking key is required for this type of address", exception.getError().getMessage());
  }

  @Test
  void getCardanoRewardAddressTest() {
    PublicKey stakingCredential = givenPublicKey();
    PublicKey publicKey = givenPublicKey();

    String cardanoAddress = genesisService
            .getCardanoAddress(REWARD, stakingCredential, publicKey, PREPROD);

    assertEquals("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef", cardanoAddress);
  }

  @Test
  void getCardanoRewardAddressWOStakingTest() {
    PublicKey publicKey = givenPublicKey();

    String cardanoAddress = genesisService
            .getCardanoAddress(REWARD, null, publicKey, PREPROD);

    assertEquals("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef", cardanoAddress);
  }

  @Test
  void getCardanoNullAddressTest() {
    ApiException exception = assertThrows(ApiException.class,
            () -> genesisService.getCardanoAddress(AddressType.POOL_KEY_HASH,
                    null, new PublicKey(), PREPROD));

    assertEquals("Provided address type is invalid", exception.getError().getMessage());
  }

  @Test
  void getHdPublicKeyFromRosettaKeyTest() {
    PublicKey publicKey = new PublicKey("48656C6C6F2C20776F726C6421", EDWARDS25519);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> genesisService.getHdPublicKeyFromRosettaKey(publicKey));

    assertEquals("Invalid public key length", exception.getMessage());
  }

}
