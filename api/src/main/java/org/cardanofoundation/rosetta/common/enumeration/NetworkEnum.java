package org.cardanofoundation.rosetta.common.enumeration;

import lombok.Getter;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;

import org.cardanofoundation.rosetta.common.util.Constants;

@Getter
public enum NetworkEnum {

    MAINNET(Constants.MAINNET, Networks.mainnet()),
    PREPROD(Constants.PREPROD, Networks.preprod()),
    TESTNET(Constants.TESTNET, Networks.testnet()),
    DEVNET(Constants.DEVKIT, new Network(0b0000, 42));

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
