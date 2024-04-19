package org.cardanofoundation.rosetta.common.services.impl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.rosetta.api.account.model.entity.AmtEntity;
import org.cardanofoundation.rosetta.api.block.mapper.WithdrawalEntityToWithdrawal;
import org.cardanofoundation.rosetta.api.block.model.repository.WithdrawalRepository;
import org.cardanofoundation.rosetta.common.mapper.AmtEntityToAmt;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Currency;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.StakeAddressBalanceEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressBalanceRepository;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.BlockTxToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.WithdrawalEntityToWithdrawal;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.block.model.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.DelegationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRetirementRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.StakeAddressRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.StakeRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.WithdrawalRepository;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsToEntity;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.Formatters;

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
  private final WithdrawalRepository withdrawalRepository;

  private final ProtocolParamService protocolParamService;

  private final ModelMapper mapper;
  private final BlockToEntity mapperBlock;
  private final BlockTxToEntity mapperTran;
  private final ProtocolParamsToEntity mapperProtocolParams;
  private final WithdrawalEntityToWithdrawal withdrawalEntityToWithdrawal;

  @Override
  public GenesisBlock findGenesisBlock() {
    log.debug("[findGenesisBlock] About to run findGenesisBlock query");
    List<BlockEntity> blocks = blockRepository.findGenesisBlock();
    if (!blocks.isEmpty()) {
      BlockEntity genesis = blocks.getFirst();
      return GenesisBlock.builder().hash(genesis.getHash())
          .number(genesis.getNumber())
          .build();
    }
    log.debug("[findGenesisBlock] Genesis block was not found");
    return null;
  }

  @Override
  public Block findBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findBlock] Parameters received for run query blockNumber: {} , blockHash: {}",
        blockNumber, blockHash);
    List<BlockEntity> blocks;
    if (blockHash == null && blockNumber != null) {
      blocks = blockRepository.findByNumber(blockNumber);
    } else if (blockHash != null && blockNumber == null) {
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
      Block model = mapperBlock.fromEntity(block);
      populateTransactions(model.getTransactions());
      return model;
    }
    log.debug("[findBlock] No block was found");
    return null;
  }

  private void populateTransactions(List<BlockTx> transactions) {
    for (BlockTx transaction : transactions) {
      populateUtxos(transaction.getInputs());
      populateUtxos(transaction.getOutputs());
      transaction.setStakeRegistrations(
          stakeRegistrationRepository.findByTxHash(transaction.getHash())
              .stream().map(StakeRegistration::fromEntity)
              .toList()); // TODO Refacotring - do this via JPA
      transaction.setDelegations(delegationRepository.findByTxHash(transaction.getHash())
          .stream().map(Delegation::fromEntity).toList()); // TODO Refacotring - do this via JPA
      transaction.setPoolRegistrations(
          poolRegistrationRepository.findByTxHash(transaction.getHash())
              .stream().map(PoolRegistration::fromEntity)
              .toList()); // TODO Refacotring - do this via JPA
      transaction.setPoolRetirements(poolRetirementRepository.findByTxHash(transaction.getHash())
              .stream().map(PoolRetirement::fromEntity).toList()); // TODO Refacotring - do this via JPA
      transaction.setWithdrawals(withdrawalRepository.findByTxHash(transaction.getHash())
              .stream().map(withdrawalEntityToWithdrawal::fromEntity).toList()); // TODO Refacotring - do this via JPA

      ProtocolParams protocolParametersFromIndexerAndConfig = findProtocolParametersFromIndexerAndConfig();
      transaction.setSize((Long.parseLong(transaction.getFee()) - protocolParametersFromIndexerAndConfig.getMinFeeB().longValue()) / protocolParametersFromIndexerAndConfig.getMinFeeA().longValue());
    }
  }

  private void populateUtxos(List<Utxo> inputs) {
    for (Utxo utxo : inputs) {
      AddressUtxoEntity first = addressUtxoRepository.findAddressUtxoEntitiesByOutputIndexAndTxHash(
          utxo.getOutputIndex(), utxo.getTxHash()).getFirst();
      if (first != null) {
        // Populating the values from entity to model
        mapper.map(first, utxo);
      }
    }
  }

  @Override
  public List<AddressBalance> findBalanceByAddressAndBlock(String address, Long number) {
    List<AddressBalanceEntity> balances = addressBalanceRepository.findAddressBalanceByAddressAndBlockNumber(
        address, number);
    return balances.stream().map(AddressBalance::fromEntity).toList();
  }

  @Override
  public List<StakeAddressBalance> findStakeAddressBalanceByAddressAndBlock(String address,
      Long number) {
    List<StakeAddressBalanceEntity> balances = stakeAddressRepository.findStakeAddressBalanceByAddressAndBlockNumber(
        address, number);
    return balances.stream().map(StakeAddressBalance::fromEntity).toList();
  }

  @Override
  public Long findLatestBlockNumber() {
    return blockRepository.findLatestBlockNumber();
  }

  @Override
  public List<Utxo> findUtxoByAddressAndCurrency(String address, List<Currency> currencies) {
    List<AddressUtxoEntity> addressUtxoEntities = addressUtxoRepository.findUtxosByAddress(address);
    return addressUtxoEntities.stream()
        .map(entity -> createUtxoModel(currencies, entity))
        .toList();
  }

  @Override
  public Block findLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    Long latestBlockNumber = findLatestBlockNumber();
    log.info("[getLatestBlock] Latest block number is {}", latestBlockNumber);
    Block latestBlock = findBlock(latestBlockNumber, null);
    if (Objects.isNull(latestBlock)) {
      log.error("[getLatestBlock] Latest block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.debug("[getLatestBlock] Returning latest block {}", latestBlock);
    return latestBlock;
  }

  @Override
  public List<BlockTx> findTransactionsByBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findTransactionsByBlock] Parameters received for run query blockNumber: {} blockHash: {}",
        blockNumber, blockHash);

    List<BlockEntity> byNumberAndHash = blockRepository.findByNumberAndHash(blockNumber, blockHash);
    if (byNumberAndHash.isEmpty()) {
      log.debug(
          "[findTransactionsByBlock] No block found for blockNumber: {} blockHash: {}",
          blockNumber, blockHash);
      return Collections.emptyList();
    }
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(
        byNumberAndHash.getFirst().getHash());
    log.debug(
        "[findTransactionsByBlock] Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      List<BlockTx> transactions = txList.stream().map(mapperTran::fromEntity).toList();
      populateTransactions(transactions);
      return transactions;
    }
    return Collections.emptyList();
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexerAndConfig() {
    ProtocolParamsEntity paramsEntity = epochParamRepository.findLatestProtocolParams();
    ProtocolParams protocolParams = mapperProtocolParams.fromEntity(paramsEntity);
    ProtocolParams protocolParametersFromConfigFile = protocolParamService.getProtocolParameters();

    return mapperProtocolParams.merge(protocolParams, protocolParametersFromConfigFile);
  }

  private Utxo createUtxoModel(List<Currency> currencies, AddressUtxoEntity entity) {
    Utxo utxoModel = mapper.map(
        UtxoKey.builder().outputIndex(entity.getOutputIndex()).txHash(entity.getTxHash()).build(),
        Utxo.class);
    utxoModel.setAmounts(getAmts(currencies, entity));
    return utxoModel;
  }

  private static List<Amt> getAmts(List<Currency> currencies, AddressUtxoEntity entity) {
    return currencies.isEmpty()
        ? entity.getAmounts().stream().map(AmtEntityToAmt::fromEntity).toList()
        : entity.getAmounts().stream()
            .filter(amt -> isAmountMatchesCurrency(currencies, amt))
            .map(AmtEntityToAmt::fromEntity)
            .toList();
  }

  private static boolean isAmountMatchesCurrency(List<Currency> currencies, AmtEntity amt) {
    return currencies.stream()
        .anyMatch(currency -> {
          String currencyUnit = Formatters.isEmptyHexString(currency.getSymbol()) ?
              currency.getMetadata().getPolicyId() :
              currency.getMetadata().getPolicyId() + currency.getSymbol();
          return currencyUnit.equals(amt.getUnit());
        });
  }
}
