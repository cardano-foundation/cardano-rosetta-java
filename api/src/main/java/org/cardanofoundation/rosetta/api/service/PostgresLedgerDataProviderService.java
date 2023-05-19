package org.cardanofoundation.rosetta.api.service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.projection.BlockProjection;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockProjection;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.repository.RewardRepository;
import org.cardanofoundation.rosetta.api.repository.TxRepository;
import org.cardanofoundation.rosetta.api.repository.customRepository.UtxoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostgresLedgerDataProviderService implements LedgerDataProviderService {

  private final Map<String, PostgresLedgerDataProviderClient> clients = new HashMap<>();
  @Autowired
  private RosettaConfig rosettaConfig;
  @Autowired
  private BlockRepository blockRepository;
  @Autowired
  private RewardRepository rewardRepository;
  @Autowired
  private UtxoRepository utxoRepository;
  @Autowired
  private TxRepository txRepository;

  @PostConstruct
  void init() {
    rosettaConfig.getNetworks().forEach(networkConfig -> {
      clients.put(networkConfig.getSanitizedNetworkId(), PostgresLedgerDataProviderClient.builder()
          .networkId(networkConfig.getSanitizedNetworkId()).build());
    });
  }

  @Override
  public BlockIdentifier getTip(final String networkId) {
    if (clients.containsKey(networkId)) {
      return clients.get(networkId).getTip();
    }

    throw new IllegalArgumentException("Invalid network id specified.");
  }

  @Override
  public GenesisBlockDto findGenesisBlock() {
    log.debug("[findGenesisBlock] About to run findGenesisBlock query");
    Page<GenesisBlockProjection> genesisBlockProjectionPage = blockRepository.findGenesisBlock(
        PageRequest.of(0, 1));
    if (!genesisBlockProjectionPage.getContent().isEmpty()) {
      log.debug(
          "[GenesisBlockProjectionPage] is hash : " + genesisBlockProjectionPage.getContent().get(0)
              .getHash());

      GenesisBlockProjection genesis = genesisBlockProjectionPage
          .getContent()
          .get(0);
      return GenesisBlockDto.builder().hash(genesis.getHash())
          .number(genesis.getBlockNo())
          .build();
    }
    log.debug("[findGenesisBlock] Genesis block was not found");
    return null;
  }

  @Override
  public BlockDto findBlock(Long blockNumber, String blockHash) {
    log.debug("[findBlock] Parameters received for run query blockNumber: " + blockNumber
        + " , blockHash: " + blockHash);
    List<BlockProjection> blockProjections = blockRepository.findBlock(blockNumber,
        blockHash);
    log.debug("[findBlock] blockProjections size is: " + blockProjections.size());
    if (blockProjections.size() == 1) {
      log.debug("[findBlock] Block found!");
      BlockProjection blockProjection = blockProjections.get(0);
      log.debug("[findBlock] Block is number: " + blockProjection.getNumber() + " hash: "
          + blockProjection.getHash());
      return BlockDto.builder()
          .number(blockProjection.getNumber())
          .hash(blockProjection.getHash())
          .createdAt(blockProjection.getCreatedAt().getTime())
          .previousBlockHash(blockProjection.getPreviousBlockHash())
          .previousBlockNumber(blockProjection.getPreviousBlockNumber())
          .transactionsCount(blockProjection.getTransactionsCount())
          .createdBy(blockProjection.getCreatedBy())
          .size(blockProjection.getSize())
          .epochNo(blockProjection.getEpochNo())
          .slotNo(blockProjection.getSlotNo())
          .build();
    }
    log.debug("[findBlock] No block was found");
    return null;
  }

  @Override
  public Double findBalanceByAddressAndBlock(String address, String hash) {
    return rewardRepository.findBalanceByAddressAndBlockSub1(
        address, hash)
        - rewardRepository.findBalanceByAddressAndBlockSub2(
        address, hash);
  }

  @Override
  public List<Utxo> findUtxoByAddressAndBlock(String address, String hash,
      List<Currency> currencies) {

    return utxoRepository.findUtxoByAddressAndBlock(address, hash, currencies);
  }

  @Override
  public Long findLatestBlockNumber() {
    Page<Long> latestBlockNumberPage = blockRepository.findLatestBlockNumber(
        PageRequest.of(0, 1));
    if (ObjectUtils.isNotEmpty(latestBlockNumberPage.getContent())) {
      return latestBlockNumberPage.getContent().get(0);
    }
    return null;
  }

  @Override
  public List<MaBalance> findMaBalanceByAddressAndBlock(String address, String hash) {
    return utxoRepository.findMaBalanceByAddressAndBlock(address, hash);
  }

  @Override
  public BlockDto findLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    Long latestBlockNumber = findLatestBlockNumber();
    log.info("[getLatestBlock] Latest block number is " + latestBlockNumber);
    BlockDto latestBlock = findBlock(latestBlockNumber, null);
    if(latestBlock == null){
      log.error("[getLatestBlock] Latest block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.debug("[getLatestBlock] Returning latest block " + latestBlock);
    return latestBlock;
  }
}
