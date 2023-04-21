package org.cardanofoundation.rosetta.consumer.address;

import com.sotatek.cardano.ledgersync.common.address.ShelleyAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sotatek.cardano.ledgersync.util.HexUtil;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class AddressTest {


  @Test
  public void baseAddressToStakeAddress(){
    String[] bAddr = new String[]{
        "addr_test1qphhd9z2vsxasf090h2u9fyy82u6r5knyvfnvhs56setg6x4k0mpq9j2ke9hpzys3r648cslux2d34qh50ksh704atws8pm0zf",
        "addr_test1xzphfvnxwncw8038c40qlz59qrhrhszw0l9gdpzg7gq6wyvrwjexva8suwlz0327p79g2q8w80qyul72s6zy3usp5ugsvrdl8e"
    };
    for(String addr : bAddr){
      assertTrue(ShelleyAddress.checkBech32HasStakeAddress(addr));
    }
  }

  @Test
  public void hexAddressLowercase(){
    String[] addrs = new String[]{
        "2cWKMJemoBajXqjndT8ugw64NyyywNRru3yieT4yV62R6Zje4F9WN3pXbQzwiEF3vcLFQ",
//        "addr_test1wpnlxv2xv9a9ucvnvzqakwepzl9ltx7jzgm53av2e9ncv4sysemm8",
//        "addr_test1qra8vr24tvwtx0uf3ja4y8pcae3n7d5td6dx8q24u7v33gymee02vawenm7sh4qr8v208wcx4lg9h6m6l29zt5cv083sqp2875",
//        "stake_test1uprdp963cdzh8r7xvh39jh9y8n8msndpsk7t5z9car8cu0sadfhxz"
    };
    for(String addr : addrs){
     byte[] byteNor = HexUtil.decodeHexString(addr);
     byte[] byteLower = HexUtil.decodeHexString(addr.toLowerCase());
     byte[] byteUpper = HexUtil.decodeHexString(addr.toUpperCase());
     assertEquals(byteNor,byteLower);
     assertEquals(byteNor,byteUpper);
    }
  }
}
