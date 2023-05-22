package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardanoNetwork {
    @JsonIgnore
    private String name;
    private int networkId;
    private long networkMagic;

    public static final CardanoNetwork MAINNET = new CardanoNetwork("mainnet", 1, 764824073L);
    public static final CardanoNetwork TESTNET = new CardanoNetwork("testnet", 0, 1097911063L);

    public static CardanoNetwork networkFromName(final String name) {
        if (name.equalsIgnoreCase("mainnet")) {
            return MAINNET;
        } else if (name.equalsIgnoreCase("testnet")) {
            return TESTNET;
        } else {
            return null;
        }
    }
}
