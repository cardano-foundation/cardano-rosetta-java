package org.cardanofoundation.rosetta.api.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.bcel.Const;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.NetworkIdentifier;
import org.springframework.beans.factory.annotation.Autowired;

public class NetworkServiceImplTest extends IntegrationTest {

  @Autowired
  private NetworkService networkService;

  @Test
  public void verifyCorrectNetworkTest() {
    // void function, no exception expected
    networkService.verifyNetworkRequest(createNetworkIdentifier(Constants.CARDANO_BLOCKCHAIN, Constants.DEVKIT));
  }

  @Test
  public void verifyWrongNetworkTest() {
    ApiException apiException = assertThrows(ApiException.class,
        () -> networkService.verifyNetworkRequest(createNetworkIdentifier(Constants.CARDANO_BLOCKCHAIN,
            Constants.MAINNET)));
    assertEquals(RosettaErrorType.NETWORK_NOT_FOUND.getMessage(), apiException.getError().getMessage());
    assertEquals(RosettaErrorType.NETWORK_NOT_FOUND.getCode(), apiException.getError().getCode());
  }

  @Test
  public void verifyWrongBlockchainTest() {
    ApiException apiException = assertThrows(ApiException.class,
        () -> networkService.verifyNetworkRequest(createNetworkIdentifier("Wrong Blockchain",
            Constants.DEVKIT)));
    assertEquals(RosettaErrorType.INVALID_BLOCKCHAIN.getMessage(), apiException.getError().getMessage());
    assertEquals(RosettaErrorType.INVALID_BLOCKCHAIN.getCode(), apiException.getError().getCode());
  }


  private NetworkIdentifier createNetworkIdentifier(String blockchain, String network) {
    return NetworkIdentifier.builder()
        .blockchain(blockchain)
        .network(network)
        .build();
  }

}
