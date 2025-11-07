package org.cardanofoundation.rosetta.api.block.service;

import java.time.Clock;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import lombok.val;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.*;
import org.cardanofoundation.rosetta.api.block.model.entity.*;
import org.cardanofoundation.rosetta.api.block.model.entity.projection.BlockIdentifierProjection;
import org.cardanofoundation.rosetta.api.block.model.repository.*;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.OLDEST_BLOCK_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerBlockServiceImplTest {

  @InjectMocks
  private LedgerBlockServiceImpl ledgerBlockService;

  @Mock
  private BlockMapper blockMapper;

  @Mock
  private TransactionMapper transactionMapper;

  @Mock
  private ProtocolParamService protocolParamService;

  @Mock
  private BlockRepository blockRepository;

  @Mock
  private StakeRegistrationRepository stakeRegistrationRepository;

  @Mock
  private PoolDelegationRepository poolDelegationRepository;

  @Mock
  private DrepVoteDelegationRepository drepVoteDelegationRepository;

  @Mock
  private PoolRegistrationRepository poolRegistrationRepository;

  @Mock
  private PoolRetirementRepository poolRetirementRepository;

  @Mock
  private WithdrawalRepository withdrawalRepository;

  @Mock
  private AddressUtxoRepository addressUtxoRepository;

  @Mock
  private InvalidTransactionRepository invalidTransactionRepository;

  @Spy
  private Clock clock = Clock.systemUTC();

  @Test
  void populateTransaction_marksTransactionAsInvalid_ifFoundInInvalidTransactionRepository() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val invalidTransaction = new InvalidTransactionEntity();
    invalidTransaction.setTxHash("txHash1");
    
    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(), // utxos
            Collections.emptyList(), // stakeRegistrations
            Collections.emptyList(), // delegations
            Collections.emptyList(), // drepDelegations
            Collections.emptyList(), // poolRegistrations
            Collections.emptyList(), // poolRetirements
            Collections.emptyList(), // withdrawals
            List.of(invalidTransaction)  // invalidTransactions
    );

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.isInvalid()).isTrue();
  }

  @Test
  void populateTransaction_doesNotMarkTransactionAsInvalid_ifNotFoundInInvalidTransactionRepository() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(), // utxos
            Collections.emptyList(), // stakeRegistrations
            Collections.emptyList(), // delegations
            Collections.emptyList(), // drepDelegations
            Collections.emptyList(), // poolRegistrations
            Collections.emptyList(), // poolRetirements
            Collections.emptyList(), // withdrawals
            Collections.emptyList()  // invalidTransactions - empty list means transaction is valid
    );

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();
    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.isInvalid()).isFalse();
  }

  @Test
  void populateTransaction_populatesStakeRegistrations() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();
    StakeRegistrationEntity entity1 = new StakeRegistrationEntity();
    entity1.setTxHash("txHash1");

    StakeRegistrationEntity entity2 = new StakeRegistrationEntity();
    entity2.setTxHash("txHash2");

    List<StakeRegistrationEntity> stakeRegistrations = List.of(entity1, entity2);

    when(transactionMapper.mapStakeRegistrationEntityToStakeRegistration(entity1))
            .thenReturn(new StakeRegistration());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(),
            stakeRegistrations,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
    );

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);

    assertThat(transaction.getStakeRegistrations().size()).isEqualTo(1);
  }

  @Test
  void populateTransaction_populatesDelegations() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();
    PoolDelegationEntity entity1 = new PoolDelegationEntity();
    entity1.setTxHash("txHash1");

    PoolDelegationEntity entity2 = new PoolDelegationEntity();
    entity2.setTxHash("txHash2");

    List<PoolDelegationEntity> poolDelegations = List.of(entity1, entity2);

    when(transactionMapper.mapPoolDelegationEntityToDelegation(entity1))
            .thenReturn(new StakePoolDelegation());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(),
            Collections.emptyList(),
            poolDelegations,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
    );

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.getStakePoolDelegations().size()).isEqualTo(1);
  }

  @Test
  void populateTransaction_populatesWithdrawals() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();

    WithdrawalEntity entity1 = new WithdrawalEntity();
    entity1.setTxHash("txHash1");

    WithdrawalEntity entity2 = new WithdrawalEntity();
    entity2.setTxHash("txHash2");

    List<WithdrawalEntity> withdrawals = List.of(entity1, entity2);

    when(transactionMapper.mapWithdrawalEntityToWithdrawal(entity1))
            .thenReturn(new Withdrawal());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            withdrawals,
            Collections.emptyList()
    );

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.getWithdrawals().size()).isEqualTo(1);
  }

  @Test
  void populateTransaction_populatesPoolRegistrations() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();
    PoolRegistrationEntity entity1 = new PoolRegistrationEntity();
    entity1.setTxHash("txHash1");

    PoolRegistrationEntity entity2 = new PoolRegistrationEntity();
    entity2.setTxHash("txHash2");

    List<PoolRegistrationEntity> poolRegistrations = List.of(entity1, entity2);

    when(transactionMapper.mapEntityToPoolRegistration(entity1))
            .thenReturn(new PoolRegistration());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            poolRegistrations,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
    );

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.getPoolRegistrations().size()).isEqualTo(1);
  }

  @Test
  void populateTransaction_populatesPoolRetirements() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();
    PoolRetirementEntity entity1 = new PoolRetirementEntity();
    entity1.setTxHash("txHash1");

    PoolRetirementEntity entity2 = new PoolRetirementEntity();
    entity2.setTxHash("txHash2");

    List<PoolRetirementEntity> poolRetirements = List.of(entity1, entity2);

    when(transactionMapper.mapEntityToPoolRetirement(entity1))
            .thenReturn(new PoolRetirement());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            poolRetirements,
            Collections.emptyList(),
            Collections.emptyList()
    );

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.getPoolRetirements().size()).isEqualTo(1);
  }

  @Test
  void findOldestBlockIdentifier_throwsException_whenPruningIsEnabled() {
    // Given
    ledgerBlockService.setRemovalOfSpentUTxOsEnabled(true);
    BlockIdentifierExtended latestBlock = new BlockIdentifierExtended();
    latestBlock.setNumber(5000L);

    // When & Then
    ApiException exception = assertThrows(ApiException.class, () ->
        ledgerBlockService.findOldestBlockIdentifier(latestBlock));

    // Verify the exception is the correct type
    assertThat(exception.getError().getCode()).isEqualTo(OLDEST_BLOCK_NOT_FOUND.getCode());
  }

  @Test
  void findOldestBlockIdentifier_returnsGenesisBlock_whenTargetBlockNumberIsNegative() {
    // Given
    ledgerBlockService.setRemovalOfSpentUTxOsEnabled(true);
    ledgerBlockService.setRemoveSpentUTxOsLastBlocksGraceCount(100);

    BlockIdentifierExtended latestBlock = new BlockIdentifierExtended();
    latestBlock.setNumber(50L);

    BlockIdentifierExtended genesisBlock = new BlockIdentifierExtended();
    genesisBlock.setNumber(0L);
    genesisBlock.setHash("Genesis");
    ledgerBlockService.setCachedGenesisBlock(genesisBlock);

    // When
    BlockIdentifierExtended result = ledgerBlockService.findOldestBlockIdentifier(latestBlock);

    // Then
    assertThat(result).isSameAs(genesisBlock);
  }

  @Test
  void findOldestBlockIdentifier_returnsCorrectBlock_whenTargetBlockNumberIsValid() {
    // Given
    ledgerBlockService.setRemovalOfSpentUTxOsEnabled(true);
    ledgerBlockService.setRemoveSpentUTxOsLastBlocksGraceCount(100);

    BlockIdentifierExtended latestBlock = new BlockIdentifierExtended();
    latestBlock.setNumber(500L);

    BlockIdentifierProjection blockProjection = new BlockIdentifierProjection() {
      @Override
      public String getHash() {
        return "oldestHash";
      }

      @Override
      public Long getNumber() {
        return 400L; // 500 - 100 = 400
      }

      @Override
      public Long getBlockTimeInSeconds() {
        return 0L;
      }

      @Override
      public Long getSlot() {
        return 0L;
      }
    };

    BlockIdentifierExtended expectedBlock = new BlockIdentifierExtended();
    expectedBlock.setNumber(400L);
    expectedBlock.setHash("oldestHash");

    when(blockRepository.findBlockProjectionByNumber(400L)).thenReturn(Optional.of(blockProjection));
    when(blockMapper.mapToBlockIdentifierExtended(blockProjection)).thenReturn(expectedBlock);

    // When
    BlockIdentifierExtended result = ledgerBlockService.findOldestBlockIdentifier(latestBlock);

    // Then
    assertThat(result).isEqualTo(expectedBlock);
    verify(blockRepository).findBlockProjectionByNumber(400L);
    verify(blockMapper).mapToBlockIdentifierExtended(blockProjection);
  }


  @Test
  void findOldestBlockIdentifier_throwsException_whenTargetBlockIsNotFound() {
    // Given
    ledgerBlockService.setRemovalOfSpentUTxOsEnabled(false);
    ledgerBlockService.setRemoveSpentUTxOsLastBlocksGraceCount(100);

    BlockIdentifierExtended latestBlock = new BlockIdentifierExtended();
    latestBlock.setNumber(500L);

    // When & Then
    ApiException exception = assertThrows(ApiException.class, () ->
        ledgerBlockService.findOldestBlockIdentifier(latestBlock));

    // Verify the exception is the correct type
    assertThat(exception.getError().getCode()).isEqualTo(OLDEST_BLOCK_NOT_FOUND.getCode());
  }

}
