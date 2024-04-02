package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

import static java.util.Optional.ofNullable;

@PersistenceMapper
@AllArgsConstructor
public class TranToEntity {

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

            dest(ctx).setInputs(source(ctx).getInputKeys().stream().map(Utxo::fromUtxoKey).toList());
            dest(ctx).setOutputs(source(ctx).getOutputKeys().stream().map(Utxo::fromUtxoKey).toList());
            dest(ctx).setFee(source(ctx).getFee().toString());

            return dest(ctx);


        })
        .map(model);
  }

  private static TxnEntity source(MappingContext<TxnEntity, BlockTx> ctx) {
    return ctx.getSource();
  }

  private static BlockTx dest(MappingContext<TxnEntity, BlockTx> ctx) {
    return ctx.getDestination();
  }

}
