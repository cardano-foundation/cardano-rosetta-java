package org.cardanofoundation.rosetta.crawler.mapper;


import java.util.List;
import java.util.stream.Collectors;
import org.cardanofoundation.rosetta.crawler.model.NetworkStatus;
import org.cardanofoundation.rosetta.crawler.model.Peer;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockIdentifier;
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
          .genesisBlockIdentifier(BlockIdentifier.builder().index(genesisBlock.getNumber()).hash(genesisBlock.getHash()).build())
          .peers(peers.stream().map(peer -> new Peer(peer.getAddr())).collect(Collectors.toList()))
          .build();
  }

}





