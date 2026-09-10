package org.cardanofoundation.rosetta.api.construction.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import lombok.SneakyThrows;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.api.construction.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;
import org.cardanofoundation.rosetta.api.construction.service.ProtocolParamsConverter;
import org.cardanofoundation.rosetta.common.util.CborEncodeUtil;
import org.cardanofoundation.rosetta.common.util.MinAdaCalculator;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.cardanofoundation.rosetta.EntityGenerator.givenConstructionPayloadsRequest;
import static org.cardanofoundation.rosetta.EntityGenerator.givenPublicKey;
import static org.cardanofoundation.rosetta.EntityGenerator.newNetworkId;
import static org.cardanofoundation.rosetta.EntityGenerator.givenSigningPayload;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConstructionApiServiceImplTest {

  private final ConstructionPayloadsRequest constructionPayloadsRequest = givenConstructionPayloadsRequest();

  @Mock
  private CardanoConstructionService cardanoConstructionService;

  @Mock
  private OfflineSlotService offlineSlotService;

  @Mock
  private ProtocolParamService protocolParamService;

  @Mock
  private ProtocolParamsConverter protocolParamsConverter;

  @InjectMocks
  private ConstructionApiServiceImpl underTest;

  @Test
  void constructionDeriveService_whenCip113AddressType_thenRoutesToCip113Derivation() {
    PublicKey publicKey = givenPublicKey();
    ConstructionDeriveRequest request = ConstructionDeriveRequest.builder()
            .networkIdentifier(newNetworkId())
            .publicKey(publicKey)
            .metadata(ConstructionDeriveMetadata.builder()
                    .addressType("CIP-113")
                    .build())
            .build();
    String expectedAddress = "addr_test1zp5ccj9xxqsx9qnfqa62a086jsggjhqflpduzqa3n7vg3h9mgrc6v3au3rqm66mn3kuwke340kfxga82tl7kh2nke8asgws8rn";

    when(cardanoConstructionService.getCardanoAddress(AddressType.CIP_113, null, publicKey,
            NetworkEnum.DEVNET))
            .thenReturn(expectedAddress);

    ConstructionDeriveResponse response = underTest.constructionDeriveService(request);

    assertEquals(expectedAddress, response.getAccountIdentifier().getAddress());
    verify(cardanoConstructionService).getCardanoAddress(AddressType.CIP_113, null, publicKey,
            NetworkEnum.DEVNET);
  }

  @Test
  void constructionDeriveService_whenCip113HasStakingCredential_thenThrowsNotAllowed() {
    ConstructionDeriveRequest request = ConstructionDeriveRequest.builder()
            .networkIdentifier(newNetworkId())
            .publicKey(givenPublicKey())
            .metadata(ConstructionDeriveMetadata.builder()
                    .addressType("CIP-113")
                    .stakingCredential(givenPublicKey())
                    .build())
            .build();

    ApiException exception = assertThrows(ApiException.class,
            () -> underTest.constructionDeriveService(request));

    assertEquals(RosettaErrorType.CIP113_STAKING_CREDENTIAL_NOT_ALLOWED.getCode(),
            exception.getError().getCode());
    verify(cardanoConstructionService, never()).getCardanoAddress(any(), any(), any(), any());
  }

  @Test
  void constructionDeriveService_whenCip113WrongCase_thenThrowsInvalidAddressType() {
    ConstructionDeriveRequest request = ConstructionDeriveRequest.builder()
            .networkIdentifier(newNetworkId())
            .publicKey(givenPublicKey())
            .metadata(ConstructionDeriveMetadata.builder()
                    .addressType("cip113")
                    .build())
            .build();

    ApiException exception = assertThrows(ApiException.class,
            () -> underTest.constructionDeriveService(request));

    assertEquals(RosettaErrorType.INVALID_ADDRESS_TYPE.getCode(), exception.getError().getCode());
    verify(cardanoConstructionService, never()).getCardanoAddress(any(), any(), any(), any());
  }

  // TODO
  @Test
  @SneakyThrows
  void constructionPayloadsService_thenReturnConstructionPayloadsResponse() {
    String expectedEncodedUnsignedTransaction = "encodedHash";
    SigningPayload expectedSigningPayload = givenSigningPayload();

    try (MockedStatic<CborEncodeUtil> mocked = Mockito.mockStatic(CborEncodeUtil.class)) {
      mocked.when(() -> CborEncodeUtil.encodeExtraData(anyString(), anyList()))
              .thenReturn(expectedEncodedUnsignedTransaction);

      when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyLong(), anyLong()))
              .thenReturn(createUnsignedTransaction());

      when(cardanoConstructionService.constructPayloadsForTransactionBody(any(), any()))
              .thenReturn(Collections.singletonList(expectedSigningPayload));

      when(cardanoConstructionService.convertRosettaOperations(any(Network.class), anyList())).thenReturn(new ProcessOperations());

      ConstructionPayloadsResponse result = underTest.constructionPayloadsService(
              constructionPayloadsRequest);

      assertEquals(expectedEncodedUnsignedTransaction, result.getUnsignedTransaction());
      assertFalse(result.getPayloads().isEmpty());
      assertEquals(expectedSigningPayload, result.getPayloads().getFirst());
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
    when(cardanoConstructionService.convertRosettaOperations(any(Network.class), anyList())).thenReturn(new ProcessOperations());

    when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyLong(), anyLong()))
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
    when(cardanoConstructionService.convertRosettaOperations(any(Network.class), anyList())).thenReturn(new ProcessOperations());

    try (MockedStatic<CborEncodeUtil> mocked = Mockito.mockStatic(CborEncodeUtil.class)) {
      mocked.when(() -> CborEncodeUtil.encodeExtraData(anyString(), anyList()))
              .thenThrow(new CborException("CborException"));

      when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyLong(), anyLong()))
              .thenReturn(createUnsignedTransaction());

      when(cardanoConstructionService.constructPayloadsForTransactionBody(any(), any())).thenReturn(null);

      ApiException result = assertThrows(ApiException.class,
              () -> underTest.constructionPayloadsService(constructionPayloadsRequest));

      assertEquals(RosettaErrorType.CANT_ENCODE_EXTRA_DATA.getMessage(),
              result.getError().getMessage());
      assertEquals(RosettaErrorType.CANT_ENCODE_EXTRA_DATA.getCode(),
              result.getError().getCode());
      assertFalse(result.getError().isRetriable());
    }
  }

  @Nested
  class MinAdaValidation {

    private static final String TEST_ADDRESS = "addr_test1vpqgspvmh6m2m5pwangvdg499srfzre2dd96qq57nlnw6yctpasy4";

    @Test
    void whenOutputAdaBelowMinimum_thenThrowOutputMinAdaValueNotMet() {
      ProcessOperations processOperations = new ProcessOperations();
      TransactionOutput output = new TransactionOutput(
              TEST_ADDRESS,
              Value.builder().coin(BigInteger.ONE).build());
      processOperations.setTransactionOutputs(List.of(output));

      when(cardanoConstructionService.convertRosettaOperations(any(Network.class), anyList()))
              .thenReturn(processOperations);

      ApiException result = assertThrows(ApiException.class,
              () -> underTest.constructionPayloadsService(constructionPayloadsRequest));

      assertEquals(RosettaErrorType.OUTPUT_MIN_ADA_VALUE_NOT_MET.getCode(), result.getError().getCode());
      assertEquals(RosettaErrorType.OUTPUT_MIN_ADA_VALUE_NOT_MET.getMessage(), result.getError().getMessage());
      assertFalse(result.getError().isRetriable());
    }

    @Test
    @SneakyThrows
    void whenOutputAdaAtMinimum_thenProceedWithoutError() {
      TransactionOutput output = new TransactionOutput(
              TEST_ADDRESS,
              Value.builder().coin(BigInteger.valueOf(2_000_000)).build());

      BigInteger coinsPerUtxoSize = new BigInteger(
              constructionPayloadsRequest.getMetadata().getProtocolParameters().getCoinsPerUtxoSize());
      BigInteger minAda = MinAdaCalculator.calculateMinAda(output, coinsPerUtxoSize);

      TransactionOutput sufficientOutput = new TransactionOutput(
              TEST_ADDRESS,
              Value.builder().coin(minAda).build());

      ProcessOperations processOperations = new ProcessOperations();
      processOperations.setTransactionOutputs(List.of(sufficientOutput));

      try (MockedStatic<CborEncodeUtil> mocked = Mockito.mockStatic(CborEncodeUtil.class)) {
        mocked.when(() -> CborEncodeUtil.encodeExtraData(anyString(), anyList()))
                .thenReturn("encodedHash");

        when(cardanoConstructionService.convertRosettaOperations(any(Network.class), anyList()))
                .thenReturn(processOperations);
        when(cardanoConstructionService.createUnsignedTransaction(any(), anyList(), anyLong(), anyLong()))
                .thenReturn(createUnsignedTransaction());
        when(cardanoConstructionService.constructPayloadsForTransactionBody(any(), any()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> underTest.constructionPayloadsService(constructionPayloadsRequest));
      }
    }
  }

  private UnsignedTransaction createUnsignedTransaction() {
    return new UnsignedTransaction("hash", "bytes", Collections.singleton("address"));
  }

}
