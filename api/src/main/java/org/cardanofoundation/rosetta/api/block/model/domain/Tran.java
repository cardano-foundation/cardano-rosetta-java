package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

/**
 * Cardano Transaction model.
 * Named so because of clash with the Transaction from the Rosetta API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tran {

  protected String hash;
  protected String blockHash;
  protected Long blockNo;
  protected String fee;
  protected Long size;
  protected Boolean validContract;
  protected Long scriptSize;
  protected List<Utxo> inputs;
  protected List<Utxo> outputs;
  protected List<StakeRegistration> stakeRegistrations;
  protected List<Delegation> delegations;
  protected List<PoolRegistration> poolRegistrations;
  protected List<PoolRetirement> poolRetirements;

  public static Tran fromTx(TxnEntity txnEntity) {
//    txnEntity.get
    return Tran.builder()
        .hash(txnEntity.getTxHash())
        .blockHash(txnEntity.getBlock().getHash())
        .blockNo(txnEntity.getBlock().getNumber())
        .fee(txnEntity.getFee().toString())
        .size(0L) // TODO
        .validContract(txnEntity.getInvalid())
        .scriptSize(0L) // TODO
        .inputs(txnEntity.getInputKeys().stream().map(Utxo::fromUtxoKey).toList())
        .outputs(txnEntity.getOutputKeys().stream().map(Utxo::fromUtxoKey).toList())
        .build();
  }
}
