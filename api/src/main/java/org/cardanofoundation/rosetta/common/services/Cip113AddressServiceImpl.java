package org.cardanofoundation.rosetta.common.services;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;

@Service
public class Cip113AddressServiceImpl implements Cip113AddressService {

  private static final int SCRIPT_HASH_HEX_LENGTH = 56;
  private static final Pattern SCRIPT_HASH_HEX_PATTERN = Pattern.compile(
      "^[0-9a-fA-F]{" + SCRIPT_HASH_HEX_LENGTH + "}$");
  private static final byte BASE_SCRIPT_PAYMENT_KEY_STAKE_HEADER_KIND = 0x10;

  @Value("${cardano.rosetta.CIP113_BASE_SCRIPT_HASH:}")
  private String cip113BaseScriptHash;

  @Override
  public byte[] getConfiguredScriptHash() {
    String scriptHash = Optional.ofNullable(cip113BaseScriptHash)
        .map(String::trim)
        .orElse("");

    if (scriptHash.isEmpty()) {
      throw ExceptionFactory.cip113PlbScriptHashNotConfigured();
    }
    if (!SCRIPT_HASH_HEX_PATTERN.matcher(scriptHash).matches()) {
      throw ExceptionFactory.cip113PlbScriptHashInvalid();
    }

    return decodeHexString(scriptHash);
  }

  @Override
  public String buildSmartWalletAddress(byte[] configuredScriptHash, byte[] userCredential,
                                        NetworkEnum network) {
    return CardanoAddressUtils.getAddress(
        configuredScriptHash,
        userCredential,
        BASE_SCRIPT_PAYMENT_KEY_STAKE_HEADER_KIND,
        network.getNetwork(),
        com.bloxbean.cardano.client.address.AddressType.Base).toBech32();
  }
}
