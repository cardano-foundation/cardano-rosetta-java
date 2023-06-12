package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.AddressTokenBalance;
import org.springframework.data.util.Pair;

import java.util.Collection;

public interface CustomAddressTokenBalanceRepository {

  Collection<AddressTokenBalance> findAllByAddressFingerprintPairIn(
      Collection<Pair<String, String>> addressFingerprintPairs);

  Collection<AddressTokenBalance> findAllByAddressMultiAssetIdPairIn(
      Collection<Pair<Long, Long>> addressMultiAssetIdPairs);
}
