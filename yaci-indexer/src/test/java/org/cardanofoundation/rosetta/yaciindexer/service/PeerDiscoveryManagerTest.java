package org.cardanofoundation.rosetta.yaciindexer.service;

import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddress;
import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddressType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PeerDiscoveryManagerTest {

    @InjectMocks
    private PeerDiscoveryManager peerDiscoveryManager;

    @BeforeEach
    void setUp() {
        // Set required values for the service
        ReflectionTestUtils.setField(peerDiscoveryManager, "cardanoNodeHost", "test-node.cardano.org");
        ReflectionTestUtils.setField(peerDiscoveryManager, "cardanoNodePort", 30000);
        ReflectionTestUtils.setField(peerDiscoveryManager, "protocolMagic", 1L);
    }

    @Nested
    class CachedPeersTests {

        @Test
        void shouldInitializeWithEmptyPeerList() {
            // When
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();

            // Then
            assertThat(cachedPeers).isNotNull();
            assertThat(cachedPeers).isEmpty();
        }

        @Test
        void shouldUpdateCachedPeers() {
            // Given
            List<PeerAddress> newPeers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 3001),
                    PeerAddress.ipv4("192.168.1.2", 3002),
                    PeerAddress.ipv6("::1", 3003)
            );

            // When
            peerDiscoveryManager.updateCachedPeers(newPeers);

            // Then
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(cachedPeers).hasSize(3);
            assertThat(cachedPeers).containsExactlyElementsOf(newPeers);
        }

        @Test
        void shouldClearAndReplaceExistingPeers() {
            // Given
            List<PeerAddress> initialPeers = Arrays.asList(
                    PeerAddress.ipv4("10.0.0.1", 3001),
                    PeerAddress.ipv4("10.0.0.2", 3002)
            );
            peerDiscoveryManager.updateCachedPeers(initialPeers);

            List<PeerAddress> newPeers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 3001),
                    PeerAddress.ipv6("2001:db8::1", 3002)
            );

            // When
            peerDiscoveryManager.updateCachedPeers(newPeers);

            // Then
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(cachedPeers).hasSize(2);
            assertThat(cachedPeers).containsExactlyElementsOf(newPeers);
            assertThat(cachedPeers).doesNotContainAnyElementsOf(initialPeers);
        }

        @Test
        void shouldHandleEmptyListUpdate() {
            // Given
            List<PeerAddress> initialPeers = Arrays.asList(
                    PeerAddress.ipv4("10.0.0.1", 3001),
                    PeerAddress.ipv4("10.0.0.2", 3002)
            );
            peerDiscoveryManager.updateCachedPeers(initialPeers);

            // When
            peerDiscoveryManager.updateCachedPeers(Collections.emptyList());

            // Then
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(cachedPeers).isEmpty();
        }
    }

    @Nested
    class ThreadSafetyTests {

        @Test
        void shouldHandleConcurrentReadsAndWrites() throws InterruptedException {
            // Given
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(10);

            // Create different peer lists for updates
            List<List<PeerAddress>> peerLists = Arrays.asList(
                    Arrays.asList(PeerAddress.ipv4("192.168.1.1", 3001)),
                    Arrays.asList(PeerAddress.ipv4("192.168.1.2", 3002), PeerAddress.ipv4("192.168.1.3", 3003)),
                    Arrays.asList(PeerAddress.ipv6("::1", 3004), PeerAddress.ipv6("::2", 3005))
            );

            // When - Submit concurrent read and write tasks
            for (int i = 0; i < 10; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to be ready
                        
                        if (index % 3 == 0) {
                            // Write operation
                            peerDiscoveryManager.updateCachedPeers(peerLists.get(index / 3));
                        } else {
                            // Read operation
                            List<PeerAddress> peers = peerDiscoveryManager.getCachedPeers();
                            assertThat(peers).isNotNull();
                            // Verify we can iterate without ConcurrentModificationException
                            for (PeerAddress peer : peers) {
                                assertThat(peer).isNotNull();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            // Start all threads simultaneously
            startLatch.countDown();

            // Wait for all threads to complete
            boolean completed = endLatch.await(5, TimeUnit.SECONDS);

            // Then
            assertThat(completed).isTrue();
            executor.shutdown();
            
            // Final state should be consistent
            List<PeerAddress> finalPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(finalPeers).isNotNull();
        }

        @Test
        void shouldProvideImmutableSnapshotDuringIteration() {
            // Given
            List<PeerAddress> initialPeers = Arrays.asList(
                    PeerAddress.ipv4("10.0.0.1", 3001),
                    PeerAddress.ipv4("10.0.0.2", 3002),
                    PeerAddress.ipv4("10.0.0.3", 3003)
            );
            peerDiscoveryManager.updateCachedPeers(initialPeers);

            // When - Start iterating and modify the list during iteration
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            int count = 0;
            for (PeerAddress peer : cachedPeers) {
                count++;
                if (count == 2) {
                    // Modify the cached peers during iteration
                    peerDiscoveryManager.updateCachedPeers(Arrays.asList(
                            PeerAddress.ipv4("192.168.1.1", 4001)
                    ));
                }
            }

            // Then - Iteration should complete without ConcurrentModificationException
            assertThat(count).isEqualTo(3); // Should have iterated over the original 3 elements
            
            // Verify the list was actually updated
            assertThat(peerDiscoveryManager.getCachedPeers()).hasSize(1);
        }
    }

    @Nested
    class PeerAddressValidationTests {

        @Test
        void shouldHandleIPv4Addresses() {
            // Given
            List<PeerAddress> ipv4Peers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 3001),
                    PeerAddress.ipv4("10.0.0.1", 3002),
                    PeerAddress.ipv4("172.16.0.1", 3003)
            );

            // When
            peerDiscoveryManager.updateCachedPeers(ipv4Peers);

            // Then
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(cachedPeers).hasSize(3);
            assertThat(cachedPeers).allMatch(peer -> peer.getType() == PeerAddressType.IPv4);
        }

        @Test
        void shouldHandleIPv6Addresses() {
            // Given
            List<PeerAddress> ipv6Peers = Arrays.asList(
                    PeerAddress.ipv6("2001:db8::1", 3001),
                    PeerAddress.ipv6("fe80::1", 3002),
                    PeerAddress.ipv6("::1", 3003)
            );

            // When
            peerDiscoveryManager.updateCachedPeers(ipv6Peers);

            // Then
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(cachedPeers).hasSize(3);
            assertThat(cachedPeers).allMatch(peer -> peer.getType() == PeerAddressType.IPv6);
        }

        @Test
        void shouldHandleMixedIPv4AndIPv6Addresses() {
            // Given
            List<PeerAddress> mixedPeers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 3001),
                    PeerAddress.ipv6("2001:db8::1", 3002),
                    PeerAddress.ipv4("10.0.0.1", 3003),
                    PeerAddress.ipv6("::1", 3004)
            );

            // When
            peerDiscoveryManager.updateCachedPeers(mixedPeers);

            // Then
            List<PeerAddress> cachedPeers = peerDiscoveryManager.getCachedPeers();
            assertThat(cachedPeers).hasSize(4);
            
            long ipv4Count = cachedPeers.stream()
                    .filter(peer -> peer.getType() == PeerAddressType.IPv4)
                    .count();
            long ipv6Count = cachedPeers.stream()
                    .filter(peer -> peer.getType() == PeerAddressType.IPv6)
                    .count();
            
            assertThat(ipv4Count).isEqualTo(2);
            assertThat(ipv6Count).isEqualTo(2);
        }
    }
}