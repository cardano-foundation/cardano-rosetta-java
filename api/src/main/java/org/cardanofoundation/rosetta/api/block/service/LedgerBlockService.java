package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

public interface LedgerBlockService {

  /**
   * Returns a block by its number and hash. Including all populated Transactions.
   *
   * @param number block number
   * @param hash   block hash
   * @return the block if found or empty otherwise
   */
  Optional<Block> findBlock(Long number, String hash);


  /**
   * Returns a list of all transactions within a block. The UTXO aren't populated yet. They contain only the hash and the index.
   * @param number block number
   * @param hash block hash
   * @return the list of transactions
   */
  List<BlockTx> findTransactionsByBlock(Long number, String hash);

  List<BlockTx> mapTxnEntitiesToBlockTxList(List<TxnEntity> txList);

  Page<BlockTx> mapTxnEntitiesToBlockTxList(Page<TxnEntity> txList);

  /**
   * Returns the latest block.
   * @return block domain model
   */
  Block findLatestBlock();

  /**
   * Returns the latest block identifier.
   * @return the latest block identifier
   */
  BlockIdentifierExtended findLatestBlockIdentifier();

  /**
   * Returns the oldest full block (block for which we have full data)
   * @return the oldest block identifier
   */
  BlockIdentifierExtended findOldestBlockIdentifier(BlockIdentifierExtended latestBlock);

  /**
   * Returns the genesis block identifier.
   * @return the genesis block identifier
   */
  BlockIdentifierExtended findGenesisBlockIdentifier();

  /**
   * Returns a block identifier by its number and hash.
   *
   * @param number block number
   * @param hash block hash
   * @return the block identifier if found or empty otherwise
   */
  Optional<BlockIdentifierExtended> findBlockIdentifier(Long number, String hash);

}
