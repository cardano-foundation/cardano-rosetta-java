package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnsignedTransaction {
    private String hash;
    private String bytes;
    private Set<String> addresses;
    private String metadata;
}
