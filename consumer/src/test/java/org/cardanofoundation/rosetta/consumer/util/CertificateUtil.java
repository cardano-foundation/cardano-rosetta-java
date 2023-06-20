package org.cardanofoundation.rosetta.consumer.util;

import java.util.Set;
import org.cardanofoundation.rosetta.common.ledgersync.PoolParams;
import org.cardanofoundation.rosetta.common.ledgersync.certs.PoolRegistration;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredential;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredentialType;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDelegation;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeRegistration;

public final class CertificateUtil {

  private CertificateUtil() {}

  public static StakeRegistration buildStakeRegistrationCert(int certIdx,
                                                             StakeCredentialType type,
                                                             String hash) {
    StakeCredential credential = new StakeCredential(type, hash);
    StakeRegistration stakeRegistration = new StakeRegistration();
    stakeRegistration.setStakeCredential(credential);
    stakeRegistration.setIndex(certIdx);
    return stakeRegistration;
  }

  public static PoolRegistration buildPoolRegistrationCert(int certIdx,
                                                           String rewardAccount,
                                                           String... poolOwners) {
    PoolParams poolParams = PoolParams.builder()
        .rewardAccount(rewardAccount)
        .poolOwners(Set.of(poolOwners))
        .build();

    PoolRegistration poolRegistration = PoolRegistration.builder()
        .poolParams(poolParams)
        .build();
    poolRegistration.setIndex(certIdx);
    return poolRegistration;
  }

  public static StakeDelegation buildStakeDelegationCert(int certIdx,
                                                         StakeCredentialType type,
                                                         String hash) {
    StakeCredential credential = new StakeCredential(type, hash);
    StakeDelegation stakeDelegation = new StakeDelegation();
    stakeDelegation.setStakeCredential(credential);
    stakeDelegation.setIndex(certIdx);
    return stakeDelegation;
  }
}
