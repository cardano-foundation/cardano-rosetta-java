package org.cardanofoundation.rosetta.crawler.util;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import java.util.Optional;

public class CardanoNetworks {
    public static String sanitizeNetworkId(final String networkId, final Optional<Long> protocolMagic) throws IllegalArgumentException {
        if (networkId.equalsIgnoreCase("mainnet")) {
            if (protocolMagic.isPresent()) {
                throw new IllegalArgumentException("Protocol magic specified for mainnet. This is not allowed and would be ignored anyways. Check your config file.");
            } else {
                return "mainnet";
            }
        } else if (networkId.equalsIgnoreCase(String.valueOf(Networks.preprod().getProtocolMagic())) ||
                networkId.equalsIgnoreCase("preprod")) {
            if (protocolMagic.isPresent() && Networks.preprod().getProtocolMagic() != protocolMagic.get()) {
                throw new IllegalArgumentException("Protocol magic specified that does not match predefined preprod network protocol magic.");
            } else {
                return "preprod";
            }
        } else if (networkId.equalsIgnoreCase(String.valueOf(Networks.preview().getProtocolMagic())) ||
                networkId.equalsIgnoreCase("preview")) {
            if (protocolMagic.isPresent() && Networks.preview().getProtocolMagic() != protocolMagic.get()) {
                throw new IllegalArgumentException("Protocol magic specified that does not match predefined preview network protocol magic.");
            } else {
                return "preview";
            }
        } else if (networkId.equalsIgnoreCase(String.valueOf(Networks.testnet().getProtocolMagic())) ||
                networkId.equalsIgnoreCase("testnet")) {
            if (protocolMagic.isPresent() && Networks.testnet().getProtocolMagic() != protocolMagic.get()) {
                throw new IllegalArgumentException("Protocol magic specified that does not match predefined public testnet network protocol magic.");
            } else {
                return "testnet";
            }
        } else {
            return String.valueOf(protocolMagic.orElseThrow());
        }
    }

    public static Network fromNetworkId(final String networkId, final Optional<Long> protocolMagic) throws IllegalArgumentException {
        if (networkId.equalsIgnoreCase("mainnet")) {
            if (protocolMagic.isPresent()) {
                throw new IllegalArgumentException("Protocol magic specified for mainnet. This is not allowed and would be ignored anyways. Check your config file.");
            } else {
                return Networks.mainnet();
            }
        } else if (networkId.equalsIgnoreCase(String.valueOf(Networks.preprod().getProtocolMagic())) ||
                networkId.equalsIgnoreCase("preprod")) {
            if (protocolMagic.isPresent() && Networks.preprod().getProtocolMagic() != protocolMagic.get()) {
                throw new IllegalArgumentException("Protocol magic specified that does not match predefined preprod network protocol magic.");
            } else {
                return Networks.preprod();
            }
        } else if (networkId.equalsIgnoreCase(String.valueOf(Networks.preview().getProtocolMagic())) ||
                networkId.equalsIgnoreCase("preview")) {
            if (protocolMagic.isPresent() && Networks.preview().getProtocolMagic() != protocolMagic.get()) {
                throw new IllegalArgumentException("Protocol magic specified that does not match predefined preview network protocol magic.");
            } else {
                return Networks.preview();
            }
        } else if (networkId.equalsIgnoreCase(String.valueOf(Networks.testnet().getProtocolMagic())) ||
                networkId.equalsIgnoreCase("testnet")) {
            if (protocolMagic.isPresent() && Networks.testnet().getProtocolMagic() != protocolMagic.get()) {
                throw new IllegalArgumentException("Protocol magic specified that does not match predefined public testnet network protocol magic.");
            } else {
                return Networks.testnet();
            }
        } else {
            return new Network(0, protocolMagic.orElseThrow());
        }
    }
}
