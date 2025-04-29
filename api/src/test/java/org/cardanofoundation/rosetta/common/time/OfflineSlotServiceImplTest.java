package org.cardanofoundation.rosetta.common.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.conversions.CardanoConverters;
import org.cardanofoundation.conversions.ClasspathConversionsFactory;
import org.cardanofoundation.conversions.domain.NetworkType;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfflineSlotServiceImplTest {

    @Mock
    private Clock clock;

    @Spy
    private CardanoConverters cardanoConverters = ClasspathConversionsFactory.createConverters(NetworkType.MAINNET);

    private final ZoneOffset zoneOffset = ZoneOffset.UTC;
    private final Instant shellyStartTime = Instant.parse("2020-07-29T21:44:51Z");
    private final long shelleyStartSlot = 1000L; // Example slot value

    private OfflineSlotServiceImpl offlineSlotServiceWithConverters;
    private OfflineSlotServiceImpl offlineSlotServiceWithoutConverters;

    @BeforeEach
    @SuppressWarnings("java:S5786")
    public void setup() {
        this.offlineSlotServiceWithConverters = new OfflineSlotServiceImpl(clock, zoneOffset);
        this.offlineSlotServiceWithConverters.cardanoConverters = cardanoConverters;
        this.offlineSlotServiceWithConverters.shelleyStartSlot = shelleyStartSlot;
        this.offlineSlotServiceWithConverters.shellyStartTime = shellyStartTime;

        this.offlineSlotServiceWithoutConverters = new OfflineSlotServiceImpl(clock, zoneOffset);
        this.offlineSlotServiceWithoutConverters.shelleyStartSlot = shelleyStartSlot;
        this.offlineSlotServiceWithoutConverters.shellyStartTime = shellyStartTime;
    }

    @Test
    void shouldReturnSlotBasedOnCurrentTime_WhenCardanoConvertersAvailable() {
        // Given
        LocalDateTime now = LocalDateTime.of(2023, 10, 10, 10, 10, 10);
        when(clock.instant()).thenReturn(now.toInstant(zoneOffset));
        when(clock.getZone()).thenReturn(zoneOffset);

        // When
        long slot = offlineSlotServiceWithConverters.getCurrentSlotBasedOnTimeWithFallback();

        // Then
        assertThat(slot).isEqualTo(105366319L);
    }

    @Test
    void shouldThrowException_WhenTimeIsBeforeShellyStartTime() {
        // Given
        LocalDateTime beforeShellyStart = LocalDateTime.of(2000, 1, 1, 12, 0, 0);
        when(clock.instant()).thenReturn(beforeShellyStart.toInstant(zoneOffset));
        when(clock.getZone()).thenReturn(zoneOffset);

        // When / Then
        assertThatThrownBy(() -> offlineSlotServiceWithConverters.getCurrentSlotBasedOnTimeWithFallback())
                .isInstanceOf(ApiException.class);
    }

    @Test
    void shouldVerifyCardanoConvertersSlotMethodIsCalled() {
        // Given
        LocalDateTime now = LocalDateTime.of(2023, 10, 10, 10, 10, 10);
        when(clock.instant()).thenReturn(now.toInstant(zoneOffset));
        when(clock.getZone()).thenReturn(zoneOffset);

        // When
        long slot = offlineSlotServiceWithConverters.getCurrentSlotBasedOnTimeWithFallback();

        // Then
        assertThat(slot).isEqualTo(105366319L);
        verify(cardanoConverters, atLeastOnce()).time();
    }

    @Test
    void shouldVerifyCardanoConvertersSlotMethodIsCalledWithoutFallback() {
        // Given
        LocalDateTime now = LocalDateTime.of(2023, 10, 10, 10, 10, 10);
        when(clock.instant()).thenReturn(now.toInstant(zoneOffset));
        when(clock.getZone()).thenReturn(zoneOffset);

        // When
        Optional<Long> slotM = offlineSlotServiceWithConverters.getCurrentSlotBasedOnTime();

        // Then
        assertThat(slotM).isPresent();
        assertThat(slotM.orElseThrow()).isEqualTo(105366319L);
        verify(cardanoConverters, atLeastOnce()).time();
    }

    @Test
    public void cardanoConvertersNotAvailableNoCurrentSlot() {
        assertThat(offlineSlotServiceWithoutConverters.getCurrentSlotBasedOnTime()).isNotPresent();
    }

    @Test
    public void cardanoConvertersNotAvailableCurrentSlotFallback() {
        assertThat(offlineSlotServiceWithoutConverters.getCurrentSlotBasedOnTimeWithFallback()).isEqualTo(shelleyStartSlot);
    }

}
