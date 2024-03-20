package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

public record UnsignedTransaction (String hash, String bytes, Set<String> addresses, String metadata){}
