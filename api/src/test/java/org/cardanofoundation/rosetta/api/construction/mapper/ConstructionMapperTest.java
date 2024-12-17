package org.cardanofoundation.rosetta.api.construction.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.AccountIdentifierMetadata;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ProtocolParameters;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Signature;
import org.openapitools.client.model.SignatureType;
import org.openapitools.client.model.SigningPayload;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsMapper;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsMapperImpl;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.assertj.core.api.Assertions.assertThat;

class ConstructionMapperTest {

  private final ConstructionMapperUtils constructionMapperUtils = new ConstructionMapperUtils();
  private final ProtocolParamsMapper protocolParamsMapper =
      new ProtocolParamsMapperImpl();
  private final ConstructionMapper constructionMapper =
      new ConstructionMapperImpl(protocolParamsMapper, constructionMapperUtils);

  @Test
  void mapToMetadataResponse_null_test() {
    ConstructionMetadataResponse actual = constructionMapper.mapToMetadataResponse(
        null, null, null);
    assertThat(actual).isNull();
  }

  @Test
  void mapToMetadataResponse_protocolParamsTtlNull_test() {
    ConstructionMetadataResponse actual = constructionMapper.mapToMetadataResponse(
        null, null, 100L);

    assertThat(actual.getMetadata()).isNull();
    assertThat(actual.getSuggestedFee()).hasSize(1);
    assertThat(actual.getSuggestedFee().getFirst().getValue()).isEqualTo("100");
    assertThat(actual.getSuggestedFee().getFirst().getCurrency().getSymbol())
        .isEqualTo(Constants.ADA);
    assertThat(actual.getSuggestedFee().getFirst().getCurrency().getDecimals())
        .isEqualTo(Constants.ADA_DECIMALS);
  }

  @Test
  void mapToMetadataResponse_protocolParamsNull_test() {
    ConstructionMetadataResponse actual = constructionMapper.mapToMetadataResponse(
        null, 1L, 100L);

    assertThat(actual.getMetadata()).isNotNull();
    assertThat(actual.getMetadata().getTtl()).isEqualTo(BigDecimal.ONE);
    assertThat(actual.getSuggestedFee()).hasSize(1);
    assertThat(actual.getSuggestedFee().getFirst().getValue()).isEqualTo("100");
    assertThat(actual.getSuggestedFee().getFirst().getCurrency().getSymbol())
        .isEqualTo(Constants.ADA);
    assertThat(actual.getSuggestedFee().getFirst().getCurrency().getDecimals())
        .isEqualTo(Constants.ADA_DECIMALS);
  }

  @Test
  void mapToMetadataResponse_protocolParamsSomeFieldsNull_test() {
    ProtocolParams protocolParams = getProtocolParams();
    protocolParams.setKeyDeposit(null);
    protocolParams.setProtocolMajorVer(0);
    ConstructionMetadataResponse actual = constructionMapper.mapToMetadataResponse(
        protocolParams, 1L, 100L);

    assertProtocolParameters(actual.getMetadata().getProtocolParameters(), protocolParams);
  }

  @Test
  void mapToMetadataResponse_positive_test() {
    ConstructionMetadataResponse actual = constructionMapper.mapToMetadataResponse(
        getProtocolParams(), 1L, 100L);

    assertThat(actual.getMetadata()).isNotNull();
    assertThat(actual.getMetadata().getTtl()).isEqualTo(BigDecimal.ONE);
    assertProtocolParameters(actual.getMetadata().getProtocolParameters(), getProtocolParams());
    assertThat(actual.getSuggestedFee()).hasSize(1);
    assertThat(actual.getSuggestedFee().getFirst().getValue()).isEqualTo("100");
    assertThat(actual.getSuggestedFee().getFirst().getCurrency().getSymbol())
        .isEqualTo(Constants.ADA);
    assertThat(actual.getSuggestedFee().getFirst().getCurrency().getDecimals())
        .isEqualTo(Constants.ADA_DECIMALS);
  }

