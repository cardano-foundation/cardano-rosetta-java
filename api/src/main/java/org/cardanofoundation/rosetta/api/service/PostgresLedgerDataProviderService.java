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
import org.cardanofoundation.rosetta.api.model.dto.AddressBalanceDTO;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.repository.AddressBalanceRepository;
import org.cardanofoundation.rosetta.api.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.repository.TxRepository;
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
//  private final RewardRepository rewardRepository;
  private final AddressUtxoRepository addressUtxoRepository;
  private final TxRepository txRepository;
//  private final EpochParamRepository epochParamRepository;
//  private final StakeDeregistrationRepository stakeDeregistrationRepository;
//  private final DelegationRepository delegationRepository;
//  private final TxMetadataRepository txMetadataRepository;
//  private final PoolUpdateRepository poolUpdateRepository;
//  private final PoolRetireRepository poolRetireRepository;

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
      return BlockDto.fromBlock(block);

    }
    log.debug("[findBlock] No block was found");
    return null;
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

//  @Override
//  public ProtocolParameters findProtocolParameters() {
//    log.debug("[findProtocolParameters] About to run findProtocolParameters query");
//    Page<EpochParamProjection> epochParamProjectionPage =
//        epochParamRepository.findProtocolParameters(PageRequest.of(0, 1));
//    if (ObjectUtils.isEmpty(epochParamProjectionPage.getContent())) {
//      return ProtocolParameters.builder()
//          .coinsPerUtxoSize("0")
//          .maxValSize(BigInteger.ZERO)
//          .maxCollateralInputs(0)
//          .build();
//    }
//    EpochParamProjection epochParamProjection = epochParamProjectionPage.getContent().get(0);
//    log.debug(
//        "[findProtocolParameters] epochParamProjection is " + epochParamProjection.toString());
//    return ProtocolParameters.builder()
//        .coinsPerUtxoSize(
//            Objects.nonNull(epochParamProjection.getCoinsPerUtxoSize()) ?
//                epochParamProjection.getCoinsPerUtxoSize().toString() : "0")
//        .maxTxSize(epochParamProjection.getMaxTxSize())
//        .maxValSize(Objects.nonNull(epochParamProjection.getMaxValSize()) ?
//            epochParamProjection.getMaxValSize() : BigInteger.ZERO)
//        .keyDeposit(Objects.nonNull(epochParamProjection.getKeyDeposit()) ?
//            epochParamProjection.getKeyDeposit().toString() : null)
//        .maxCollateralInputs(epochParamProjection.getMaxCollateralInputs())
//        .minFeeCoefficient(epochParamProjection.getMinFeeA())
//        .minFeeConstant(epochParamProjection.getMinFeeB())
//        .minPoolCost(Objects.nonNull(epochParamProjection.getMinPoolCost()) ?
//            epochParamProjection.getMinPoolCost().toString() : null)
//        .poolDeposit(Objects.nonNull(epochParamProjection.getPoolDeposit()) ?
//            epochParamProjection.getPoolDeposit().toString() : null)
//        .protocol(epochParamProjection.getProtocolMajor())
//        .build();
//  }

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

//  @Override
//  public List<MaBalance> findMaBalanceByAddressAndBlock(String address, String hash) {
//    return utxoRepository.findMaBalanceByAddressAndBlock(address, hash);
//  }

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
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(byNumberAndHash.getFirst().getHash());
    log.debug(
        "[findTransactionsByBlock] Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      return DataMapper.parseTransactionRows(txList);
    }
    return null;
  }

//  @Override
//  public List<PopulatedTransaction> fillTransaction(List<TransactionDto> transactions) {
//    if (ObjectUtils.isNotEmpty(transactions)) {
//      Map<String, PopulatedTransaction> transactionMap = mapTransactionsToDict(transactions);
//      return populateTransactions(transactionMap);
//    }
//    log.debug(
//        "[fillTransaction] Since no transactions were given, no inputs and outputs are looked for");
//    return null;
//  }

