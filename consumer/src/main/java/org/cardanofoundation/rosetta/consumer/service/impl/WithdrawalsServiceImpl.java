package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.transaction.spec.RedeemerTag;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.Withdrawal;
import org.cardanofoundation.rosetta.common.entity.Withdrawal.WithdrawalBuilder;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeAddressRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedWithdrawalRepository;
import org.cardanofoundation.rosetta.consumer.service.WithdrawalsService;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
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
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WithdrawalsServiceImpl implements WithdrawalsService {

  CachedWithdrawalRepository cachedWithdrawalRepository;
  CachedStakeAddressRepository cachedStakeAddressRepository;

  @Override
  public void handleWithdrawal(Collection<AggregatedTx> successTxs,
      Map<String, Tx> txMap, Map<RedeemerReference<?>, Redeemer> redeemersMap) {
    List<AggregatedTx> txWithWithdrawalList = successTxs.stream()
        .filter(aggregatedTx -> !CollectionUtils.isEmpty(aggregatedTx.getWithdrawals()))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(txWithWithdrawalList)) {
      return;
    }

    Set<String> rewardAccounts = txWithWithdrawalList.stream()
        .map(AggregatedTx::getWithdrawals)
        .flatMap(withdrawalMap -> withdrawalMap.keySet().stream())
        .collect(Collectors.toSet());
    Map<String, StakeAddress> stakeAddressMap = cachedStakeAddressRepository
        .findByHashRawIn(rewardAccounts)
        .stream()
        .collect(Collectors.toMap(StakeAddress::getHashRaw, Function.identity()));

    List<Withdrawal> withdrawals = txWithWithdrawalList.stream()
        .flatMap(aggregatedTx -> {
          Map<String, BigInteger> withdrawalMap = aggregatedTx.getWithdrawals();
          String txHash = aggregatedTx.getHash();
          Tx tx = txMap.get(txHash);

          return withdrawalMap.entrySet().stream().map(entry -> {
            String rewardAccount = entry.getKey();
            BigInteger amount = entry.getValue();
            Redeemer redeemer =
                redeemersMap.get(new RedeemerReference<>(RedeemerTag.Reward, rewardAccount));
            StakeAddress rewardAddress = stakeAddressMap.get(rewardAccount);
            if (Objects.isNull(rewardAddress)) {
              throw new IllegalStateException(
                  String.format("Stake address with address hex %s not found", rewardAccount));
            }
            return buildWithdrawal(rewardAddress, amount, tx, redeemer);
          });
        })
        .collect(Collectors.toList());

    cachedWithdrawalRepository.saveAll(withdrawals);
  }

  private Withdrawal buildWithdrawal(
      StakeAddress rewardAddress, BigInteger amount, Tx tx, Redeemer redeemer) {

    WithdrawalBuilder<?, ?> withdrawalBuilder = Withdrawal.builder();

    withdrawalBuilder.addr(rewardAddress);
    withdrawalBuilder.amount(amount);
    withdrawalBuilder.tx(tx);
    withdrawalBuilder.redeemer(redeemer);

    return withdrawalBuilder.build();
  }
}
