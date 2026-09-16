package org.cardanofoundation.rosetta.common.services;

import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.Credential;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;

@Service
public class Cip113AddressServiceImpl implements Cip113AddressService {

  private static final int CREDENTIAL_HASH_LENGTH = 28;

  @Value("${cardano.rosetta.CIP113_BASE_SCRIPT_HASH:}")
  private String cip113BaseScriptHash;

  @Override
  public @NotNull byte[] getConfiguredScriptHash() {
    String scriptHash = Optional.ofNullable(cip113BaseScriptHash)
        .map(String::trim)
        .orElse("");

    if (scriptHash.isEmpty()) {
      throw ExceptionFactory.cip113PlbScriptHashNotConfigured();
    }
    byte[] scriptHashBytes;
    try {
      scriptHashBytes = decodeHexString(scriptHash);
    } catch (Exception e) {
      throw ExceptionFactory.cip113PlbScriptHashInvalid();
    }

    if (scriptHashBytes.length != CREDENTIAL_HASH_LENGTH) {
      throw ExceptionFactory.cip113PlbScriptHashInvalid();
    }

    return scriptHashBytes;
  }

  @Override
  public @NotNull String buildSmartWalletAddress(@NotNull byte[] configuredScriptHash,
                                                 @NotNull byte[] userCredential,
                                                 @NotNull NetworkEnum network) {
    return AddressProvider.getBaseAddress(
        Credential.fromScript(configuredScriptHash),
        Credential.fromKey(userCredential),
        network.getNetwork()).toBech32();
  }
}
