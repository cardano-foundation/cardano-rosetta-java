package org.cardanofoundation.rosetta.common.util;

import java.util.List;

import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PoolMargin;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.TokenBundleItem;
import org.openapitools.client.model.VoteRegistrationMetadata;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.cardanofoundation.rosetta.EntityGenerator.givenPublicKey;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParsePoolKeyHash;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParsePoolOwners;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParsePoolRegistationParameters;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParsePoolRegistrationCert;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseTokenBundle;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseTransactionInput;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseVoteRegistrationMetadata;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateAndParseVotingKey;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateChainCode;
import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.validateDnsName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        PublicKey stakingCredential = givenPublicKey();
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
        PublicKey stakingCredential = givenPublicKey();
        VoteRegistrationMetadata voteRegistrationMetadata = VoteRegistrationMetadata
                .builder()
                .votingkey(stakingCredential)
                .stakeKey(new PublicKey("hex", EDWARDS25519))
                .build();
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseVoteRegistrationMetadata(voteRegistrationMetadata));
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

    @Test
    void validateAndParseTransactionInputWOBodyTest() {
        Operation op = Operation.builder()
                .amount(Amount.builder().value("-1").build())
                .coinChange(CoinChange
                        .builder()
                        .coinIdentifier(CoinIdentifier.builder()
                                .identifier("1:2")
                                .build())
                        .build())
                .build();
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseTransactionInput(op));
        assertEquals("Cant deserialize transaction input from transaction body", exception.getError().getMessage());
        assertEquals(4013, exception.getError().getCode());
    }

    @Test
    void validateAndParsePoolOwnersWithInvalidOwner() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParsePoolOwners(List.of("Owner")));
        assertEquals("Invalid pool owners received", exception.getError().getMessage());
        assertEquals(4034, exception.getError().getCode());
    }

    @Test
    void poolRegistrationParametersTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParsePoolRegistationParameters(PoolRegistrationParams.builder()
                        .margin(new PoolMargin())
                        .build()));
        assertEquals("Invalid pool registration parameters received", exception.getError().getMessage());
        assertEquals(4035, exception.getError().getCode());
    }

    @Test
    void validateAndParseTokenBundleWithParametersErrorTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseTokenBundle(List.of(
                        new TokenBundleItem("11111111111111111111111111111111111111111111111111111111",
                                List.of(new Amount("6", new Currency("ADA", 6, new CurrencyMetadata()), new Object()),
                                        new Amount("6", new Currency("ADA", 6, new CurrencyMetadata()), new Object()))))));
        assertEquals("Transaction outputs parameters errors in operations array", exception.getError().getMessage());
        assertEquals(4009, exception.getError().getCode());
    }

    @Test
    void parsePoolRegistrationCertWithInvalidFormatTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParsePoolRegistrationCert(NetworkEnum.MAINNET.getNetwork(),
                        "cert", "hash"));
        assertEquals("Invalid pool registration certificate format", exception.getError().getMessage());
        assertEquals(4027, exception.getError().getCode());
    }

    @Test
    void parsePoolRegistrationCertWOCertTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParsePoolRegistrationCert(NetworkEnum.MAINNET.getNetwork(),
                        "", "hash"));
        assertEquals("Pool registration certificate is required for pool registration", exception.getError().getMessage());
        assertEquals(4026, exception.getError().getCode());
    }

    @Test
    void validateAndParseVotingKeyWithInvalidKeyFormatTest() {
        PublicKey publicKey = givenPublicKey();
        publicKey.setCurveType(CurveType.PALLAS);
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParseVotingKey(publicKey));
        assertEquals("Voting key format is invalid", exception.getError().getMessage());
        assertEquals(5010, exception.getError().getCode());
    }

    @Test
    void validateAndParsePoolKeyHashWithInvalidFormatTest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> validateAndParsePoolKeyHash("1111111111111111111111111111111111111111111111111111111"));
        assertEquals("Provided pool key hash has invalid format", exception.getError().getMessage());
        assertEquals(4025, exception.getError().getCode());
    }

    @Test
    void validateAddressPresenceTest() {
        boolean actual = ValidateParseUtil.validateAddressPresence(new Operation());
        assertFalse(actual);
        actual = ValidateParseUtil
            .validateAddressPresence(new Operation().account(new AccountIdentifier()));
        assertFalse(actual);
        actual = ValidateParseUtil
            .validateAddressPresence(new Operation().account(new AccountIdentifier().address("")));
        assertFalse(actual);
        actual = ValidateParseUtil
            .validateAddressPresence(
                new Operation().account(new AccountIdentifier().address("address")));
        assertTrue(actual);
    }
}
