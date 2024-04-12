package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;

/**
 * Cardano Transaction domain model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlockTx {

  protected String hash;
  protected String blockHash;
  protected Long blockNo;
  protected String fee;
  protected Long size;
  protected Long scriptSize;
  protected List<Utxo> inputs;
  protected List<Utxo> outputs;
  protected List<StakeRegistration> stakeRegistrations;
  protected List<Delegation> delegations;
  protected List<PoolRegistration> poolRegistrations;
  protected List<PoolRetirement> poolRetirements;
  protected List<Withdrawal> withdrawals;

}
