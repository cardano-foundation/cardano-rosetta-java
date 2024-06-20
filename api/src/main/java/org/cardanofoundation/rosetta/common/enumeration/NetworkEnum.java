package org.cardanofoundation.rosetta.common.enumeration;

import lombok.Getter;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;

import org.cardanofoundation.rosetta.common.util.Constants;

@Getter
public enum NetworkEnum {

    MAINNET(Constants.MAINNET, Networks.mainnet()),
    PREPROD(Constants.PREPROD, Networks.preprod()),
    DEVNET(Constants.DEVKIT, new Network(0b0000, 42)),
    PREVIEW(Constants.PREVIEW, Networks.preview()),
    SANCHONET(Constants.SANCHONET, new Network(0b0000,  4));

    private final String name;
    private final Network network;

    NetworkEnum(String value, Network network) {
        this.name = value;
        this.network = network;
    }

    public final Network getNetwork() {
        return network;
    }

    public static NetworkEnum findByName(String name) {
        for (NetworkEnum b : NetworkEnum.values()) {
            if (b.name.equals(name)) {
                return b;
            }
        }
        return null;
    }

    public static NetworkEnum findByProtocolMagic(long protocolMagic) {
        for (NetworkEnum b : NetworkEnum.values()) {
            if (b.network.getProtocolMagic() == protocolMagic) {
                return b;
            }
        }
        return null;
    }

}
