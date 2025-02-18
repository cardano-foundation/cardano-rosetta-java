package org.cardanofoundation.rosetta.common.enumeration;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<NetworkEnum> findByName(String name) {
        return Arrays.stream(NetworkEnum.values()).filter(b -> b.name.equals(name)).findFirst();
    }

    public static Optional<NetworkEnum> findByProtocolMagic(long protocolMagic) {
        return Arrays.stream(NetworkEnum.values()).filter(b -> b.network.getProtocolMagic() == protocolMagic).findFirst();
    }

}
