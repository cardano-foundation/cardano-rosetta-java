package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronUpdateData {
  private String appDiffHash;
  private String pkgHash;
  private String updaterHash;
  private String metadataHash;
}
