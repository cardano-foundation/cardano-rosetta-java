package org.cardanofoundation.rosetta.common.ledgersync.byron.payload;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronSignedCommitment;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronSscCert;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronCommitmentsPayload implements ByronSscPayload {

  public static final String TYPE = "ByronCommitmentsPayload";

  private List<ByronSignedCommitment> sscCommitments;
  private List<ByronSscCert> sscCerts;

  @Override
  public String getType() {
    return TYPE;
  }
}
