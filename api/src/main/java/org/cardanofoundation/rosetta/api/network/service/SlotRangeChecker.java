package org.cardanofoundation.rosetta.api.network.service;

import org.springframework.stereotype.Service;

@Service
public class SlotRangeChecker {

    public boolean isSlotWithinEpsilon(long slotBasedOnTime,
                                       long slotBasedOnLatestBlock,
                                       long epsilon) {
        assert slotBasedOnTime >= 0;
        assert slotBasedOnLatestBlock >= 0;

        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon must be non-negative");
        }

        return Math.abs(slotBasedOnTime - slotBasedOnLatestBlock) <= epsilon;
    }

}
