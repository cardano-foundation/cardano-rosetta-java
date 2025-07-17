package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GovActionParamsUtilTest {

    private static final String VALID_TX_ID = "df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c79";
    private static final String VALID_GOV_ACTION_INDEX_0 = VALID_TX_ID + "00";
    private static final String VALID_GOV_ACTION_INDEX_15 = VALID_TX_ID + "0f";
    private static final String VALID_GOV_ACTION_INDEX_99 = VALID_TX_ID + "63";

    @Nested
    @DisplayName("parseAndValidate")
    class ParseAndValidateTest {

        @Test
        @DisplayName("should parse valid governance action with index 0")
        void shouldParseValidGovernanceActionWithIndex0() {
            // when
            GovActionParamsUtil.ParsedGovActionParams result = 
                GovActionParamsUtil.parseAndValidate(VALID_GOV_ACTION_INDEX_0);

            // then
            assertEquals(VALID_TX_ID, result.getTxId());
            assertEquals(0, result.getIndex());
        }

        @Test
        @DisplayName("should parse valid governance action with index 15")
        void shouldParseValidGovernanceActionWithIndex15() {
            // when
            GovActionParamsUtil.ParsedGovActionParams result = 
                GovActionParamsUtil.parseAndValidate(VALID_GOV_ACTION_INDEX_15);

            // then
            assertEquals(VALID_TX_ID, result.getTxId());
            assertEquals(15, result.getIndex());
        }

        @Test
        @DisplayName("should parse valid governance action with index 99 (maximum)")
        void shouldParseValidGovernanceActionWithIndex99() {
            // when
            GovActionParamsUtil.ParsedGovActionParams result = 
                GovActionParamsUtil.parseAndValidate(VALID_GOV_ACTION_INDEX_99);

            // then
            assertEquals(VALID_TX_ID, result.getTxId());
            assertEquals(99, result.getIndex());
        }

        @Test
        @DisplayName("should parse governance action with uppercase hex characters")
        void shouldParseGovernanceActionWithUppercaseHex() {
            // given - using 4F (79 decimal) which is valid (< 99)
            String govActionWithUppercase = "DF58F714C0765F3489AFB6909384A16C31D600695BE7E86FF9C59CF2E8A48C794F";

            // when
            GovActionParamsUtil.ParsedGovActionParams result = 
                GovActionParamsUtil.parseAndValidate(govActionWithUppercase);

            // then
            assertEquals("DF58F714C0765F3489AFB6909384A16C31D600695BE7E86FF9C59CF2E8A48C79", result.getTxId());
            assertEquals(79, result.getIndex());
        }

        @Test
        @DisplayName("should trim whitespace before parsing")
        void shouldTrimWhitespaceBeforeParsing() {
            // given
            String govActionWithWhitespace = "  " + VALID_GOV_ACTION_INDEX_0 + "  ";

            // when
            GovActionParamsUtil.ParsedGovActionParams result = 
                GovActionParamsUtil.parseAndValidate(govActionWithWhitespace);

            // then
            assertEquals(VALID_TX_ID, result.getTxId());
            assertEquals(0, result.getIndex());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("should throw exception for null or empty strings")
        void shouldThrowExceptionForNullOrEmptyStrings(String input) {
            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(input));
            
            assertEquals("Invalid governance vote, reason: Governance action parameter is required", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for null input")
        void shouldThrowExceptionForNullInput() {
            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(null));
            
            assertEquals("Invalid governance vote, reason: Governance action parameter is required", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for too short string")
        void shouldThrowExceptionForTooShortString() {
            // given
            String shortString = "df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c7";

            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(shortString));
            
            assertEquals("Invalid governance vote, reason: Governance action must be exactly 66 characters long (64 for tx_id + 2 for index), got 63", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for too long string")
        void shouldThrowExceptionForTooLongString() {
            // given
            String longString = VALID_TX_ID + "000";

            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(longString));

            assertEquals("Invalid governance vote, reason: Governance action must be exactly 66 characters long (64 for tx_id + 2 for index), got 67", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "gf58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c7900", // 'g' is not hex
            "df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c79zz", // 'z' is not hex
            "df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c79!@"  // special chars
        })
        @DisplayName("should throw exception for non-hex characters")
        void shouldThrowExceptionForNonHexCharacters(String invalidHex) {
            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(invalidHex));
            
            assertEquals("Invalid governance vote, reason: Governance action must contain only hexadecimal characters (0-9, a-f, A-F)", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for index exceeding maximum value")
        void shouldThrowExceptionForIndexExceedingMaximumValue() {
            // given - index 64 hex = 100 decimal (exceeds max of 99)
            String govActionWithHighIndex = VALID_TX_ID + "64";

            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(govActionWithHighIndex));
            
            assertEquals("Invalid governance vote, reason: Index 100 exceeds maximum allowed value 99", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for index FF (255 decimal)")
        void shouldThrowExceptionForIndexFF() {
            // given
            String govActionWithMaxHexIndex = VALID_TX_ID + "ff";

            // when & then
            ApiException exception = assertThrows(ApiException.class, 
                () -> GovActionParamsUtil.parseAndValidate(govActionWithMaxHexIndex));
            
            assertEquals("Invalid governance vote, reason: Index 255 exceeds maximum allowed value 99", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("formatGovActionString")
    class FormatGovActionStringTest {

        @Test
        @DisplayName("should format governance action string with index 0")
        void shouldFormatGovernanceActionStringWithIndex0() {
            // when
            String result = GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 0);

            // then
            assertEquals(VALID_GOV_ACTION_INDEX_0, result);
        }

        @Test
        @DisplayName("should format governance action string with index 15")
        void shouldFormatGovernanceActionStringWithIndex15() {
            // when
            String result = GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 15);

            // then
            assertEquals(VALID_GOV_ACTION_INDEX_15, result);
        }

        @Test
        @DisplayName("should format governance action string with index 99")
        void shouldFormatGovernanceActionStringWithIndex99() {
            // when
            String result = GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 99);

            // then
            assertEquals(VALID_GOV_ACTION_INDEX_99, result);
        }

        @Test
        @DisplayName("should format with leading zero for single digit index")
        void shouldFormatWithLeadingZeroForSingleDigitIndex() {
            // when
            String result = GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 5);

            // then
            assertEquals(VALID_TX_ID + "05", result);
        }

        @Test
        @DisplayName("should throw exception for null tx_id")
        void shouldThrowExceptionForNullTxId() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> GovActionParamsUtil.formatGovActionString(null, 0));
            
            assertEquals("Transaction ID must be exactly 64 characters", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for short tx_id")
        void shouldThrowExceptionForShortTxId() {
            // given
            String shortTxId = "df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c7";

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> GovActionParamsUtil.formatGovActionString(shortTxId, 0));
            
            assertEquals("Transaction ID must be exactly 64 characters", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for long tx_id")
        void shouldThrowExceptionForLongTxId() {
            // given
            String longTxId = VALID_TX_ID + "0";

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> GovActionParamsUtil.formatGovActionString(longTxId, 0));
            
            assertEquals("Transaction ID must be exactly 64 characters", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for negative index")
        void shouldThrowExceptionForNegativeIndex() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> GovActionParamsUtil.formatGovActionString(VALID_TX_ID, -1));
            
            assertEquals("Index must be between 0 and 99", exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception for index greater than 99")
        void shouldThrowExceptionForIndexGreaterThan99() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 100));
            
            assertEquals("Index must be between 0 and 99", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("ParsedGovActionParams")
    class ParsedGovActionParamsTest {

        @Test
        @DisplayName("should create GovActionId correctly")
        void shouldCreateGovActionIdCorrectly() {
            // given
            GovActionParamsUtil.ParsedGovActionParams parsed = 
                new GovActionParamsUtil.ParsedGovActionParams(VALID_TX_ID, 15);

            // when
            GovActionId govActionId = parsed.toGovActionId();

            // then
            assertEquals(VALID_TX_ID, govActionId.getTransactionId());
            assertEquals(15, govActionId.getGovActionIndex());
        }

        @Test
        @DisplayName("should have correct getters")
        void shouldHaveCorrectGetters() {
            // given
            GovActionParamsUtil.ParsedGovActionParams parsed = 
                new GovActionParamsUtil.ParsedGovActionParams(VALID_TX_ID, 42);

            // then
            assertEquals(VALID_TX_ID, parsed.getTxId());
            assertEquals(42, parsed.getIndex());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTest {

        @Test
        @DisplayName("should round trip format and parse correctly")
        void shouldRoundTripFormatAndParseCorrectly() {
            // given
            String originalTxId = VALID_TX_ID;
            int originalIndex = 42;

            // when
            String formatted = GovActionParamsUtil.formatGovActionString(originalTxId, originalIndex);
            GovActionParamsUtil.ParsedGovActionParams parsed = GovActionParamsUtil.parseAndValidate(formatted);

            // then
            assertEquals(originalTxId, parsed.getTxId());
            assertEquals(originalIndex, parsed.getIndex());
        }

        @Test
        @DisplayName("should handle boundary values correctly")
        void shouldHandleBoundaryValuesCorrectly() {
            // Test minimum index (0)
            String formatted0 = GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 0);
            GovActionParamsUtil.ParsedGovActionParams parsed0 = GovActionParamsUtil.parseAndValidate(formatted0);
            assertEquals(0, parsed0.getIndex());

            // Test maximum index (99)
            String formatted99 = GovActionParamsUtil.formatGovActionString(VALID_TX_ID, 99);
            GovActionParamsUtil.ParsedGovActionParams parsed99 = GovActionParamsUtil.parseAndValidate(formatted99);
            assertEquals(99, parsed99.getIndex());
        }

        @Test
        @DisplayName("should create valid GovActionId from parsed params")
        void shouldCreateValidGovActionIdFromParsedParams() {
            // given
            String govActionString = VALID_GOV_ACTION_INDEX_15;

            // when
            GovActionParamsUtil.ParsedGovActionParams parsed = GovActionParamsUtil.parseAndValidate(govActionString);
            GovActionId govActionId = parsed.toGovActionId();

            // then
            assertNotNull(govActionId);
            assertEquals(VALID_TX_ID, govActionId.getTransactionId());
            assertEquals(15, govActionId.getGovActionIndex());
        }
    }

}