package org.cardanofoundation.rosetta.common.ledgersync;

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
public class HeaderBody {

  private long blockNumber;
  private Epoch slotId;
  private String prevHash;
  private String issuerVkey;
  private String vrfVkey;
  private VrfCert nonceVrf; //removed in babbage
  private VrfCert leaderVrf; //removed in babbage
  private VrfCert vrfResult; //babbage
  private long blockBodySize;
  private String blockBodyHash;
  private ProtocolVersion protocolVersion;
  //Derived value
  private byte[] blockHash;
  private OperationalCert operationalCert;
}

