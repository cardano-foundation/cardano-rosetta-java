package org.cardanofoundation.rosetta.consumer.repository;

import com.sotatek.cardano.common.entity.Block;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlockRepository extends JpaRepository<Block, Long> {
  Optional<Block> findBlockByHash(String hash);

  boolean existsBlockByHash(String hash);

  @Query("SELECT MAX(block.blockNo) FROM Block block")
  Optional<Long> getBlockHeight();

}