  @Test
  void mapRosettaSignatureToSignaturesList_test() {
    Signature signature = getSignature();
    Signature signatureAccountNull = getSignature();
    signatureAccountNull.getSigningPayload().setAccountIdentifier(null);
    Signature signatureAddressNull = getSignature();
    signatureAddressNull.getSigningPayload().getAccountIdentifier().setAddress(null);
    Signature signatureMetadataNull = getSignature();
    signatureMetadataNull.getSigningPayload().getAccountIdentifier().setMetadata(null);
    List<Signature> signatureList = List.of(signature, signatureAccountNull,
        signatureAddressNull, signatureMetadataNull);

    List<Signatures> actual = constructionMapper.mapRosettaSignatureToSignaturesList(signatureList);

    assertThat(actual).hasSize(signatureList.size());
    for (int i = 0; i < signatureList.size(); i++) {
      assertSignature(actual.get(i), signatureList.get(i));
    }
  }

  private ProtocolParams getProtocolParams() {
    return ProtocolParams.builder()
        .minFeeA(0)
        .minFeeB(99)
        .maxTxSize(2)
        .keyDeposit(BigInteger.TWO)
        .poolDeposit(BigInteger.TEN)
        .protocolMajorVer(0)
        .minPoolCost(BigInteger.ONE)
        .adaPerUtxoByte(BigInteger.ONE)
        .maxValSize(3L)
        .costModels(null) // cost models will not be mapped
        .maxCollateralInputs(4)
        .build();
  }

  private Signature getSignature() {
    return Signature.builder()
        .signingPayload(SigningPayload.builder()
            .accountIdentifier(AccountIdentifier.builder()
                .address("address")
                .metadata(AccountIdentifierMetadata.builder()
                    .chainCode("chainCode")
                    .build())
                .build())
            .build())
        .publicKey(new PublicKey("publicKeyHexBytes", null))
        .signatureType(SignatureType.ECDSA)
        .hexBytes("hexBytes")
        .build();
  }

  private void assertProtocolParameters(ProtocolParameters actual, ProtocolParams expected) {
    assertThat(actual.getCoinsPerUtxoSize())
        .isEqualTo(Objects.toString(expected.getAdaPerUtxoByte(), null));
    assertThat(actual.getMaxTxSize()).isEqualTo(expected.getMaxTxSize());
    assertThat(actual.getMaxValSize()).isEqualTo(expected.getMaxValSize());
    assertThat(actual.getKeyDeposit())
        .isEqualTo(Objects.toString(expected.getKeyDeposit(), null));
    assertThat(actual.getMaxCollateralInputs()).isEqualTo(expected.getMaxCollateralInputs());
    assertThat(actual.getMinFeeCoefficient()).isEqualTo(expected.getMinFeeA());
    assertThat(actual.getMinFeeConstant()).isEqualTo(expected.getMinFeeB());
    assertThat(actual.getMinPoolCost())
        .isEqualTo(Objects.toString(expected.getMinPoolCost(), null));
    assertThat(actual.getPoolDeposit())
        .isEqualTo(Objects.toString(expected.getPoolDeposit(), null));
    assertThat(actual.getProtocol())
        .isEqualTo(expected.getProtocolMajorVer() != null ? expected.getProtocolMajorVer() : null);
  }

  private void assertSignature(Signatures actual, Signature expected) {
    assertThat(actual.signature()).isEqualTo(expected.getHexBytes());
    assertThat(actual.publicKey()).isEqualTo(expected.getPublicKey().getHexBytes());
    SigningPayload signingPayload = expected.getSigningPayload();
    assertThat(actual.address()).isEqualTo(signingPayload == null ? null :
        signingPayload.getAccountIdentifier() == null ? null :
            signingPayload.getAccountIdentifier().getAddress());
    assertThat(actual.chainCode()).isEqualTo(signingPayload == null ? null :
        signingPayload.getAccountIdentifier() == null ? null :
            signingPayload.getAccountIdentifier().getMetadata() == null ? null :
                signingPayload.getAccountIdentifier().getMetadata().getChainCode());
  }
}
