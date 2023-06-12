package org.cardanofoundation.rosetta.consumer.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.springframework.util.MultiValueMap;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Builder
@AllArgsConstructor
public class TransactionOutMultiAssets {
  TxOut txOut;
  MultiValueMap<String, MaTxOut> pMaTxOuts;
  String scriptRefer;
  String datumHash;
}
