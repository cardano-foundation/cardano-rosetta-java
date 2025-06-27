package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import co.nstant.in.cbor.model.Map;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.AccountIdentifierMetadata;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolMargin;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.SubAccountIdentifier;
import org.openapitools.client.model.TokenBundleItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.mapper.OperationToCborMap.convertToCborMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OperationToCborMapTest {

    @Test
    void convertToCborMapAndViseVersaTest() {
        //given
        AccountIdentifier accountIdentifier = new AccountIdentifier("address", new SubAccountIdentifier("address", null),
                new AccountIdentifierMetadata());
        PoolRegistrationParams poolRegistrationParams = PoolRegistrationParams
                .builder()
                .poolOwners(List.of("owner"))
                .margin(new PoolMargin("numerator", "denominator"))
                .relays(List.of(new Relay("ipv4", "ipv6", "name", 8080, "type")))
                .poolMetadata(new PoolMetadata("url", "hash"))
                .build();
        OperationMetadata operationMetadata = OperationMetadata.builder()
                .poolRegistrationParams(poolRegistrationParams)
                .refundAmount(new Amount("2", new Currency(Constants.ADA, 2, new CurrencyMetadata("policyId")), new Object()))
                .tokenBundle(List.of(new TokenBundleItem("tokenBundlePolicyId", List.of(new Amount()))))
                .build();

        Operation operation = Operation
                .builder()
                .operationIdentifier(new OperationIdentifier())
                .account(accountIdentifier)
                .type("poolRegistration")
                .metadata(operationMetadata)
                .build();
        //when
        Map map = convertToCborMap(operation);
        Operation opr = CborMapToOperation.cborMapToOperation(map);
        //then
        assertEquals(4, map.getKeys().size());
        assertEquals(operation.getAccount().getAddress(), opr.getAccount().getAddress());
        assertEquals(operation.getAccount().getSubAccount(), opr.getAccount().getSubAccount());
        assertEquals(operation.getOperationIdentifier(), opr.getOperationIdentifier());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getPoolOwners(), opr.getMetadata().getPoolRegistrationParams().getPoolOwners());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getRelays().getFirst().getDnsName(),
                opr.getMetadata().getPoolRegistrationParams().getRelays().getFirst().getDnsName());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getMargin(), opr.getMetadata().getPoolRegistrationParams().getMargin());
        assertEquals(operation.getMetadata().getPoolRegistrationParams().getPoolMetadata(), opr.getMetadata().getPoolRegistrationParams().getPoolMetadata());
    }

}
