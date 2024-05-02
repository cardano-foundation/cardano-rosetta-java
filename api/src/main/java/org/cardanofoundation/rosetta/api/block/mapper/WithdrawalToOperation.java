package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.Withdrawal;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

@OpenApiMapper
@AllArgsConstructor
public class WithdrawalToOperation extends AbstractToOperation<Withdrawal>{

  private final ModelMapper modelMapper;

  @Override
  public Operation toDto(Withdrawal model, OperationStatus status, int index) {
    return modelMapper.typeMap(Withdrawal.class, Operation.class)
        .addMappings(mp -> {
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.map(f -> OperationType.WITHDRAWAL.getValue(), Operation::setType);
          mp.<String>map(Withdrawal::getStakeAddress, (d, v) -> d.getAccount().setAddress(v));
          mp.<Amount>map(f -> updateDepositAmount(
              Optional.ofNullable(model.getAmount())
                  .map(BigInteger::negate)
                  .orElse(BigInteger.ZERO)),
              (d, v) -> d.getMetadata().setWithdrawalAmount(v));
          mp.<Long>map(f -> index, (d, v) -> d.getOperationIdentifier().setIndex(v));
        })
        .map(model);
  }
}
