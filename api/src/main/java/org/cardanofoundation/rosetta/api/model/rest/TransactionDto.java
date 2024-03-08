package org.cardanofoundation.rosetta.api.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.api.model.dto.*;
import org.cardanofoundation.rosetta.api.model.entity.TxnEntity;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionDto {

  protected String hash;
  protected String blockHash;
  protected Long blockNo;
  protected String fee;
  protected Long size;
  protected Boolean validContract;
  protected Long scriptSize;
  protected List<UtxoDto> inputs;
  protected List<UtxoDto> outputs;
  protected List<StakeRegistrationDTO> stakeRegistrations;
  protected List<DelegationDTO> delegations;
  protected List<PoolRegistrationDTO> poolRegistrations;
  protected List<PoolRetirementDTO> poolRetirements;

  public static TransactionDto fromTx(TxnEntity txnEntity) {
//    txnEntity.get
    return TransactionDto.builder()
                    .hash(txnEntity.getTxHash())
                    .blockHash(txnEntity.getBlock().getHash())
                    .blockNo(txnEntity.getBlock().getNumber())
                    .fee(txnEntity.getFee().toString())
                    .size(0L) // TODO
                    .validContract(txnEntity.getInvalid())
                    .scriptSize(0L) // TODO
                    .inputs(txnEntity.getInputKeys().stream().map(utxoKey -> UtxoDto.fromUtxoKey(utxoKey)).toList())
                    .outputs(txnEntity.getOutputKeys().stream().map(utxoKey -> UtxoDto.fromUtxoKey(utxoKey)).toList())
                    .build();
  }
}