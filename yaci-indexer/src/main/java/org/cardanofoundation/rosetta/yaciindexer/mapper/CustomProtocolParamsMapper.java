//package org.cardanofoundation.rosetta.yaciindexer.mapper;
//
//import com.bloxbean.cardano.yaci.core.model.Epoch;
//import com.bloxbean.cardano.yaci.store.epoch.domain.EpochParam;
//import com.bloxbean.cardano.yaci.store.epoch.domain.ProtocolParamsProposal;
//import com.bloxbean.cardano.yaci.store.epoch.storage.impl.mapper.ProtocolParamsMapper;
//import com.bloxbean.cardano.yaci.store.epoch.storage.impl.mapper.ProtocolParamsMapperImpl;
//import com.bloxbean.cardano.yaci.store.epoch.storage.impl.model.EpochParamEntity;
//import com.bloxbean.cardano.yaci.store.epoch.storage.impl.model.ProtocolParamsProposalEntity;
//
//public class CustomProtocolParamsMapper extends ProtocolParamsMapperImpl {
//
//  @Override
//  public ProtocolParamsProposalEntity toEntity(ProtocolParamsProposal protocolParamsProposal) {
//    return super.toEntity(protocolParamsProposal);
//  }
//
//  @Override
//  public EpochParamEntity toEntity(EpochParam epochParam) {
//    return EpochParamEntity.builder()
//        .epoch(epochParam.getEpoch())
//        .params(epochParam.getParams())
//        .build();
//  }
//}
