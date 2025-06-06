package org.cardanofoundation.rosetta.common.time;

import java.util.Optional;

public interface OfflineSlotService {

    long getCurrentSlotBasedOnTimeWithFallback();

    Optional<Long> getCurrentSlotBasedOnTime();

}
