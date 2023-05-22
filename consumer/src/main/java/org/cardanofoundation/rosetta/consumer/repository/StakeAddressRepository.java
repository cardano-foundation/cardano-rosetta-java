package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StakeAddressRepository extends JpaRepository<StakeAddress, Long> {

  @Transactional(readOnly = true)
  Optional<StakeAddress> findByHashRaw(String hashRaw);

  @Transactional(readOnly = true)
  List<StakeAddress> findByHashRawIn(Collection<String> hashRaw);
}