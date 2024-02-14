package org.cardanofoundation.rosetta.api.mapper;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.model.NetworkStatus;
import org.cardanofoundation.rosetta.api.model.Peer;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component

public class DataMapper {

  public static final String COIN_SPENT_ACTION = "coin_spent";
  public static final String COIN_CREATED_ACTION = "coin_created";

  private DataMapper() {

  }

  public static NetworkListResponse mapToNetworkListResponse(Network supportedNetwork) {
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(Constants.CARDANO)
            .network(supportedNetwork.getNetworkId()).build();
    return NetworkListResponse.builder().networkIdentifiers(List.of(identifier)).build();
  }

  public static NetworkStatusResponse mapToNetworkStatusResponse(NetworkStatus networkStatus) {
    BlockDto latestBlock = networkStatus.getLatestBlock();
    GenesisBlockDto genesisBlock = networkStatus.getGenesisBlock();
    List<Peer> peers = networkStatus.getPeers();
    return NetworkStatusResponse.builder()
            .currentBlockIdentifier(
                    BlockIdentifier.builder().index(latestBlock.getNumber()).hash(latestBlock.getHash())
                            .build())
            .currentBlockTimeStamp(latestBlock.getCreatedAt())
            .genesisBlockIdentifier(BlockIdentifier.builder().index(
                            genesisBlock.getNumber() != null ? genesisBlock.getNumber() : 0)
                    .hash(genesisBlock.getHash()).build())
            .peers(peers.stream().map(peer -> new Peer(peer.getAddr())).toList())
            .build();
  }

}





