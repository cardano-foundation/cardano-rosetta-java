package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

import static java.util.Optional.ofNullable;

@PersistenceMapper
@AllArgsConstructor
public class BlockTxToEntity {

  final ModelMapper modelMapper;

  public BlockTx fromEntity(TxnEntity model) {
    return ofNullable(modelMapper.getTypeMap(TxnEntity.class, BlockTx.class))
        .orElseGet(() -> modelMapper.createTypeMap(TxnEntity.class, BlockTx.class))
        .addMappings(mapper -> {

          mapper.map(TxnEntity::getTxHash, BlockTx::setHash);
          mapper.map(tx -> tx.getBlock().getHash(), BlockTx::setBlockHash);
          mapper.map(tx -> tx.getBlock().getNumber(), BlockTx::setBlockNo);
          mapper.map(tx -> 0L, BlockTx::setSize); // TODO saa: why is this 0L?
          mapper.map(tx -> 0L, BlockTx::setScriptSize); // TODO why is this 0L?

          mapper.map(TxnEntity::getInvalid, BlockTx::setValidContract);

        })
        .setPostConverter(ctx -> {
          TxnEntity source = ctx.getSource();
          BlockTx dest = ctx.getDestination();
          dest.setInputs(source.getInputKeys().stream().map(Utxo::fromUtxoKey).toList());
          dest.setOutputs(source.getOutputKeys().stream().map(Utxo::fromUtxoKey).toList());
          dest.setFee(Optional
              .ofNullable(source.getFee())
              .map(BigInteger::toString)
              .orElse(null));

          return dest;
        })
        .map(model);
  }
}
