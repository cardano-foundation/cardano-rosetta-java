package org.cardanofoundation.rosetta.api.service.utils;

import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;

public class Common {
  public static NetworkRequest generateNetworkPayload(String blockchain , String network){
    return NetworkRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder().blockchain(blockchain).network(network).build())
        .build();
  }
}
