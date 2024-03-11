package org.cardanofoundation.rosetta.api.service;

import java.text.SimpleDateFormat;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.entity.*;
import org.cardanofoundation.rosetta.api.model.dto.*;
import org.cardanofoundation.rosetta.api.model.dto.TransactionDto;
import org.cardanofoundation.rosetta.api.repository.*;
import org.openapitools.client.model.Currency;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresLedgerDataProviderService implements LedgerDataProviderService {

  private final BlockRepository blockRepository;
  private final AddressBalanceRepository addressBalanceRepository;
  private final AddressUtxoRepository addressUtxoRepository;
  private final TxRepository txRepository;
  private final StakeRegistrationRepository stakeRegistrationRepository;
  private final DelegationRepository delegationRepository;
  private final PoolRegistrationRepository poolRegistrationRepository;
  private final PoolRetirementRepository poolRetirementRepository;
  private final StakeAddressRepository stakeAddressRepository;
  private final EpochParamRepository epochParamRepository;

  private final CardanoConfigService cardanoConfigService;

  @Override
  public GenesisBlockDto findGenesisBlock() {
    log.debug("[findGenesisBlock] About to run findGenesisBlock query");
    List<BlockEntity> blocks = blockRepository.findGenesisBlock();
    if(!blocks.isEmpty()) {
      BlockEntity genesis = blocks.getFirst();
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
      BlockEntity block = blocks.getFirst();
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
      AddressUtxoEntity first = addressUtxoRepository.findAddressUtxoEntitiesByOutputIndexAndTxHash(utxo.getOutputIndex(), utxo.getTxHash()).getFirst();
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
  public List<StakeAddressBalanceDTO> findStakeAddressBalanceByAddressAndBlock(String address, Long number) {
    List<StakeAddressBalanceEntity> balances = stakeAddressRepository.findStakeAddressBalanceByAddressAndBlockNumber(address, number);
    return balances.stream().map(StakeAddressBalanceDTO::fromEntity).toList();
  }

  @Override
  public Long findLatestBlockNumber() {
        return blockRepository.findLatestBlockNumber();
  }

  @Override
  public List<UtxoDto> findUtxoByAddressAndCurrency(String address, List<Currency> currencies) {
    List<AddressUtxoEntity> addressUtxoEntities = addressUtxoRepository.findUtxosByAddress(address);
    List<UtxoDto> utxos = new ArrayList<>();
    for(AddressUtxoEntity entity : addressUtxoEntities) {
      List<Amt> amountsToAdd = new ArrayList<>();
      for(Amt amt : entity.getAmounts()) {
        boolean addToList = currencies.isEmpty() || currencies.stream().anyMatch(currency -> currency.getSymbol().equals(amt.getUnit()));
        if(addToList) {
          amountsToAdd.add(amt);
        }
      }
      UtxoDto utxoDto = UtxoDto.fromUtxoKey(UtxoKey.builder().outputIndex(entity.getOutputIndex()).txHash(entity.getTxHash()).build());
      utxoDto.setAmounts(amountsToAdd);
      utxos.add(utxoDto);
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
      return Collections.emptyList();
    }
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(byNumberAndHash.getFirst().getHash());
    log.debug(
        "[findTransactionsByBlock] Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      return txList.stream().map(TransactionDto::fromTx).toList();
    }
    return Collections.emptyList();
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexer() {
    return epochParamRepository.findLatestProtocolParams();
  }

  @Override
  public ProtocolParams findProtolParametersFromConfig() {
    return cardanoConfigService.getProtocolParameters();
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexerAndConfig() {
    ProtocolParams protocolParams = findProtocolParametersFromIndexer();
    protocolParams.merge(findProtolParametersFromConfig());
    return protocolParams;
  }
}
