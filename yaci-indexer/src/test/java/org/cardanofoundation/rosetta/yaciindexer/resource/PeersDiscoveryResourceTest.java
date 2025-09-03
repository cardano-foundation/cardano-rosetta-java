package org.cardanofoundation.rosetta.yaciindexer.resource;

import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddress;
import org.cardanofoundation.rosetta.yaciindexer.domain.model.PeerAddressDto;
import org.cardanofoundation.rosetta.yaciindexer.service.PeerDiscoveryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeersDiscoveryResourceTest {

    @Mock
    private PeerDiscoveryManager peerDiscoveryManager;

    @InjectMocks
    private PeersDiscoveryResource peersDiscoveryResource;

    private CopyOnWriteArrayList<PeerAddress> testPeers;

    @BeforeEach
    void setUp() {
        testPeers = new CopyOnWriteArrayList<>();
        // Set the path field that would normally be injected by Spring
        ReflectionTestUtils.setField(peersDiscoveryResource, "path", "/api/v1/rosetta/peers");
    }

    @Nested
    class GetDiscoveredPeersTests {

        @Test
        void shouldReturnEmptyListWhenNoPeersDiscovered() {
            // Given
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnDiscoveredPeersInCorrectFormat() {
            // Given
            testPeers.addAll(Arrays.asList(
                    PeerAddress.ipv4("192.168.1.100", 30000),
                    PeerAddress.ipv4("10.0.0.50", 30001),
                    PeerAddress.ipv6("2001:db8::1", 30002)
            ));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(3);
            
            // First peer (IPv4)
            assertThat(result.get(0).getType()).isEqualTo("IPv4");
            assertThat(result.get(0).getAddress()).isEqualTo("192.168.1.100");
            assertThat(result.get(0).getPort()).isEqualTo(30000);
            
            // Second peer (IPv4)
            assertThat(result.get(1).getType()).isEqualTo("IPv4");
            assertThat(result.get(1).getAddress()).isEqualTo("10.0.0.50");
            assertThat(result.get(1).getPort()).isEqualTo(30001);
            
            // Third peer (IPv6)
            assertThat(result.get(2).getType()).isEqualTo("IPv6");
            assertThat(result.get(2).getAddress()).isEqualTo("2001:db8::1");
            assertThat(result.get(2).getPort()).isEqualTo(30002);
        }

        @Test
        void shouldHandleLargePeerList() {
            // Given - Create 100 peers
            for (int i = 0; i < 100; i++) {
                testPeers.add(PeerAddress.ipv4("192.168.1." + i, 30000 + i));
            }
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(100);
            assertThat(result.get(0).getType()).isEqualTo("IPv4");
            assertThat(result.get(99).getType()).isEqualTo("IPv4");
            assertThat(result.get(99).getAddress()).isEqualTo("192.168.1.99");
            assertThat(result.get(99).getPort()).isEqualTo(30099);
        }

        @Test
        void shouldReturnOnlyIPv6Peers() {
            // Given
            testPeers.addAll(Arrays.asList(
                    PeerAddress.ipv6("2001:db8::1", 30000),
                    PeerAddress.ipv6("fe80::1", 30001),
                    PeerAddress.ipv6("::1", 30002)
            ));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(dto -> "IPv6".equals(dto.getType()));
            assertThat(result.get(0).getAddress()).isEqualTo("2001:db8::1");
            assertThat(result.get(1).getAddress()).isEqualTo("fe80::1");
            assertThat(result.get(2).getAddress()).isEqualTo("::1");
        }

        @Test
        void shouldHandleMixedIPVersions() {
            // Given
            testPeers.addAll(Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 30000),
                    PeerAddress.ipv6("2001:db8::1", 30001),
                    PeerAddress.ipv4("10.0.0.1", 30002),
                    PeerAddress.ipv6("fe80::1", 30003)
            ));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(4);
            
            long ipv4Count = result.stream()
                    .filter(dto -> "IPv4".equals(dto.getType()))
                    .count();
            long ipv6Count = result.stream()
                    .filter(dto -> "IPv6".equals(dto.getType()))
                    .count();
            
            assertThat(ipv4Count).isEqualTo(2);
            assertThat(ipv6Count).isEqualTo(2);
        }

        @Test
        void shouldHandleEmptyPeerList() {
            // Given
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(new CopyOnWriteArrayList<>());

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        void shouldHandleSinglePeer() {
            // Given
            testPeers.add(PeerAddress.ipv4("192.168.1.1", 30000));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo("IPv4");
            assertThat(result.get(0).getAddress()).isEqualTo("192.168.1.1");
            assertThat(result.get(0).getPort()).isEqualTo(30000);
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldHandlePeersWithMaxPortNumber() {
            // Given
            testPeers.add(PeerAddress.ipv4("192.168.1.1", 65535));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPort()).isEqualTo(65535);
        }

        @Test
        void shouldHandlePeersWithMinPortNumber() {
            // Given
            testPeers.add(PeerAddress.ipv4("192.168.1.1", 1));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPort()).isEqualTo(1);
        }

        @Test
        void shouldHandleSpecialIPAddresses() {
            // Given
            testPeers.addAll(Arrays.asList(
                    PeerAddress.ipv4("0.0.0.0", 30000),      // Any address
                    PeerAddress.ipv4("127.0.0.1", 30001),    // Loopback
                    PeerAddress.ipv4("255.255.255.255", 30002), // Broadcast
                    PeerAddress.ipv6("::", 30003),           // IPv6 any
                    PeerAddress.ipv6("::1", 30004)           // IPv6 loopback
            ));
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(5);
            assertThat(result.get(0).getAddress()).isEqualTo("0.0.0.0");
            assertThat(result.get(1).getAddress()).isEqualTo("127.0.0.1");
            assertThat(result.get(2).getAddress()).isEqualTo("255.255.255.255");
            assertThat(result.get(3).getAddress()).isEqualTo("::");
            assertThat(result.get(4).getAddress()).isEqualTo("::1");
        }
    }

    @Nested
    class PeerAddressDtoMappingTests {

        @Test
        void shouldCorrectlyMapPeerAddressToDto() {
            // Given
            PeerAddress peerAddress = PeerAddress.ipv4("192.168.1.100", 30000);
            testPeers.add(peerAddress);
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(1);
            PeerAddressDto dto = result.get(0);
            assertThat(dto.getType()).isEqualTo("IPv4");
            assertThat(dto.getAddress()).isEqualTo("192.168.1.100");
            assertThat(dto.getPort()).isEqualTo(30000);
        }

        @Test
        void shouldCorrectlyMapIPv6PeerAddressToDto() {
            // Given
            PeerAddress peerAddress = PeerAddress.ipv6("2001:db8::1", 30000);
            testPeers.add(peerAddress);
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(1);
            PeerAddressDto dto = result.get(0);
            assertThat(dto.getType()).isEqualTo("IPv6");
            assertThat(dto.getAddress()).isEqualTo("2001:db8::1");
            assertThat(dto.getPort()).isEqualTo(30000);
        }

        @Test
        void shouldPreserveOrderOfPeers() {
            // Given
            List<PeerAddress> orderedPeers = Arrays.asList(
                    PeerAddress.ipv4("192.168.1.1", 30001),
                    PeerAddress.ipv4("192.168.1.2", 30002),
                    PeerAddress.ipv4("192.168.1.3", 30003)
            );
            testPeers.addAll(orderedPeers);
            when(peerDiscoveryManager.getCachedPeers()).thenReturn(testPeers);

            // When
            List<PeerAddressDto> result = peersDiscoveryResource.getDiscoveredPeers();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getAddress()).isEqualTo("192.168.1.1");
            assertThat(result.get(0).getPort()).isEqualTo(30001);
            assertThat(result.get(1).getAddress()).isEqualTo("192.168.1.2");
            assertThat(result.get(1).getPort()).isEqualTo(30002);
            assertThat(result.get(2).getAddress()).isEqualTo("192.168.1.3");
            assertThat(result.get(2).getPort()).isEqualTo(30003);
        }
    }
}