package org.cardanofoundation.rosetta.common.time;

import java.time.*;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.cardanofoundation.conversions.CardanoConverters;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfflineSlotServiceImpl implements OfflineSlotService {

    private final Clock clock;

    private final ZoneId zoneId;

    @Autowired
    @Qualifier("shellyStartTime")
    @SuppressWarnings("java:S6813")
    protected Instant shellyStartTime;

    @Autowired
    @Qualifier("shelleyStartSlot")
    @SuppressWarnings("java:S6813")
    protected Long shelleyStartSlot;

    @Autowired(required = false)
    @Nullable
    @SuppressWarnings("java:S6813")
    protected CardanoConverters cardanoConverters;

    @Override
    public long getCurrentSlotBasedOnTime() {
        if (cardanoConverters != null) {
            LocalDateTime nowDateTime = LocalDateTime.now(clock);
            ZoneOffset zoneOffset = ZonedDateTime.now(zoneId).getOffset();

            if (nowDateTime.toInstant(zoneOffset).isBefore(shellyStartTime)) {
                throw new ApiException(ExceptionFactory.misconfiguredTime(nowDateTime).getError());
            }

            return cardanoConverters.time().toSlot(nowDateTime);
        }

        return shelleyStartSlot;
    }

}
