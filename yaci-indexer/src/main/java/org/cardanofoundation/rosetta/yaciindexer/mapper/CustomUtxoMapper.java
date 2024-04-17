//package org.cardanofoundation.rosetta.yaciindexer.mapper;
//
//import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
//import com.bloxbean.cardano.yaci.store.utxo.storage.impl.mapper.UtxoMapperImpl_;
//import com.bloxbean.cardano.yaci.store.utxo.storage.impl.model.AddressUtxoEntity;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Component;
//
//@Component
//@Primary
//public class CustomUtxoMapper extends UtxoMapperImpl_ {
//
//  @Override
//  public AddressUtxoEntity toAddressUtxoEntity(AddressUtxo addressUtxo) {
//    return AddressUtxoEntity.builder()
//        .txHash(addressUtxo.getTxHash())
//        .outputIndex(addressUtxo.getOutputIndex())
//        .ownerAddr(addressUtxo.getOwnerAddr())
//        .amounts(addressUtxo.getAmounts().stream().map())
//        .slot(null)
//        .lovelaceAmount(null)
//        .ownerPaymentCredential(null)
//        .ownerStakeCredential(null)
//        .blockHash(null)
//        .blockNumber(null)
//        .blockTime(null)
//        .build();
//  }
//
//}
