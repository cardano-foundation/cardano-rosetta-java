package org.cardanofoundation.rosetta.consumer.mapper;


import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EpochParamMapper {
  @Mapping(target = "extraEntropy", source = "paramProposal.entropy")
  @Mapping(target = "epochNo", ignore = true)
  @Mapping(target = "id", ignore = true)
  void updateByParamProposal(@MappingTarget EpochParam epochParam, ParamProposal paramProposal);

  @Mapping(target = "extraEntropy", ignore = true)
  @Mapping(target = "epochNo", ignore = true)
  @Mapping(target = "id", ignore = true)
  void updateByEpochParam(@MappingTarget EpochParam epochParamTarget, EpochParam epochParamSource);
}
