package org.cardanofoundation.rosetta.api.network.mapper;

import com.bloxbean.cardano.client.common.model.Network;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkStatusResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class, uses = {NetworkMapperUtils.class})
public interface NetworkMapper {

  @Mapping(target = "networkIdentifiers", source = "supportedNetwork", qualifiedByName = "toNetworkIdentifier")
  NetworkListResponse toNetworkListResponse(Network supportedNetwork);

  @Mapping(target = "currentBlockIdentifier.index", source = "latestBlock.number")
  @Mapping(target = "currentBlockIdentifier.hash", source = "latestBlock.hash")
  @Mapping(target = "syncStatus", source = "syncStatus")
  @Mapping(target = "currentBlockTimestamp",
      expression = "java(java.util.concurrent.TimeUnit.SECONDS.toMillis(networkStatus.getLatestBlock().getBlockTimeInSeconds()))")
  @Mapping(target = "genesisBlockIdentifier.index", source = "genesisBlock.number", defaultValue = "0L")
  @Mapping(target = "genesisBlockIdentifier.hash", source = "genesisBlock.hash")
  @Mapping(target = "peers", qualifiedByName = "getPeerWithoutMetadata")
  NetworkStatusResponse toNetworkStatusResponse(NetworkStatus networkStatus);
}
