package org.cardanofoundation.rosetta.api.util;


import com.bloxbean.cardano.client.address.Address;

import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.address.util.AddressEncoderDecoderUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressRuntimeException;
import com.bloxbean.cardano.client.spec.NetworkId;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.model.cardano.Metadata;
import org.cardanofoundation.rosetta.api.model.constants.Constants;
import org.cardanofoundation.rosetta.api.model.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.model.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.model.enumeration.NonStakeAddressPrefix;
import org.cardanofoundation.rosetta.api.model.enumeration.StakeAddressPrefix;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.PublicKey;

import java.util.Arrays;

@Slf4j
public class CardanoAddressUtils {

  private CardanoAddressUtils() {

  }

  public static boolean isStakeAddress(String address) {
    String addressPrefix = address.substring(0, Constants.PREFIX_LENGTH);
    String[] types = {StakeAddressPrefix.MAIN.getPrefix(), StakeAddressPrefix.TEST.getPrefix()};

    return Arrays.stream(types)
        .anyMatch(addressPrefix::contains);
  }

  public static Address getAddress(byte[] paymentKeyHash, byte[] stakeKeyHash, byte headerKind,
      Network networkInfo, AddressType addressType) {
    NetworkId network = AddressEncoderDecoderUtil.getNetworkId(networkInfo);
    String var10000 = AddressEncoderDecoderUtil.getPrefixHeader(addressType);
    String prefix = var10000 + AddressEncoderDecoderUtil.getPrefixTail(network);
    byte header = AddressEncoderDecoderUtil.getAddressHeader(headerKind, networkInfo, addressType);
    byte[] addressArray = getAddressBytes(paymentKeyHash, stakeKeyHash, addressType, header);
    return new Address(prefix, addressArray);
  }

