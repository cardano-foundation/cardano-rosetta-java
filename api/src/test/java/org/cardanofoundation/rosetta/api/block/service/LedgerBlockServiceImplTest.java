package org.cardanofoundation.rosetta.api.block.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import lombok.val;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.*;
import org.cardanofoundation.rosetta.api.block.model.entity.*;
import org.cardanofoundation.rosetta.api.block.model.repository.*;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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
  private DelegationRepository delegationRepository;

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

  @Test
  void testExecutorServiceExceptions() {
    //given
    long index = 1;
    String hash = "hash1";

    BlockEntity blockEntity = mock(BlockEntity.class);

    when(blockRepository.findByNumberAndHash(index, hash)).thenReturn(Optional.of(blockEntity));
    Block model = mock(Block.class);
    BlockTx blockTx = mock(BlockTx.class);
    when(blockTx.getHash()).thenReturn(hash);
    when(model.getTransactions()).thenReturn(Collections.singletonList(blockTx));
    when(blockMapper.mapToBlock(blockEntity)).thenReturn(model);
    ProtocolParams protocolParams = mock(ProtocolParams.class);
    when(protocolParamService.findProtocolParameters()).thenReturn(protocolParams);
    ExecutionException exception = new ExecutionException(new Throwable());

    given(addressUtxoRepository.findByTxHashIn(Collections.singletonList(hash)))
            .willAnswer(t -> exception);

    //when
    ApiException actualException = assertThrows(ApiException.class,
            () -> ledgerBlockService.findBlock(index, hash));

    //then
    assertEquals(RosettaErrorType.UNSPECIFIED_ERROR.getMessage(),
            actualException.getError().getMessage());
    assertEquals("Error fetching transaction data",
            actualException.getError().getDetails().getMessage());
  }

  @Test
  void populateTransaction_marksTransactionAsInvalid_ifFoundInInvalidTransactionRepository() {
    val transaction = new BlockTx();
    transaction.setHash("txHash1");
    transaction.setInputs(Collections.emptyList());
    transaction.setOutputs(Collections.emptyList());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(), // utxos
            Collections.emptyList(), // stakeRegistrations
            Collections.emptyList(), // delegations
            Collections.emptyList(), // poolRegistrations
            Collections.emptyList(), // poolRetirements
            Collections.emptyList()  // withdrawals
    );

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();

    when(invalidTransactionRepository.findById("txHash1")).thenReturn(Optional.of(new InvalidTransactionEntity()));
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
            Collections.emptyList(), // poolRegistrations
            Collections.emptyList(), // poolRetirements
            Collections.emptyList()  // withdrawals
    );

    val utxoMap = new HashMap<LedgerBlockServiceImpl.UtxoKey, AddressUtxoEntity>();
    when(invalidTransactionRepository.findById("txHash1")).thenReturn(Optional.empty());
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
    DelegationEntity entity1 = new DelegationEntity();
    entity1.setTxHash("txHash1");

    DelegationEntity entity2 = new DelegationEntity();
    entity2.setTxHash("txHash2");

    List<DelegationEntity> delegations = List.of(entity1, entity2);

    when(transactionMapper.mapDelegationEntityToDelegation(entity1))
            .thenReturn(new StakePoolDelegation());

    val transactionInfo = new LedgerBlockServiceImpl.TransactionInfo(
            Collections.emptyList(),
            Collections.emptyList(),
            delegations,
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
            withdrawals
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
            poolRegistrations,
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
            poolRetirements,
            Collections.emptyList()
    );

    ledgerBlockService.populateTransaction(transaction, transactionInfo, utxoMap);
    assertThat(transaction.getPoolRetirements().size()).isEqualTo(1);
  }

}
