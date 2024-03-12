package org.cardanofoundation.rosetta.common.util;

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
    return null;
  }

  public static Inet6Address parseIpv6(String ip) throws UnknownHostException {
    if (!ObjectUtils.isEmpty(ip)) {
      String ipNew = ip.replace(":", "");
      byte[] parsedIp = HexUtil.decodeHexString(ipNew);
      return (Inet6Address) InetAddress.getByAddress(parsedIp);
    }
    return null;
  }

  public static List<String> parsePoolOwners(Integer network, PoolRegistration poolRegistration) {
    List<String> poolOwners = new ArrayList<>();
    Set<String> owners = poolRegistration.getPoolOwners();
    int ownersCount = owners.size();
    for (int i = 0; i < ownersCount; i++) {
      if (network == NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getValue()) {
        Address address = CardanoAddressUtil.getAddress(null,
                HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), (byte) -32,
                Networks.testnet(), com.bloxbean.cardano.client.address.AddressType.Reward);
        poolOwners.add(address.getAddress());
      }
      if (network == NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getValue()) {
        Address address = CardanoAddressUtil.getAddress(null,
                HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), (byte) -32,
                Networks.preprod(), com.bloxbean.cardano.client.address.AddressType.Reward);
        poolOwners.add(address.getAddress());
      }
      if (network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()) {
        Address address = CardanoAddressUtil.getAddress(null,
                HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), (byte) -32,
                Networks.mainnet(), com.bloxbean.cardano.client.address.AddressType.Reward);
        poolOwners.add(address.getAddress());
      }
    }
    return poolOwners;
  }

  public static String parsePoolRewardAccount(Integer network, PoolRegistration poolRegistration) {
    String cutRewardAccount = poolRegistration.getRewardAccount();
    if (poolRegistration.getRewardAccount().length() == 58) {
      cutRewardAccount = poolRegistration.getRewardAccount().substring(2);
    }
    if (network == NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getValue()) {
      return CardanoAddressUtil.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
                      (byte) -32,
                      Networks.testnet(), com.bloxbean.cardano.client.address.AddressType.Reward)
              .getAddress();
    }
    if (network == NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getValue()) {
      return CardanoAddressUtil.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
                      (byte) -32,
                      Networks.preprod(), com.bloxbean.cardano.client.address.AddressType.Reward)
              .getAddress();
    }
    if (network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()) {
      return CardanoAddressUtil.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
                      (byte) -32,
                      Networks.mainnet(), com.bloxbean.cardano.client.address.AddressType.Reward)
              .getAddress();
    }

    return null;
  }

}
