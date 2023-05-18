package org.cardanofoundation.rosetta.crawler.construction.data.repository;

import org.cardanofoundation.rosetta.crawler.construction.data.entity.Block;
import org.cardanofoundation.rosetta.crawler.construction.data.response.BlockResponse;
import org.cardanofoundation.rosetta.crawler.construction.data.ProtocolParametersResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block,Long> {
    @Query(value = "SELECT\n" +
            "  block_no as \"blockHeight\"\n" +
            "FROM block\n" +
            "WHERE block_no IS NOT NULL\n" +
            "ORDER BY block_no DESC\n" +
            "LIMIT 1",nativeQuery = true)
    Long findLatestBlockNumber();

    @Query(value="SELECT \n" +
            "  b.hash as hash,\n" +
            "  b.block_no as number,\n" +
            "  (b.time at time zone 'utc') as \"createdAt\",\n" +
            "  CASE\n" +
            "    WHEN b2.block_no IS NOT NULL THEN b2.block_no\n" +
            "    WHEN b3.block_no IS NOT NULL THEN b3.block_no\n" +
            "    ELSE 0\n" +
            "  END AS \"previousBlockNumber\",\n" +
            "  CASE\n" +
            "    WHEN b2.block_no IS NOT NULL THEN b2.hash\n" +
            "    WHEN b3.block_no IS NOT NULL THEN b3.hash\n" +
            "    WHEN b.block_no = 1 THEN b3.hash -- block 1\n" +
            "    ELSE b.hash -- genesis\n" +
            "  END AS \"previousBlockHash\",\n" +
            "  b.tx_count as \"transactionsCount\",\n" +
            "  s.description as \"createdBy\",\n" +
            "  b.size as size,\n" +
            "  b.epoch_no as \"epochNo\",\n" +
            "  b.slot_no as \"slotNo\"\n" +
            "FROM \n" +
            "  block b \n" +
            "  LEFT JOIN slot_leader s ON b.slot_leader_id = s.id\n" +
            "  LEFT JOIN block b2 ON b.previous_id = b2.id\n" +
            "  LEFT JOIN block b3 ON b2.previous_id = b3.id\n" +
            "WHERE\n" +
            "  (:blockNumber is null or b.block_no=:blockNumber) AND\n" +
            "  (:blockHash is null or b.hash like :blockHash)\n" +
            "LIMIT 1",nativeQuery = true)
    BlockResponse findBlock(@Param("blockNumber")Long blockNumber,@Param("blockHash")byte[] blockHash);

    @Query(value = "  SELECT \n" +
            "    coins_per_utxo_size as coinsPerUtxoSize,\n" +
            "    max_tx_size as maxTxSize ,\n" +
            "    max_val_size as maxValSize,\n" +
            "    key_deposit as keyDeposit ,\n" +
            "    max_collateral_inputs as maxCollateralInputs ,\n" +
            "    min_fee_a as minFeeCoefficient,\n" +
            "    min_fee_b as minFeeConstant,\n" +
            "    min_pool_cost as minPoolCost,\n" +
            "    pool_deposit as poolDeposit ,\n" +
            "    protocol_major as protocolMajor\n" +
            "  FROM epoch_param  \n" +
            "  ORDER BY id \n" +
            "  DESC LIMIT 1",nativeQuery = true)
    ProtocolParametersResponse findProtocolParameters();
}
