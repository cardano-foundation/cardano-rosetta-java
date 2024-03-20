package org.cardanofoundation.rosetta.common.util;

import static com.bloxbean.cardano.client.address.AddressType.Reward;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@Slf4j
public class ParseConstructionUtil {

  private ParseConstructionUtil() {

  }
  public static Inet4Address parseIpv4(String ip) throws UnknownHostException {
    if (!ObjectUtils.isEmpty(ip)) {
      String[] ipNew = ip.split("\\.");
      byte[] bytes = new byte[ipNew.length];
      for (int i = 0; i < ipNew.length; i++) {
        bytes[i] = Byte.parseByte(ipNew[i]);
      }
      return (Inet4Address) InetAddress.getByAddress(bytes);
    }
    throw new UnknownHostException("Error Parsing IP Address");
  }

  public static Inet6Address parseIpv6(String ip) throws UnknownHostException {
    if (!ObjectUtils.isEmpty(ip)) {
      String ipNew = ip.replace(":", "");
      byte[] parsedIp = HexUtil.decodeHexString(ipNew);
      return (Inet6Address) InetAddress.getByAddress(parsedIp);
    }
    throw new UnknownHostException("Error Parsing IP Address");
  }


  public static List<String> getOwnerAddressesFromPoolRegistrations(Integer network, PoolRegistration poolRegistration) {
    List<String> poolOwners = new ArrayList<>();
    Set<String> owners = poolRegistration.getPoolOwners();
    int ownersCount = owners.size();
    for (int i = 0; i < ownersCount; i++) {
      if (network == NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getValue()) {
        Address address = CardanoAddressUtil.getAddress(null,
                HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), Constants.STAKE_KEY_HASH_HEADER_KIND,
                Networks.testnet(), Reward);
        poolOwners.add(address.getAddress());
      }
      if (network == NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getValue()) {
        Address address = CardanoAddressUtil.getAddress(null,
                HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), Constants.STAKE_KEY_HASH_HEADER_KIND,
                Networks.preprod(), Reward);
        poolOwners.add(address.getAddress());
      }
      if (network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()) {
        Address address = CardanoAddressUtil.getAddress(null,
                HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), Constants.STAKE_KEY_HASH_HEADER_KIND,
                Networks.mainnet(), Reward);
        poolOwners.add(address.getAddress());
      }
    }
    return poolOwners;
  }

  public static String getRewardAddressFromPoolRegistration(Integer network, PoolRegistration poolRegistration) {
    String cutRewardAccount = poolRegistration.getRewardAccount();
    if (cutRewardAccount.length() == Constants.HEX_PREFIX_AND_REWARD_ACCOUNT_LENGTH) {
      // removing prefix 0x from reward account, reward account is 56 bytes
      cutRewardAccount = poolRegistration.getRewardAccount().substring(2);
    }
    if (network == NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getValue()) {
      return CardanoAddressUtil.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
                      Constants.STAKE_KEY_HASH_HEADER_KIND,
                      Networks.testnet(), Reward)
              .getAddress();
    }
    if (network == NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getValue()) {
      return CardanoAddressUtil.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
              Constants.STAKE_KEY_HASH_HEADER_KIND,
                      Networks.preprod(), Reward)
              .getAddress();
    }
    if (network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()) {
      return CardanoAddressUtil.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
              Constants.STAKE_KEY_HASH_HEADER_KIND,
                      Networks.mainnet(), Reward)
              .getAddress();
    }

    throw ExceptionFactory.invalidAddressError("Can't get Reward address from PoolRegistration");
  }

}
