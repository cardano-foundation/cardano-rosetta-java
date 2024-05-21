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
        .setPostConverter(ctx -> {
          TxnEntity source = ctx.getSource();
          BlockTx dest = ctx.getDestination();

          dest.setBlockNo(source.getBlock().getNumber());
          dest.setHash(source.getTxHash());
          dest.setBlockHash(source.getBlock().getHash());
          dest.setSize(0L);
          dest.setScriptSize(0L); //calculated later

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
