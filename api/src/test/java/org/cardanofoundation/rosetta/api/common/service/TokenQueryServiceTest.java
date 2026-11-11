package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.model.entity.Cip68MetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.Cip26MetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.repository.Cip68MetadataRepository;
import org.cardanofoundation.rosetta.api.common.model.repository.Cip26MetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenQueryService Tests")
class TokenQueryServiceTest {

    @Mock
    private Cip26MetadataRepository cip26MetadataRepository;

    @Mock
    private Cip68MetadataRepository cip68MetadataRepository;

    private TokenQueryServiceImpl tokenQueryService;

    // 56-char hex policy ID
    private static final String POLICY_ID = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3";
    private static final String ASSET_HEX = "54657374546f6b656e"; // "TestToken"
    private static final String SUBJECT = POLICY_ID + ASSET_HEX;

    // CIP-68 fungible token prefix + name
    private static final String CIP68_FT_ASSET = "0014df10" + "aabbccdd";
    private static final String CIP68_REF_NFT_ASSET = "000643b0" + "aabbccdd";
    private static final String CIP68_SUBJECT = POLICY_ID + CIP68_FT_ASSET;
    // CIP-68 rich fungible token (444) prefix + the same name: same reference NFT as CIP68_FT_ASSET
    private static final String CIP68_RFT_ASSET = "001bc280" + "aabbccdd";

