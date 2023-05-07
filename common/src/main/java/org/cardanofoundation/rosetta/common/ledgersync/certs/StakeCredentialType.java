package org.cardanofoundation.rosetta.common.ledgersync.certs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@RequiredArgsConstructor
public enum StakeCredentialType {
  ADDR_KEYHASH("ADDR_KEYHASH"),
  SCRIPT_HASH("SCRIPT_HASH");
  String value;
}
