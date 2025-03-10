package org.cardanofoundation.rosetta.common.util;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.enumeration.OperationType;

import static org.junit.jupiter.api.Assertions.*;

class MoreEnumsTest {

    @Test
    void testGetIfPresent_validValue() {
        // Test when a valid value is passed (case-insensitive)
        Optional<OperationType> result = MoreEnums.getIfPresent(OperationType.class, "INPUT");

        // Verify that the result is present and correct
        assertTrue(result.isPresent());
        assertEquals(OperationType.INPUT, result.get());
    }

    @Test
    void testGetIfPresent_invalidValue() {
        // Test when an invalid value is passed
        Optional<OperationType> result = MoreEnums.getIfPresent(OperationType.class, "INVALID");

        // Verify that the result is empty
        assertFalse(result.isPresent());
    }

    @Test
    void testGetIfPresent_nullValue() {
        // Test when the value is null
        Optional<OperationType> result = MoreEnums.getIfPresent(OperationType.class, null);

        // Verify that the result is empty
        assertFalse(result.isPresent());
    }

    @Test
    void testGetIfPresent_caseInsensitive() {
        // Test when a valid value is passed with different case (case-insensitive)
        Optional<OperationType> result = MoreEnums.getIfPresent(OperationType.class, "input");

        // Verify that the result is present and correct
        assertTrue(result.isPresent());
        assertEquals(OperationType.INPUT, result.get());
    }

    @Test
    void testGetIfPresent_emptyString() {
        // Test when an empty string is passed
        Optional<OperationType> result = MoreEnums.getIfPresent(OperationType.class, "");

        // Verify that the result is empty
        assertFalse(result.isPresent());
    }

    @Test
    void testGetIfPresent_null() {
        Optional<OperationType> result = MoreEnums.getIfPresent(OperationType.class, null);

        assertTrue(result.isEmpty());
    }

}
