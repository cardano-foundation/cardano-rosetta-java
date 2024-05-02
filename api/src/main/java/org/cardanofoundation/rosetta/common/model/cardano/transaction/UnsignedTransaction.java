package org.cardanofoundation.rosetta.common.model.cardano.transaction;


import java.util.Set;

public record UnsignedTransaction (String hash, String bytes, Set<String> addresses, String metadata){}
