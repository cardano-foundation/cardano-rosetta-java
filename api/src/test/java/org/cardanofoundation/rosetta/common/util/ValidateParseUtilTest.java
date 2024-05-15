package org.cardanofoundation.rosetta.common.util;

import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.VoteRegistrationMetadata;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParsePoolKeyHash;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseTransactionInput;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseVoteRegistrationMetadata;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseVotingKey;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateChainCode;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateDnsName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

@SuppressWarnings("java:S5778")
class ValidateParseUtilTest {

    @Test
    void missingChainCodeTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateChainCode(""));
        assertEquals("Missing chain code", exception.getError().getMessage());
        assertEquals(5012, exception.getError().getCode());
    }

    @Test
    void poolKeyHashTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParsePoolKeyHash(""));
        assertEquals("Pool key hash is required to operate", exception.getError().getMessage());
        assertEquals(4020, exception.getError().getCode());
    }

    @Test
    void validateDnsNaneTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateDnsName(""));
        assertEquals("Dns name expected for pool relay", exception.getError().getMessage());
        assertEquals(4032, exception.getError().getCode());
    }

    @Test
    void  validateAndParseTransactionInputTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseTransactionInput(new Operation()));
        assertEquals("Transaction inputs parameters errors in operations array",
                exception.getError().getMessage());
        assertEquals(4008, exception.getError().getCode());
    }

    @Test
    void validateAndParseVoteRegistrationMetadataWOStaikingKeyTest() {
        PublicKey stakingCredential = PublicKey
                .builder()
                .hexBytes("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F")
                .curveType(EDWARDS25519)
                .build();
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseVoteRegistrationMetadata(VoteRegistrationMetadata
                        .builder()
                        .votingkey(stakingCredential)
                        .stakeKey(new PublicKey())
                        .build()));
        assertEquals("Staking key is required for this type of address", exception.getError().getMessage());
        assertEquals(4018, exception.getError().getCode());
    }

    @Test
    void validateAndParseVoteRegistrationMetadataInvalidStaikinKetTest() {
        PublicKey stakingCredential = PublicKey
                .builder()
                .hexBytes("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F")
                .curveType(EDWARDS25519)
                .build();
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseVoteRegistrationMetadata(VoteRegistrationMetadata
                        .builder()
                        .votingkey(stakingCredential)
                        .stakeKey(new PublicKey("hex", EDWARDS25519))
                        .build()));
        assertEquals("Invalid staking key format", exception.getError().getMessage());
        assertEquals(4017, exception.getError().getCode());
    }

    @Test
    void validateAndParseVotingKeyWOVotingKey() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseVotingKey(new PublicKey()));
        assertEquals("Voting key is missing", exception.getError().getMessage());
        assertEquals(5009, exception.getError().getCode());
    }
}
