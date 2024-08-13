package org.cardanofoundation.rosetta.api.search.mapper;

import java.util.List;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsResponse;

@Mapper(config = BaseMapper.class)
public interface SearchMapper {

  @Mapping(target = "transactions", source = "transactionList")
  @Mapping(target = "nextOffset", source = "nextOffset")
  @Mapping(target = "totalCount", expression = "java((long) transactionList.size())")
  SearchTransactionsResponse mapToSearchTransactionsResponse(List<BlockTransaction> transactionList, Long nextOffset);

}
