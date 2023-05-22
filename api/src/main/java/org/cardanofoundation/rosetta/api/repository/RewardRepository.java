package org.cardanofoundation.rosetta.api.repository;


import org.cardanofoundation.rosetta.common.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardRepository extends JpaRepository<Reward, Long> {

//    @Query(value = "SELECT (SELECT COALESCE(SUM(r.amount),0) " +
//            "FROM reward r " +
//            "JOIN stake_address ON stake_address.id = r.addr_id " +
//            "WHERE stake_address.view = :stakeAddress " +
//            "AND r.spendable_epoch <= (SELECT epoch_no FROM block WHERE hash = :blockHash) " +
//            ") - ( " +
//            "SELECT COALESCE(SUM(w.amount),0) " +
//            "FROM withdrawal w " +
//            "JOIN tx ON tx.id = w.tx_id " +
//            "   AND tx.valid_contract = TRUE " +
//            "   AND tx.block_id <= (SELECT id FROM block WHERE hash = :blockHash) " +
//            "JOIN stake_address ON stake_address.id = w.addr_id " +
//            "WHERE stake_address.view = :stakeAddress" +
//            ") AS balance", nativeQuery = true)
//    Double findBalanceByAddressAndBlock(@Param("stakeAddress") String stakeAddress,
//                                        @Param("blockHash") String blockHash);

  @Query("SELECT COALESCE(SUM(r.amount), 0) " +
      "FROM Reward r " +
      "   JOIN StakeAddress s ON s.id = r.addr.id " +
      "WHERE s.view = :stakeAddress " +
      "AND r.spendableEpoch <= (SELECT b.epochNo FROM Block b WHERE b.hash = :blockHash)")
  Double findBalanceByAddressAndBlockSub1(@Param("stakeAddress") String stakeAddress,
      @Param("blockHash") String blockHash);

  @Query("SELECT COALESCE(SUM(w.amount), 0) " +
      "FROM Withdrawal w " +
      "JOIN Tx tx on tx = w.tx " +
      "   AND tx.validContract = TRUE " +
      "   AND tx.block.id <= (SELECT b2.id FROM Block b2 WHERE b2.hash = :blockHash)" +
      "JOIN StakeAddress s ON s.id = w.addr.id " +
      "WHERE s.view = :stakeAddress ")
  Double findBalanceByAddressAndBlockSub2(@Param("stakeAddress") String stakeAddress,
      @Param("blockHash") String blockHash);

}