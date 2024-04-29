package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

@PersistenceMapper
@AllArgsConstructor
public class BlockTxToEntity {

  final ModelMapper modelMapper;

  public BlockTx fromEntity(TxnEntity model) {
    return modelMapper.typeMap(TxnEntity.class, BlockTx.class)
        .addMappings(mapper -> {
          mapper.map(TxnEntity::getTxHash, BlockTx::setHash);
          mapper.map(tx -> tx.getBlock().getHash(), BlockTx::setBlockHash);
          mapper.map(tx -> tx.getBlock().getNumber(), BlockTx::setBlockNo);
          mapper.map(tx -> 0L, BlockTx::setSize); // will be calcualted, within the population method
          mapper.map(tx -> 0L, BlockTx::setScriptSize); // TODO Needs to be calulated if needed
        })
        .setPostConverter(ctx -> {
          TxnEntity source = ctx.getSource();
          BlockTx dest = ctx.getDestination();
          dest.setInputs(source.getInputKeys().stream()
              .map(utxoKey -> modelMapper.map(utxoKey, Utxo.class)).toList());
          dest.setOutputs(source.getOutputKeys().stream()
              .map(utxoKey -> modelMapper.map(utxoKey, Utxo.class)).toList());
          dest.setFee(Optional
              .ofNullable(source.getFee())
              .map(BigInteger::toString)
              .orElse(null));
          return dest;
        })
        .map(model);
  }
}
