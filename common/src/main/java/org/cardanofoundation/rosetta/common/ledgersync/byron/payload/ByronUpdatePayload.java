package org.cardanofoundation.rosetta.common.ledgersync.byron.payload;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronUpdateVote;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronUpdatePayload {
  private ByronUpdateProposal proposal;
  private List<ByronUpdateVote> votes;
}
