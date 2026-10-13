package org.cardanofoundation.rosetta.common.services;

import jakarta.validation.constraints.NotNull;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;

public interface Cip113AddressService {

  @NotNull byte[] getConfiguredScriptHash();

  @NotNull String buildSmartWalletAddress(
      @NotNull byte[] configuredScriptHash,
      @NotNull byte[] userCredential,
      @NotNull NetworkEnum network);
}
