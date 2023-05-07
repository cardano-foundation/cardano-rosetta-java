package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.crypto.Bech32;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.common.entity.PoolMetadataRef;
import org.cardanofoundation.rosetta.common.entity.PoolOwner;
import org.cardanofoundation.rosetta.common.entity.PoolRelay;
import org.cardanofoundation.rosetta.common.entity.PoolRelay.PoolRelayBuilder;
import org.cardanofoundation.rosetta.common.entity.PoolUpdate;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.common.ledgersync.PoolParams;
import org.cardanofoundation.rosetta.common.ledgersync.Relay;
import org.cardanofoundation.rosetta.common.ledgersync.certs.PoolRegistration;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredential;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredentialType;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolHashRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolMetadataRefRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolOwnerRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolRelayRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolUpdateRepository;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PoolRegistrationServiceImpl extends CertificateSyncService<PoolRegistration> {

  CachedPoolHashRepository cachedPoolHashRepository;
  CachedPoolMetadataRefRepository cachedPoolMetadataRefRepository;
  CachedPoolUpdateRepository cachedPoolUpdateRepository;
  CachedPoolOwnerRepository cachedPoolOwnerRepository;
  CachedPoolRelayRepository cachedPoolRelayRepository;
  StakeAddressService stakeAddressService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      PoolRegistration certificate, int certificateIdx,
      Tx tx, Redeemer redeemer) {
    var hexBytes = certificate.getPoolParams().getOperator();
    PoolHash poolHash = getOrInsertPoolHash(hexBytes,aggregatedBlock.getEpochNo());
    PoolMetadataRef poolMetadata = insertPoolMetadataRef(certificate.getPoolParams(), poolHash, tx);
    int epochActivationDelay = getEpochActivationDelay(poolHash);
    int activeEpochNo = epochActivationDelay + aggregatedBlock.getEpochNo();

    // Workaround for bug https://github.com/input-output-hk/cardano-db-sync/issues/546
    String rewardAccountHex = certificate.getPoolParams().getRewardAccount();
    byte[] rewardAccountBytes = HexUtil.decodeHexString(rewardAccountHex);
    int networkId = Constant.isTestnet(aggregatedBlock.getNetwork())? 0 : 1;
    byte header = rewardAccountBytes[0];
    if (((header & 0xff) & networkId) == 0) {
      rewardAccountBytes[0] = (byte) ((header & ~1) | networkId);
    }

    rewardAccountHex = HexUtil.encodeHexString(rewardAccountBytes);
    StakeAddress rewardAccount = stakeAddressService.getStakeAddress(rewardAccountHex);
    PoolUpdate poolUpdate = insertPoolUpdate(certificate.getPoolParams(),
        activeEpochNo, certificateIdx, poolHash, rewardAccount, poolMetadata, tx);
    insertPoolOwners(aggregatedBlock.getNetwork(), certificate.getPoolParams(), poolUpdate);
    insertPoolRelays(certificate.getPoolParams(), poolUpdate);
  }

  private int getEpochActivationDelay(PoolHash poolHash) {
    Boolean otherUpdateExist = cachedPoolUpdateRepository.existsByPoolHash(poolHash);

    // If the pool is first registered, return 2, else 3
    // This method is currently not accurate. Real world case requires
    // communicating with node. Will be re-implemented after implementing
    // local state query mini-protocol
    return Boolean.FALSE.equals(otherUpdateExist) ? 2 : 3;
  }

  // Insert new or get pool hash from DB
  private PoolHash getOrInsertPoolHash(String hexBytes, int epochNo) {
    return cachedPoolHashRepository.findPoolHashByHashRaw(hexBytes)
        .orElseGet(() -> {
          PoolHash newPoolHash = buildPoolHash(hexBytes,epochNo);
          return cachedPoolHashRepository.save(newPoolHash);
        });
  }

  private PoolMetadataRef insertPoolMetadataRef(PoolParams poolParams, PoolHash poolHash, Tx tx) {
    if (!StringUtils.hasText(poolParams.getPoolMetadataUrl())) {
      return null;
    }

    return cachedPoolMetadataRefRepository
        .findPoolMetadataRefByPoolHashAndUrlAndHash(
            poolHash, poolParams.getPoolMetadataUrl(), poolParams.getPoolMetadataHash())
        .orElseGet(() -> {
          PoolMetadataRef poolMetadataRef = buildPoolMetadataRef(poolParams, poolHash, tx);
          return cachedPoolMetadataRefRepository.save(poolMetadataRef);
        });
  }

  private PoolUpdate insertPoolUpdate(PoolParams poolParams,
      int activeEpochNo, int certificateIdx, PoolHash poolHash,
      StakeAddress rewardAccount, PoolMetadataRef poolMetadata, Tx tx) {
    PoolUpdate poolUpdate = PoolUpdate.builder()
        .poolHash(poolHash)
        .certIndex(certificateIdx)
        .vrfKeyHash(poolParams.getVrfKeyHash())
        .pledge(poolParams.getPledge())
        .rewardAddr(rewardAccount)
        .activeEpochNo(activeEpochNo)
        .meta(poolMetadata)
        .margin(parseMargin(poolParams.getMargin()))
        .fixedCost(poolParams.getCost())
        .registeredTx(tx)
        .build();

    return cachedPoolUpdateRepository.save(poolUpdate);
  }

  private void insertPoolOwners(int network, PoolParams poolParams, PoolUpdate poolUpdate) {
    List<PoolOwner> poolOwners = poolParams.getPoolOwners().stream()
        .map(poolOwnerHash -> {
          String stakeAddressHex = AddressUtil.getRewardAddressString(
              new StakeCredential(StakeCredentialType.ADDR_KEYHASH, poolOwnerHash), network);
          StakeAddress stakeAddress = stakeAddressService.getStakeAddress(stakeAddressHex);
          return buildPoolOwner(stakeAddress, poolUpdate);
        }).collect(Collectors.toList());

    cachedPoolOwnerRepository.saveAll(poolOwners);
  }

  private static PoolHash buildPoolHash(String hexBytes, int epochNo) {
    PoolHash newPoolHash = new PoolHash();
    newPoolHash.setHashRaw(hexBytes);
    newPoolHash.setView(
        Bech32.encode(HexUtil.decodeHexString(hexBytes), ConsumerConstant.POOL_HASH_PREFIX));
    newPoolHash.setPoolSize(BigInteger.ZERO); //TODO hardcode pool size
    newPoolHash.setEpochNo(epochNo);//TODO verify
    return newPoolHash;
  }

  private static PoolMetadataRef buildPoolMetadataRef(
      PoolParams poolParams, PoolHash poolHash, Tx tx) {
    return PoolMetadataRef.builder()
        .poolHash(poolHash)
        .url(poolParams.getPoolMetadataUrl())
        .hash(poolParams.getPoolMetadataHash())
        .registeredTx(tx)
        .build();
  }

  private PoolOwner buildPoolOwner(StakeAddress poolOwnerStakeAddress, PoolUpdate poolUpdate) {
    return PoolOwner.builder()
        .stakeAddress(poolOwnerStakeAddress)
        .poolUpdate(poolUpdate)
        .build();
  }

  private void insertPoolRelays(PoolParams poolParams, PoolUpdate poolUpdate) {
    List<PoolRelay> poolRelays = poolParams.getRelays().stream()
        .map(relay -> buildPoolRelay(relay, poolUpdate))
        .collect(Collectors.toList());

    cachedPoolRelayRepository.saveAll(poolRelays);
  }

  private PoolRelay buildPoolRelay(Relay relay, PoolUpdate poolUpdate) {
    PoolRelayBuilder<?, ?> poolRelayBuilder = PoolRelay.builder();

    poolRelayBuilder.poolUpdate(poolUpdate);

    if (Objects.nonNull(relay.getIpv4())) {
      poolRelayBuilder.ipv4(relay.getIpv4());
      poolRelayBuilder.ipv6(relay.getIpv6());
    }

    if (Objects.nonNull(relay.getDnsName())) {
      if (Objects.nonNull(relay.getPort())) {
        poolRelayBuilder.port(relay.getPort());
        poolRelayBuilder.dnsName(relay.getDnsName());
      } else {
        poolRelayBuilder.dnsSrvName(relay.getDnsName());
      }
    }

    return poolRelayBuilder.build();
  }

  private Double parseMargin(String margin) {
    String[] values = margin.split("/");
    double divider = Double.parseDouble(values[0]);
    double dividend = Double.parseDouble(values[1]);
    return divider / dividend;
  }
}
