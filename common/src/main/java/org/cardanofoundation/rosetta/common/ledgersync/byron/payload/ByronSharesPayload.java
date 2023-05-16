package org.cardanofoundation.rosetta.common.ledgersync.byron.payload;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronInnerSharesMap;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronSscCert;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronSharesPayload implements ByronSscPayload {

  public static final String TYPE = "ByronSharesPayload";

  private Map<String, ByronInnerSharesMap> sscShares;
  private List<ByronSscCert> sscCerts;

  @Override
  public String getType() {
    return TYPE;
  }
}
