package org.cardanofoundation.rosetta.common.util;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import java.util.List;

import static org.cardanofoundation.rosetta.common.util.ValidateParseUtil.*;
import static org.junit.jupiter.api.Assertions.*;

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
                () -> validateAndParsePoolRegistrationParameters(PoolRegistrationParams.builder()
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
