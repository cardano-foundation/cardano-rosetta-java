package org.cardanofoundation.rosetta.api.account.model.domain;

import java.math.BigInteger;

import lombok.Builder;

@Builder
public record AddressBalance(String address,
                             String unit,
                             Long slot,
                             BigInteger quantity,
                             Long number) {
}
