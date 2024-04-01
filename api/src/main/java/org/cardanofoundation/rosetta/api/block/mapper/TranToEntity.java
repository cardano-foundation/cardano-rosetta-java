package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

@PersistenceMapper
@AllArgsConstructor
public class TranToEntity {

  final ModelMapper modelMapper;

  public Tran fromEntity(TxnEntity entity) {
    return Tran.builder()
        .hash(entity.getTxHash())
        .blockHash(entity.getBlock().getHash())
        .blockNo(entity.getBlock().getNumber())
        .fee(entity.getFee().toString())
        .size(0L) // TODO
        .validContract(entity.getInvalid())
        .scriptSize(0L) // TODO
        .inputs(entity.getInputKeys().stream().map(Utxo::fromUtxoKey).toList())
        .outputs(entity.getOutputKeys().stream().map(Utxo::fromUtxoKey).toList())
        .build();
  }

}
