package org.cardanofoundation.rosetta.common.ledgersync;

import java.util.Map;
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
public class Update {

  private Map<String, ProtocolParamUpdate> protocolParamUpdates;
  private long epoch;
}
