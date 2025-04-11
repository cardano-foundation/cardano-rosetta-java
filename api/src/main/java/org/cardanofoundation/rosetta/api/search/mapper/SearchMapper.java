package org.cardanofoundation.rosetta.api.search.mapper;

import java.util.List;
import javax.annotation.Nullable;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsResponse;

import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface SearchMapper {

  @Mapping(target = "transactions", source = "transactions")
  @Mapping(target = "nextOffset", source = "nextOffset")
  @Mapping(target = "totalCount", source = "totalCount")
  SearchTransactionsResponse mapToSearchTransactionsResponse(List<BlockTransaction> transactions,
                                                             @Nullable Long nextOffset,
                                                             int totalCount);

}
