package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.util.HexUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@UtilityClass
public class IPV4Parser {

    public Inet4Address parseIpv4(String ip) throws UnknownHostException {
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

    public Inet6Address parseIpv6(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            String ipNew = ip.replace(":", "");
            byte[] parsedIp = HexUtil.decodeHexString(ipNew);
            return (Inet6Address) InetAddress.getByAddress(parsedIp);
        }

        throw new UnknownHostException("Error Parsing IP Address");
    }

}
