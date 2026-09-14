package org.cardanofoundation.rosetta.common.services;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;

public interface Cip113AddressService {

  byte[] getConfiguredScriptHash();

  String buildSmartWalletAddress(
      byte[] configuredScriptHash,
      byte[] userCredential,
      NetworkEnum network);
}
