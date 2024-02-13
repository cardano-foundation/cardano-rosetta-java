package org.cardanofoundation.rosetta.api.repository;


import org.cardanofoundation.rosetta.common.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardRepository extends JpaRepository<Reward, Long> {
  @Query("SELECT COALESCE(SUM(r.amount), 0) "
      + "FROM Reward r "
      + "   JOIN StakeAddress s ON s.id = r.addr.id "
      + "WHERE s.view = :stakeAddress "
      + "AND r.spendableEpoch <= (SELECT b.epoch FROM Block b WHERE b.hash = :blockHash)")
  Long findRewardBalanceByAddressAndBlock(@Param("stakeAddress") String stakeAddress,
      @Param("blockHash") String blockHash);

  @Query("SELECT COALESCE(SUM(w.amount), 0) "
      + "FROM Withdrawal w "
      + "JOIN Tx tx on tx = w.tx "
      + "   AND tx.validContract = TRUE "
      + "   AND tx.block.id <= (SELECT b2.id FROM Block b2 WHERE b2.hash = :blockHash)"
      + "JOIN StakeAddress s ON s.id = w.addr.id "
      + "WHERE s.view = :stakeAddress ")
  Long findWithdrwalBalanceByAddressAndBlock(@Param("stakeAddress") String stakeAddress,
      @Param("blockHash") String blockHash);

}