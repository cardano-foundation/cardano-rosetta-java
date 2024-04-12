package org.cardanofoundation.rosetta.yaciindexer.mapper;

import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.mapper.UtxoMapperImpl_;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.model.AddressUtxoEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CustomUtxoMapper extends UtxoMapperImpl_ {

  @Override
  public AddressUtxoEntity toAddressUtxoEntity(AddressUtxo addressUtxo) {
    return AddressUtxoEntity.builder()
        .txHash(addressUtxo.getTxHash())
        .outputIndex(addressUtxo.getOutputIndex())
        .blockHash(addressUtxo.getBlockHash())
        .blockNumber(addressUtxo.getBlockNumber())
        .blockTime(addressUtxo.getBlockTime())
        .ownerAddr(addressUtxo.getOwnerAddr())
        .lovelaceAmount(addressUtxo.getLovelaceAmount())
        .amounts(addressUtxo.getAmounts())
        .build();
  }

}
