package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.cardanofoundation.rosetta.api.account.model.dto.UtxoDto;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Transaction {

  protected String hash;
  protected String blockHash;
  protected Long blockNo;
  protected String fee;
  protected Long size;
  protected Boolean validContract;
  protected Long scriptSize;
  protected List<UtxoDto> inputs;
  protected List<UtxoDto> outputs;
  protected List<StakeRegistration> stakeRegistrations;
  protected List<Delegation> delegations;
  protected List<PoolRegistration> poolRegistrations;
  protected List<PoolRetirement> poolRetirements;

  public static Transaction fromTx(TxnEntity txnEntity) {
//    txnEntity.get
    return Transaction.builder()
        .hash(txnEntity.getTxHash())
        .blockHash(txnEntity.getBlock().getHash())
        .blockNo(txnEntity.getBlock().getNumber())
        .fee(txnEntity.getFee().toString())
        .size(0L) // TODO
        .validContract(txnEntity.getInvalid())
        .scriptSize(0L) // TODO
        .inputs(
            txnEntity.getInputKeys().stream().map(utxoKey -> UtxoDto.fromUtxoKey(utxoKey)).toList())
        .outputs(txnEntity.getOutputKeys().stream().map(utxoKey -> UtxoDto.fromUtxoKey(utxoKey))
            .toList())
        .build();
  }
}