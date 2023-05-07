package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.PoolHash;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PoolHashRepository extends JpaRepository<PoolHash, Long> {

  @Transactional(readOnly = true)
  Optional<PoolHash> findPoolHashByHashRaw(String hashBytes);
}
