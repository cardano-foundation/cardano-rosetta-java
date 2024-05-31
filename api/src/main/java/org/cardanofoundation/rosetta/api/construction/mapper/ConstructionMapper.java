package org.cardanofoundation.rosetta.api.construction.mapper;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.Signature;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsMapper;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;

@Mapper(config = BaseMapper.class, uses = {ProtocolParamsMapper.class, ConstructionMapperUtils.class})
public interface ConstructionMapper {

  @Mapping(target = "metadata.ttl", source = "ttl")
  @Mapping(target = "metadata.protocolParameters", source = "protocolParams")
  @Mapping(target = "suggestedFee", source = "suggestedFee", qualifiedByName = "mapAmounts")
  ConstructionMetadataResponse mapToMetadataResponse(ProtocolParams protocolParams, Long ttl,
      Long suggestedFee);

  List<Signatures> mapRosettaSignatureToSignaturesList(List<Signature> signatures);

  default Signatures mapRosettaSignatureToSignatures(Signature signature) {
    String chainCode = null;
    String address = null;
    AccountIdentifier accountIdentifier = signature.getSigningPayload().getAccountIdentifier();
    if (!ObjectUtils.isEmpty(accountIdentifier)) {
      chainCode = accountIdentifier.getMetadata().getChainCode();
      address = accountIdentifier.getAddress();
    }
    return new Signatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(),
        chainCode, address);
  }
}