//  @Override
//  public List<PopulatedTransaction> populateTransactions(
//      Map<String, PopulatedTransaction> transactionsMap) {
//
//    List<String> transactionsHashes = transactionsMap.keySet().stream().toList();
//
//    List<FindTransactionsInputs> inputs = getFindTransactionsInputs(
//        transactionsHashes);
//    List<FindTransactionsOutputs> outputs = getFindTransactionsOutputs(
//        transactionsHashes);
//    List<FindTransactionWithdrawals> withdrawals = getFindTransactionWithdrawals(
//        transactionsHashes);
//    List<FindTransactionRegistrations> registrations = getFindTransactionRegistrations(
//        transactionsHashes);
//    List<FindTransactionDeregistrations> deregistrations = getFindTransactionDeregistrations(
//        transactionsHashes);
//    List<FindTransactionDelegations> delegations = getFindTransactionDelegations(
//        transactionsHashes);
//    List<TransactionMetadataDto> votes = getTransactionMetadataDtos(
//        transactionsHashes);
//    List<FindTransactionPoolRegistrationsData> poolsData = getTransactionPoolRegistrationsData(
//        transactionsHashes);
//    List<FindTransactionPoolOwners> poolsOwners = getFindTransactionPoolOwners(
//        transactionsHashes);
//    List<FindTransactionPoolRelays> poolsRelays = getFindTransactionPoolRelays(
//        transactionsHashes);
//    List<FindPoolRetirements> poolRetirements = getFindPoolRetirements(
//        transactionsHashes);
//    var parseInputsRow = DataMapper.parseInputsRowFactory();
//    var parseOutputsRow = DataMapper.parseOutputsRowFactory();
//    var parseWithdrawalsRow = DataMapper.parseWithdrawalsRowFactory();
//    var parseRegistrationsRow = DataMapper.parseRegistrationsRowFactory();
//    var parseDeregistrationsRow = DataMapper.parseDeregistrationsRowFactory();
//    var parseDelegationsRow = DataMapper.parseDelegationsRowFactory();
//    var parsePoolRetirementRow = DataMapper.parsePoolRetirementRowFactory();
//    var parseVoteRow = DataMapper.parseVoteRowFactory();
//    var parsePoolRegistrationsRows = DataMapper.parsePoolRegistrationsRowsFactory();
//    transactionsMap = populateTransactionField(transactionsMap, inputs, parseInputsRow);
//    transactionsMap = populateTransactionField(transactionsMap, outputs, parseOutputsRow);
//    transactionsMap = populateTransactionField(transactionsMap, withdrawals, parseWithdrawalsRow);
//    transactionsMap = populateTransactionField(transactionsMap, registrations,
//        parseRegistrationsRow);
//    transactionsMap = populateTransactionField(transactionsMap, deregistrations,
//        parseDeregistrationsRow);
//    transactionsMap = populateTransactionField(transactionsMap, delegations, parseDelegationsRow);
//    transactionsMap = populateTransactionField(transactionsMap, poolRetirements,
//        parsePoolRetirementRow);
//    transactionsMap = populateTransactionField(transactionsMap, votes, parseVoteRow);
//    List<TransactionPoolRegistrations> mappedPoolRegistrations = mapToTransactionPoolRegistrations(
//        poolsData, poolsOwners, poolsRelays);
//    transactionsMap = populateTransactionField(transactionsMap, mappedPoolRegistrations,
//        parsePoolRegistrationsRows);
//    return new ArrayList<>(transactionsMap.values());
//  }

//  @Override
//  public PopulatedTransaction findTransactionByHashAndBlock(String hash,
//      Long blockNumber, String blockHash) {
//    List<Tx> txList = blockRepository.findTransactionByHashAndBlock(hash, blockHash);
//    if(txList.isEmpty()) {
//      log.debug(
//          "[findTransactionByHashAndBlock] No transactions found for hash {} and block {}",
//          hash, blockHash);
//      return null;
//    }
//
//    log.debug(
//        "[findTransactionByHashAndBlock] Found {} transactions", txList.size());
//      Map<String, PopulatedTransaction> transactionsMap = mapTransactionsToDict(
//          parseTransactionRows(txList));
//      return populateTransactions(transactionsMap).get(0);
//  }

//  @Override
//  public List<FindTransactionsInputs> getFindTransactionsInputs(List<String> transactionsHashes) {
//    log.debug("[findTransactionsInputs] with parameters {}", transactionsHashes);
//    return txRepository.findTransactionsInputs(transactionsHashes);
//  }

//  @Override
//  public List<FindPoolRetirements> getFindPoolRetirements(List<String> transactionsHashes) {
//    return poolRetireRepository.findPoolRetirements(
//        transactionsHashes);
//  }

//  @Override
//  public List<FindTransactionPoolRelays> getFindTransactionPoolRelays(
//      List<String> transactionsHashes) {
//    return poolUpdateRepository.findTransactionPoolRelays(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionPoolOwners> getFindTransactionPoolOwners(
//      List<String> transactionsHashes) {
//    return poolUpdateRepository.findTransactionPoolOwners(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionPoolRegistrationsData> getTransactionPoolRegistrationsData(
//      List<String> transactionsHashes) {
//    return getFindTransactionPoolRegistrationsData(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionPoolRegistrationsData> getFindTransactionPoolRegistrationsData(
//      List<String> transactionsHashes) {
//    return poolUpdateRepository.findTransactionPoolRegistrationsData(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<TransactionMetadataDto> getTransactionMetadataDtos(List<String> transactionsHashes) {
//    return txMetadataRepository.findTransactionMetadata(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionDelegations> getFindTransactionDelegations(
//      List<String> transactionsHashes) {
//    return delegationRepository.findTransactionDelegations(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionDeregistrations> getFindTransactionDeregistrations(
//      List<String> transactionsHashes) {
//    return stakeDeregistrationRepository.findTransactionDeregistrations(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionRegistrations> getFindTransactionRegistrations(
//      List<String> transactionsHashes) {
//    return stakeDeregistrationRepository.findTransactionRegistrations(
//        transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionWithdrawals> getFindTransactionWithdrawals(
//      List<String> transactionsHashes) {
//    return txRepository.findTransactionWithdrawals(transactionsHashes);
//  }
//
//  @Override
//  public List<FindTransactionsOutputs> getFindTransactionsOutputs(
//      List<String> transactionsHashes) {
//    return txRepository.findTransactionsOutputs(
//        transactionsHashes);
//  }
}
