package org.cardanofoundation.rosetta.common.ledgersync.byron.payload;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTx;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTxWitnesses;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronTxPayload {

  private ByronTx transaction;
  private List<ByronTxWitnesses> witnesses;
}
