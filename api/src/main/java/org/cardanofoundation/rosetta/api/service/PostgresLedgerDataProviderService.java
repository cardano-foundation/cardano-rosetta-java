package org.cardanofoundation.rosetta.api.service;

import jakarta.annotation.PostConstruct;

import java.text.SimpleDateFormat;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.dto.*;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.repository.*;
import org.cardanofoundation.rosetta.common.model.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresLedgerDataProviderService implements LedgerDataProviderService {

  private final Map<String, PostgresLedgerDataProviderClient> clients = new HashMap<>();
  private final RosettaConfig rosettaConfig;
  private final BlockRepository blockRepository;
  private final AddressBalanceRepository addressBalanceRepository;
  private final AddressUtxoRepository addressUtxoRepository;
  private final TxRepository txRepository;
  private final StakeRegistrationRepository stakeRegistrationRepository;
  private final DelegationRepository delegationRepository;
  private final PoolRegistrationRepository poolRegistrationRepository;
  private final PoolRetirementRepository poolRetirementRepository;

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
    List<BlockEntity> blocks = blockRepository.findGenesisBlock();
    if(!blocks.isEmpty()) {
      BlockEntity genesis = blocks.get(0);
      return GenesisBlockDto.builder().hash(genesis.getHash())
          .number(genesis.getNumber())
          .build();
    }
    log.debug("[findGenesisBlock] Genesis block was not found");
    return null;
  }

  @Override
  public BlockDto findBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findBlock] Parameters received for run query blockNumber: {} , blockHash: {}",
        blockNumber, blockHash);
    List<BlockEntity> blocks;
    if(blockHash == null && blockNumber != null) {
      blocks = blockRepository.findByNumber(blockNumber);
    } else if(blockHash != null && blockNumber == null){
      blocks = blockRepository.findByHash(blockHash);
    } else {
      blocks = blockRepository.findByNumberAndHash(blockNumber, blockHash);
    }
    if (!blocks.isEmpty()) {
      log.debug("[findBlock] Block found!");
      BlockEntity block = blocks.get(0);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      // Populating transactions
      BlockDto blockDto = BlockDto.fromBlock(block);
      for (TransactionDto transaction : blockDto.getTransactions()) {
        populateUtxos(transaction.getInputs());
        populateUtxos(transaction.getOutputs());
        transaction.setStakeRegistrations(
                stakeRegistrationRepository.findByTxHash(transaction.getHash())
                        .stream().map(StakeRegistrationDTO::fromEntity).toList()); // TODO Refacotring - do this via JPA
        transaction.setDelegations(delegationRepository.findByTxHash(transaction.getHash())
                .stream().map(DelegationDTO::fromEntity).toList()); // TODO Refacotring - do this via JPA
        transaction.setPoolRegistrations(poolRegistrationRepository.findByTxHash(transaction.getHash())
                .stream().map(PoolRegistrationDTO::fromEntity).toList()); // TODO Refacotring - do this via JPA
        transaction.setPoolRetirements(poolRetirementRepository.findByTxHash(transaction.getHash())
                .stream().map(PoolRetirementDTO::fromEntity).toList()); // TODO Refacotring - do this via JPA
      }
      return blockDto;
    }
    log.debug("[findBlock] No block was found");
    return null;
  }

  private void populateUtxos(List<UtxoDto> inputs) {
    for (UtxoDto utxo : inputs) {
      AddressUtxoEntity first = addressUtxoRepository.findAddressUtxoEntitiesByOutputIndexAndTxHash(utxo.getOutputIndex(), utxo.getTxHash()).get(0);
      if(first != null) {
        utxo.populateFromUtxoEntity(first);
      }
    }
  }

  @Override
  public List<AddressBalanceDTO> findBalanceByAddressAndBlock(String address, Long number) {
    List<AddressBalanceEntity> balances = addressBalanceRepository.findAdressBalanceByAddressAndBlockNumber(address, number);
    return balances.stream().map(AddressBalanceDTO::fromEntity).toList();
  }

  @Override
  public Long findLatestBlockNumber() {
        return blockRepository.findLatestBlockNumber();
  }

  @Override
  public List<Utxo> findUtxoByAddressAndBlock(String address, String hash, List<Currency> currencies) {
    List<AddressUtxoEntity> addressUtxoEntities = addressUtxoRepository.findUtxoByAddressAndBlock(address, hash);
    List<Utxo> utxos = new ArrayList<>();
    for(AddressUtxoEntity entity : addressUtxoEntities) {
      for(Amt amt : entity.getAmounts()) {
        boolean present = currencies.stream().filter(currency -> currency.getSymbol().equals(amt.getUnit())).findFirst().isPresent();
        if(present) {
          Utxo utxo = Utxo.builder()
                  .policy(amt.getPolicyId())
//                  .value() // TODO
                  .index(entity.getOutputIndex())
                  .name(amt.getAssetName())
                  .quantity(amt.getQuantity().toString())
                  .transactionHash(entity.getTxHash())
                  .build();
          utxos.add(utxo);
        }
      }
    }
    return utxos;
  }

  @Override
  public BlockDto findLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    Long latestBlockNumber = findLatestBlockNumber();
    log.info("[getLatestBlock] Latest block number is {}", latestBlockNumber);
    BlockDto latestBlock = findBlock(latestBlockNumber, null);
    if (Objects.isNull(latestBlock)) {
      log.error("[getLatestBlock] Latest block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.debug("[getLatestBlock] Returning latest block {}", latestBlock);
    return latestBlock;
  }

  @Override
  public List<TransactionDto> findTransactionsByBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findTransactionsByBlock] Parameters received for run query blockNumber: {} blockHash: {}",
        blockNumber, blockHash);

    List<BlockEntity> byNumberAndHash = blockRepository.findByNumberAndHash(blockNumber, blockHash);
    if(byNumberAndHash.isEmpty()) {
      log.debug(
          "[findTransactionsByBlock] No block found for blockNumber: {} blockHash: {}",
          blockNumber, blockHash);
      return null;
    }
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(byNumberAndHash.get(0).getHash());
    log.debug(
        "[findTransactionsByBlock] Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      return txList.stream().map(TransactionDto::fromTx).toList();
    }
    return null;
  }
}
