package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.model.entity.MetadataReferenceNftEntity;
import org.cardanofoundation.rosetta.api.common.model.entity.TokenMetadataEntity;
import org.cardanofoundation.rosetta.api.common.model.repository.MetadataReferenceNftRepository;
import org.cardanofoundation.rosetta.api.common.model.repository.TokenMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private TokenMetadataRepository tokenMetadataRepository;

    @Mock
    private MetadataReferenceNftRepository metadataReferenceNftRepository;

    private TokenQueryServiceImpl tokenQueryService;

    // 56-char hex policy ID
    private static final String POLICY_ID = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3";
    private static final String ASSET_HEX = "54657374546f6b656e"; // "TestToken"
    private static final String SUBJECT = POLICY_ID + ASSET_HEX;

    // CIP-68 fungible token prefix + name
    private static final String CIP68_FT_ASSET = "0014df10" + "aabbccdd";
    private static final String CIP68_REF_NFT_ASSET = "000643b0" + "aabbccdd";
    private static final String CIP68_SUBJECT = POLICY_ID + CIP68_FT_ASSET;

    @BeforeEach
    void setUp() {
        tokenQueryService = new TokenQueryServiceImpl(tokenMetadataRepository, metadataReferenceNftRepository);
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
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.getSubject()).isEqualTo(SUBJECT);
            assertThat(result.getName()).isNull();
            assertThat(result.getDescription()).isNull();
            assertThat(result.getDecimals()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return CIP-26 metadata when only CIP-26 exists")
        void shouldReturnCip26WhenOnlyCip26Exists() {
            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("HOSKY").description("Dog token")
                    .ticker("HOSKY").url("https://hosky.io").decimals(0L).build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getName()).isEqualTo("HOSKY");
            assertThat(result.getDescription()).isEqualTo("Dog token");
            assertThat(result.getTicker()).isEqualTo("HOSKY");
            assertThat(result.getUrl()).isEqualTo("https://hosky.io");
            assertThat(result.getDecimals()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return CIP-68 metadata when only CIP-68 exists")
        void shouldReturnCip68WhenOnlyCip68Exists() {
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("iUSD").description("Stablecoin").ticker("iUSD").decimals(6L).version(1L).build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
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
            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("Old Name").description("Old Desc")
                    .ticker("OLD").decimals(0L).url("https://old.com").build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("New Name").description("New Desc").ticker("NEW").decimals(8L).version(2L).build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
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
            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("CIP26 Name").description("CIP26 Desc")
                    .ticker("C26").url("https://cip26.com").decimals(6L).build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            // CIP-68 only has name and decimals, rest null
            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("CIP68 Name").description(null).ticker(null).decimals(8L).build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
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
            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").decimals(null).build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getDecimals()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("CIP-68 subject prefix conversion")
    class Cip68PrefixConversionTests {

        @Test
        @DisplayName("Should not attempt CIP-68 lookup for non-fungible token prefix")
        void shouldSkipNonFungiblePrefix() {
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            querySingle(POLICY_ID, "000de140aabb"); // NFT prefix, not fungible

            verifyNoInteractions(metadataReferenceNftRepository);
        }

        @Test
        @DisplayName("Should not attempt CIP-68 lookup for plain asset name without prefix")
        void shouldSkipPlainAssetName() {
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            querySingle(POLICY_ID, ASSET_HEX);

            verifyNoInteractions(metadataReferenceNftRepository);
        }

        @Test
        @DisplayName("Should not attempt CIP-68 lookup for empty symbol")
        void shouldSkipEmptySymbol() {
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            querySingle(POLICY_ID, "");

            verifyNoInteractions(metadataReferenceNftRepository);
        }

        @Test
        @DisplayName("Should convert fungible token prefix 0014df10 to reference NFT prefix 000643b0")
        void shouldConvertFungibleToReferencePrefix() {
            String hexName = "aabbccdd";
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of());

            querySingle(POLICY_ID, "0014df10" + hexName);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
            verify(metadataReferenceNftRepository).findLatestByConcatenatedKeys(captor.capture());
            assertThat(captor.getValue()).containsExactly(POLICY_ID + "000643b0" + hexName);
        }

        @Test
        @DisplayName("Should skip CIP-68 when symbol is exactly the prefix length with no name part")
        void shouldSkipWhenSymbolIsBarePrefix() {
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            querySingle(POLICY_ID, "0014df10"); // prefix only, no hex name

            verifyNoInteractions(metadataReferenceNftRepository);
        }
    }

    @Nested
    @DisplayName("Logo handling")
    class LogoTests {

        @Test
        @DisplayName("Should not include logo when logoEnabled is false")
        void shouldNotIncludeLogoWhenDisabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);

            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").logo("iVBORw0KGgo=").build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should include CIP-26 BASE64 logo when enabled (logo is on cip26_metadata row)")
        void shouldIncludeCip26LogoWhenEnabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").logo("iVBORw0KGgo=").build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, ASSET_HEX);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
            assertThat(result.getLogo().getValue()).isEqualTo("iVBORw0KGgo=");
        }

        @Test
        @DisplayName("Should detect ipfs:// scheme as URL format for CIP-68")
        void shouldDetectIpfsAsUrl() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ipfs://QmHash").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
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

            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("https://example.com/logo.png").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
        }

        @Test
        @DisplayName("Should detect ar:// scheme as URL format for CIP-68")
        void shouldDetectArweaveAsUrl() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ar://tx-id").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
        }

        @Test
        @DisplayName("Should detect raw base64 string as BASE64 format for CIP-68")
        void shouldDetectRawBase64AsBase64() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("iVBORw0KGgoAAAANSUhEUg==").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
            assertThat(result.getLogo().getValue()).isEqualTo("iVBORw0KGgoAAAANSUhEUg==");
        }

        @Test
        @DisplayName("Should detect data: URI as BASE64 format for CIP-68 (consumers decode, not fetch)")
        void shouldDetectDataUriAsBase64() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("data:image/png;base64,iVBORw0KGgo=").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
        }

        @Test
        @DisplayName("CIP-68 logo should override CIP-26 logo when both exist")
        void cip68LogoShouldOverrideCip26Logo() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("Token").description("Desc").logo("base64data").build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ipfs://Override").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
            assertThat(result.getLogo().getValue()).isEqualTo("ipfs://Override");
        }

        @Test
        @DisplayName("Should not include CIP-68 logo when logoEnabled is false even if data exists")
        void shouldNotIncludeCip68LogoWhenDisabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);

            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(500L).label(333)
                    .name("Token").description("Desc").logo("ipfs://QmHash").build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            TokenRegistryCurrencyData result = querySingle(POLICY_ID, CIP68_FT_ASSET);

            assertThat(result.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should handle null logo in CIP-26 gracefully")
        void shouldHandleNullCip26Logo() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("Token").description("Desc").logo(null).build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

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

            TokenMetadataEntity e1 = TokenMetadataEntity.builder().subject(SUBJECT).name("Token1").description("Desc1").decimals(6L).build();
            TokenMetadataEntity e2 = TokenMetadataEntity.builder().subject(subject2).name("Token2").description("Desc2").decimals(0L).build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(e1, e2));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp1, fp2));

            assertThat(result).hasSize(2);
            assertThat(result.get(fp1).getName()).isEqualTo("Token1");
            assertThat(result.get(fp2).getName()).isEqualTo("Token2");
            verify(tokenMetadataRepository).findAllBySubjectIn(anyList());
        }

        @Test
        @DisplayName("Should return entry for subjects not found in DB")
        void shouldReturnEntryForMissingSubjects() {
            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of());

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result).hasSize(1);
            assertThat(result.get(fp).getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.get(fp).getName()).isNull();
        }

        @Test
        @DisplayName("Should return empty map when fingerprints list is empty")
        void shouldReturnEmptyMapWhenEmpty() {
            assertThat(tokenQueryService.queryMetadataBatch(List.of())).isEmpty();
            verifyNoInteractions(tokenMetadataRepository, metadataReferenceNftRepository);
        }

        @Test
        @DisplayName("Should expose logo from CIP-26 row when logoEnabled is true")
        void shouldExposeLogoWhenEnabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", true);

            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            TokenMetadataEntity e1 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("T").description("D").logo("base64img").build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(e1));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result.get(fp).getLogo()).isNotNull();
            assertThat(result.get(fp).getLogo().getValue()).isEqualTo("base64img");
        }

        @Test
        @DisplayName("Should not expose logo when logoEnabled is false even when present in DB")
        void shouldNotExposeLogoWhenDisabled() {
            ReflectionTestUtils.setField(tokenQueryService, "logoEnabled", false);

            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, ASSET_HEX);
            TokenMetadataEntity e1 = TokenMetadataEntity.builder()
                    .subject(SUBJECT).name("T").description("D").logo("base64img").build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(e1));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result.get(fp).getLogo()).isNull();
        }

        @Test
        @DisplayName("Should apply CIP-68 override in batch context")
        void shouldApplyCip68OverrideInBatch() {
            AssetFingerprint fp = AssetFingerprint.of(POLICY_ID, CIP68_FT_ASSET);

            TokenMetadataEntity cip26 = TokenMetadataEntity.builder()
                    .subject(CIP68_SUBJECT).name("CIP26").description("Old").decimals(0L).build();
            when(tokenMetadataRepository.findAllBySubjectIn(anyList())).thenReturn(List.of(cip26));

            MetadataReferenceNftEntity cip68 = MetadataReferenceNftEntity.builder()
                    .policyId(POLICY_ID).assetName(CIP68_REF_NFT_ASSET).slot(100L).label(333)
                    .name("CIP68").description("New").decimals(6L).build();
            when(metadataReferenceNftRepository.findLatestByConcatenatedKeys(anyCollection()))
                    .thenReturn(List.of(cip68));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenQueryService.queryMetadataBatch(List.of(fp));

            assertThat(result.get(fp).getName()).isEqualTo("CIP68");
            assertThat(result.get(fp).getDecimals()).isEqualTo(6);
        }
    }

}
