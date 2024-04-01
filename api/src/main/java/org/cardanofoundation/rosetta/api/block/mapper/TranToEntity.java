package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

import static java.util.Optional.ofNullable;

@PersistenceMapper
@AllArgsConstructor
public class TranToEntity {

  final ModelMapper modelMapper;

  public Tran fromEntity(TxnEntity model) {

    return ofNullable(modelMapper.getTypeMap(TxnEntity.class, Tran.class))
        .orElseGet(() -> modelMapper.createTypeMap(TxnEntity.class, Tran.class))
        .addMappings(mapper -> {

          mapper.map(TxnEntity::getTxHash, Tran::setHash);
          mapper.map(tx -> tx.getBlock().getHash(), Tran::setBlockHash);
          mapper.map(tx -> tx.getBlock().getNumber(), Tran::setBlockNo);
          mapper.map(tx -> 0L, Tran::setSize); // TODO saa: why is this 0L?
          mapper.map(tx -> 0L, Tran::setScriptSize); // TODO why is this 0L?

          mapper.map(TxnEntity::getInvalid, Tran::setValidContract);

        })
        .setPostConverter(ctx -> {

            dest(ctx).setInputs(source(ctx).getInputKeys().stream().map(Utxo::fromUtxoKey).toList());
            dest(ctx).setOutputs(source(ctx).getOutputKeys().stream().map(Utxo::fromUtxoKey).toList());
            dest(ctx).setFee(source(ctx).getFee().toString());

            return dest(ctx);


        })
        .map(model);
  }

  private static TxnEntity source(MappingContext<TxnEntity, Tran> ctx) {
    return ctx.getSource();
  }

  private static Tran dest(MappingContext<TxnEntity, Tran> ctx) {
    return ctx.getDestination();
  }

}
