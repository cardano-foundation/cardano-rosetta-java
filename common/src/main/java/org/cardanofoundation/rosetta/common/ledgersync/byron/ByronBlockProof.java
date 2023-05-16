package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

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
