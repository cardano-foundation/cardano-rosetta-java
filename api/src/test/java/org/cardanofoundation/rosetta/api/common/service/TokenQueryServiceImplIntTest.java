package org.cardanofoundation.rosetta.api.common.service;

import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.model.entity.Cip68MetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.repository.Cip68MetadataRepository;
import org.cardanofoundation.rosetta.testgenerator.transactions.impl.Step4_Cip68Transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.*;

/**
 * CIP-68 token metadata read from the devkit fixture (cip68_metadata, written by yaci-store
 * assets-ext from the tokens minted by the test-data-generator's Step4_Cip68Transactions).
 * <p>
 * Uses the real {@link TokenQueryServiceImpl}: the integration test context replaces the
 * {@code TokenQueryService} with a mock ({@code TestConfig}), so the real bean is injected by name.
 */
class TokenQueryServiceImplIntTest extends IntegrationTest {

  private static final String REFERENCE_NFT_PREFIX = "000643b0";
  private static final String FT_PREFIX = "0014df10";   // 333
  private static final String RFT_PREFIX = "001bc280";  // 444

  @Autowired
  @Qualifier("tokenQueryServiceImpl")
  private TokenQueryServiceImpl tokenQueryService;

  @Autowired
  private Cip68MetadataRepository cip68MetadataRepository;

  private String policyId;

  @BeforeEach
  void setUp() throws CborSerializationException {
    policyId = Step4_Cip68Transactions.cip68PolicyScript(
        new Account(Networks.testnet(), SENDER_1_MNEMONIC)).getPolicyId();
    ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);
  }

  @AfterEach
  void tearDown() {
    ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);
  }

  private static String hex(String name) {
    return HexFormat.of().formatHex(name.getBytes());
  }

  @Test
  void fixtureContainsReferenceNftDatumsForBothTokens() {
    List<Cip68MetadataEntity> rows = cip68MetadataRepository.findLatestByConcatenatedKeys(List.of(
        policyId + REFERENCE_NFT_PREFIX + hex(CIP68_FT_NAME),
        policyId + REFERENCE_NFT_PREFIX + hex(CIP68_RFT_NAME)));

    assertThat(rows).extracting(Cip68MetadataEntity::getLabel).containsExactlyInAnyOrder(333, 444);
  }

  @Test
  void resolvesFungibleToken333FromItsReferenceNft() {
    AssetFingerprint ft = AssetFingerprint.of(policyId, FT_PREFIX + hex(CIP68_FT_NAME));

    TokenRegistryCurrencyData data = tokenQueryService.queryMetadataBatch(List.of(ft)).get(ft);

    assertThat(data.getDecimals()).isEqualTo(CIP68_FT_DECIMALS);
    assertThat(data.getName()).isEqualTo(CIP68_FT_NAME);
    assertThat(data.getTicker()).isEqualTo(CIP68_FT_TICKER);
    assertThat(data.getDescription()).isEqualTo(CIP68_FT_DESCRIPTION);
    assertThat(data.getUrl()).isEqualTo(CIP68_FT_URL);
    assertThat(data.getLogo()).isNotNull();
    assertThat(data.getLogo().getValue()).isEqualTo(CIP68_FT_LOGO);
    assertThat(data.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
  }

  @Test
  void resolvesRichFungibleToken444WithImageAsLogo() {
    AssetFingerprint rft = AssetFingerprint.of(policyId, RFT_PREFIX + hex(CIP68_RFT_NAME));

    TokenRegistryCurrencyData data = tokenQueryService.queryMetadataBatch(List.of(rft)).get(rft);

    assertThat(data.getDecimals()).isEqualTo(CIP68_RFT_DECIMALS);
    assertThat(data.getName()).isEqualTo(CIP68_RFT_NAME);
    assertThat(data.getDescription()).isEqualTo(CIP68_RFT_DESCRIPTION);
    assertThat(data.getLogo()).isNotNull();
    assertThat(data.getLogo().getValue()).isEqualTo(CIP68_RFT_IMAGE);
    assertThat(data.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
  }

  @Test
  void resolvesBothTokensInOneBatchAndDefaultsUnknownTokenToZeroDecimals() {
    AssetFingerprint ft = AssetFingerprint.of(policyId, FT_PREFIX + hex(CIP68_FT_NAME));
    AssetFingerprint rft = AssetFingerprint.of(policyId, RFT_PREFIX + hex(CIP68_RFT_NAME));
    AssetFingerprint unknown = AssetFingerprint.of(policyId, FT_PREFIX + hex("NotMinted"));

    Map<AssetFingerprint, TokenRegistryCurrencyData> result =
        tokenQueryService.queryMetadataBatch(List.of(ft, rft, unknown));

    assertThat(result.get(ft).getDecimals()).isEqualTo(CIP68_FT_DECIMALS);
    assertThat(result.get(rft).getDecimals()).isEqualTo(CIP68_RFT_DECIMALS);
    assertThat(result.get(unknown).getDecimals()).isZero();
    assertThat(result.get(unknown).getName()).isNull();
  }
}