  private static byte[] getAddressBytes(byte[] paymentKeyHash, byte[] stakeKeyHash,
      AddressType addressType, byte header) {
    byte[] addressArray;
    switch (addressType) {
      case Base, Ptr -> {
        addressArray = new byte[1 + paymentKeyHash.length + stakeKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(paymentKeyHash, 0, addressArray, 1, paymentKeyHash.length);
        System.arraycopy(stakeKeyHash, 0, addressArray, paymentKeyHash.length + 1,
            stakeKeyHash.length);
      }
      case Enterprise -> {
        addressArray = new byte[1 + paymentKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(paymentKeyHash, 0, addressArray, 1, paymentKeyHash.length);
      }
      case Reward -> {
        addressArray = new byte[1 + stakeKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(stakeKeyHash, 0, addressArray, 1, stakeKeyHash.length);
      }
      default -> throw new AddressRuntimeException("Unknown address type");
    }

    return addressArray;
  }

  public static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte aByte : bytes) {
      int decimal =
          aByte & 0xff;               // bytes widen to int, need mask, prevent sign extension
      // get last 8 bits
      String hex = Integer.toHexString(decimal);
      if (hex.length() % 2 == 1) {                    // if half hex, pad with zero, e.g \t
        hex = "0" + hex;
      }
      result.append(hex);
    }
    return result.toString();
  }

  public static Boolean isKeyValid(String publicKeyBytes, String curveType) {
    if (publicKeyBytes == null) {
      return false;
    }
    return publicKeyBytes.length() == Constants.PUBLIC_KEY_BYTES_LENGTH && curveType.equals(
        Constants.VALID_CURVE_TYPE);
  }

  public static String add0xPrefix(String hex) {
    return (hex.startsWith("0x") ? hex : Constants.EMPTY_SYMBOl + hex);
  }

  public static boolean isEd25519Signature(String hash) {
    byte[] signatureByte = HexUtil.decodeHexString(hash);
    return signatureByte.length >= Constants.Ed25519_Key_Signature_BYTE_LENGTH;
  }
//
//  public static com.bloxbean.cardano.client.transaction.spec.cert.Relay generateSpecificRelay(
//      Relay relay) {
//    try {
//      String type = relay.getType();
//      if (type == null) {
//        throw ExceptionFactory.invalidPoolRelayTypeError();
//      }
//      switch (type) {
//        case "single_host_addr" -> {
//          if (relay.getIpv4() != null) {
//            InetAddressValidator validator = InetAddressValidator.getInstance();
//            if (!validator.isValidInet4Address(relay.getIpv4())) {
//              throw ExceptionFactory.invalidIpv4();
//            }
//          }
//          Integer port =
//              ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10);
//          return new SingleHostAddr(Objects.requireNonNullElse(port, 0),
//              ParseConstructionUtils.parseIpv4(relay.getIpv4()),
//              ParseConstructionUtils.parseIpv6(relay.getIpv6()));
//        }
//        case "single_host_name" -> {
//          ValidateOfConstruction.validateDnsName(relay.getDnsName());
//          Integer port =
//              ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10);
//          return new SingleHostName(Objects.requireNonNullElse(port, 0), relay.getDnsName());
//        }
//        case "multi_host_name" -> {
//          ValidateOfConstruction.validateDnsName(relay.getDnsName());
//          return new MultiHostName(relay.getDnsName());
//        }
//        default -> throw ExceptionFactory.invalidPoolRelayTypeError();
//      }
//    } catch (Exception error) {
//      log.error("[validateAndParsePoolRelays] invalid pool relay");
//      throw ExceptionFactory.invalidPoolRelaysError(error.getMessage());
//    }
//  }


  public static Object generateAddress(String address) {
    EraAddressType addressType = getEraAddressType(address);
    if (addressType == EraAddressType.BYRON) {
      return new ByronAddress(address);
    }
    return new Address(address);
  }

  public static EraAddressType getEraAddressType(String address) {
    try {
      if (address.startsWith("addr") || address.startsWith("stake")) {
        return EraAddressType.SHELLEY;
      }
      new ByronAddress(address).getAddress();
      return EraAddressType.BYRON;
    } catch (Exception e) {
      return null;
    }
  }

  public static Boolean isPolicyIdValid(String policyId) {
    return policyId.matches(Constants.PolicyId_Validation);
  }

  public static Boolean isTokenNameValid(String name) {
    return name.matches(Constants.Token_Name_Validation) || isEmptyHexString(name);
  }

  public static Boolean isEmptyHexString(String toCheck) {
    return !ObjectUtils.isEmpty(toCheck) && toCheck.equals(Constants.EMPTY_HEX);
  }

  public static Amount mapAmount(String value, String symbol, Integer decimals,
                                 Metadata metadata) {
    return new Amount(value,
        new Currency(ObjectUtils.isEmpty(symbol) ? Constants.ADA : hexStringFormatter(symbol),
            ObjectUtils.isEmpty(decimals) ? Constants.ADA_DECIMALS : decimals, metadata), null);
  }

  public static String hexStringFormatter(String toFormat) {
    if (ObjectUtils.isEmpty(toFormat)) {
      return Constants.EMPTY_HEX;
    } else {
      return toFormat;
    }
  }

  public static String hexFormatter(byte[] bytes) {
    return HexUtil.encodeHexString(bytes);
  }

  public static String bytesToHex(byte[] bytes) {
    return hexFormatter(bytes);
  }

  public static String getAddressPrefix(Integer network, StakeAddressPrefix addressPrefix) {
    if (ObjectUtils.isEmpty(addressPrefix)) {
      return network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()
          ? NonStakeAddressPrefix.MAIN.getValue() : NonStakeAddressPrefix.TEST.getValue();
    }
    return network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()
        ? StakeAddressPrefix.MAIN.getPrefix() : StakeAddressPrefix.TEST.getPrefix();
  }

  public static String generateAddress(NetworkIdentifierType networkIdentifierType,
      String publicKeyString,
      String stakingCredentialString,
      org.cardanofoundation.rosetta.api.model.enumeration.AddressType type) {
    log.info(
        "[generateAddress] About to generate address from public key {} and network identifier {}",
        publicKeyString, networkIdentifierType);
    HdPublicKey paymentCredential = new HdPublicKey();
    paymentCredential.setKeyData(HexUtil.decodeHexString(publicKeyString));
    if (!ObjectUtils.isEmpty(type) && type.getValue().equals(
        org.cardanofoundation.rosetta.api.model.enumeration.AddressType.REWARD.getValue())) {
      return generateRewardAddress(networkIdentifierType, paymentCredential);
    }

    if (!ObjectUtils.isEmpty(type) && type.getValue().equals(
        org.cardanofoundation.rosetta.api.model.enumeration.AddressType.BASE.getValue())) {
      if (stakingCredentialString == null) {
        log.error("[constructionDerive] No staking key was provided for base address creation");
        throw ExceptionFactory.missingStakingKeyError();
      }
      HdPublicKey stakingCredential = new HdPublicKey();
      stakingCredential.setKeyData(HexUtil.decodeHexString(stakingCredentialString));
      return generateBaseAddress(networkIdentifierType, paymentCredential, stakingCredential);
    }

    return generateEnterpriseAddress(paymentCredential, networkIdentifierType);
  }

  public static String generateRewardAddress(NetworkIdentifierType networkIdentifierType,
      HdPublicKey paymentCredential) {
    log.info(
        "[generateRewardAddress] Deriving cardano reward address from valid public staking key");
    Address rewardAddress = AddressProvider.getRewardAddress(paymentCredential,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
    log.info("[generateRewardAddress] reward address is {}", rewardAddress.toBech32());
    return rewardAddress.toBech32();
  }

  public static String generateBaseAddress(NetworkIdentifierType networkIdentifierType,
      HdPublicKey paymentCredential, HdPublicKey stakingCredential) {
    log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
    Address baseAddress = AddressProvider.getBaseAddress(paymentCredential, stakingCredential,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
    log.info("generateAddress] base address is {}", baseAddress.toBech32());
    return baseAddress.toBech32();
  }

  public static String generateEnterpriseAddress(HdPublicKey paymentCredential,
      NetworkIdentifierType networkIdentifierType) {
    log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
    Address entAddress = AddressProvider.getEntAddress(paymentCredential,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
    log.info("generateAddress] base address is {}", entAddress.toBech32());
    return entAddress.toBech32();
  }

  public static Address getAddressFromHexString(String hex) {
    return new Address(hexStringToBuffer(hex));
  }

  public static byte[] hexStringToBuffer(String input) {
    boolean checkEmptyHexString = CardanoAddressUtils.isEmptyHexString(input);
    return checkEmptyHexString ? HexUtil.decodeHexString("") : HexUtil.decodeHexString(input);
  }

  public static String remove0xPrefix(String hex) {
    return (hex.startsWith("0x") ? hex.substring("0x".length()) : hex);
  }
  public static EraAddressType getEraAddressTypeOrNull(String address) {
    try {
      return CardanoAddressUtils.getEraAddressType(address);
    } catch (Exception error) {
      return null;
    }
  }
  public static boolean isEd25519KeyHash(String hash) {
    try {
      HexUtil.decodeHexString(KeyGenUtil.getKeyHash(HexUtil.decodeHexString(hash)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  public static StakeCredential getStakingCredentialFromHex(PublicKey stakingCredential) {
    HdPublicKey stakingKey = getPublicKey(stakingCredential);
    return StakeCredential.fromKeyHash(stakingKey.getKeyHash());
  }
  public static HdPublicKey getPublicKey(PublicKey publicKey) {
    if (ObjectUtils.isEmpty(publicKey) || ObjectUtils.isEmpty(publicKey.getHexBytes())) {
      log.error("[getPublicKey] Staking key not provided");
      throw ExceptionFactory.missingStakingKeyError();
    }
    boolean checkKey = CardanoAddressUtils.isKeyValid(publicKey.getHexBytes(),
        publicKey.getCurveType().toString());
    if (!checkKey) {
      log.info("[getPublicKey] Staking key has an invalid format");
      throw ExceptionFactory.invalidStakingKeyFormat();
    }
    byte[] stakingKeyBuffer = CardanoAddressUtils.hexStringToBuffer(publicKey.getHexBytes());
    HdPublicKey hdPublicKey = new HdPublicKey();
    hdPublicKey.setKeyData(stakingKeyBuffer);
    return hdPublicKey;
  }
}
