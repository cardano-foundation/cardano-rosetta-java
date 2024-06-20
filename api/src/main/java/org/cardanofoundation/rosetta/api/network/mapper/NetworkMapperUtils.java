package org.cardanofoundation.rosetta.api.network.mapper;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import com.bloxbean.cardano.client.common.model.Network;
import org.mapstruct.Named;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.Peer;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.util.Constants;

@Component
public class NetworkMapperUtils {

  @Named("getPeerWithoutMetadata")
  public Peer getPeerWithoutMetadata(Peer peer) {
    return new Peer(peer.getPeerId(), null);
  }

  @Named("toNetworkIdentifier")
  public List<NetworkIdentifier> toNetworkIdentifier(Network network) {
    return List.of(new NetworkIdentifier()
        .blockchain(Constants.CARDANO)
        .network(Objects.requireNonNull(NetworkEnum.findByProtocolMagic(network.getProtocolMagic()))
            .getName()));
  }
}
