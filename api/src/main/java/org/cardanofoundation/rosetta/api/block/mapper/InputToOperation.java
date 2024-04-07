package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.modelmapper.ModelMapper;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;

@OpenApiMapper
@AllArgsConstructor
public class InputToOperation extends AbstractToOperation<Utxo> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(Utxo model, OperationStatus status, int index) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Utxo.class, Operation.class))
        .orElseGet(() -> modelMapper.createTypeMap(Utxo.class, Operation.class))
        .addMappings(mp -> {

          mp.map(f -> Constants.INPUT, Operation::setType);
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.<String>map(Utxo::getOwnerAddr, (d, v) -> d.getAccount().setAddress(v));
          mp.map(Utxo::getLovelaceAmount, (d, v) -> d.getAmount().setValue(String.valueOf(v)));
          mp.<String>map(f -> ADA, (d, v) -> d.getAmount().getCurrency().setSymbol(v));
          mp.map(f -> f,
              (d, v) -> d.getCoinChange()
                  .getCoinIdentifier()
                  .setIdentifier(model.getTxHash() + ":" + model.getOutputIndex()));
          mp.<CoinAction>map(f -> CoinAction.SPENT, (d, v) -> d.getCoinChange().setCoinAction(v));
//          mp.<CoinAction>map(Utxo::getAmounts, (d, v) -> d.setMetadata(convertToMataData_TODO));

        })
        .setPostConverter(ctx -> {

          ctx.getDestination().setStatus(status.getStatus());
          ctx.getDestination().setMetadata(OperationMetadata.builder()
              .depositAmount(DataMapper.mapAmount("2000000", ADA, ADA_DECIMALS, null))
              .build());
          // TODO saa: need to get this from protocolparams
          // Create and inject  GenesisService to get the stake deposit amount
          // see similar implementation in BlockService.getPoolDeposit

          return ctx.getDestination();
        }).map(model);
  }


  private String convert(CertificateType model) {
    if (model == null) {
      return null;
    } else {
      return model.equals(CertificateType.STAKE_REGISTRATION)
          ? OperationType.STAKE_KEY_REGISTRATION.toString() :
          model.equals(CertificateType.STAKE_DEREGISTRATION)
              ? OperationType.STAKE_KEY_DEREGISTRATION.toString() : null;
    }
  }


}
