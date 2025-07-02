package org.cardanofoundation.rosetta.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IPVParserTest {

    @Nested
    @DisplayName("parseIpv4 Tests")
    class ParseIpv4Tests {

        @Test
        @DisplayName("should parse valid IPv4 address")
        void shouldParseValidIpv4() throws UnknownHostException {
            Inet4Address result = IPVParser.parseIpv4("127.0.0.1");
            assertThat(result).isInstanceOf(Inet4Address.class);
            assertThat(result.getHostAddress()).isEqualTo("127.0.0.1");
        }

        @Test
        @DisplayName("should fail for null IPv4 string")
        void shouldFailForNullIpv4() {
            assertThatThrownBy(() -> IPVParser.parseIpv4(null))
                    .isInstanceOf(UnknownHostException.class)
                    .hasMessage("Error Parsing IP Address");
        }

        @Test
        @DisplayName("should fail for malformed IPv4 address")
        void shouldFailForMalformedIpv4() {
            assertThatThrownBy(() -> IPVParser.parseIpv4("300.0.1"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("should fail for non-numeric IPv4 octet")
        void shouldFailForNonNumericIpv4() {
            assertThatThrownBy(() -> IPVParser.parseIpv4("127.a.0.1"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("parseIpv6 Tests")
    class ParseIpv6Tests {

        @Test
        @DisplayName("should parse valid IPv6 address (1) hex string")
        void shouldParseValidIpv61() throws UnknownHostException {
            // Example: "::1" -> 00000000000000000000000000000001
            Inet6Address result = IPVParser.parseIpv6("00000000000000000000000000000001");
            assertThat(result).isInstanceOf(Inet6Address.class);
            assertThat(result.getHostAddress()).isEqualTo("0:0:0:0:0:0:0:1");
        }


        @Test
        @DisplayName("should parse valid IPv6 address (2) hex string")
        void shouldParseValidIpv62() throws UnknownHostException {
            // Example: "::1" -> 00000000000000000000000000000001
            Inet6Address result = IPVParser.parseIpv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
            assertThat(result).isInstanceOf(Inet6Address.class);
            assertThat(result.getHostAddress()).isEqualTo("2001:db8:85a3:0:0:8a2e:370:7334");
        }

        @Test
        @DisplayName("should fail for null IPv6 string")
        void shouldFailForNullIpv6() {
            assertThatThrownBy(() -> IPVParser.parseIpv6(null))
                    .isInstanceOf(UnknownHostException.class)
                    .hasMessage("Error Parsing IP Address");
        }

        @Test
        @DisplayName("should fail for non-hex IPv6 string")
        void shouldFailForInvalidHexIpv6() {
            assertThatThrownBy(() -> IPVParser.parseIpv6("ZZZZ::ZZZZ"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should fail if IPv6 byte length is wrong")
        void shouldFailForInvalidLengthIpv6() {
            assertThatThrownBy(() -> IPVParser.parseIpv6("0001")) // Only 2 bytes, should be 16
                    .isInstanceOf(UnknownHostException.class);
        }
    }

}
