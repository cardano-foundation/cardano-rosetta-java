package org.cardanofoundation.rosetta.consumer.unit.service.impl;

import static org.cardanofoundation.rosetta.consumer.util.CertificateUtil.buildStakeDelegationCert;
import static org.cardanofoundation.rosetta.consumer.util.CertificateUtil.buildStakeRegistrationCert;
import static org.cardanofoundation.rosetta.consumer.util.TestStringUtils.generateRandomHash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredentialType;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.consumer.factory.CertificateSyncServiceFactory;
import org.cardanofoundation.rosetta.consumer.repository.ExtraKeyWitnessRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.service.AddressBalanceService;
import org.cardanofoundation.rosetta.consumer.service.BatchCertificateDataService;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.DatumService;
import org.cardanofoundation.rosetta.consumer.service.MultiAssetService;
import org.cardanofoundation.rosetta.consumer.service.ParamProposalService;
import org.cardanofoundation.rosetta.consumer.service.RedeemerService;
import org.cardanofoundation.rosetta.consumer.service.ReferenceInputService;
import org.cardanofoundation.rosetta.consumer.service.ScriptService;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import org.cardanofoundation.rosetta.consumer.service.TxInService;
import org.cardanofoundation.rosetta.consumer.service.TxMetaDataService;
import org.cardanofoundation.rosetta.consumer.service.TxOutService;
import org.cardanofoundation.rosetta.consumer.service.WithdrawalsService;
import org.cardanofoundation.rosetta.consumer.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  private static final int BLOCK_HASH_LENGTH = 64;
  private static final int TX_HASH_LENGTH = 64;
  private static final int STAKE_KEY_HASH_LENGTH = 56;
  private static final int REQUIRED_SIGNER_HASH_LENGTH = 56;

  @Mock
  TxRepository txRepository;

  @Mock
  ExtraKeyWitnessRepository extraKeyWitnessRepository;

  @Mock
  MultiAssetService multiAssetService;

  @Mock
  StakeAddressService stakeAddressService;

  @Mock
  ParamProposalService paramProposalService;

  @Mock
  AddressBalanceService addressBalanceService;

  @Mock
  WithdrawalsService withdrawalsService;

  @Mock
  TxMetaDataService txMetaDataService;

  @Mock
  RedeemerService redeemerService;

  @Mock
  ScriptService scriptService;

  @Mock
  DatumService datumService;

  @Mock
  BlockDataService blockDataService;

  @Mock
  TxOutService txOutService;

  @Mock
  TxInService txInService;

  @Mock
  ReferenceInputService referenceInputService;

  @Mock
  CertificateSyncServiceFactory certificateSyncServiceFactory;

  @Mock
  BatchCertificateDataService batchCertificateDataService;

  TransactionServiceImpl victim;

  @BeforeEach
  void setUp() {
    victim = new TransactionServiceImpl(
        txRepository, extraKeyWitnessRepository, multiAssetService, stakeAddressService,
        paramProposalService, addressBalanceService, withdrawalsService, txMetaDataService,
        redeemerService, scriptService, datumService, blockDataService, txOutService, txInService,
        referenceInputService, certificateSyncServiceFactory, batchCertificateDataService
    );
  }

  @Test
  @DisplayName("Should skip tx handling if no txs were supplied")
  void shouldSkipTxHandlingIfNoTxsSuppliedTest() {
    Map<String, Block> blockMap = Collections.emptyMap();
    Collection<AggregatedBlock> aggregatedBlocks = Collections.emptyList();

    victim.prepareAndHandleTxs(blockMap, aggregatedBlocks);

    Mockito.verifyNoInteractions(txRepository);
    Mockito.verifyNoInteractions(extraKeyWitnessRepository);
    Mockito.verifyNoInteractions(multiAssetService);
    Mockito.verifyNoInteractions(stakeAddressService);
    Mockito.verifyNoInteractions(paramProposalService);
    Mockito.verifyNoInteractions(addressBalanceService);
    Mockito.verifyNoInteractions(withdrawalsService);
    Mockito.verifyNoInteractions(txMetaDataService);
    Mockito.verifyNoInteractions(redeemerService);
    Mockito.verifyNoInteractions(scriptService);
    Mockito.verifyNoInteractions(datumService);
    Mockito.verifyNoInteractions(blockDataService);
    Mockito.verifyNoInteractions(txOutService);
    Mockito.verifyNoInteractions(txInService);
    Mockito.verifyNoInteractions(referenceInputService);
    Mockito.verifyNoInteractions(certificateSyncServiceFactory);
    Mockito.verifyNoInteractions(batchCertificateDataService);
  }

  @Test
  @DisplayName("Should handle txs successfully")
  void shouldHandleTxsSuccessfullyTest() {
    Map<String, Block> blockMap = new LinkedHashMap<>();
    Collection<AggregatedBlock> aggregatedBlocks = new ArrayList<>();

    IntStream.range(0, 10).forEach(idx -> {
      String blockHash = generateRandomHash(BLOCK_HASH_LENGTH);
      blockMap.put(blockHash, givenBlockEntity(blockHash, idx));

      AggregatedBlock aggregatedBlock = Mockito.mock(AggregatedBlock.class);
      List<AggregatedTx> aggregatedTxList = IntStream.range(0, 10)
          .boxed()
          .map(blockIdx -> givenAggregatedTxWithSufficientData(blockHash, blockIdx))
          .toList();
      Mockito.when(aggregatedBlock.getTxList()).thenReturn(aggregatedTxList);
      aggregatedBlocks.add(aggregatedBlock);
    });

    victim.prepareAndHandleTxs(blockMap, aggregatedBlocks);

    Mockito.verify(txRepository, Mockito.times(1))
        .saveAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(txRepository);
    Mockito.verify(extraKeyWitnessRepository, Mockito.times(1))
        .findByHashIn(Mockito.anyCollection());
    Mockito.verify(extraKeyWitnessRepository, Mockito.times(1))
        .saveAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(extraKeyWitnessRepository);
    Mockito.verify(multiAssetService, Mockito.times(1))
        .handleMultiAssetMint(Mockito.anyCollection(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(multiAssetService);
    Mockito.verify(stakeAddressService, Mockito.times(1))
        .handleStakeAddressesFromTxs(Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(stakeAddressService);
    Mockito.verify(paramProposalService, Mockito.times(1))
        .handleParamProposals(Mockito.anyCollection(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(paramProposalService);
    Mockito.verify(addressBalanceService, Mockito.times(1))
        .handleAddressBalance(Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(addressBalanceService);
    Mockito.verify(withdrawalsService, Mockito.times(1))
        .handleWithdrawal(
            Mockito.anyCollection(), Mockito.anyMap(),
            Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(withdrawalsService);
    Mockito.verify(txMetaDataService, Mockito.times(1))
        .handleAuxiliaryDataMaps(Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(txMetaDataService);
    Mockito.verify(redeemerService, Mockito.times(1))
        .handleRedeemers(Mockito.anyCollection(), Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(redeemerService);
    Mockito.verify(scriptService, Mockito.times(1))
        .handleScripts(Mockito.anyCollection(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(scriptService);
    Mockito.verify(datumService, Mockito.times(1))
        .handleDatum(Mockito.anyCollection(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(datumService);
    Mockito.verify(blockDataService, Mockito.times(1))
        .getStakeAddressTxHashMap();
    Mockito.verify(blockDataService, Mockito.times(1))
        .getAggregatedAddressBalanceMap();
    Mockito.verify(blockDataService, Mockito.times(100))
        .getAggregatedBlock(Mockito.anyString());
    Mockito.verifyNoMoreInteractions(blockDataService);
    Mockito.verify(txOutService, Mockito.times(2))
        .prepareTxOuts(Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(txOutService);
    Mockito.verify(txInService, Mockito.times(2))
        .handleTxIns(Mockito.anyCollection(), Mockito.anyMap(),
            Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(txInService);
    Mockito.verify(referenceInputService, Mockito.times(1))
        .handleReferenceInputs(Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(referenceInputService);
    Mockito.verify(certificateSyncServiceFactory, Mockito.times(100))
        .handle(Mockito.any(), Mockito.any(), Mockito.anyInt(),
            Mockito.any(), Mockito.any(), Mockito.anyMap());
    Mockito.verifyNoMoreInteractions(certificateSyncServiceFactory);
    Mockito.verify(batchCertificateDataService, Mockito.times(1))
        .saveAllAndClearBatchData();
    Mockito.verifyNoMoreInteractions(batchCertificateDataService);
  }

  private static AggregatedTx givenAggregatedTxWithSufficientData(String blockHash, int blockIdx) {
    Set<String> requiredSigners = IntStream.range(0, 10)
        .mapToObj(unused -> generateRandomHash(REQUIRED_SIGNER_HASH_LENGTH))
        .collect(Collectors.toSet());

    return AggregatedTx.builder()
        .blockHash(blockHash)
        .hash(generateRandomHash(TX_HASH_LENGTH))
        .blockIndex(blockIdx)
        .validContract(blockIdx % 2 == 0)
        .deposit(0L)
        .txInputs(Set.of(Mockito.mock(AggregatedTxIn.class)))
        .txOutputs(List.of(Mockito.mock(AggregatedTxOut.class)))
        .collateralInputs(blockIdx % 2 == 0
            ? Collections.emptySet()
            : Set.of(Mockito.mock(AggregatedTxIn.class)))
        .collateralReturn(blockIdx % 2 == 0
            ? null
            : Mockito.mock(AggregatedTxOut.class))
        .certificates(givenCertificates())
        .requiredSigners(requiredSigners)
        .build();
  }

  private static Block givenBlockEntity(String blockHash, long blockNo) {
    return Block.builder().hash(blockHash).blockNo(blockNo).build();
  }

  private static List<Certificate> givenCertificates() {
    return List.of(
        buildStakeRegistrationCert(0, StakeCredentialType.SCRIPT_HASH,
            generateRandomHash(STAKE_KEY_HASH_LENGTH)),
        buildStakeDelegationCert(1, StakeCredentialType.SCRIPT_HASH,
            generateRandomHash(STAKE_KEY_HASH_LENGTH))
    );
  }
}