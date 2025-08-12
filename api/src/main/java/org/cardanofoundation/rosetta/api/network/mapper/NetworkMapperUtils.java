package org.cardanofoundation.rosetta.api.network.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import com.bloxbean.cardano.client.common.model.Network;
import org.mapstruct.Named;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.Peer;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.util.Constants;

@Component
public class NetworkMapperUtils {

  @Named("getPeerWithMetadata")
  public Peer getPeerWithetadata(Peer peer) {
    return new Peer(peer.getPeerId(), peer.getMetadata());
  }

  @Named("toNetworkIdentifier")
  public List<NetworkIdentifier> toNetworkIdentifier(Network network) {
    return NetworkEnum.findByProtocolMagic(network.getProtocolMagic())
            .map(n -> List.of(new NetworkIdentifier()
                    .blockchain(Constants.CARDANO)
                    .network(n.getName())))
            .orElseGet(List::of);
  }

}
