package org.cardanofoundation.rosetta.api.network.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlotRangeCheckerTest {

    @Test
    void testIsSlotWithinEpsilonWhenWithinRange() {
        SlotRangeChecker slotRangeChecker = new SlotRangeChecker();
        assertTrue(slotRangeChecker.isSlotWithinEpsilon(100, 105, 10));
    }

    @Test
    void testIsSlotWithinEpsilonWhenOutOfRange() {
        SlotRangeChecker slotRangeChecker = new SlotRangeChecker();
        assertFalse(slotRangeChecker.isSlotWithinEpsilon(100, 120, 10));
    }

    @Test
    void testIsSlotWithinEpsilonWhenAtBoundary() {
        SlotRangeChecker slotRangeChecker = new SlotRangeChecker();
        assertTrue(slotRangeChecker.isSlotWithinEpsilon(100, 110, 10));
    }

    @Test
    void testIsSlotWithinEpsilonThrowsExceptionWhenEpsilonNegative() {
        SlotRangeChecker slotRangeChecker = new SlotRangeChecker();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slotRangeChecker.isSlotWithinEpsilon(100, 105, -1));
        assertEquals("Epsilon must be non-negative", exception.getMessage());
    }

}
