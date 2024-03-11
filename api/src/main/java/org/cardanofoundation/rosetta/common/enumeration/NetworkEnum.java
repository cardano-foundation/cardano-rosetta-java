package org.cardanofoundation.rosetta.common.enumeration;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import lombok.Getter;

@Getter
public enum NetworkEnum {

    MAINNET("mainnet", Networks.mainnet()),
    PREPROD("preprod", Networks.preprod()),
    TESTNET("testnet", Networks.testnet()),
    DEVNET("devnet", new Network(0b0000, 42));

    private final String value;
    private final Network network;

    NetworkEnum(String value, Network network) {
        this.value = value;
        this.network = network;
    }

    public final Network getNetwork() {
        return network;
    }

    public static NetworkEnum fromValue(String value) {
        for (NetworkEnum b : NetworkEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        return null;
    }

    public static NetworkEnum fromProtocolMagic(long protocolMagic) {
        for (NetworkEnum b : NetworkEnum.values()) {
            if (b.network.getProtocolMagic() == protocolMagic) {
                return b;
            }
        }
        return null;
    }

}
