package org.cardanofoundation.rosetta.common.ledgersync.byron;

import java.util.List;
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
public class ByronTx {

  private List<ByronTxIn> inputs;
  private List<ByronTxOut> outputs;
  private byte[] txHash;

  //TODO -- Attributes
}
