package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.byron.payload.ByronDlgPayload;
import org.cardanofoundation.rosetta.common.ledgersync.byron.payload.ByronSscPayload;
import org.cardanofoundation.rosetta.common.ledgersync.byron.payload.ByronTxPayload;
import org.cardanofoundation.rosetta.common.ledgersync.byron.payload.ByronUpdatePayload;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronMainBody {
  private List<ByronTxPayload> txPayload;
  private ByronSscPayload sscPayload;
  private List<ByronDlgPayload> dlgPayload;
  private ByronUpdatePayload updPayload;
}
