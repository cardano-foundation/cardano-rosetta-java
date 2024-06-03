package org.cardanofoundation.rosetta.api.construction.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Named;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.Signature;

import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.util.Constants;

@Component
public class ConstructionMapperUtils {

  @Named("mapAmounts")
  public List<Amount> getAmounts(Long suggestedFee) {
    return List.of(Amount.builder()
        .value(String.valueOf(suggestedFee))
        .currency(getAdaCurrency())
        .build());
  }

  public Signatures getSignatures(Signature signature) {
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

  private Currency getAdaCurrency() {
    return Currency.builder()
        .symbol(Constants.ADA)
        .decimals(Constants.ADA_DECIMALS)
        .build();
  }
}
