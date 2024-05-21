package org.cardanofoundation.rosetta.common.util;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.spec.UnitInterval;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostAddr;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.Relay;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.addRelayToPoolReLayOfTypeSingleHostAddr;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseAsset;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolCertToOperation;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parsePoolRegistration;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseTokenAsset;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseVoteMetadataToOperation;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.parseWithdrawalToOperation;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.transactionInputToOperation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5778")
class ParseConstructionUtilTest {

    @Test
    void parsePoolCertToOperationWOCert() throws CborException, CborSerializationException {
        //when
        Operation operation = parsePoolCertToOperation(1, null, 1L, "type");
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
        PoolRegistrationParams poolRegistrationParams = parsePoolRegistration(1, poolRegistration);
        //then
        Assertions.assertEquals("1", poolRegistrationParams.getCost());
        Assertions.assertEquals("stake1uydzk0qg9zh3a", poolRegistrationParams.getRewardAddress());
        Assertions.assertEquals(1, poolRegistrationParams.getPoolOwners().size());
        Assertions.assertEquals("1", poolRegistrationParams.getPledge());
    }

    @Test
    void parseVoteMetadataToOperationWOVoteRegMetadataTest()  {
        //when //then
        ApiException exception = assertThrows(ApiException.class,
                () -> parseVoteMetadataToOperation(1L,""));

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

}
