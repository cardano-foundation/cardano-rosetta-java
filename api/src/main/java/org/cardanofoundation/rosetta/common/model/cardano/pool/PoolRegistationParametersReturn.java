package org.cardanofoundation.rosetta.common.model.cardano.pool;

import java.math.BigInteger;

public record PoolRegistationParametersReturn (BigInteger cost, BigInteger pledge, BigInteger numerator, BigInteger denominator) {}