    @BeforeEach
    void setUp() {
        tokenQueryService = new TokenQueryServiceImpl(cip26MetadataRepository, cip68MetadataRepository);
        ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);
    }

    // Helper: invoke batch with a single fingerprint (policyId + symbolHex) and return the one result
    private TokenRegistryCurrencyData querySingle(String policyId, String symbolHex) {
        AssetFingerprint fp = AssetFingerprint.of(policyId, symbolHex);
        Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));
        return result.get(fp);
    }

    @Nested
    @DisplayName("Single-subject merge semantics")
    class SingleSubjectMergeTests {

        @Test
        @DisplayName("Should return identity fields with default decimals=0 when no CIP-26/CIP-68 row exists")
        void shouldReturnIdentityFieldsWhenNothingFound() {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.getSubject()).isEqualTo(SUBJECT);
            assertThat(result.getName()).isNull();
            assertThat(result.getDescription()).isNull();
            assertThat(result.getDecimals()).isZero();
        }

        @Test
        @DisplayName("Should return CIP-26 metadata when only CIP-26 exists")
        void shouldReturnCip26WhenOnlyCip26Exists() {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("HOSKY").description("Dog token")
                    .ticker("HOSKY").url("https://hosky.io").decimals(0L).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getName()).isEqualTo("HOSKY");
            assertThat(result.getDescription()).isEqualTo("Dog token");
            assertThat(result.getTicker()).isEqualTo("HOSKY");
            assertThat(result.getUrl()).isEqualTo("https://hosky.io");
            assertThat(result.getDecimals()).isZero();
        }

        @Test
        @DisplayName("Should return CIP-68 metadata when only CIP-68 exists")
        void shouldReturnCip68WhenOnlyCip68Exists() {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("iUSD").description("Stablecoin").ticker("iUSD").decimals(6L).version(1L).build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getName()).isEqualTo("iUSD");
            assertThat(result.getDescription()).isEqualTo("Stablecoin");
            assertThat(result.getTicker()).isEqualTo("iUSD");
            assertThat(result.getDecimals()).isEqualTo(6);
            assertThat(result.getVersion()).isEqualTo(BigDecimal.valueOf(1L));
        }

        @Test
        @DisplayName("CIP-68 should override CIP-26 fields where both exist")
        void cip68ShouldOverrideCip26() {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("Old Name").description("Old Desc")
                    .ticker("OLD").decimals(0L).url("https://old.com").build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("New Name").description("New Desc").ticker("NEW").decimals(8L).version(2L).build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getTicker()).isEqualTo("NEW");
            assertThat(result.getDecimals()).isEqualTo(8);
            assertThat(result.getVersion()).isEqualTo(BigDecimal.valueOf(2L));
        }

        @Test
        @DisplayName("CIP-68 partial override should keep CIP-26 fields for missing CIP-68 values")
        void cip68PartialOverrideShouldKeepCip26Fields() {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("CIP26 Name").description("CIP26 Desc")
                    .ticker("C26").url("https://cip26.com").decimals(6L).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            // CIP-68 only has name and decimals, rest null
            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("CIP68 Name").description(null).ticker(null).decimals(8L).build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            // CIP-68 overrides name and decimals
            assertThat(result.getName()).isEqualTo("CIP68 Name");
            assertThat(result.getDecimals()).isEqualTo(8);
            // CIP-26 values preserved for fields CIP-68 didn't provide
            assertThat(result.getDescription()).isEqualTo("CIP26 Desc");
            assertThat(result.getTicker()).isEqualTo("C26");
            assertThat(result.getUrl()).isEqualTo("https://cip26.com");
        }

        @Test
        @DisplayName("Should default decimals to 0 when CIP-26 has null decimals and no CIP-68 data")
        void shouldDefaultDecimalsToZeroWhenBothStandardsLackIt() {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").decimals(null).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getDecimals()).isZero();
        }

        @ParameterizedTest(name = "CIP-26 decimals {0} -> 0")
        @ValueSource(longs = {-1L, 256L, 4_294_967_296L, Long.MAX_VALUE})
        @DisplayName("Should treat out-of-range CIP-26 decimals as absent (0) instead of failing")
        void shouldTreatOutOfRangeCip26DecimalsAsAbsent(long decimals) {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").decimals(decimals).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getDecimals()).isZero();
            assertThat(result.getName()).isEqualTo("Token");
        }

        @Test
        @DisplayName("Should accept the CIP upper bound of 255 decimals")
        void shouldAcceptMaxDecimals() {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").decimals(255L).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getDecimals()).isEqualTo(255);
        }

        @Test
        @DisplayName("Out-of-range CIP-68 decimals should fall back to the CIP-26 value")
        void outOfRangeCip68DecimalsShouldFallBackToCip26() {
            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("CIP26 Name").description("CIP26 Desc").decimals(6L).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("CIP68 Name").decimals(1_000L).build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getDecimals()).isEqualTo(6);
            assertThat(result.getName()).isEqualTo("CIP68 Name");
        }

        @Test
        @DisplayName("CIP-68 row with null version (allowed since assets-ext V0_1700_2) should not fail")
        void cip68NullVersionShouldNotFail() {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("CIP68 Name").decimals(2L).version(null).build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getDecimals()).isEqualTo(2);
            assertThat(result.getVersion()).isNull();
        }
    }

    @Nested
    @DisplayName("CIP-68 rich fungible tokens (444)")
    class Cip68RichFungibleTokenTests {

        private Cip68MetadataEntity.Cip68MetadataEntityBuilder referenceNft() {
            return Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).txHash("tx1").txIndex(0).label(444);
        }

        @Test
        @DisplayName("Should resolve decimals, name and ticker of a 444 token from its reference NFT")
        void shouldResolveRichFungibleTokenFromReferenceNft() {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(referenceNft().name("Rich Token").ticker("RICH").decimals(4L).build()));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_RFT_ASSET);

            assertThat(result.getDecimals()).isEqualTo(4);
            assertThat(result.getName()).isEqualTo("Rich Token");
            assertThat(result.getTicker()).isEqualTo("RICH");
            assertThat(result.getSubject()).isEqualTo(POLICY_ID + CIP68_RFT_ASSET);
        }

        @Test
        @DisplayName("Should serve the image of a 444 token as its logo and ignore a logo field in its datum")
        void shouldServeImageAsLogoForRichFungibleToken() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(referenceNft().name("Rich Token")
                            .image("ipfs://QmImage").logo("https://not-cip68-for-444.example/logo.png").build()));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_RFT_ASSET);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getValue()).isEqualTo("ipfs://QmImage");
            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
        }

        @Test
        @DisplayName("Should not serve a logo for a 444 token without an image, even if its datum has a logo field")
        void shouldNotServeDatumLogoForRichFungibleTokenWithoutImage() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(referenceNft().name("Rich Token").logo("https://example.com/logo.png").build()));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_RFT_ASSET);

            assertThat(result.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should keep serving the logo (not the image) for a 333 token")
        void shouldServeLogoNotImageForFungibleToken() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(referenceNft().label(333).name("FT")
                            .logo("https://example.com/logo.png").image("ipfs://QmImage").build()));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getValue()).isEqualTo("https://example.com/logo.png");
        }

        @Test
        @DisplayName("A 333 and a 444 token of the same reference NFT resolve in one batch with a single lookup key")
        void shouldResolveFungibleAndRichFungibleTokenOfSameReferenceNft() {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(referenceNft().name("Shared").decimals(2L).build()));
            AssetFingerprint ft = AssetFingerprint.of(POLICY_ID, CIP68_FT_ASSET);
            AssetFingerprint rft = AssetFingerprint.of(POLICY_ID, CIP68_RFT_ASSET);

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(ft, rft));

            assertThat(result.get(ft).getDecimals()).isEqualTo(2);
            assertThat(result.get(rft).getDecimals()).isEqualTo(2);
            assertThat(result.get(rft).getName()).isEqualTo("Shared");
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
            verify(cip68MetadataRepository).findLatestByConcatenatedKeys(captor.capture());
            assertThat(captor.getValue()).containsExactly(POLICY_ID + CIP68_REF_NFT_ASSET);
        }

        @Test
        @DisplayName("Should convert rich fungible prefix 001bc280 to reference NFT prefix 000643b0")
        void shouldConvertRichFungibleToReferencePrefix() {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection())).thenReturn(List.of());

            querySingle(POLICY_ID, CIP68_RFT_ASSET);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
            verify(cip68MetadataRepository).findLatestByConcatenatedKeys(captor.capture());
            assertThat(captor.getValue()).containsExactly(POLICY_ID + CIP68_REF_NFT_ASSET);
        }
    }

    @Nested
    @DisplayName("CIP-68 subject prefix conversion")
    class Cip68PrefixConversionTests {

        @ParameterizedTest(name = "symbol \"{0}\"")
        @ValueSource(strings = {
                "000de140aabb", // CIP-68 NFT prefix (222), not the fungible one
                ASSET_HEX,      // plain asset name without a CIP-68 prefix
                "",             // empty symbol
                "0014df10"      // bare fungible prefix with no name part
        })
        @DisplayName("Should not attempt CIP-68 lookup for symbols that are not CIP-68 fungible tokens")
        void shouldSkipCip68LookupForNonFungibleSymbols(String symbol) {
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            querySingle(POLICY_ID, symbol);

            verifyNoInteractions(cip68MetadataRepository);
        }

        @Test
        @DisplayName("Should convert fungible token prefix 0014df10 to reference NFT prefix 000643b0")
        void shouldConvertFungibleToReferencePrefix() {
            String hexName = "aabbccdd";
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of());

            querySingle(POLICY_ID, "0014df10" + hexName);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
            verify(cip68MetadataRepository).findLatestByConcatenatedKeys(captor.capture());
            assertThat(captor.getValue()).containsExactly(POLICY_ID + "000643b0" + hexName);
        }
    }

    @Nested
    @DisplayName("Logo handling")
    class LogoTests {

        @Test
        @DisplayName("Should not include logo when logoEnabled is false")
        void shouldNotIncludeLogoWhenDisabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);

            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").logo("iVBORw0KGgo=").build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should include CIP-26 BASE64 logo when enabled (logo is on cip26_metadata row)")
        void shouldIncludeCip26LogoWhenEnabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").logo("iVBORw0KGgo=").build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
            assertThat(result.getLogo().getValue()).isEqualTo("iVBORw0KGgo=");
        }

        @Test
        @DisplayName("Should detect ipfs:// scheme as URL format for CIP-68")
        void shouldDetectIpfsAsUrl() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ipfs://QmHash").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
            assertThat(result.getLogo().getValue()).isEqualTo("ipfs://QmHash");
        }

        @Test
        @DisplayName("Should detect https:// scheme as URL format for CIP-68")
        void shouldDetectHttpsAsUrl() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("https://example.com/logo.png").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
        }

        @Test
        @DisplayName("Should detect ar:// scheme as URL format for CIP-68")
        void shouldDetectArweaveAsUrl() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ar://tx-id").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
        }

        @Test
        @DisplayName("Should detect raw base64 string as BASE64 format for CIP-68")
        void shouldDetectRawBase64AsBase64() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("iVBORw0KGgoAAAANSUhEUg==").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
            assertThat(result.getLogo().getValue()).isEqualTo("iVBORw0KGgoAAAANSUhEUg==");
        }

        @Test
        @DisplayName("Should detect data: URI as BASE64 format for CIP-68 (consumers decode, not fetch)")
        void shouldDetectDataUriAsBase64() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("data:image/png;base64,iVBORw0KGgo=").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
        }

        @Test
        @DisplayName("CIP-68 logo should override CIP-26 logo when both exist")
        void cip68LogoShouldOverrideCip26Logo() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("Token").description("Desc").logo("base64data").build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ipfs://Override").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
            assertThat(result.getLogo().getValue()).isEqualTo("ipfs://Override");
        }

        @Test
        @DisplayName("Should not include CIP-68 logo when logoEnabled is false even if data exists")
        void shouldNotIncludeCip68LogoWhenDisabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);

            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ipfs://QmHash").build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should handle null logo in CIP-26 gracefully")
        void shouldHandleNullCip26Logo() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").logo(null).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getLogo()).isNull();
        }
    }

    @Nested
    @DisplayName("queryMetadataBatch - multi-subject")
    class QueryMetadataBatchTests {

        @Test
        @DisplayName("Should batch fetch CIP-26 metadata for multiple subjects")
        void shouldBatchFetchCip26() {
            AssetFingerprint fp1 = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            AssetFingerprint fp2 = AssetFingerprint.of(POLICY_ID, "deadbeef");
            String subject2 = fp2.toSubject();

            Cip26MetadataEntity e1 = Cip26MetadataEntity.builder().subject(SUBJECT).name("Token1").description("Desc1").decimals(6L).build();
            Cip26MetadataEntity e2 = Cip26MetadataEntity.builder().subject(subject2).name("Token2").description("Desc2").decimals(0L).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(e1, e2));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp1, fp2));

            assertThat(result).hasSize(2);
            assertThat(result.get(fp1).getName()).isEqualTo("Token1");
            assertThat(result.get(fp2).getName()).isEqualTo("Token2");
            verify(cip26MetadataRepository).findAllBySubjectIn(anyList());
        }

        @Test
        @DisplayName("Should return entry for subjects not found in DB")
        void shouldReturnEntryForMissingSubjects() {
            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result).hasSize(1);
            assertThat(result.get(fp).getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.get(fp).getName()).isNull();
        }

        @Test
        @DisplayName("Should return empty map when fingerprints list is empty")
        void shouldReturnEmptyMapWhenEmpty() {
            assertThat(tokenQueryService.queryMetadataBatch(List.of())).isEmpty();
            verifyNoInteractions(cip26MetadataRepository, cip68MetadataRepository);
        }

        @Test
        @DisplayName("Should expose logo from CIP-26 row when logoEnabled is true")
        void shouldExposeLogoWhenEnabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            Cip26MetadataEntity e1 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("T").description("D").logo("base64img").build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(e1));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result.get(fp).getLogo()).isNotNull();
            assertThat(result.get(fp).getLogo().getValue()).isEqualTo("base64img");
        }

        @Test
        @DisplayName("Should not expose logo when logoEnabled is false even when present in DB")
        void shouldNotExposeLogoWhenDisabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);

            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            Cip26MetadataEntity e1 = Cip26MetadataEntity.builder()
                    .subject(SUBJECT).name("T").description("D").logo("base64img").build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(e1));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result.get(fp).getLogo()).isNull();
        }

        @Test
        @DisplayName("Should apply CIP-68 override in batch context")
        void shouldApplyCip68OverrideInBatch() {
            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, CIP68_FT_ASSET);

            Cip26MetadataEntity cip26 = Cip26MetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("CIP26").description("Old").decimals(0L).build();
            when(cip26MetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            Cip68MetadataEntity cip68 = Cip68MetadataEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(100L).label(333)
                    .name("CIP68").description("New").decimals(6L).build();
            when(cip68MetadataRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result.get(fp).getName()).isEqualTo("CIP68");
            assertThat(result.get(fp).getDecimals()).isEqualTo(6);
        }
    }

}
