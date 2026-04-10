package org.cardanofoundation.rosetta.common.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MinAdaCalculatorTest {

    @Nested
    class CalculateMinAda {
        @Test
        void testCalculateMinAdaPureAda() {
            TransactionOutput output = new TransactionOutput(
                    "addr_test1vpqgspvmh6m2m5pwangvdg499srfzre2dd96qq57nlnw6yctpasy4",
                    Value.builder().coin(BigInteger.valueOf(1000000)).build());

            BigInteger minAda = MinAdaCalculator.calculateMinAda(output, BigInteger.valueOf(4310));
            assertTrue(minAda.compareTo(BigInteger.ZERO) > 0);
        }
    }
}
