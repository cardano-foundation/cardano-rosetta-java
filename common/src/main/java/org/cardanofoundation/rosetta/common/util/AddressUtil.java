package org.cardanofoundation.rosetta.common.util;

import org.apache.commons.codec.binary.Hex;
import org.cardanofoundation.rosetta.common.entity.Address;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredential;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredentialType;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.common.exception.AddressException;


public final class AddressUtil {

  private AddressUtil() {

  }
// TODO 
//  private static Address getStakeAddress(Address baseAddress) throws AddressException {
//
//    String hexData = Hex.encodeHexString(baseAddress.getAddress().getBytes());
//    String network = "e0";
//    String hrp = "stake_test";
//    if (baseAddress.getNetwork().getProtocolMagic() == Constant.MAINNET) {
//      network = "e1";
//      hrp = "stake";
//    }
//    String hexStakeAddress = network + hexData.substring(hexData.length() - 56);
//    return new Address(Bech32.encode(HexUtil.decodeHexString(hexStakeAddress), hrp));
//  }

  public static String getRewardAddressString(StakeCredential credential, int network) {
    StakeCredentialType credType = credential.getStakeType();
    int stakeHeader = credType.equals(StakeCredentialType.ADDR_KEYHASH) ?
        0b1110_0000 : 0b1111_0000;
    int networkId = Constant.isTestnet(network) ? 0 : 1;
    stakeHeader = stakeHeader | networkId;

    return HexUtil.encodeHexString(new byte[] {(byte) stakeHeader}) + credential.getHash();
  }
}
