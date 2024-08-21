package org.cardanofoundation.rosetta.common.util;

import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.address.util.AddressEncoderDecoderUtil;
import com.bloxbean.cardano.client.address.util.AddressUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressRuntimeException;
import com.bloxbean.cardano.client.spec.NetworkId;
import com.bloxbean.cardano.client.transaction.spec.cert.MultiHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostAddr;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.bloxbean.cardano.client.util.HexUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Relay;

import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.StakeAddressPrefix;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@Slf4j
public class CardanoAddressUtils {

  private CardanoAddressUtils() {

  }

  public static boolean isStakeAddress(String address) {
    String addressPrefix = address.substring(0, Constants.PREFIX_LENGTH);

    return addressPrefix.contains(StakeAddressPrefix.MAIN.getPrefix())
        || addressPrefix.contains(StakeAddressPrefix.TEST.getPrefix());
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
    return (hex.startsWith("0x") ? hex : Constants.EMPTY_SYMBOL + hex);
  }

  public static boolean isEd25519Signature(String hash) {
    byte[] signatureByte = HexUtil.decodeHexString(hash);
    return signatureByte.length >= Constants.ED_25519_KEY_SIGNATURE_BYTE_LENGTH;
  }


  public static com.bloxbean.cardano.client.transaction.spec.cert.Relay generateSpecificRelay(
      Relay relay) {
    try {
      String type = relay.getType();
      if (type == null) {
        throw ExceptionFactory.invalidPoolRelayTypeError();
      }
      switch (type) {
        case Constants.SINGLE_HOST_ADDR -> {
          if (relay.getIpv4() != null) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if (!validator.isValidInet4Address(relay.getIpv4())) {
              throw ExceptionFactory.invalidIpv4();
            }
          }
          Integer port =
              ObjectUtils.isEmpty(relay.getPort()) ? null : relay.getPort();
          return new SingleHostAddr(Objects.requireNonNullElse(port, 0),
              ParseConstructionUtil.parseIpv4(relay.getIpv4()),
              ParseConstructionUtil.parseIpv6(relay.getIpv6()));
        }
        case Constants.SINGLE_HOST_NAME -> {
          ValidateParseUtil.validateDnsName(relay.getDnsName());
          Integer port =
              ObjectUtils.isEmpty(relay.getPort()) ? null : relay.getPort();
          return new SingleHostName(Objects.requireNonNullElse(port, 0), relay.getDnsName());
        }
        case Constants.MULTI_HOST_NAME -> {
          ValidateParseUtil.validateDnsName(relay.getDnsName());
          return new MultiHostName(relay.getDnsName());
        }
        default -> throw ExceptionFactory.invalidPoolRelayTypeError();
      }
    } catch (Exception error) {
      log.error("[validateAndParsePoolRelays] invalid pool relay");
      throw ExceptionFactory.invalidPoolRelaysError(error.getMessage());
    }
  }


  public static Object generateAddress(String address) {
    EraAddressType addressType = getEraAddressType(address);
    if (addressType == EraAddressType.BYRON) {
      return new ByronAddress(address);
    }
    return new Address(address);
  }

  public static EraAddressType getEraAddressType(String address) {
    try {
      if(address == null || !AddressUtil.isValidAddress(address))
        return null;
      if (address.startsWith(Constants.ADDRESS_PREFIX)
          || address.startsWith(StakeAddressPrefix.MAIN.getPrefix())) {
        // validate bech32 address. Unfortunately, it will throw a runtime exception in case of invalid address
        Bech32.decode(address);
        return EraAddressType.SHELLEY;
      }
      new ByronAddress(address).getAddress();
      return EraAddressType.BYRON;
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static Boolean isPolicyIdValid(String policyId) {
    return policyId.matches(Constants.POLICY_ID_VALIDATION);
  }

  public static Boolean isTokenNameValid(String name) {
    return name.matches(Constants.TOKEN_NAME_VALIDATION) || isEmptyHexString(name);
  }

  public static Boolean isEmptyHexString(String toCheck) {
    return !ObjectUtils.isEmpty(toCheck) && toCheck.equals(Constants.EMPTY_HEX);
  }

  public static String generateRewardAddress(Network network, HdPublicKey paymentCredential) {
    log.info(
        "[generateRewardAddress] Deriving cardano reward address from valid public staking key");
    Address rewardAddress = AddressProvider.getRewardAddress(paymentCredential, network);
    log.info("[generateRewardAddress] reward address is {}", rewardAddress.toBech32());
    return rewardAddress.toBech32();
  }

  public static String generateBaseAddress(Network network,
      HdPublicKey paymentCredential, HdPublicKey stakingCredential) {
    log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
    Address baseAddress = AddressProvider.getBaseAddress(paymentCredential, stakingCredential,
        network);
    log.info("generateAddress] base address is {}", baseAddress.toBech32());
    return baseAddress.toBech32();
  }

  public static String generateEnterpriseAddress(Network network, HdPublicKey paymentCredential) {
    log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
    Address entAddress = AddressProvider.getEntAddress(paymentCredential, network);
    log.info("generateAddress] base address is {}", entAddress.toBech32());
    return entAddress.toBech32();
  }

  public static boolean isEd25519KeyHash(String hash) {
    try {
      HexUtil.decodeHexString(KeyGenUtil.getKeyHash(HexUtil.decodeHexString(hash)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static StakeCredential getStakingCredentialFromStakeKey(PublicKey stakingCredential) {
    HdPublicKey stakingKey = publicKeyToHdPublicKey(stakingCredential);
    return StakeCredential.fromKeyHash(stakingKey.getKeyHash());
  }

  public static HdPublicKey publicKeyToHdPublicKey(PublicKey publicKey) {
    if (publicKey == null || ObjectUtils.isEmpty(publicKey.getHexBytes())) {
      log.error("[getPublicKey] Staking key not provided");
      throw ExceptionFactory.missingStakingKeyError();
    }
    boolean isKeyValid = CardanoAddressUtils.isKeyValid(publicKey.getHexBytes(),
        publicKey.getCurveType().toString());
    if (!isKeyValid) {
      log.info("[getPublicKey] Staking key has an invalid format");
      throw ExceptionFactory.invalidStakingKeyFormat();
    }
    byte[] stakingKeyBuffer = HexUtil.decodeHexString(publicKey.getHexBytes());
    HdPublicKey hdPublicKey = new HdPublicKey();
    hdPublicKey.setKeyData(stakingKeyBuffer);
    return hdPublicKey;
  }

  public static Address getAddressFromHexString(String hex) {
    return new Address(hexStringToBuffer(hex));
  }

  public static byte[] hexStringToBuffer(String input) {
    boolean checkEmptyHexString = CardanoAddressUtils.isEmptyHexString(input);
    return checkEmptyHexString ? HexUtil.decodeHexString(StringUtils.EMPTY) : HexUtil.decodeHexString(input);
  }

  public static void verifyAddress(String address) {
    try {
      if (address == null || !AddressUtil.isValidAddress(address)) {
        throw ExceptionFactory.invalidAddressError(address);
      }
    } catch (RuntimeException e) {
      log.debug("[verifyAddress] Provided address is invalid {}", address);
      throw ExceptionFactory.invalidAddressError(Optional.ofNullable(address).orElse(Constants.EMPTY_SYMBOL));
    }
    // Shelley era checking for lower case, upper case characters can lead to problems with other tools
    // if Shelley Era and contains upper case characters
    if (Objects.equals(getEraAddressType(address), EraAddressType.SHELLEY) && address.chars().anyMatch(Character::isUpperCase)) {
        log.debug("[verifyAddress] Provided address is invalid {}", address);
        throw ExceptionFactory.invalidAddressCasingError(address);
    }
  }
}
