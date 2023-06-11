package org.cardanofoundation.rosetta.consumer.service.impl.block;

import com.bloxbean.cardano.client.crypto.Bech32;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.ledgersync.*;
import org.cardanofoundation.rosetta.common.ledgersync.certs.*;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.*;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx.AggregatedTxBuilder;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.service.BlockAggregatorService;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.SlotLeaderService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class BlockAggregatorServiceImpl extends BlockAggregatorService<Block> {

  public BlockAggregatorServiceImpl(
      SlotLeaderService slotLeaderService,
      BlockDataService blockDataService) {
    super(slotLeaderService, blockDataService);
  }

  @Override
  public AggregatedBlock aggregateBlock(Block blockCddl) {
    if (blockCddl.getTransactionBodies().size() != blockCddl.getTransactionWitness().size()) {
      log.error(
          "Transaction body size [{}] different with transaction witness size [{}], Block no: {}, Block hash {}",
          blockCddl.getTransactionBodies().size(), blockCddl.getTransactionWitness().size(),
          blockCddl.getBlockNumber(), blockCddl.getBlockHash());
      throw new IllegalStateException();
    }
    return mapBlockCddlToAggregatedBlock(blockCddl);
  }

  /**
   * handle mapping {@link org.cardanofoundation.rosetta.common.ledgersync.Block} to
   * {@link AggregatedBlock}
   *
   * @param blockCddl {@link org.cardanofoundation.rosetta.common.ledgersync.Block} cddl block
   * @return {@link AggregatedBlock}
   */
  private AggregatedBlock mapBlockCddlToAggregatedBlock(Block blockCddl) {

    var header = blockCddl.getHeader();
    var blockHash = header.getHeaderBody().getBlockHash();

    var epoch = blockCddl.getHeader().getHeaderBody().getSlotId();
    var epochNo = (int) epoch.getValue();
    var epochSlotNo = (int) epoch.getSlotOfEpoch();
    var slotNo = blockCddl.getSlot();
    var blockNo = blockCddl.getBlockNumber();
    var prevHash = blockCddl.getHeader().getHeaderBody().getPrevHash();
    var slotLeader = slotLeaderService.getSlotLeaderHashAndPrefix(blockCddl);

    var blockSize = (int) blockCddl.getHeader().getHeaderBody().getBlockBodySize();
    var blockTime = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
        blockCddl.getBlockTime(), 0, ZoneOffset.ofHours(0)));
    var txCount = (long) blockCddl.getTransactionBodies().size();

    var headerBody = header.getHeaderBody();
    var protocolVersion = headerBody.getProtocolVersion();
    var protoMajor = (int) protocolVersion.getProtoMajor();
    var protoMinor = (int) protocolVersion.getProtoMinor();
    var vrfKey = Bech32.encode(
        HexUtil.decodeHexString(headerBody.getVrfVkey()), ConsumerConstant.VRF_KEY_PREFIX);

    var opCert = headerBody.getOperationalCert();
    var actualOpCert = opCert.getHotKey();
    var opCertCounter = (long) opCert.getSequenceNumber();

    List<AggregatedTx> txList = mapCddlTxToAggregatedTx(blockCddl);
    return AggregatedBlock.builder()
        .era(blockCddl.getEraType())
        .network(blockCddl.getNetwork())
        .hash(blockHash)
        .epochNo(epochNo)
        .epochSlotNo(epochSlotNo)
        .slotNo(slotNo)
        .blockNo(blockNo)
        .prevBlockHash(prevHash)
        .slotLeader(slotLeader)
        .blockSize(blockSize)
        .blockTime(blockTime)
        .txCount(txCount)
        .protoMajor(protoMajor)
        .protoMinor(protoMinor)
        .vrfKey(vrfKey)
        .opCert(actualOpCert)
        .opCertCounter(opCertCounter)
        .txList(txList)
        .auxiliaryDataMap(blockCddl.getAuxiliaryDataMap())
        .build();
  }

  /**
   * This method transforms CDDL tx data to aggregated tx objects, which
   * will be used later by block processing and transactions handling
   * services
   *
   * @param blockCddl   transformed block data from CDDL, containing tx data
   * @return            list of aggregated tx objects
   */
  private List<AggregatedTx> mapCddlTxToAggregatedTx(Block blockCddl) {
    List<TransactionBody> transactionBodies = blockCddl.getTransactionBodies();
    List<Witnesses> transactionWitness = blockCddl.getTransactionWitness();
    Set<Integer> invalidTransactions = new HashSet<>();

    if (!CollectionUtils.isEmpty(blockCddl.getInvalidTransactions())) {
      invalidTransactions.addAll(blockCddl.getInvalidTransactions());
    }

    return IntStream.range(0, transactionBodies.size()).mapToObj(txIdx -> {
      boolean validContract = !invalidTransactions.contains(txIdx);
      TransactionBody transactionBody = transactionBodies.get(txIdx);
      Witnesses witnesses = transactionWitness.get(txIdx);
      AggregatedTx aggregatedTx = txToAggregatedTx(blockCddl.getBlockHash(),
          validContract, txIdx, transactionBody, witnesses);
      mapStakeAddressToTxHash(aggregatedTx, blockCddl.getNetwork());
      return aggregatedTx;
    }).collect(Collectors.toList());
  }

  /**
   * This method searches for every stake address existed in an aggregated tx, then
   * mark all the stake addresses' first appeared tx hash as the hash of currently
   * processing tx, in case the stake address has not existed in any other tx before.
   * If the stake address has already been marked, it is skipped
   *
   * @param aggregatedTx  aggregated tx in process
   * @param network       network magic of this tx
   */
  private void mapStakeAddressToTxHash(AggregatedTx aggregatedTx, int network) {
    String txHash = aggregatedTx.getHash();

    // From txOutputs and collateral return
    List<AggregatedTxOut> txOutputs = aggregatedTx.getTxOutputs();
    txOutputs.stream()
        .map(AggregatedTxOut::getAddress)
        .filter(AggregatedAddress::hasStakeReference)
        .forEach(aggregatedAddress -> blockDataService
            .saveFirstAppearedTxHashForStakeAddress(aggregatedAddress.getStakeAddress(), txHash));

    AggregatedTxOut collateralReturn = aggregatedTx.getCollateralReturn();
    if (Objects.nonNull(collateralReturn)) {
      AggregatedAddress address = collateralReturn.getAddress();
      if (address.hasStakeReference()) {
        blockDataService.saveFirstAppearedTxHashForStakeAddress(address.getStakeAddress(), txHash);
      }
    }

    // Do not process further if this tx is invalid
    if (!aggregatedTx.isValidContract()) {
      return;
    }

    // From withdrawals
    if (!CollectionUtils.isEmpty(aggregatedTx.getWithdrawals())) {
      aggregatedTx.getWithdrawals().keySet().forEach(rewardAccountHex ->
          blockDataService.saveFirstAppearedTxHashForStakeAddress(rewardAccountHex, txHash));
    }

    // From certificates
    aggregatedTx.getCertificates().forEach(certificate ->
        mapCertificateStakeAddressToTxHash(network, txHash, certificate));
  }

  /**
   * This method marks the stake address(s) inside a certificate's first appearance tx hash
   *
   * @param network       network magic of this tx
   * @param txHash        tx hash of currently processing tx
   * @param certificate   certificate in process
   */
  private void mapCertificateStakeAddressToTxHash(
      int network, String txHash, Certificate certificate) {
    CertType certType = certificate.getCertType();
    switch (certType) {
      case MOVE_INSTATANEOUS:
        MoveInstataneous moveInstataneous = (MoveInstataneous) certificate;
        if (Objects.nonNull(moveInstataneous.getAccountingPotCoin())) {
          break;
        }

        moveInstataneous.getStakeCredentialCoinMap()
            .keySet().stream()
            .map(credential -> AddressUtil.getRewardAddressString(credential, network))
            .forEach(rewardAddress -> blockDataService
                .saveFirstAppearedTxHashForStakeAddress(rewardAddress, txHash));
        break;
      case STAKE_REGISTRATION:
        StakeRegistration stakeRegistration = (StakeRegistration) certificate;
        blockDataService.saveFirstAppearedTxHashForStakeAddress(AddressUtil
            .getRewardAddressString(stakeRegistration.getStakeCredential(), network), txHash);
        break;
      case STAKE_DEREGISTRATION:
        StakeDeregistration stakeDeregistration = (StakeDeregistration) certificate;
        blockDataService.saveFirstAppearedTxHashForStakeAddress(AddressUtil
            .getRewardAddressString(stakeDeregistration.getStakeCredential(), network), txHash);
        break;
      case STAKE_DELEGATION:
        StakeDelegation stakeDelegation = (StakeDelegation) certificate;
        blockDataService.saveFirstAppearedTxHashForStakeAddress(AddressUtil
            .getRewardAddressString(stakeDelegation.getStakeCredential(), network), txHash);
        break;
      case POOL_REGISTRATION:
        PoolRegistration poolRegistration = (PoolRegistration) certificate;

        // Reward account
        // Workaround for bug https://github.com/input-output-hk/cardano-db-sync/issues/546
        String rewardAccountHex = poolRegistration.getPoolParams().getRewardAccount();
        byte[] rewardAccountBytes = HexUtil.decodeHexString(rewardAccountHex);
        int networkId = Constant.isTestnet(network) ? 0 : 1;
        byte header = rewardAccountBytes[0];
        if (((header & 0xff) & networkId) == 0) {
          rewardAccountBytes[0] = (byte) ((header & ~1) | networkId);
        }
        blockDataService.saveFirstAppearedTxHashForStakeAddress(
            HexUtil.encodeHexString(rewardAccountBytes), txHash);

        // Pool owners
        poolRegistration.getPoolParams().getPoolOwners().forEach(poolOwnerHash -> {
          String stakeAddressHex = AddressUtil.getRewardAddressString(
              new StakeCredential(StakeCredentialType.ADDR_KEYHASH, poolOwnerHash), network);
          blockDataService.saveFirstAppearedTxHashForStakeAddress(stakeAddressHex, txHash);
        });
        break;
      default:
        break;
    }
  }

  /**
   * This method transforms a single CDDL tx data to aggregated tx object
   *
   * @param blockHash         block hash where the currently processing tx lies in
   * @param validContract     currently processing tx's contract validity
   * @param idx               currently processing tx's index within a block
   * @param transactionBody   transformed CDDL tx data
   * @param witnesses         currently processing tx's witnesses data
   * @return                  aggregated tx object
   */
  private AggregatedTx txToAggregatedTx(String blockHash, boolean validContract,
      int idx, TransactionBody transactionBody, Witnesses witnesses) {
    AggregatedTxBuilder aggregatedTxBuilder = AggregatedTx.builder();

    // Handle basic tx data
    var txHash = transactionBody.getTxHash();
    aggregatedTxBuilder.hash(txHash);
    aggregatedTxBuilder.blockHash(blockHash);
    aggregatedTxBuilder.blockIndex(idx);
    aggregatedTxBuilder.validContract(validContract);
    aggregatedTxBuilder.deposit(0);

    // Converts CDDL tx ins data to aggregated tx ins
    var txInputs = transactionBody.getInputs();
    aggregatedTxBuilder.txInputs(txInsToAggregatedTxIns(txInputs));
    aggregatedTxBuilder.collateralInputs(
        txInsToAggregatedTxIns(transactionBody.getCollateralInputs()));
    aggregatedTxBuilder.referenceInputs(
        txInsToAggregatedTxIns(transactionBody.getReferenceInputs()));

    /*
     * Converts CDDL tx outs/collateral return data to aggregated tx outs/collateral return
     * Both tx out and collateral return use the same object, their aggregated outputs are also
     * under the same object, just different field name
     */
    var aggregatedTxOuts = txOutsToAggregatedTxOuts(transactionBody.getOutputs());
    aggregatedTxBuilder.txOutputs(aggregatedTxOuts);

    var collateralReturn = AggregatedTxOut.from(transactionBody.getCollateralReturn());
    if (Objects.nonNull(collateralReturn)) {
      collateralReturn.setIndex(aggregatedTxOuts.size());
    }
    List<AggregatedTxOut> collateralReturnsSingleList =
        (!validContract && Objects.nonNull(collateralReturn))
            ? List.of(collateralReturn) : Collections.emptyList();
    aggregatedTxBuilder.collateralReturn(collateralReturn);

    /*
     * Handle address balance from tx outputs or collateral return
     * This is initial step of calculating balance. The same process will be
     * done when tx ins are taken into account
     */
    mapAggregatedTxOutsToAddressBalanceMap(
        validContract ? aggregatedTxOuts : collateralReturnsSingleList, txHash);

    aggregatedTxBuilder.certificates(transactionBody.getCertificates());
    aggregatedTxBuilder.withdrawals(transactionBody.getWithdrawals());
    aggregatedTxBuilder.update(transactionBody.getUpdate());
    aggregatedTxBuilder.mint(transactionBody.getMint());
    aggregatedTxBuilder.requiredSigners(transactionBody.getRequiredSigners());
    aggregatedTxBuilder.witnesses(witnesses);

    var fee = transactionBody.getFee();
    if (!validContract && Objects.nonNull(transactionBody.getTotalCollateral())) {
      fee = transactionBody.getTotalCollateral();
    }
    aggregatedTxBuilder.fee(fee);

    var outSum = BigInteger.ZERO;
    if (validContract) {
      outSum = AggregatedTxOut.calculateOutSum(transactionBody.getOutputs());
    } else if (Objects.nonNull(transactionBody.getCollateralReturn())) {
      outSum = AggregatedTxOut.calculateOutSum(List.of(transactionBody.getCollateralReturn()));
    }
    aggregatedTxBuilder.outSum(outSum);
    aggregatedTxBuilder.deposit(0);

    return aggregatedTxBuilder.build();
  }

  private Set<AggregatedTxIn> txInsToAggregatedTxIns(Set<TransactionInput> txInputs) {
    if (CollectionUtils.isEmpty(txInputs)) {
      return Collections.emptySet();
    }

    return txInputs.stream().map(AggregatedTxIn::of).collect(Collectors.toSet());
  }

  private List<AggregatedTxOut> txOutsToAggregatedTxOuts(List<TransactionOutput> txOutputs) {
    if (CollectionUtils.isEmpty(txOutputs)) {
      return Collections.emptyList();
    }

    return txOutputs.stream().map(AggregatedTxOut::from).collect(Collectors.toList());
  }
}
