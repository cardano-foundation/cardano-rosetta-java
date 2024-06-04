package org.cardanofoundation.rosetta.common.util;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.spec.UnitInterval;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.cert.MultiHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRetirement;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostAddr;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostName;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.Relay;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.addRelayToPoolReLayOfTypeSingleHostAddr;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.addRelayToPoolReLayOfTypeSingleHostName;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.addRelayToPoolRelayOfTypeMultiHost;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.checkStakeCredential;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.getMultiHostRelay;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.getOwnerAddressesFromPoolRegistrations;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.getRewardAddressFromPoolRegistration;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.getSingleHostAddr;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.getSingleHostName;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseAsset;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseIpv4;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseIpv6;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolCertToOperation;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolMetadata;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolOwners;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolRegistration;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolRewardAccount;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseTokenAsset;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseVoteMetadataToOperation;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseWithdrawalToOperation;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.transactionInputToOperation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5778")
class ParseConstructionUtilTest {

    @Test
    void parsePoolCertToOperationWOCert() throws CborException, CborSerializationException {
        //when
        Operation operation = parsePoolCertToOperation(1L, null, 1L, "type");
        //then
        assertEquals("", operation.getStatus());
        assertEquals("type", operation.getType());
    }

    @Test
    void parsePoolRegiStrationTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration.builder()
                .cost(BigInteger.ONE)
                .vrfKeyHash("58404682ed74c2ae7f5a1b3d208b6e36a2e9c7bafeca5832d91aee25955a1e3".getBytes(StandardCharsets.UTF_8))
                .pledge(BigInteger.ONE)
                .rewardAccount("1A2B3C")
                .relays(List.of(() -> null))
                .margin(new UnitInterval(BigInteger.ONE, BigInteger.TWO))
                .poolOwners(Set.of("1A2B3C"))
                .build();
        //when
        PoolRegistrationParams poolRegistrationParams = parsePoolRegistration(764824073L, poolRegistration);
        //then
        Assertions.assertEquals("1", poolRegistrationParams.getCost());
        Assertions.assertEquals("stake1uydzk0qg9zh3a", poolRegistrationParams.getRewardAddress());
        Assertions.assertEquals(1, poolRegistrationParams.getPoolOwners().size());
        Assertions.assertEquals("1", poolRegistrationParams.getPledge());
    }

    @Test
    void parseVoteMetadataToOperationWOVoteRegMetadataTest() {
        //when //then
        ApiException exception = assertThrows(ApiException.class,
                () -> parseVoteMetadataToOperation(1L, ""));

        assertEquals("Missing vote registration metadata", exception.getError().getMessage());
    }

    @Test
    void addRelayToPoolReLayOfTypeSingleHostAddrTest() {
        //given
        List<Relay> relays = new ArrayList<>();
        Inet4Address inet4Address = mock(Inet4Address.class);
        Inet6Address inet6Address = mock(Inet6Address.class);
        when(inet4Address.getHostAddress()).thenReturn("inet4Host");
        when(inet6Address.getHostAddress()).thenReturn("inet6Host");
        SingleHostAddr singleHostAddr = SingleHostAddr
                .builder()
                .ipv4(inet4Address)
                .ipv6(inet6Address)
                .build();
        //when
        addRelayToPoolReLayOfTypeSingleHostAddr(relays, singleHostAddr);
        //then
        assertEquals(1, relays.size());
        assertEquals("SINGLE_HOST_ADDR", relays.getFirst().getIpv4());
    }

    @Test
    void parseAssetWOAssetTest() {
        //when //then
        ApiException exception = assertThrows(ApiException.class,
                () -> parseAsset(List.of(), ""));
        assertEquals("Asset value is required for token asset", exception.getError().getMessage());
        assertEquals(4022, exception.getError().getCode());
    }

    @Test
    void parseAssetWithEmptyAssetTest() {
        //when //then
        ApiException exception = assertThrows(ApiException.class,
                () -> parseTokenAsset(List.of(new MultiAsset()), ""));
        assertEquals("Assets are required for output operation token bundle", exception.getError().getMessage());
        assertEquals(4021, exception.getError().getCode());
    }

    @Test
    void withdrawalToOperationTest() {
        //when //then
        Operation operation = parseWithdrawalToOperation("value", "hex", 1L, "address");
        assertEquals("withdrawal", operation.getType());
        assertEquals("", operation.getStatus());
        assertEquals("address", operation.getAccount().getAddress());
        assertEquals("value", operation.getAmount().getValue());
        assertEquals("hex", operation.getMetadata().getStakingCredential().getHexBytes());
    }

    @Test
    void transactionInputToOperationTest() {
        //when //then
        Operation operation = transactionInputToOperation(new TransactionInput("id", 1), 1L);
        assertEquals("input", operation.getType());
        assertEquals("", operation.getStatus());
        assertEquals(1L, operation.getOperationIdentifier().getIndex());
        assertEquals("id:1", operation.getCoinChange().getCoinIdentifier().getIdentifier());
        assertEquals("coin_spent", operation.getCoinChange().getCoinAction().getValue());
    }

    @Test
    void ownerAddressesFromPoolRegistrationPreviewNetworkTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .poolOwners(Set.of("0x4A6F686E446F65"))
                .build();
        //when
        List<String> ownerAddresses = getOwnerAddressesFromPoolRegistrations(Constants.PREVIEW, poolRegistration);
        //then
        assertThat(ownerAddresses)
                .hasSize(1)
                .element(0).isEqualTo("stake_test1up9x76rwg3hk2an50ek");
    }

    @Test
    void rewardAddressFromPoolRegistrationPreviewNebtworkTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .rewardAccount("0x4A6F686E446F65")
                .build();
        //when
        String rewardAddress = getRewardAddressFromPoolRegistration("preview", poolRegistration);
        //then
        assertThat(rewardAddress).isEqualTo("stake_test1up9x76rwg3hk2an50ek");
    }

    @Test
    void shouldThrowExceptionWhenIp4EmptyTest() {
        //when //then
        Exception exception = Assertions.assertThrows(UnknownHostException.class, () -> parseIpv4(""));
        assertEquals("Error Parsing IP Address", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIp6EmptyTest() {
        //when //then
        Exception exception = Assertions.assertThrows(UnknownHostException.class, () -> parseIpv6(""));
        assertEquals("Error Parsing IP Address", exception.getMessage());
    }

    @Test
    void checkStakeCredentialTest() {
        //when //then
        boolean checkStakeCredential = checkStakeCredential(new Operation());
        assertFalse(checkStakeCredential);
    }

    @Test
    void addRelayToPoolReLayOfTypeSingleHostNameTest() {
        //given
        List<Relay> relays = new ArrayList<>();
        SingleHostName singleHostName = new SingleHostName(8080, "dnsName");
        //when
        addRelayToPoolReLayOfTypeSingleHostName(relays, singleHostName);
        //then
        assertThat(relays)
                .hasSize(1)
                .element(0)
                .satisfies(relay -> {
                    assertThat(relay.getDnsName()).isEqualTo("dnsName");
                    assertThat(relay.getPort()).isEqualTo(8080);
                    assertThat(relay.getType()).isEqualTo("SINGLE_HOST_NAME");
                });
    }

    @Test
    void parsePoolMetadataTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .poolMetadataHash("metadataHash")
                .poolMetadataUrl("metadataUrl")
                .build();
        //when
        PoolMetadata poolMetadata = parsePoolMetadata(poolRegistration);
        //then
        assertThat(poolMetadata)
                .isNotNull()
                .satisfies(pm -> {
                    assertThat(pm.getHash()).isEqualTo("metadataHash");
                    assertThat(pm.getUrl()).isEqualTo("metadataUrl");
                });
    }

    @Test
    void multiHostRelayTest() {
        //given
        MultiHostName relay = new MultiHostName("dnsName");
        //when
        MultiHostName multiHostRelay = getMultiHostRelay(relay);
        //then
        assertThat(multiHostRelay).isNotNull()
                .extracting("dnsName")
                .isEqualTo("dnsName");
    }

    @Test
    void singleHostRelayTest() {
        //given
        SingleHostName relay = new SingleHostName(8080, "dnsName");
        //when
        SingleHostName singleHostName = getSingleHostName(relay);
        //then
        assertThat(singleHostName)
                .isNotNull()
                .satisfies(hostName -> {
                    assertThat(hostName.getDnsName()).isEqualTo("dnsName");
                    assertThat(hostName.getPort()).isEqualTo(8080);
                });
    }

    @Test
    void singleHostAddrTest() {
        //given
        SingleHostAddr relay = new SingleHostAddr(8080, null, null);
        //when
        SingleHostAddr singleHostAddr = getSingleHostAddr(relay);
        //then
        assertThat(singleHostAddr)
                .isNotNull()
                .extracting("port")
                .isEqualTo(8080);
    }

    @Test
    void addRelayToPoolRelayOfTypeMultiHostTest() {
        //given
        List<Relay> relays = new ArrayList<>();
        MultiHostName relay = new MultiHostName("dnsName");
        //when
        addRelayToPoolRelayOfTypeMultiHost(relays, relay);
        //then
        assertThat(relays)
                .hasSize(1)
                .element(0)
                .extracting("dnsName")
                .isEqualTo("dnsName");
    }

    @Test
    void shouldReturnNullWhenParseRewaedAccuntNetworkNullTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .rewardAccount("a".repeat(58))
                .build();
        //when
        String rewardAccount = parsePoolRewardAccount(123L, poolRegistration);
        //then
        assertThat(rewardAccount).isNull();
    }

    @Test
    void shouldNotAddToPoolOwnersWithWrongNetworkTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .poolOwners(Set.of("owner"))
                .build();
        //when
        List<String> poolOwners = parsePoolOwners(123L, poolRegistration);
        //then
        assertThat(poolOwners)
                .isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenUnknownNetworkTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .rewardAccount("a".repeat(58))
                .build();
        //when
        Exception exception = Assertions.assertThrows(ApiException.class,
                () -> getRewardAddressFromPoolRegistration("custom", poolRegistration));
        //then
        assertThat(exception)
                .extracting("error")
                .extracting("details")
                .extracting("message")
                .isEqualTo("Can't get Reward address from PoolRegistration");
    }

    @Test
    void shouldNotAddToOwnerAddressesFromPoolRegistrationsWithWrongNetworkTest() {
        //given
        PoolRegistration poolRegistration = PoolRegistration
                .builder()
                .poolOwners(Set.of("asdasd"))
                .build();
        //when
        List<String> poolOwners = getOwnerAddressesFromPoolRegistrations("custom", poolRegistration);
        //then
        assertThat(poolOwners)
                .isEmpty();
    }

    @Test
    void parsePoolCertToOperationPoolRetirementTest() throws CborException, CborSerializationException {
        Certificate certificate = PoolRetirement
                .builder()
                .epoch(1L)
                .build();
        //when
        Operation operation = parsePoolCertToOperation(1L, certificate , 1L,
                OperationType.POOL_RETIREMENT.getValue());
        //then
        assertEquals("", operation.getStatus());
        assertEquals(OperationType.POOL_RETIREMENT.getValue(), operation.getType());
        assertEquals(1, operation.getMetadata().getEpoch());
    }

    @Test
    void parsePoolCertToOperationTest() throws CborException, CborSerializationException {
        //given
        Certificate certificate = PoolRegistration
                .builder()
                .rewardAccount("4A6F686E")
                .pledge(new BigInteger("12"))
                .poolOwners(Set.of("4A6F686E"))
                .cost(new BigInteger("2"))
                .margin(new UnitInterval(new BigInteger("1"), new BigInteger("1")))
                .relays(List.of(new SingleHostName()))
                .build();
        //when
        Operation operation = parsePoolCertToOperation(1L, certificate , 1L,
                OperationType.POOL_REGISTRATION.getValue());
        //then
        assertEquals("", operation.getStatus());
        assertEquals(OperationType.POOL_REGISTRATION.getValue(), operation.getType());
        assertEquals("stake_test1up9x76rw39cxph", operation.getMetadata().getPoolRegistrationParams().getRewardAddress());
        assertEquals("12", operation.getMetadata().getPoolRegistrationParams().getPledge());
        assertEquals("2", operation.getMetadata().getPoolRegistrationParams().getCost());
        assertEquals(1, operation.getMetadata().getPoolRegistrationParams().getPoolOwners().size());
        assertEquals("1", operation.getMetadata().getPoolRegistrationParams().getMargin().getNumerator());
        assertEquals("1", operation.getMetadata().getPoolRegistrationParams().getMargin().getDenominator());
    }

}
