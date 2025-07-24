package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import co.nstant.in.cbor.model.UnsignedInteger;
import org.assertj.core.api.Assertions;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import java.util.Collections;
import java.util.List;


/**
 * Unit tests for the {@link CborMapToOperation} class.
 * These tests validate that a CBOR Map is correctly transformed into a Rosetta Operation object.
 */
class CborMapToOperationTest {

    /**
     * Helper method to create a UnicodeString key, mimicking the behavior of Formatters.key().
     * This improves the readability of test data setup.
     * @param key The string key.
     * @return A CBOR UnicodeString.
     */
    private UnicodeString key(String key) {
        return new UnicodeString(key);
    }

    @Test
    void cborMapToOperation_shouldReturnEmptyOperation_whenMapIsEmpty() {
        // Arrange
        Map operationMap = new Map();
        Operation expectedOperation = new Operation();

        // Act
        Operation actualOperation = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actualOperation)
                .usingRecursiveComparison()
                .isEqualTo(expectedOperation);
    }

    @Test
    void cborMapToOperation_shouldMapOperationIdentifier() {
        // Arrange
        Map operationMap = new Map();
        Map operationIdentifierMap = new Map();
        operationIdentifierMap.put(key(Constants.INDEX), new UnsignedInteger(1L));
        operationIdentifierMap.put(key(Constants.NETWORK_INDEX), new UnsignedInteger(2L));
        operationMap.put(key(Constants.OPERATION_IDENTIFIER), operationIdentifierMap);

        Operation expected = new Operation();
        expected.setOperationIdentifier(new OperationIdentifier(1L, 2L));

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapRelatedOperations() {
        // Arrange
        Map operationMap = new Map();
        Array relatedOperationsArray = new Array();
        Map relatedOp1Map = new Map();
        relatedOp1Map.put(key(Constants.INDEX), new UnsignedInteger(10L));
        Map relatedOp2Map = new Map();
        relatedOp2Map.put(key(Constants.INDEX), new UnsignedInteger(11L));
        relatedOp2Map.put(key(Constants.NETWORK_INDEX), new UnsignedInteger(5L));
        relatedOperationsArray.add(relatedOp1Map);
        relatedOperationsArray.add(relatedOp2Map);
        operationMap.put(key(Constants.RELATED_OPERATION), relatedOperationsArray);

        Operation expected = new Operation();
        expected.setRelatedOperations(List.of(
                new OperationIdentifier(10L, null),
                new OperationIdentifier(11L, 5L)
        ));

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapTypeAndStatus() {
        // Arrange
        Map operationMap = new Map();
        operationMap.put(key(Constants.TYPE), new UnicodeString("input"));
        operationMap.put(key(Constants.STATUS), new UnicodeString("success"));

        Operation expected = new Operation();
        expected.setType("input");
        expected.setStatus("success");

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapAmountWithCurrencyAndPolicyId() {
        // Arrange
        Map operationMap = new Map();
        Map amountMap = new Map();
        amountMap.put(key(Constants.VALUE), new UnicodeString("1000000"));

        Map currencyMap = new Map();
        currencyMap.put(key(Constants.SYMBOL), new UnicodeString("tADA"));
        currencyMap.put(key(Constants.DECIMALS), new UnsignedInteger(6));

        Map currencyMetadataMap = new Map();
        currencyMetadataMap.put(key(Constants.POLICYID), new UnicodeString("policy123"));
        currencyMap.put(key(Constants.METADATA), currencyMetadataMap);

        amountMap.put(key(Constants.CURRENCY), currencyMap);
        operationMap.put(key(Constants.AMOUNT), amountMap);

        Operation expected = new Operation();
        Currency currency = new Currency("tADA", 6, null);
        currency.setMetadata(new CurrencyMetadata().policyId("policy123"));
        Amount amount = new Amount("1000000", currency, null);
        expected.setAmount(amount);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapCoinChange() {
        // Arrange
        Map operationMap = new Map();
        Map coinChangeMap = new Map();
        coinChangeMap.put(key(Constants.COIN_ACTION), new UnicodeString(CoinAction.SPENT.getValue()));

        Map coinIdentifierMap = new Map();
        coinIdentifierMap.put(key(Constants.IDENTIFIER), new UnicodeString("tx1_hash:0"));
        coinChangeMap.put(key(Constants.COIN_IDENTIFIER), coinIdentifierMap);

        operationMap.put(key(Constants.COIN_CHANGE), coinChangeMap);

        Operation expected = new Operation();
        CoinChange coinChange = new CoinChange();
        coinChange.setCoinAction(CoinAction.SPENT);
        coinChange.setCoinIdentifier(new CoinIdentifier("tx1_hash:0"));
        expected.setCoinChange(coinChange);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapMetadataWithSimpleFields() {
        // Arrange
        Map operationMap = new Map();
        Map metadataMap = new Map();
        metadataMap.put(key(Constants.EPOCH), new UnsignedInteger(100));
        metadataMap.put(key(Constants.POOL_KEY_HASH), new UnicodeString("pool_hash_123"));
        metadataMap.put(key(Constants.POOL_REGISTRATION_CERT), new UnicodeString("cert_cbor_hex"));
        operationMap.put(key(Constants.METADATA), metadataMap);

        Operation expected = new Operation();
        OperationMetadata metadata = new OperationMetadata();
        metadata.setEpoch(100);
        metadata.setPoolKeyHash("pool_hash_123");
        metadata.setPoolRegistrationCert("cert_cbor_hex");
        expected.setMetadata(metadata);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapMetadataWithAmountFields() {
        // Arrange
        Map operationMap = new Map();
        Map metadataMap = new Map();
        Currency ada = new Currency("ADA", 6, null);

        // Withdrawal Amount
        Map withdrawalAmountMap = new Map();
        withdrawalAmountMap.put(key(Constants.VALUE), new UnicodeString("100"));
        withdrawalAmountMap.put(key(Constants.CURRENCY), fromCurrency(ada));
        metadataMap.put(key(Constants.WITHDRAWALAMOUNT), withdrawalAmountMap);

        // Deposit Amount
        Map depositAmountMap = new Map();
        depositAmountMap.put(key(Constants.VALUE), new UnicodeString("200"));
        depositAmountMap.put(key(Constants.CURRENCY), fromCurrency(ada));
        metadataMap.put(key(Constants.DEPOSITAMOUNT), depositAmountMap);

        // Refund Amount
        Map refundAmountMap = new Map();
        refundAmountMap.put(key(Constants.VALUE), new UnicodeString("50"));
        refundAmountMap.put(key(Constants.CURRENCY), fromCurrency(ada));
        metadataMap.put(key(Constants.REFUNDAMOUNT), refundAmountMap);

        operationMap.put(key(Constants.METADATA), metadataMap);


        Operation expected = new Operation();
        OperationMetadata metadata = new OperationMetadata();
        metadata.setWithdrawalAmount(new Amount("100", ada, null));
        metadata.setDepositAmount(new Amount("200", ada, null));
        metadata.setRefundAmount(new Amount("50", ada, null));
        expected.setMetadata(metadata);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapStakingCredential() {
        // Arrange
        Map operationMap = new Map();
        Map metadataMap = new Map();
        Map stakingCredentialMap = new Map();
        stakingCredentialMap.put(key(Constants.HEX_BYTES), new UnicodeString("hex_bytes_123"));
        stakingCredentialMap.put(key(Constants.CURVE_TYPE), new UnicodeString(CurveType.EDWARDS25519.getValue()));
        metadataMap.put(key(Constants.STAKING_CREDENTIAL), stakingCredentialMap);
        operationMap.put(key(Constants.METADATA), metadataMap);

        Operation expected = new Operation();
        OperationMetadata metadata = new OperationMetadata();
        PublicKey publicKey = new PublicKey("hex_bytes_123", CurveType.EDWARDS25519);
        metadata.setStakingCredential(publicKey);
        expected.setMetadata(metadata);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapTokenBundle() {
        // Arrange
        Map operationMap = new Map();
        Map metadataMap = new Map();
        Array tokenBundleArray = new Array();

        // First Token Bundle Item
        Map item1Map = new Map();
        item1Map.put(key(Constants.POLICYID), new UnicodeString("policy1"));
        Array tokens1Array = new Array();
        Map amount1Wrapper = new Map();
        amount1Wrapper.put(key(Constants.AMOUNT), fromAmount(new Amount("10", new Currency("tokenA", 0, null), null)));
        tokens1Array.add(amount1Wrapper);
        item1Map.put(key(Constants.TOKENS), tokens1Array);
        tokenBundleArray.add(item1Map);

        // Second Token Bundle Item
        Map item2Map = new Map();
        item2Map.put(key(Constants.POLICYID), new UnicodeString("policy2"));
        Array tokens2Array = new Array();
        Map amount2Wrapper = new Map();
        amount2Wrapper.put(key(Constants.AMOUNT), fromAmount(new Amount("20", new Currency("tokenB", 0, null), null)));
        Map amount3Wrapper = new Map();
        amount3Wrapper.put(key(Constants.AMOUNT), fromAmount(new Amount("30", new Currency("tokenC", 0, null), null)));
        tokens2Array.add(amount2Wrapper);
        tokens2Array.add(amount3Wrapper);
        item2Map.put(key(Constants.TOKENS), tokens2Array);
        tokenBundleArray.add(item2Map);

        metadataMap.put(key(Constants.TOKEN_BUNDLE), tokenBundleArray);
        operationMap.put(key(Constants.METADATA), metadataMap);

        Operation expected = new Operation();
        OperationMetadata metadata = new OperationMetadata();
        TokenBundleItem item1 = new TokenBundleItem();
        item1.setPolicyId("policy1");
        item1.setTokens(Collections.singletonList(new Amount("10", new Currency("tokenA", 0, null), null)));

        TokenBundleItem item2 = new TokenBundleItem();
        item2.setPolicyId("policy2");
        item2.setTokens(List.of(
                new Amount("20", new Currency("tokenB", 0, null), null),
                new Amount("30", new Currency("tokenC", 0, null), null)
        ));
        metadata.setTokenBundle(List.of(item1, item2));
        expected.setMetadata(metadata);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapPoolRegistrationParams() {
        // Arrange
        Map operationMap = new Map();
        Map metadataMap = new Map();
        Map poolParamsMap = new Map();

        poolParamsMap.put(key(Constants.VRF_KEY_HASH), new UnicodeString("vrf_hash"));
        poolParamsMap.put(key(Constants.REWARD_ADDRESS), new UnicodeString("reward_addr"));
        poolParamsMap.put(key(Constants.PLEDGE), new UnicodeString("1000000"));
        poolParamsMap.put(key(Constants.COST), new UnicodeString("340000000"));

        Array ownersArray = new Array();
        ownersArray.add(new UnicodeString("owner1_hex"));
        ownersArray.add(new UnicodeString("owner2_hex"));
        poolParamsMap.put(key(Constants.POOL_OWNERS), ownersArray);

        Map marginMap = new Map();
        marginMap.put(key(Constants.NUMERATOR), new UnicodeString("1"));
        marginMap.put(key(Constants.DENOMINATOR), new UnicodeString("10"));
        poolParamsMap.put(key(Constants.MARGIN), marginMap);

        poolParamsMap.put(key(Constants.MARGIN_PERCENTAGE), new UnicodeString("10.0"));

        Map poolMetadataMap = new Map();
        poolMetadataMap.put(key(Constants.URL), new UnicodeString("http://pool.io"));
        poolMetadataMap.put(key(Constants.HASH), new UnicodeString("pool_metadata_hash"));
        poolParamsMap.put(key(Constants.POOL_METADATA), poolMetadataMap);

        Array relaysArray = new Array();
        Map relayMap = new Map();
        relayMap.put(key(Constants.TYPE), new UnicodeString("single_host_addr"));
        relayMap.put(key(Constants.IPV4), new UnicodeString("127.0.0.1"));
        relaysArray.add(relayMap);
        poolParamsMap.put(key(Constants.RELAYS), relaysArray);

        metadataMap.put(key(Constants.POOL_REGISTRATION_PARAMS), poolParamsMap);
        operationMap.put(key(Constants.METADATA), metadataMap);

        // Expected
        Operation expected = new Operation();
        OperationMetadata metadata = new OperationMetadata();
        PoolRegistrationParams params = new PoolRegistrationParams();
        params.setVrfKeyHash("vrf_hash");
        params.setRewardAddress("reward_addr");
        params.setPledge("1000000");
        params.setCost("340000000");
        params.setPoolOwners(List.of("owner1_hex", "owner2_hex"));
        params.setMargin(new PoolMargin().numerator("1").denominator("10"));
        params.setMarginPercentage("10.0");
        params.setPoolMetadata(new PoolMetadata().url("http://pool.io").hash("pool_metadata_hash"));
        Relay relay = new Relay();
        relay.setType("single_host_addr");
        relay.setIpv4("127.0.0.1");
        params.setRelays(List.of(relay));
        metadata.setPoolRegistrationParams(params);
        expected.setMetadata(metadata);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void cborMapToOperation_shouldMapPoolGovernanceVoteParamsWithGovActionId() {
        // Arrange
        Map operationMap = new Map();
        Map metadataMap = new Map();
        Map poolGovernanceVoteParamsMap = new Map();
        
        // Test the concatenated governance action string: 64-char tx_id + 2-char hex index
        String testTxId = "abc123def456789012345678901234567890123456789012345678901234abcd";
        String testIndexHex = "0a"; // hex for 10
        String concatenatedGovAction = testTxId + testIndexHex;
        
        poolGovernanceVoteParamsMap.put(key(Constants.GOVERNANCE_ACTION_HASH), new UnicodeString(concatenatedGovAction));
        
        metadataMap.put(key("poolGovernanceVoteParams"), poolGovernanceVoteParamsMap);
        operationMap.put(key(Constants.METADATA), metadataMap);

        Operation expected = new Operation();
        OperationMetadata metadata = new OperationMetadata();
        PoolGovernanceVoteParams poolGovVoteParams = new PoolGovernanceVoteParams();
        
        // The method should reconstruct this format using GovActionParamsUtil.formatGovActionString
        String expectedGovActionHash = org.cardanofoundation.rosetta.common.util.GovActionParamsUtil
                .formatGovActionString(testTxId, 10);
        poolGovVoteParams.setGovernanceActionHash(expectedGovActionHash);
        metadata.setPoolGovernanceVoteParams(poolGovVoteParams);
        expected.setMetadata(metadata);

        // Act
        Operation actual = CborMapToOperation.cborMapToOperation(operationMap);

        // Assert
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void addGovActionId_shouldParseGovernanceActionString() {
        // Arrange
        Map metadataMap = new Map();
        String testTxId = "abcdef12345678901234567890123456789012345678901234567890123456ef"; // 64 chars
        String testIndexHex = "63"; // hex for 99 (max allowed)
        String concatenatedGovAction = testTxId + testIndexHex;
        
        metadataMap.put(key(Constants.GOVERNANCE_ACTION_HASH), new UnicodeString(concatenatedGovAction));
        
        PoolGovernanceVoteParams poolGovVoteParams = new PoolGovernanceVoteParams();

        // Act
        CborMapToOperation.addGovActionId(metadataMap, poolGovVoteParams);

        // Assert
        String expectedGovActionHash = org.cardanofoundation.rosetta.common.util.GovActionParamsUtil
                .formatGovActionString(testTxId, 99);
        Assertions.assertThat(poolGovVoteParams.getGovernanceActionHash())
                .isEqualTo(expectedGovActionHash);
    }

    @Test
    void addGovActionId_shouldParseRealGovernanceActionString() {
        // Arrange
        Map metadataMap = new Map();
        String realGovActionString = "40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00";
        String expectedTxId = "40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc";
        String expectedIndexHex = "00";
        int expectedIndex = 0; // hex "00" = decimal 0
        
        metadataMap.put(key(Constants.GOVERNANCE_ACTION_HASH), new UnicodeString(realGovActionString));
        
        PoolGovernanceVoteParams poolGovVoteParams = new PoolGovernanceVoteParams();

        // Act
        CborMapToOperation.addGovActionId(metadataMap, poolGovVoteParams);

        // Assert
        String expectedGovActionHash = org.cardanofoundation.rosetta.common.util.GovActionParamsUtil
                .formatGovActionString(expectedTxId, expectedIndex);
        Assertions.assertThat(poolGovVoteParams.getGovernanceActionHash())
                .isEqualTo(expectedGovActionHash);
        
        // Additional verification - ensure the parsed components are correct
        Assertions.assertThat(expectedTxId).hasSize(64);
        Assertions.assertThat(expectedIndex).isEqualTo(0);
    }

    @Test
    void addGovActionId_shouldIgnoreInvalidLengthGovernanceActionString_tooShort() {
        // Arrange
        Map metadataMap = new Map();
        String invalidGovActionString = "40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9"; // 63 chars
        
        metadataMap.put(key(Constants.GOVERNANCE_ACTION_HASH), new UnicodeString(invalidGovActionString));
        
        PoolGovernanceVoteParams poolGovVoteParams = new PoolGovernanceVoteParams();

        // Act
        CborMapToOperation.addGovActionId(metadataMap, poolGovVoteParams);

        // Assert - governance action hash should remain null since invalid length was ignored
        Assertions.assertThat(poolGovVoteParams.getGovernanceActionHash()).isNull();
    }

    @Test
    void addGovActionId_shouldIgnoreInvalidLengthGovernanceActionString_tooLong() {
        // Arrange
        Map metadataMap = new Map();
        String invalidGovActionString = "40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc000"; // 67 chars
        
        metadataMap.put(key(Constants.GOVERNANCE_ACTION_HASH), new UnicodeString(invalidGovActionString));
        
        PoolGovernanceVoteParams poolGovVoteParams = new PoolGovernanceVoteParams();

        // Act
        CborMapToOperation.addGovActionId(metadataMap, poolGovVoteParams);

        // Assert - governance action hash should remain null since invalid length was ignored
        Assertions.assertThat(poolGovVoteParams.getGovernanceActionHash()).isNull();
    }

    @Test
    void addGovActionId_shouldIgnoreEmptyGovernanceActionString() {
        // Arrange
        Map metadataMap = new Map();
        String emptyGovActionString = "";
        
        metadataMap.put(key(Constants.GOVERNANCE_ACTION_HASH), new UnicodeString(emptyGovActionString));
        
        PoolGovernanceVoteParams poolGovVoteParams = new PoolGovernanceVoteParams();

        // Act
        CborMapToOperation.addGovActionId(metadataMap, poolGovVoteParams);

        // Assert - governance action hash should remain null since empty string was ignored
        Assertions.assertThat(poolGovVoteParams.getGovernanceActionHash()).isNull();
    }

    // Helper methods to convert Rosetta models back to CBOR DataItems for test setup
    private Map fromCurrency(Currency currency) {
        Map currencyMap = new Map();
        currencyMap.put(key(Constants.SYMBOL), new UnicodeString(currency.getSymbol()));
        currencyMap.put(key(Constants.DECIMALS), new UnsignedInteger(currency.getDecimals()));
        if (currency.getMetadata() != null && currency.getMetadata().getPolicyId() != null) {
            Map metadataMap = new Map();
            metadataMap.put(key(Constants.POLICYID), new UnicodeString(currency.getMetadata().getPolicyId()));
            currencyMap.put(key(Constants.METADATA), metadataMap);
        }
        return currencyMap;
    }

    private Map fromAmount(Amount amount) {
        Map amountMap = new Map();
        amountMap.put(key(Constants.VALUE), new UnicodeString(amount.getValue()));
        amountMap.put(key(Constants.CURRENCY), fromCurrency(amount.getCurrency()));

        return amountMap;
    }

}
