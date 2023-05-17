package org.cardanofoundation.rosetta.crawler.mapper;


import java.util.List;
import java.util.stream.Collectors;
import org.cardanofoundation.rosetta.crawler.common.constants.Constants;
import org.cardanofoundation.rosetta.crawler.model.Network;
import org.cardanofoundation.rosetta.crawler.model.NetworkStatus;
import org.cardanofoundation.rosetta.crawler.model.Peer;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.crawler.projection.BlockDto;
import org.cardanofoundation.rosetta.crawler.projection.GenesisBlockDto;

public class DataMapper {

  public static NetworkStatusResponse mapToNetworkStatusResponse(NetworkStatus networkStatus) {
    BlockDto latestBlock = networkStatus.getLatestBlock();
    GenesisBlockDto genesisBlock = networkStatus.getGenesisBlock();
    List<Peer> peers = networkStatus.getPeers();
      return NetworkStatusResponse.builder()
          .currentBlockIdentifier(BlockIdentifier.builder().index(latestBlock.getNumber()).hash(latestBlock.getHash()).build())
          .currentBlockTimeStamp(latestBlock.getCreatedAt())
          .genesisBlockIdentifier(BlockIdentifier.builder().index(
                  genesisBlock.getNumber() != null ? genesisBlock.getNumber() : 0 )
          .hash(genesisBlock.getHash()).build())
          .peers(peers.stream().map(peer -> new Peer(peer.getAddr())).collect(Collectors.toList()))
          .build();
  }

  public static NetworkListResponse mapToNetworkListResponse(Network supportedNetwork) {
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(Constants.CARDANO).network(supportedNetwork.getNetworkId()).build();
    return NetworkListResponse.builder().networkIdentifiers(List.of(identifier)).build();
  }
}





