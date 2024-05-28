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
import org.openapitools.client.model.ConstructionPayloadsResponse;
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
