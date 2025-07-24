package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import org.assertj.core.api.Assertions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.AccountIdentifierMetadata;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.GovVoteParams;
import org.openapitools.client.model.GovVoteRationaleParams;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolGovernanceVoteParams;
import org.openapitools.client.model.PoolMargin;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.SubAccountIdentifier;
import org.openapitools.client.model.TokenBundleItem;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.mapper.OperationToCborMap.convertToCborMap;
import static org.cardanofoundation.rosetta.common.util.Formatters.key;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OperationToCborMapTest {

    @Test
    void convertToCborMapAndViseVersaTest() {
        //given
        AccountIdentifier accountIdentifier = new AccountIdentifier("address", new SubAccountIdentifier("address", null),
                new AccountIdentifierMetadata());
        PoolRegistrationParams poolRegistrationParams = PoolRegistrationParams
                .builder()
                .poolOwners(List.of("owner"))
                .margin(new PoolMargin("numerator", "denominator"))
                .relays(List.of(new Relay("ipv4", "ipv6", "name", 8080, "type")))
                .poolMetadata(new PoolMetadata("url", "hash"))
                .build();
        OperationMetadata operationMetadata = OperationMetadata.builder()
                .poolRegistrationParams(poolRegistrationParams)
                .refundAmount(new Amount("2", new Currency(Constants.ADA, 2, new CurrencyMetadata("policyId")), new Object()))
                .tokenBundle(List.of(new TokenBundleItem("tokenBundlePolicyId", List.of(new Amount()))))
                .build();

        Operation operation = Operation
                .builder()
                .operationIdentifier(new OperationIdentifier())
                .account(accountIdentifier)
                .type("poolRegistration")
                .metadata(operationMetadata)
                .build();
        //when
        Map map = convertToCborMap(operation);
        Operation opr = CborMapToOperation.cborMapToOperation(map);
        //then
        assertEquals(4, map.getKeys().size());
        assertEquals(operation.getAccount().getAddress(), opr.getAccount().getAddress());
        assertEquals(operation.getAccount().getSubAccount(), opr.getAccount().getSubAccount());
        assertEquals(operation.getOperationIdentifier(), opr.getOperationIdentifier());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getPoolOwners(), opr.getMetadata().getPoolRegistrationParams().getPoolOwners());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getRelays().getFirst().getDnsName(),
                opr.getMetadata().getPoolRegistrationParams().getRelays().getFirst().getDnsName());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getMargin(), opr.getMetadata().getPoolRegistrationParams().getMargin());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getPoolMetadata(), opr.getMetadata().getPoolRegistrationParams().getPoolMetadata());
    }

    @Nested
    class PoolGovernanceVoteMetadataTests {

        @Test
        void shouldMapPoolGovernanceVoteWithAllFields() {
            // Given
            String validTxId = "40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc";
            int governanceActionIndex = 5;
            String governanceActionHash = validTxId + String.format("%02x", governanceActionIndex); // "05" in hex
            
            PublicKey poolCredential = PublicKey.builder()
                    .hexBytes("abc123def456")
                    .curveType(CurveType.SECP256K1)
                    .build();
            
            GovVoteRationaleParams voteRationale = GovVoteRationaleParams.builder()
                    .url("https://example.com/vote-rationale")
                    .dataHash("hash123456789")
                    .build();
            
            PoolGovernanceVoteParams poolGovernanceVoteParams = PoolGovernanceVoteParams.builder()
                    .governanceActionHash(governanceActionHash)
                    .poolCredential(poolCredential)
                    .vote(GovVoteParams.YES)
                    .voteRationale(voteRationale)
                    .build();
            
            OperationMetadata operationMetadata = OperationMetadata.builder()
                    .poolGovernanceVoteParams(poolGovernanceVoteParams)
                    .build();
            
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(operationMetadata)
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            assertThat(cborMap).isNotNull();
            
            // Verify metadata structure
            Map metadataMap = (Map) cborMap.get(key(Constants.METADATA));
            assertThat(metadataMap).isNotNull();
            
            // Verify pool governance vote params structure
            Map poolGovVoteParamsMap = (Map) metadataMap.get(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS));
            assertThat(poolGovVoteParamsMap).isNotNull();
            
            // Verify pool credential
            Map poolCredentialMap = (Map) poolGovVoteParamsMap.get(key(Constants.POOL_CREDENTIAL));
            assertThat(poolCredentialMap).isNotNull();
            assertThat(((UnicodeString) poolCredentialMap.get(key(Constants.HEX_BYTES))).getString())
                    .isEqualTo("abc123def456");
            assertThat(((UnicodeString) poolCredentialMap.get(key(Constants.CURVE_TYPE))).getString())
                    .isEqualTo("secp256k1");
            
            // Verify vote
            assertThat(((UnicodeString) poolGovVoteParamsMap.get(key(Constants.VOTE))).getString())
                    .isEqualTo("yes");
            
            // Verify vote rationale
            Map voteRationaleMap = (Map) poolGovVoteParamsMap.get(key(Constants.VOTE_RATIONALE));
            assertThat(voteRationaleMap).isNotNull();
            assertThat(((UnicodeString) voteRationaleMap.get(key("url"))).getString())
                    .isEqualTo("https://example.com/vote-rationale");
            assertThat(((UnicodeString) voteRationaleMap.get(key("data_hash"))).getString())
                    .isEqualTo("hash123456789");
            
            // Verify governance action hash (should be reformatted by GovActionParamsUtil)
            assertThat(((UnicodeString) poolGovVoteParamsMap.get(key(Constants.GOVERNANCE_ACTION_HASH))).getString())
                    .isEqualTo(governanceActionHash);
        }

        @Test
        void shouldMapPoolGovernanceVoteWithoutVoteRationale() {
            // Given
            String validTxId = "df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c79";
            int governanceActionIndex = 0;
            String governanceActionHash = validTxId + String.format("%02x", governanceActionIndex); // "00" in hex
            
            PublicKey poolCredential = PublicKey.builder()
                    .hexBytes("def456abc123")
                    .curveType(CurveType.EDWARDS25519)
                    .build();
            
            PoolGovernanceVoteParams poolGovernanceVoteParams = PoolGovernanceVoteParams.builder()
                    .governanceActionHash(governanceActionHash)
                    .poolCredential(poolCredential)
                    .vote(GovVoteParams.NO)
                    .build(); // No vote rationale
            
            OperationMetadata operationMetadata = OperationMetadata.builder()
                    .poolGovernanceVoteParams(poolGovernanceVoteParams)
                    .build();
            
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(operationMetadata)
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            assertThat(cborMap).isNotNull();
            
            Map metadataMap = (Map) cborMap.get(key(Constants.METADATA));
            Map poolGovVoteParamsMap = (Map) metadataMap.get(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS));
            
            // Verify pool credential
            Map poolCredentialMap = (Map) poolGovVoteParamsMap.get(key(Constants.POOL_CREDENTIAL));
            assertThat(((UnicodeString) poolCredentialMap.get(key(Constants.HEX_BYTES))).getString())
                    .isEqualTo("def456abc123");
            assertThat(((UnicodeString) poolCredentialMap.get(key(Constants.CURVE_TYPE))).getString())
                    .isEqualTo("edwards25519");
            
            // Verify vote
            assertThat(((UnicodeString) poolGovVoteParamsMap.get(key(Constants.VOTE))).getString())
                    .isEqualTo("no");
            
            // Verify vote rationale is not present
            assertThat(poolGovVoteParamsMap.get(key(Constants.VOTE_RATIONALE))).isNull();
            
            // Verify governance action hash
            assertThat(((UnicodeString) poolGovVoteParamsMap.get(key(Constants.GOVERNANCE_ACTION_HASH))).getString())
                    .isEqualTo(governanceActionHash);
        }

        @Test
        void shouldMapPoolGovernanceVoteWithAbstainVote() {
            // Given
            String validTxId = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
            int governanceActionIndex = 99; // Maximum allowed index
            String governanceActionHash = validTxId + String.format("%02x", governanceActionIndex); // "63" in hex
            
            PublicKey poolCredential = PublicKey.builder()
                    .hexBytes("fedcba0987654321")
                    .curveType(CurveType.SECP256K1)
                    .build();
            
            PoolGovernanceVoteParams poolGovernanceVoteParams = PoolGovernanceVoteParams.builder()
                    .governanceActionHash(governanceActionHash)
                    .poolCredential(poolCredential)
                    .vote(GovVoteParams.ABSTAIN)
                    .build();
            
            OperationMetadata operationMetadata = OperationMetadata.builder()
                    .poolGovernanceVoteParams(poolGovernanceVoteParams)
                    .build();
            
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(operationMetadata)
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            Map metadataMap = (Map) cborMap.get(key(Constants.METADATA));
            Map poolGovVoteParamsMap = (Map) metadataMap.get(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS));
            
            // Verify vote is abstain
            assertThat(((UnicodeString) poolGovVoteParamsMap.get(key(Constants.VOTE))).getString())
                    .isEqualTo("abstain");
            
            // Verify governance action hash with maximum index
            assertThat(((UnicodeString) poolGovVoteParamsMap.get(key(Constants.GOVERNANCE_ACTION_HASH))).getString())
                    .isEqualTo(governanceActionHash);
        }

        @Test
        void shouldHandleNullPoolGovernanceVoteParams() {
            // Given
            OperationMetadata operationMetadata = OperationMetadata.builder()
                    .poolGovernanceVoteParams(null) // Null pool governance vote params
                    .build();
            
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(operationMetadata)
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            Map metadataMap = (Map) cborMap.get(key(Constants.METADATA));
            assertThat(metadataMap).isNotNull();
            
            // Pool governance vote params should not be present in the map
            assertThat(metadataMap.get(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS))).isNull();
        }

        @Test
        void shouldHandleNullOperationMetadata() {
            // Given
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(null) // Null metadata
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            // Metadata should not be present in the map
            assertThat(cborMap.get(key(Constants.METADATA))).isNull();
        }

        @Test
        void shouldMapVoteRationaleWithOnlyUrl() {
            // Given
            String validTxId = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
            String governanceActionHash = validTxId + "01"; // Index 1
            
            GovVoteRationaleParams voteRationale = GovVoteRationaleParams.builder()
                    .url("https://vote-url.example")
                    .dataHash(null) // Only URL, no data hash
                    .build();
            
            PoolGovernanceVoteParams poolGovernanceVoteParams = PoolGovernanceVoteParams.builder()
                    .governanceActionHash(governanceActionHash)
                    .poolCredential(PublicKey.builder()
                            .hexBytes("123456")
                            .curveType(CurveType.EDWARDS25519)
                            .build())
                    .vote(GovVoteParams.YES)
                    .voteRationale(voteRationale)
                    .build();
            
            OperationMetadata operationMetadata = OperationMetadata.builder()
                    .poolGovernanceVoteParams(poolGovernanceVoteParams)
                    .build();
            
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(operationMetadata)
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            Map metadataMap = (Map) cborMap.get(key(Constants.METADATA));
            Map poolGovVoteParamsMap = (Map) metadataMap.get(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS));
            Map voteRationaleMap = (Map) poolGovVoteParamsMap.get(key(Constants.VOTE_RATIONALE));
            
            assertThat(voteRationaleMap).isNotNull();
            assertThat(((UnicodeString) voteRationaleMap.get(key("url"))).getString())
                    .isEqualTo("https://vote-url.example");
            // data_hash should not be present since it was null
            assertThat(voteRationaleMap.get(key("data_hash"))).isNull();
        }

        @Test
        void shouldMapVoteRationaleWithOnlyDataHash() {
            // Given
            String validTxId = "fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321";
            String governanceActionHash = validTxId + "0a"; // Index 10
            
            GovVoteRationaleParams voteRationale = GovVoteRationaleParams.builder()
                    .url(null) // No URL
                    .dataHash("datahash123456789abcdef")
                    .build();
            
            PoolGovernanceVoteParams poolGovernanceVoteParams = PoolGovernanceVoteParams.builder()
                    .governanceActionHash(governanceActionHash)
                    .poolCredential(PublicKey.builder()
                            .hexBytes("789abc")
                            .curveType(CurveType.SECP256K1)
                            .build())
                    .vote(GovVoteParams.NO)
                    .voteRationale(voteRationale)
                    .build();
            
            OperationMetadata operationMetadata = OperationMetadata.builder()
                    .poolGovernanceVoteParams(poolGovernanceVoteParams)
                    .build();
            
            Operation operation = Operation.builder()
                    .operationIdentifier(new OperationIdentifier())
                    .type(Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
                    .metadata(operationMetadata)
                    .build();

            // When
            Map cborMap = convertToCborMap(operation);

            // Then
            Map metadataMap = (Map) cborMap.get(key(Constants.METADATA));
            Map poolGovVoteParamsMap = (Map) metadataMap.get(key(Constants.POOL_GOVERNANCE_VOTE_PARAMS));
            Map voteRationaleMap = (Map) poolGovVoteParamsMap.get(key(Constants.VOTE_RATIONALE));
            
            assertThat(voteRationaleMap).isNotNull();
            // url should not be present since it was null
            assertThat(voteRationaleMap.get(key("url"))).isNull();
            assertThat(((UnicodeString) voteRationaleMap.get(key("data_hash"))).getString())
                    .isEqualTo("datahash123456789abcdef");
        }
    }

}
