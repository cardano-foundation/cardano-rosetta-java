package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.StakeAddress.StakeAddressBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.address.ShelleyAddress;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeAddressRepository;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StakeAddressServiceImpl implements StakeAddressService {

  CachedStakeAddressRepository cachedStakeAddressRepository;


  @Override
  public StakeAddress getStakeAddress(String stakeAddressHex) {
    return cachedStakeAddressRepository.findByHashRaw(stakeAddressHex).orElse(null);
  }

  @Override
  public void handleStakeAddressesFromTxs(
      Map<String, byte[]> stakeAddressTxHashMap, Map<byte[], Tx> txMap) {
    Set<String> stakeAddressesHex = stakeAddressTxHashMap.keySet();
    Map<String, StakeAddress> stakeAddresses = cachedStakeAddressRepository
        .findByHashRawIn(stakeAddressesHex).parallelStream()
        .collect(Collectors.toConcurrentMap(StakeAddress::getHashRaw, Function.identity()));

    /*
     * For each stake address and its first appeared tx hash, check if the stake address
     * was existed before. If not, create a new stake address record and save it
     */
    stakeAddressTxHashMap.forEach((stakeAddressHex, txHash) -> {
      StakeAddress stakeAddress = stakeAddresses.get(stakeAddressHex);
      if (Objects.isNull(stakeAddress)) {
        byte[] addressBytes = HexUtil.decodeHexString(stakeAddressHex);
        ShelleyAddress shelleyAddress = new ShelleyAddress(addressBytes);
        StakeAddress newStakeAddress = buildStakeAddress(shelleyAddress, txMap.get(txHash));
        stakeAddresses.put(stakeAddressHex, newStakeAddress);
      }
    });

    // We cache both existing and new records
    cachedStakeAddressRepository.saveAll(stakeAddresses.values());
  }

  private StakeAddress buildStakeAddress(ShelleyAddress address, Tx tx) {
    String stakeReference = HexUtil.encodeHexString(address.getStakeReference());

    StakeAddressBuilder<?, ?> stakeAddressBuilder = StakeAddress.builder()
        .hashRaw(stakeReference)
        .view(address.getAddress());

    if (address.hasScriptHashReference()) {
      String scriptHash = stakeReference.substring(2);
      stakeAddressBuilder.scriptHash(scriptHash);
    }

    stakeAddressBuilder.balance(BigInteger.ZERO);
    stakeAddressBuilder.availableReward(BigInteger.ZERO);

    return stakeAddressBuilder.build();
  }
}
