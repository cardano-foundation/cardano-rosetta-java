package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronUpdateVote {
  private String voter;
  private String proposalId;
  private boolean vote;
  private String signature;
}
