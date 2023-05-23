package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.common.entity.Block;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
  Optional<Block> findBlockByHash(String hash);

  Optional<Block> findBlockByBlockNo(long number);

  boolean existsBlockByHash(String hash);

  List<Block> findAllByBlockNoGreaterThanOrderByBlockNoDesc(Long blockNo);

  @Query("SELECT MAX(block.blockNo) FROM Block block")
  Optional<Long> getBlockHeight();

  Optional<Block> findFirstByEpochNo(Integer epochNo);
}
