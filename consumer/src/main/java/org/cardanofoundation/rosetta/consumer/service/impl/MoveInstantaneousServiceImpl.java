package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.PotTransfer;
import org.cardanofoundation.rosetta.common.entity.PotTransfer.PotTransferBuilder;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Reserve;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Treasury;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.MoveInstataneous;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPotTransferRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedReserveRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTreasuryRepository;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MoveInstantaneousServiceImpl extends CertificateSyncService<MoveInstataneous> {

  CachedReserveRepository cachedReserveRepository;
  CachedTreasuryRepository cachedTreasuryRepository;
  CachedPotTransferRepository cachedPotTransferRepository;

  StakeAddressService stakeAddressService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      MoveInstataneous certificate, int certificateIdx,
      Tx tx, Redeemer redeemer) {
    BigInteger potCoin = certificate.getAccountingPotCoin();
    if (Objects.nonNull(potCoin)) {
      insertPotTransfer(certificateIdx, potCoin, certificate.isTreasury(), tx);
      return;
    }

    insertRewards(aggregatedBlock, certificate, certificateIdx, tx);
  }

  private void insertRewards(AggregatedBlock aggregatedBlock,
      MoveInstataneous certificate, int certIdx, Tx tx) {
    Map<String, BigInteger> rewardsMap = certificate
        .getStakeCredentialCoinMap()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> AddressUtil.getRewardAddressString(
                entry.getKey(), aggregatedBlock.getNetwork()), Entry::getValue));

    List<Reserve> reserves = new ArrayList<>();
    List<Treasury> treasuries = new ArrayList<>();
    rewardsMap.forEach((stakeAddressHex, coin) -> {
      StakeAddress stakeAddress = stakeAddressService.getStakeAddress(stakeAddressHex);
      if (certificate.isTreasury()) {
        treasuries.add(buildTreasury(stakeAddress, tx, certIdx, coin));
      } else {
        reserves.add(buildReserve(stakeAddress, tx, certIdx, coin));
      }
    });

    if (!CollectionUtils.isEmpty(reserves)) {
      cachedReserveRepository.saveAll(reserves);
    }

    if (!CollectionUtils.isEmpty(treasuries)) {
      cachedTreasuryRepository.saveAll(treasuries);
    }
  }

  private Reserve buildReserve(StakeAddress stakeAddress, Tx tx, int certIdx, BigInteger coin) {
    return Reserve.builder()
        .addr(stakeAddress)
        .tx(tx)
        .certIndex(certIdx)
        .amount(coin)
        .build();
  }

  private Treasury buildTreasury(StakeAddress stakeAddress, Tx tx, int certIdx, BigInteger coin) {
    return Treasury.builder()
        .addr(stakeAddress)
        .tx(tx)
        .certIndex(certIdx)
        .amount(coin)
        .build();
  }

  private void insertPotTransfer(int certificateIdx, BigInteger coin, boolean isTreasury, Tx tx) {
    PotTransferBuilder<?, ?> potTransferBuilder = PotTransfer.builder();

    potTransferBuilder.certIndex(certificateIdx);

    if (isTreasury) {
      potTransferBuilder.treasury(coin);
    } else {
      potTransferBuilder.reserves(coin);
    }

    potTransferBuilder.tx(tx);
    cachedPotTransferRepository.save(potTransferBuilder.build());
  }
}
