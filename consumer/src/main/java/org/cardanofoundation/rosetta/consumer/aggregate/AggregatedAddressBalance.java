package org.cardanofoundation.rosetta.consumer.aggregate;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.util.Pair;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatedAddressBalance {

  AggregatedAddress address;

  // Key is tx hash
  Map<byte[], AtomicReference<BigInteger>> txNativeBalance;

  // Key is a pair of tx hash and fingerprint
  Map<Pair<byte[], String>, AtomicReference<BigInteger>> maBalances;

  public static AggregatedAddressBalance from(String address) {
    AggregatedAddress aggregatedAddress = AggregatedAddress.from(address);
    return new AggregatedAddressBalance(
        aggregatedAddress, new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
  }

  /*
   * To minimize thread contention and blocking, the balance value must be wrapped with
   * atomic reference. This will make sure after every single balance calculating operation,
   * the new balance value is always available for other thread to use
   *
   * The non-conditional for block uses compare-and-swap technique for atomic calculation
   * Reference: https://en.wikipedia.org/wiki/Compare-and-swap
   */
  public void addNativeBalance(byte[] txHash, BigInteger balance) {
    AtomicReference<BigInteger> nativeBalanceReference = txNativeBalance
        .computeIfAbsent(txHash, s -> new AtomicReference<>(BigInteger.ZERO));
    for (; ; ) {
      BigInteger nativeBalance = nativeBalanceReference.get();
      if (nativeBalanceReference.compareAndSet(nativeBalance, nativeBalance.add(balance))) {
        return;
      }
    }
  }

  public void subtractNativeBalance(byte[] txHash, BigInteger balance) {
    AtomicReference<BigInteger> nativeBalanceReference = txNativeBalance
        .computeIfAbsent(txHash, s -> new AtomicReference<>(BigInteger.ZERO));
    for (; ; ) {
      BigInteger nativeBalance = nativeBalanceReference.get();
      if (nativeBalanceReference.compareAndSet(nativeBalance, nativeBalance.subtract(balance))) {
        return;
      }
    }
  }

  public void addAssetBalance(byte[] txHash, String fingerprint, BigInteger assetBalance) {
    Pair<byte[], String> key = Pair.of(txHash, fingerprint);
    AtomicReference<BigInteger> maBalanceReference = maBalances
        .computeIfAbsent(key, s -> new AtomicReference<>(BigInteger.ZERO));
    for (; ; ) {
      BigInteger maBalance = maBalanceReference.get();
      if (maBalanceReference.compareAndSet(maBalance, maBalance.add(assetBalance))) {
        return;
      }
    }
  }

  public void subtractAssetBalance(byte[] txHash, String fingerprint, BigInteger assetBalance) {
    Pair<byte[], String> key = Pair.of(txHash, fingerprint);
    AtomicReference<BigInteger> maBalanceReference = maBalances
        .computeIfAbsent(key, s -> new AtomicReference<>(BigInteger.ZERO));
    for (; ; ) {
      BigInteger maBalance = maBalanceReference.get();
      if (maBalanceReference.compareAndSet(maBalance, maBalance.subtract(assetBalance))) {
        return;
      }
    }
  }

  public boolean isAddressHasScript() {
    return address.isAddressHasScript();
  }

  public int getTxCount() {
    return txNativeBalance.size();
  }

  public BigInteger getTotalNativeBalance() {
    return txNativeBalance.values().stream()
        .map(AtomicReference::get)
        .reduce(BigInteger.ZERO, BigInteger::add);
  }
}
