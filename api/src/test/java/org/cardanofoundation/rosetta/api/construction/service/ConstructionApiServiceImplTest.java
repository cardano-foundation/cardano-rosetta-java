package org.cardanofoundation.rosetta.api.construction.service;

import java.io.IOException;
import java.util.Collections;

import lombok.SneakyThrows;

import co.nstant.in.cbor.CborException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsRequestMetadata;
import org.openapitools.client.model.ConstructionPayloadsResponse;
import org.openapitools.client.model.ProtocolParameters;
import org.openapitools.client.model.SigningPayload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.util.CborEncodeUtil;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionPayloadsRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenSigningPayload;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConstructionApiServiceImplTest {

  private final ConstructionPayloadsRequest constructionPayloadsRequest = givenConstructionPayloadsRequest();

  @Mock
  private CardanoConstructionService cardanoConstructionService;

  @InjectMocks
  private ConstructionApiServiceImpl underTest;

  @Test
  @SneakyThrows
  void constructionPayloadsService_thenReturnConstructionPayloadsResponse() {

    String expectedEncodedUnsignedTransaction = "encodedHash";
    SigningPayload expectedSigningPayload = givenSigningPayload();
    try (MockedStatic<CborEncodeUtil> mocked = Mockito.mockStatic(CborEncodeUtil.class)) {
      mocked.when(() -> CborEncodeUtil.encodeExtraData(anyString(), anyList(), anyString()))
          .thenReturn(expectedEncodedUnsignedTransaction);
      when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyInt(), any()))
          .thenReturn(createUnsignedTransaction());
      when(cardanoConstructionService.constructPayloadsForTransactionBody(any(), any()))
          .thenReturn(Collections.singletonList(expectedSigningPayload));

      ConstructionPayloadsResponse result = underTest.constructionPayloadsService(
          constructionPayloadsRequest);

      assertEquals(expectedEncodedUnsignedTransaction, result.getUnsignedTransaction());
      assertFalse(result.getPayloads().isEmpty());
      assertEquals(expectedSigningPayload, result.getPayloads().get(0));
    }
  }

  @Test
  void verifyProtocolParametersTest() {
    ConstructionPayloadsRequest constructionPayloadsRequest = new ConstructionPayloadsRequest();
    ConstructionPayloadsRequestMetadata metaData = ConstructionPayloadsRequestMetadata.builder()
        .build();
    ProtocolParameters protocolParameters = ProtocolParameters.builder().build();
    ApiException apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.TTL_MISSING.getCode(), apiException.getError().getCode());

    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.TTL_MISSING.getCode(), apiException.getError().getCode());

    metaData.setTtl(1);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.PROTOCOL_PARAMETERS_MISSING.getCode(), apiException.getError().getCode());

    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.COINS_PER_UTXO_SIZE_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setCoinsPerUtxoSize("1");
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.MAX_TX_SIZE_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setMaxTxSize(1);
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.MAX_VAL_SIZE_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setMaxValSize(1L);
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.KEY_DEPOSIT_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setKeyDeposit("1");
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.MAX_COLLATERAL_INPUTS_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setMaxCollateralInputs(1);
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.MIN_FEE_COEFFICIENT_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setMinFeeCoefficient(1);
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.MIN_FEE_CONSTANT_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setMinFeeConstant(1);
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.MIN_POOL_COST_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setMinPoolCost("1");
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.POOL_DEPOSIT_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setPoolDeposit("1");
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    apiException = assertThrows(ApiException.class,
        () -> underTest.verifyProtocolParameters(constructionPayloadsRequest));
    assertEquals(RosettaErrorType.PROTOCOL_MISSING.getCode(), apiException.getError().getCode());

    protocolParameters.setProtocol(1);
    metaData.setProtocolParameters(protocolParameters);
    constructionPayloadsRequest.setMetadata(metaData);
    underTest.verifyProtocolParameters(constructionPayloadsRequest);

  }

  @Test
  @SneakyThrows
  void constructionPayloadsService_whenCannotCreateUnsignedTransaction_thenShouldThrowError() {

    when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyInt(), any()))
        .thenThrow(new IOException());

    ApiException result = assertThrows(ApiException.class,
        () -> underTest.constructionPayloadsService(constructionPayloadsRequest));

    assertEquals(RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.getMessage(),
        result.getError().getMessage());
    assertEquals(RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.getCode(),
        result.getError().getCode());
    assertFalse(result.getError().isRetriable());
  }

  @Test
  @SneakyThrows
  void constructionPayloadsService_whenCannotEncodeUnsignedTransaction_thenShouldThrowError() {

    try (MockedStatic<CborEncodeUtil> mocked = Mockito.mockStatic(CborEncodeUtil.class)) {
      mocked.when(() -> CborEncodeUtil.encodeExtraData(anyString(), anyList(), anyString()))
          .thenThrow(new CborException("CborException"));
      when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyInt(), any()))
          .thenReturn(createUnsignedTransaction());
      when(cardanoConstructionService.constructPayloadsForTransactionBody(any(), any())).thenReturn(
          null);

      ApiException result = assertThrows(ApiException.class,
          () -> underTest.constructionPayloadsService(constructionPayloadsRequest));

      assertEquals(RosettaErrorType.CANT_ENCODE_EXTRA_DATA.getMessage(),
          result.getError().getMessage());
      assertEquals(RosettaErrorType.CANT_ENCODE_EXTRA_DATA.getCode(),
          result.getError().getCode());
      assertFalse(result.getError().isRetriable());
    }
  }

  private UnsignedTransaction createUnsignedTransaction() {
    return new UnsignedTransaction("hash", "bytes", Collections.singleton("address"), "metadata");
  }
}
