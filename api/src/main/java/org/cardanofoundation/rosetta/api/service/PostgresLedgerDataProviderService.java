package org.cardanofoundation.rosetta.api.service;

import static org.cardanofoundation.rosetta.api.mapper.DataMapper.mapToTransactionPoolRegistrations;
import static org.cardanofoundation.rosetta.api.mapper.DataMapper.mapTransactionsToDict;
import static org.cardanofoundation.rosetta.api.mapper.DataMapper.parseTransactionRows;
import static org.cardanofoundation.rosetta.api.mapper.DataMapper.populateTransactionField;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.projection.BlockProjection;
import org.cardanofoundation.rosetta.api.projection.EpochParamProjection;
import org.cardanofoundation.rosetta.api.projection.FindTransactionProjection;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockProjection;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.FindPoolRetirements;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDelegations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionDeregistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolOwners;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRegistrationsData;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionPoolRelays;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionRegistrations;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionWithdrawals;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsInputs;
import org.cardanofoundation.rosetta.api.projection.dto.FindTransactionsOutputs;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.PopulatedTransaction;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionMetadataDto;
import org.cardanofoundation.rosetta.api.projection.dto.TransactionPoolRegistrations;
import org.cardanofoundation.rosetta.api.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.repository.DelegationRepository;
import org.cardanofoundation.rosetta.api.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.api.repository.PoolRetireRepository;
import org.cardanofoundation.rosetta.api.repository.PoolUpdateRepository;
import org.cardanofoundation.rosetta.api.repository.RewardRepository;
import org.cardanofoundation.rosetta.api.repository.StakeDeregistrationRepository;
import org.cardanofoundation.rosetta.api.repository.TxMetadataRepository;
import org.cardanofoundation.rosetta.api.repository.TxRepository;
import org.cardanofoundation.rosetta.api.repository.customrepository.UtxoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
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
  @Autowired
  private EpochParamRepository epochParamRepository;
  @Autowired
  private StakeDeregistrationRepository stakeDeregistrationRepository;
  @Autowired
  private DelegationRepository delegationRepository;
  @Autowired
  private TxMetadataRepository txMetadataRepository;
  @Autowired
  private PoolUpdateRepository poolUpdateRepository;
  @Autowired
  private PoolRetireRepository poolRetireRepository;

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
    log.debug("[GenesisBlockProjectionPage] is " + genesisBlockProjectionPage);
    genesisBlockProjectionPage.getContent();
    if (!genesisBlockProjectionPage.getContent().isEmpty()) {
      GenesisBlockProjection genesis = genesisBlockProjectionPage
          .getContent()
          .get(0);
      return GenesisBlockDto.builder().hash(genesis.getHash())
          .number(genesis.getIndex())
          .build();
    }
    log.debug("[findGenesisBlock] Genesis block was not found");
    return null;
  }

  @Override
  public BlockDto findBlock(Long blockNumber, String blockHash) {
    log.debug("[findBlock] Parameters received for run query blockNumber: " + blockNumber
        + " , blockHash: " + blockHash);
    List<BlockProjection> blockProjections = blockRepository.findBlock(blockNumber, blockHash);
    if (blockProjections.size() == 1) {
      log.debug("[findBlock] Block found!");
      BlockProjection blockProjection = blockProjections.get(0);

      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = dateFormat.parse(blockProjection.getCreatedAt().toString());
        log.debug("[findBlock] timestamp: " + blockProjection.getCreatedAt());
        log.debug("[findBlock] miliseconds " + date.getTime());
        return BlockDto.builder()
            .number(blockProjection.getNumber())
            .hash(blockProjection.getHash())
            .createdAt(date.getTime())
            .previousBlockHash(
                blockProjection.getPreviousBlockHash())
            .previousBlockNumber(blockProjection.getPreviousBlockNumber())
            .transactionsCount(blockProjection.getTransactionsCount())
            .createdBy(blockProjection.getCreatedBy())
            .size(blockProjection.getSize())
            .epochNo(blockProjection.getEpochNo())
            .slotNo(blockProjection.getSlotNo())
            .build();
      } catch (ParseException e) {
        log.error(e.getMessage());
      }

    }
    log.debug("[findBlock] No block was found");
    return null;
  }

  @Override
  public Long findBalanceByAddressAndBlock(String address, String hash) {
    return rewardRepository.findBalanceByAddressAndBlockSub1(address, hash)
        - rewardRepository.findBalanceByAddressAndBlockSub2(address, hash);
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
  public ProtocolParameters findProtocolParameters() {
    log.debug("[findProtocolParameters] About to run findProtocolParameters query");
    Page<EpochParamProjection> epochParamProjectionPage = epochParamRepository.findProtocolParameters(
        PageRequest.of(0, 1));
    if (ObjectUtils.isEmpty(epochParamProjectionPage.getContent())) {
      return ProtocolParameters.builder()
          .coinsPerUtxoSize("0")
          .maxValSize(BigInteger.ZERO)
          .maxCollateralInputs(0)
          .build();
    }
    EpochParamProjection epochParamProjection = epochParamProjectionPage.getContent().get(0);
    log.debug(
        "[findProtocolParameters] epochParamProjection is " + epochParamProjection.toString());
    return ProtocolParameters.builder()
        .coinsPerUtxoSize(Objects.nonNull(epochParamProjection.getCoinsPerUtxoSize())
            ? epochParamProjection.getCoinsPerUtxoSize().toString() : "0")
        .maxTxSize(epochParamProjection.getMaxTxSize())
        .maxValSize(Objects.nonNull(epochParamProjection.getMaxValSize())
            ? epochParamProjection.getMaxValSize() : BigInteger.ZERO)
        .keyDeposit(Objects.nonNull(epochParamProjection.getKeyDeposit())
            ? epochParamProjection.getKeyDeposit().toString() : null)
        .maxCollateralInputs(epochParamProjection.getMaxCollateralInputs())
        .minFeeCoefficient(epochParamProjection.getMinFeeA())
        .minFeeConstant(epochParamProjection.getMinFeeB())
        .minPoolCost(Objects.nonNull(epochParamProjection.getMinPoolCost())
            ? epochParamProjection.getMinPoolCost().toString() : null)
        .poolDeposit(Objects.nonNull(epochParamProjection.getPoolDeposit())
            ? epochParamProjection.getPoolDeposit().toString() : null)
        .protocol(epochParamProjection.getProtocolMajor())
        .build();
  }

  @Override
  public List<Utxo> findUtxoByAddressAndBlock(String address, String hash,
      List<Currency> currencies) {

    return utxoRepository.findUtxoByAddressAndBlock(address, hash, currencies);
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
    if (latestBlock == null) {
      log.error("[getLatestBlock] Latest block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.debug("[getLatestBlock] Returning latest block " + latestBlock);
    return latestBlock;
  }

  @Override
  public List<TransactionDto> findTransactionsByBlock(Long blockNumber, String blockHash) {
    log.debug("[findTransactionsByBlock] Parameters received for run query blockNumber: "
        + blockNumber + "blockHash: " + blockHash);
    log.debug("[findTransactionsByBlock] About to run findTransactionsByBlock query with params"
        + blockNumber + " and " + blockHash);
    List<FindTransactionProjection> findTransactionProjections = txRepository.findTransactionsByBlock(
        blockNumber, blockHash);
    log.debug("[findTransactionsByBlock] Found " + findTransactionProjections.size()
        + " transactions");
    if (ObjectUtils.isNotEmpty(findTransactionProjections)) {
      return parseTransactionRows(findTransactionProjections);
    }
    return null;
  }

  @Override
  public List<PopulatedTransaction> fillTransaction(List<TransactionDto> transactions) {
    if (ObjectUtils.isNotEmpty(transactions)) {
      Map<String, PopulatedTransaction> transactionMap = mapTransactionsToDict(transactions);
      return populateTransactions(transactionMap);
    }
    log.debug(
        "[fillTransaction] Since no transactions were given, no inputs and outputs are looked for");
    return null;
  }

  @Override
  public List<PopulatedTransaction> populateTransactions(
      Map<String, PopulatedTransaction> transactionsMap) {

    List<String> transactionsHashes = transactionsMap.keySet().stream().toList();

    List<FindTransactionsInputs> inputs = getFindTransactionsInputs(
        transactionsHashes);
    List<FindTransactionsOutputs> outputs = getFindTransactionsOutputs(
        transactionsHashes);
    List<FindTransactionWithdrawals> withdrawals = getFindTransactionWithdrawals(
        transactionsHashes);
    List<FindTransactionRegistrations> registrations = getFindTransactionRegistrations(
        transactionsHashes);
    List<FindTransactionDeregistrations> deregistrations = getFindTransactionDeregistrations(
        transactionsHashes);
    List<FindTransactionDelegations> delegations = getFindTransactionDelegations(
        transactionsHashes);
    List<TransactionMetadataDto> votes = getTransactionMetadataDtos(
        transactionsHashes);
    List<FindTransactionPoolRegistrationsData> poolsData = getTransactionPoolRegistrationsData(
        transactionsHashes);
    List<FindTransactionPoolOwners> poolsOwners = getFindTransactionPoolOwners(
        transactionsHashes);
    List<FindTransactionPoolRelays> poolsRelays = getFindTransactionPoolRelays(
        transactionsHashes);
    List<FindPoolRetirements> poolRetirements = getFindPoolRetirements(
        transactionsHashes);
    var parseInputsRow = DataMapper.parseInputsRowFactory();
    var parseOutputsRow = DataMapper.parseOutputsRowFactory();
    var parseWithdrawalsRow = DataMapper.parseWithdrawalsRowFactory();
    var parseRegistrationsRow = DataMapper.parseRegistrationsRowFactory();
    var parseDeregistrationsRow = DataMapper.parseDeregistrationsRowFactory();
    var parseDelegationsRow = DataMapper.parseDelegationsRowFactory();
    var parsePoolRetirementRow = DataMapper.parsePoolRetirementRowFactory();
    var parseVoteRow = DataMapper.parseVoteRowFactory();
    var parsePoolRegistrationsRows = DataMapper.parsePoolRegistrationsRowsFactory();
    transactionsMap = populateTransactionField(transactionsMap, inputs, parseInputsRow);
    transactionsMap = populateTransactionField(transactionsMap, outputs, parseOutputsRow);
    transactionsMap = populateTransactionField(transactionsMap, withdrawals, parseWithdrawalsRow);
    transactionsMap = populateTransactionField(transactionsMap, registrations,
        parseRegistrationsRow);
    transactionsMap = populateTransactionField(transactionsMap, deregistrations,
        parseDeregistrationsRow);
    transactionsMap = populateTransactionField(transactionsMap, delegations, parseDelegationsRow);
    transactionsMap = populateTransactionField(transactionsMap, poolRetirements,
        parsePoolRetirementRow);
    transactionsMap = populateTransactionField(transactionsMap, votes, parseVoteRow);
    List<TransactionPoolRegistrations> mappedPoolRegistrations = mapToTransactionPoolRegistrations(
        poolsData, poolsOwners, poolsRelays);
    transactionsMap = populateTransactionField(transactionsMap, mappedPoolRegistrations,
        parsePoolRegistrationsRows);
    return new ArrayList<>(transactionsMap.values());
  }

  @Override
  public PopulatedTransaction findTransactionByHashAndBlock(String hash,
      Long blockNumber, String blockHash) {
    log.debug("[findTransactionByHashAndBlock] Parameters received for run query blockNumber: "
        + blockNumber + " , blockHash: " + blockHash);
    List<FindTransactionProjection> findTransactions = blockRepository.findTransactionByHashAndBlock(
        hash, blockNumber, blockHash);
    log.debug("[findTransactionByHashAndBlock] Found " + findTransactions.size() + " transactions");
    if (ObjectUtils.isNotEmpty(findTransactions)) {
      Map<String, PopulatedTransaction> transactionsMap = mapTransactionsToDict(
          parseTransactionRows(findTransactions));
      return populateTransactions(transactionsMap).get(0);
    }
    return null;
  }
  @Override
  public List<FindTransactionsInputs> getFindTransactionsInputs(List<String> transactionsHashes) {
    log.debug("[findTransactionsInputs] with parameters " + transactionsHashes);
    return txRepository.findTransactionsInputs(transactionsHashes);
  }

  @Override
  public List<FindPoolRetirements> getFindPoolRetirements(List<String> transactionsHashes) {
    return poolRetireRepository.findPoolRetirements(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionPoolRelays> getFindTransactionPoolRelays(
      List<String> transactionsHashes) {
    return poolUpdateRepository.findTransactionPoolRelays(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionPoolOwners> getFindTransactionPoolOwners(
      List<String> transactionsHashes) {
    return poolUpdateRepository.findTransactionPoolOwners(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionPoolRegistrationsData> getTransactionPoolRegistrationsData(
      List<String> transactionsHashes) {
    return getFindTransactionPoolRegistrationsData(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionPoolRegistrationsData> getFindTransactionPoolRegistrationsData(
      List<String> transactionsHashes) {
    return poolUpdateRepository.findTransactionPoolRegistrationsData(
        transactionsHashes);
  }
  @Override
  public List<TransactionMetadataDto> getTransactionMetadataDtos(List<String> transactionsHashes) {
    return txMetadataRepository.findTransactionMetadata(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionDelegations> getFindTransactionDelegations(
      List<String> transactionsHashes) {
    return delegationRepository.findTransactionDelegations(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionDeregistrations> getFindTransactionDeregistrations(
      List<String> transactionsHashes) {
    return stakeDeregistrationRepository.findTransactionDeregistrations(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionRegistrations> getFindTransactionRegistrations(
      List<String> transactionsHashes) {
    return stakeDeregistrationRepository.findTransactionRegistrations(
        transactionsHashes);
  }
  @Override
  public List<FindTransactionWithdrawals> getFindTransactionWithdrawals(
      List<String> transactionsHashes) {
    return txRepository.findTransactionWithdrawals(transactionsHashes);
  }
  @Override
  public List<FindTransactionsOutputs> getFindTransactionsOutputs(
      List<String> transactionsHashes) {
    return txRepository.findTransactionsOutputs(
        transactionsHashes);
  }
}
