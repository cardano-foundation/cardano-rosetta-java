package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronCommitment {
  private Map<String, String> map;
  private ByronSecretProof vssProof;
}
