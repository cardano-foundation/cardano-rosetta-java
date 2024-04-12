package org.cardanofoundation.rosetta.yaciindexer.mapper;

import com.bloxbean.cardano.yaci.store.transaction.domain.Txn;
import com.bloxbean.cardano.yaci.store.transaction.domain.TxnWitness;
import com.bloxbean.cardano.yaci.store.transaction.domain.Withdrawal;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.mapper.TxnMapperImpl;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.model.TxnEntity;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.model.TxnWitnessEntity;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.model.WithdrawalEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CustomTxnMapper extends TxnMapperImpl {

  @Override
  public TxnEntity toTxnEntity(Txn txn) {
    return TxnEntity.builder()
        .txHash(txn.getTxHash())
        .blockHash(txn.getBlockHash())
        .blockNumber(txn.getBlockNumber())
        .inputs(txn.getInputs())
        .outputs(txn.getOutputs())
        .fee(txn.getFee())
        .collateralInputs(txn.getCollateralInputs())
        .build();
  }

  @Override
  public TxnWitnessEntity toTxnWitnessEntity(TxnWitness txnWitness) {
    // don't save anything
    return TxnWitnessEntity.builder().build();
  }

  @Override
  public WithdrawalEntity toWithdrawalEntity(Withdrawal withdrawal) {
    return WithdrawalEntity.builder()
        .address(withdrawal.getAddress())
        .amount(withdrawal.getAmount())
        .build();
  }
}
