package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronBlockProof {

  private ByronTxProof txProof;
  private ByronSscProof sscProof;
  private String dlgProof;
  private String updProof;
}
