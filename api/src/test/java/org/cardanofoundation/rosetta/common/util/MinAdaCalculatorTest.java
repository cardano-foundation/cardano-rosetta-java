package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MinAdaCalculatorTest {

    private static final String TEST_ADDRESS = "addr_test1vpqgspvmh6m2m5pwangvdg499srfzre2dd96qq57nlnw6yctpasy4";
    private static final BigInteger COINS_PER_UTXO_SIZE = BigInteger.valueOf(4310);

    @Nested
    class CalculateMinAda {

        @Test
        void pureAdaOutput_returnsCorrectMinAda() throws Exception {
            TransactionOutput output = new TransactionOutput(
                    TEST_ADDRESS,
                    Value.builder().coin(BigInteger.valueOf(1_000_000)).build());

            BigInteger minAda = MinAdaCalculator.calculateMinAda(output, COINS_PER_UTXO_SIZE);

            byte[] outputBytes = CborSerializationUtil.serialize(output.serialize());
            BigInteger expected = BigInteger.valueOf(outputBytes.length).multiply(COINS_PER_UTXO_SIZE);
            assertThat(minAda).isEqualTo(expected);
        }

        @Test
        void multiAssetOutput_returnsHigherMinAdaThanPureAda() throws Exception {
            TransactionOutput pureAdaOutput = new TransactionOutput(
                    TEST_ADDRESS,
                    Value.builder().coin(BigInteger.valueOf(1_000_000)).build());

            MultiAsset multiAsset = new MultiAsset(
                    "b0d07d45fe9514f80213f4020e5a61241458be626841cde717cb38a7",
                    List.of(new Asset("tokenA", BigInteger.valueOf(1000))));

            TransactionOutput multiAssetOutput = new TransactionOutput(
                    TEST_ADDRESS,
                    Value.builder()
                            .coin(BigInteger.valueOf(2_000_000))
                            .multiAssets(List.of(multiAsset))
                            .build());

            BigInteger pureAdaMin = MinAdaCalculator.calculateMinAda(pureAdaOutput, COINS_PER_UTXO_SIZE);
            BigInteger multiAssetMin = MinAdaCalculator.calculateMinAda(multiAssetOutput, COINS_PER_UTXO_SIZE);

            assertThat(multiAssetMin).isGreaterThan(pureAdaMin);
        }
    }
}
