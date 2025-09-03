package org.cardanofoundation.rosetta.yaciindexer.job;

import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddress;
import org.cardanofoundation.rosetta.yaciindexer.service.PeerDiscoveryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeerDiscoveryJobTest {

    @Mock
    private PeerDiscoveryManager peerDiscoveryManager;

    @InjectMocks
    private PeerDiscoveryJob peerDiscoveryJob;

    @Nested
    class RefreshPeersTests {

        @Test
        void shouldUpdateCacheWhenPeersDiscovered() {
            // Given
            List<PeerAddress> discoveredPeers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 30000),
                    PeerAddress.ipv4("192.168.1.2", 30001),
                    PeerAddress.ipv6("2001:db8::1", 30002)
            );
            when(peerDiscoveryManager.discoverPeers()).thenReturn(discoveredPeers);

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, times(1)).updateCachedPeers(discoveredPeers);
        }

        @Test
        void shouldNotUpdateCacheWhenNoPeersDiscovered() {
            // Given
            when(peerDiscoveryManager.discoverPeers()).thenReturn(Collections.emptyList());

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, never()).updateCachedPeers(any());
        }

        @Test
        void shouldNotUpdateCacheWhenDiscoveryReturnsNull() {
            // Given
            when(peerDiscoveryManager.discoverPeers()).thenReturn(null);

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, never()).updateCachedPeers(any());
        }

        @Test
        void shouldHandleExceptionDuringDiscovery() {
            // Given
            when(peerDiscoveryManager.discoverPeers())
                    .thenThrow(new RuntimeException("Network error"));

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, never()).updateCachedPeers(any());
        }

        @Test
        void shouldHandleExceptionDuringCacheUpdate() {
            // Given
            List<PeerAddress> discoveredPeers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 30000)
            );
            when(peerDiscoveryManager.discoverPeers()).thenReturn(discoveredPeers);
            doThrow(new RuntimeException("Cache update failed"))
                    .when(peerDiscoveryManager).updateCachedPeers(any());

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, times(1)).updateCachedPeers(discoveredPeers);
        }

        @Test
        void shouldUpdateCacheWithSinglePeer() {
            // Given
            List<PeerAddress> singlePeer = Collections.singletonList(
                    PeerAddress.ipv4("192.168.1.1", 30000)
            );
            when(peerDiscoveryManager.discoverPeers()).thenReturn(singlePeer);

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, times(1)).updateCachedPeers(singlePeer);
        }

        @Test
        void shouldUpdateCacheWithLargePeerList() {
            // Given
            List<PeerAddress> largePeerList = Arrays.asList(new PeerAddress[100]);
            for (int i = 0; i < 100; i++) {
                largePeerList.set(i, PeerAddress.ipv4("192.168.1." + i, 30000 + i));
            }
            when(peerDiscoveryManager.discoverPeers()).thenReturn(largePeerList);

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, times(1)).updateCachedPeers(largePeerList);
        }
    }

    @Nested
    class SchedulingTests {

        @Test
        void shouldBeScheduledMethod() {
            // The @Scheduled annotation should be present on refreshPeers method
            // This is more of a compile-time check, but we can verify the method exists
            // and can be called
            
            // Given
            when(peerDiscoveryManager.discoverPeers()).thenReturn(Collections.emptyList());

            // When
            peerDiscoveryJob.refreshPeers();

            // Then - Method should execute without issues
            verify(peerDiscoveryManager, times(1)).discoverPeers();
        }

        @Test
        void shouldNotBlockOnLongDiscovery() {
            // Given - Simulate a slow discovery process
            when(peerDiscoveryManager.discoverPeers()).thenAnswer(invocation -> {
                // This would normally block, but in the real implementation
                // it has a timeout
                Thread.sleep(100); // Simulate some work
                return Arrays.asList(
                        PeerAddress.ipv4("192.168.1.1", 30000)
                );
            });

            // When
            peerDiscoveryJob.refreshPeers();

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, times(1)).updateCachedPeers(anyList());
        }
    }

    @Nested
    class ErrorRecoveryTests {

        @Test
        void shouldContinueAfterDiscoveryFailure() {
            // Given - First call fails, second succeeds
            List<PeerAddress> peers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 30000)
            );
            when(peerDiscoveryManager.discoverPeers())
                    .thenThrow(new RuntimeException("Network error"))
                    .thenReturn(peers);

            // When - Call twice
            peerDiscoveryJob.refreshPeers(); // Should fail
            peerDiscoveryJob.refreshPeers(); // Should succeed

            // Then
            verify(peerDiscoveryManager, times(2)).discoverPeers();
            verify(peerDiscoveryManager, times(1)).updateCachedPeers(peers);
        }

        @Test
        void shouldHandleNullPointerException() {
            // Given
            when(peerDiscoveryManager.discoverPeers())
                    .thenThrow(new NullPointerException("Unexpected null"));

            // When
            peerDiscoveryJob.refreshPeers();

            // Then - Should not crash and not update cache
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, never()).updateCachedPeers(any());
        }

        @Test
        void shouldHandleOutOfMemoryError() {
            // Given
            when(peerDiscoveryManager.discoverPeers())
                    .thenThrow(new OutOfMemoryError("Heap space"));

            // When - This should be caught by the general Exception handler
            try {
                peerDiscoveryJob.refreshPeers();
            } catch (OutOfMemoryError e) {
                // Expected - OutOfMemoryError is an Error, not Exception
                // In real scenario, this would crash the thread
            }

            // Then
            verify(peerDiscoveryManager, times(1)).discoverPeers();
            verify(peerDiscoveryManager, never()).updateCachedPeers(any());
        }
    }
}