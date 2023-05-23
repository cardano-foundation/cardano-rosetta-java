package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronInnerSharesMap {
  private String stakeholderId;
  private List<String> shares;
}
