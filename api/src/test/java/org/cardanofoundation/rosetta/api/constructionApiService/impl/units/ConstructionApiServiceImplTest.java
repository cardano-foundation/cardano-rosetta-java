package org.cardanofoundation.rosetta.api.constructionApiService.impl.units;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Assertions.*;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.cardanofoundation.rosetta.api.addedRepo.BlockRepository;
import org.cardanofoundation.rosetta.api.addedenum.AddressType;
import org.cardanofoundation.rosetta.api.addedenum.NetworkIdentifierEnum;
import org.cardanofoundation.rosetta.api.constructionApiService.CardanoService;
import org.cardanofoundation.rosetta.api.constructionApiService.impl.ConstructionApiServiceImpl;
import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ConstructionApiServiceImplTest {

  @MockBean
  CardanoService cardanoService;

  @MockBean
  BlockRepository blockRepository;

  @Autowired
  ConstructionApiServiceImpl constructionApiService;

  @Test
  void test_construct_derive_service() throws CborSerializationException, IllegalAccessException {

    //prepare data
    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();
    NetworkIdentifier networkIdentifier = new NetworkIdentifier("cardano", "mainnet");
    PublicKey publicKey = new PublicKey("hex_bytes", "curve_type");
    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata(
        new PublicKey("staking_credential_hex_bytes", "curve_type"),
        "Reward"
    );
    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
    constructionDeriveRequest.setPublicKey(publicKey);
    constructionDeriveRequest.setMetadata(metadata);

    String address = "address";

    //mock
    when(cardanoService.isKeyValid(anyString(), anyString())).thenReturn(true);
    when(cardanoService.isAddressTypeValid(anyString())).thenReturn(true);
    when(cardanoService.getNetworkIdentifierByRequestParameters(any(NetworkIdentifier.class))).thenReturn(NetworkIdentifierEnum.CARDANO_MAINNET_NETWORK);
    when(cardanoService.generateAddress(any(NetworkIdentifierEnum.class), anyString(), anyString(), any(AddressType.class))).thenReturn(address);

    //act
    ConstructionDeriveResponse response = constructionApiService.constructionDeriveService(
        constructionDeriveRequest);

    //assert
    verify(cardanoService, times(2)).isKeyValid(anyString(), anyString());
    verify(cardanoService, times(1)).isAddressTypeValid(anyString());
    verify(cardanoService, times(1)).getNetworkIdentifierByRequestParameters(any(NetworkIdentifier.class));
    verify(cardanoService, times(1)).generateAddress(any(NetworkIdentifierEnum.class), anyString(), anyString(), any(AddressType.class));
  }

  @Test
  void test_throw_exception_when_public_key_invalid() {
    //prepare data
    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();
    NetworkIdentifier networkIdentifier = new NetworkIdentifier("cardano", "mainnet");
    PublicKey publicKey = new PublicKey("hex_bytes", "curve_type");
    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata(
        new PublicKey("staking_credential_hex_bytes", "curve_type"),
        "Reward"
    );
    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
    constructionDeriveRequest.setPublicKey(publicKey);
    constructionDeriveRequest.setMetadata(metadata);

    //act
    when(cardanoService.isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType())).thenReturn(false);

    //assert
    assertThrows(IllegalArgumentException.class, () -> constructionApiService.constructionDeriveService(constructionDeriveRequest));
    verify(cardanoService, times(1)).isKeyValid(anyString(), anyString());
  }

  @Test
  void test_throw_exception_when_staking_credential_invalid() {
    //prepare data
    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();
    NetworkIdentifier networkIdentifier = new NetworkIdentifier("cardano", "mainnet");
    PublicKey publicKey = new PublicKey("hex_bytes", "curve_type");
    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata(
        new PublicKey("staking_credential_hex_bytes", "curve_type"),
        "Reward"
    );
    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
    constructionDeriveRequest.setPublicKey(publicKey);
    constructionDeriveRequest.setMetadata(metadata);

    //act
    when(cardanoService.isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType())).thenReturn(true);
    when(cardanoService.isKeyValid(metadata.getStakingCredential().getHexBytes(),
        metadata.getStakingCredential().getCurveType())).thenReturn(false);

    //assert
    assertThrows(IllegalArgumentException.class, () -> constructionApiService.constructionDeriveService(constructionDeriveRequest));
    verify(cardanoService, times(2)).isKeyValid(anyString(), anyString());
  }

  @Test
  void test_throw_exception_when_address_type_invalid() {
    //prepare data
    ConstructionDeriveRequest constructionDeriveRequest = new ConstructionDeriveRequest();
    NetworkIdentifier networkIdentifier = new NetworkIdentifier("cardano", "mainnet");
    PublicKey publicKey = new PublicKey("hex_bytes", "curve_type");
    ConstructionDeriveRequestMetadata metadata = new ConstructionDeriveRequestMetadata(
        new PublicKey("staking_credential_hex_bytes", "curve_type"),
        "Reward"
    );
    constructionDeriveRequest.setNetworkIdentifier(networkIdentifier);
    constructionDeriveRequest.setPublicKey(publicKey);
    constructionDeriveRequest.setMetadata(metadata);

    //act
    when(cardanoService.isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType())).thenReturn(true);
    when(cardanoService.isKeyValid(metadata.getStakingCredential().getHexBytes(),
        metadata.getStakingCredential().getCurveType())).thenReturn(true);
    when(cardanoService.isAddressTypeValid(anyString())).thenReturn(false);

    //assert
    assertThrows(IllegalArgumentException.class, () -> constructionApiService.constructionDeriveService(constructionDeriveRequest));
    verify(cardanoService, times(2)).isKeyValid(anyString(), anyString());
    verify(cardanoService, times(1)).isAddressTypeValid(anyString());
  }

}
