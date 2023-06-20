package org.cardanofoundation.rosetta.consumer.unit.service.impl.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.repository.AddressTokenRepository;
import org.cardanofoundation.rosetta.consumer.repository.AddressTxBalanceRepository;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.DatumRepository;
import org.cardanofoundation.rosetta.consumer.repository.DelegationRepository;
import org.cardanofoundation.rosetta.consumer.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.consumer.repository.ExtraKeyWitnessRepository;
import org.cardanofoundation.rosetta.consumer.repository.MaTxMintRepository;
import org.cardanofoundation.rosetta.consumer.repository.MultiAssetTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.ParamProposalRepository;
import org.cardanofoundation.rosetta.consumer.repository.PoolMetadataRefRepository;
import org.cardanofoundation.rosetta.consumer.repository.PoolOwnerRepository;
import org.cardanofoundation.rosetta.consumer.repository.PoolRelayRepository;
import org.cardanofoundation.rosetta.consumer.repository.PoolRetireRepository;
import org.cardanofoundation.rosetta.consumer.repository.PoolUpdateRepository;
import org.cardanofoundation.rosetta.consumer.repository.PotTransferRepository;
import org.cardanofoundation.rosetta.consumer.repository.RedeemerDataRepository;
import org.cardanofoundation.rosetta.consumer.repository.RedeemerRepository;
import org.cardanofoundation.rosetta.consumer.repository.ReferenceInputRepository;
import org.cardanofoundation.rosetta.consumer.repository.ReserveRepository;
import org.cardanofoundation.rosetta.consumer.repository.RollbackHistoryRepository;
import org.cardanofoundation.rosetta.consumer.repository.ScriptRepository;
import org.cardanofoundation.rosetta.consumer.repository.StakeDeregistrationRepository;
import org.cardanofoundation.rosetta.consumer.repository.StakeRegistrationRepository;
import org.cardanofoundation.rosetta.consumer.repository.TreasuryRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxInRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxMetadataRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.repository.WithdrawalRepository;
import org.cardanofoundation.rosetta.consumer.service.AddressBalanceService;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.cardanofoundation.rosetta.consumer.service.MultiAssetService;
import org.cardanofoundation.rosetta.consumer.service.impl.block.RollbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RollbackServiceImplTest {

  @Mock
  BlockRepository blockRepository;

  @Mock
  TxRepository txRepository;

  @Mock
  AddressTokenRepository addressTokenRepository;

  @Mock
  AddressTxBalanceRepository addressTxBalanceRepository;

  @Mock
  DatumRepository datumRepository;

  @Mock
  DelegationRepository delegationRepository;

  @Mock
  ExtraKeyWitnessRepository extraKeyWitnessRepository;
  @Mock
  MaTxMintRepository maTxMintRepository;

  @Mock
  MultiAssetTxOutRepository multiAssetTxOutRepository;

  @Mock
  EpochParamRepository epochParamRepository;

  @Mock
  ParamProposalRepository paramProposalRepository;

  @Mock
  PoolMetadataRefRepository poolMetadataRefRepository;

  @Mock
  PoolOwnerRepository poolOwnerRepository;

  @Mock
  PoolRelayRepository poolRelayRepository;

  @Mock
  PoolRetireRepository poolRetireRepository;

  @Mock
  PoolUpdateRepository poolUpdateRepository;

  @Mock
  PotTransferRepository potTransferRepository;

  @Mock
  RedeemerRepository redeemerRepository;

  @Mock
  RedeemerDataRepository redeemerDataRepository;

  @Mock
  ReferenceInputRepository referenceInputRepository;

  @Mock
  ReserveRepository reserveRepository;

  @Mock
  ScriptRepository scriptRepository;

  @Mock
  StakeDeregistrationRepository stakeDeregistrationRepository;

  @Mock
  StakeRegistrationRepository stakeRegistrationRepository;

  @Mock
  TreasuryRepository treasuryRepository;

  @Mock
  TxInRepository txInRepository;

  @Mock
  TxMetadataRepository txMetadataRepository;

  @Mock
  TxOutRepository txOutRepository;


  @Mock
  WithdrawalRepository withdrawalRepository;

  @Mock
  RollbackHistoryRepository rollbackHistoryRepository;

  @Mock
  EpochService epochService;

  @Mock
  AddressBalanceService addressBalanceService;

  @Mock
  MultiAssetService multiAssetService;


  private static final long ROLLBACK_BLOCK_NO = 123456;

  RollbackServiceImpl victim;

  @BeforeEach
  void setUp() {
    victim = new RollbackServiceImpl(
        blockRepository, txRepository, addressTokenRepository, addressTxBalanceRepository,
        datumRepository, delegationRepository, extraKeyWitnessRepository,
        maTxMintRepository, multiAssetTxOutRepository, epochParamRepository,
        paramProposalRepository,
        poolMetadataRefRepository, poolOwnerRepository, poolRelayRepository, poolRetireRepository,
        poolUpdateRepository, potTransferRepository, redeemerRepository, redeemerDataRepository,
        referenceInputRepository, reserveRepository, scriptRepository,
        stakeDeregistrationRepository,
        stakeRegistrationRepository, treasuryRepository, txInRepository, txMetadataRepository,
        txOutRepository, withdrawalRepository, rollbackHistoryRepository,
        epochService, addressBalanceService, multiAssetService
    );
  }

  @Test
  @DisplayName("Should skip rollback if target rollback block not found")
  void shouldSkipRollbackIfBlockNotFoundTest() { // NOSONAR
    Optional<Block> block = Optional.empty();

    Mockito.when(blockRepository.findBlockByBlockNo(ROLLBACK_BLOCK_NO)).thenReturn(block);

    victim.rollBackFrom(ROLLBACK_BLOCK_NO);

    Mockito.verify(blockRepository, Mockito.times(1)).findBlockByBlockNo(Mockito.anyLong());
    Mockito.verifyNoMoreInteractions(blockRepository);
    Mockito.verifyNoInteractions(txRepository);
    Mockito.verifyNoInteractions(addressTokenRepository);
    Mockito.verifyNoInteractions(addressTxBalanceRepository);
    Mockito.verifyNoInteractions(datumRepository);
    Mockito.verifyNoInteractions(delegationRepository);
    Mockito.verifyNoInteractions(extraKeyWitnessRepository);
    Mockito.verifyNoInteractions(maTxMintRepository);
    Mockito.verifyNoInteractions(multiAssetTxOutRepository);
    Mockito.verifyNoInteractions(epochParamRepository);
    Mockito.verifyNoInteractions(paramProposalRepository);
    Mockito.verifyNoInteractions(poolMetadataRefRepository);
    Mockito.verifyNoInteractions(poolOwnerRepository);
    Mockito.verifyNoInteractions(poolRelayRepository);
    Mockito.verifyNoInteractions(poolRetireRepository);
    Mockito.verifyNoInteractions(poolUpdateRepository);
    Mockito.verifyNoInteractions(potTransferRepository);
    Mockito.verifyNoInteractions(redeemerRepository);
    Mockito.verifyNoInteractions(redeemerDataRepository);
    Mockito.verifyNoInteractions(referenceInputRepository);
    Mockito.verifyNoInteractions(reserveRepository);
    Mockito.verifyNoInteractions(scriptRepository);
    Mockito.verifyNoInteractions(stakeDeregistrationRepository);
    Mockito.verifyNoInteractions(stakeRegistrationRepository);
    Mockito.verifyNoInteractions(treasuryRepository);
    Mockito.verifyNoInteractions(txInRepository);
    Mockito.verifyNoInteractions(txMetadataRepository);
    Mockito.verifyNoInteractions(txOutRepository);
    Mockito.verifyNoInteractions(withdrawalRepository);
    Mockito.verifyNoInteractions(rollbackHistoryRepository);
    Mockito.verifyNoInteractions(epochService);
    Mockito.verifyNoInteractions(addressBalanceService);
    Mockito.verifyNoInteractions(multiAssetService);
  }

  @Test
  @DisplayName("Rollback empty block")
  void rollbackEmptyBlockTest() { // NOSONAR
    Optional<Block> block = Optional.of(Mockito.mock(Block.class));

    Mockito.when(blockRepository.findBlockByBlockNo(ROLLBACK_BLOCK_NO)).thenReturn(block);
    Mockito.when(blockRepository.findAllByBlockNoGreaterThanOrderByBlockNoDesc(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Mockito.when(txRepository.findAllByBlockIn(Mockito.anyCollection()))
        .thenReturn(Collections.emptyList());

    victim.rollBackFrom(ROLLBACK_BLOCK_NO);

    Mockito.verify(blockRepository, Mockito.times(1)).findBlockByBlockNo(Mockito.anyLong());
    Mockito.verify(blockRepository, Mockito.times(1))
        .findAllByBlockNoGreaterThanOrderByBlockNoDesc(Mockito.anyLong());
    Mockito.verify(blockRepository, Mockito.times(1)).deleteAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(blockRepository);
    Mockito.verify(txRepository, Mockito.times(1)).findAllByBlockIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(txRepository);
    Mockito.verifyNoInteractions(addressTokenRepository);
    Mockito.verifyNoInteractions(addressTxBalanceRepository);
    Mockito.verifyNoInteractions(datumRepository);
    Mockito.verifyNoInteractions(delegationRepository);
    Mockito.verifyNoInteractions(extraKeyWitnessRepository);
    Mockito.verifyNoInteractions(maTxMintRepository);
    Mockito.verifyNoInteractions(multiAssetTxOutRepository);
    Mockito.verifyNoInteractions(epochParamRepository);
    Mockito.verifyNoInteractions(paramProposalRepository);
    Mockito.verifyNoInteractions(poolMetadataRefRepository);
    Mockito.verifyNoInteractions(poolOwnerRepository);
    Mockito.verifyNoInteractions(poolRelayRepository);
    Mockito.verifyNoInteractions(poolRetireRepository);
    Mockito.verifyNoInteractions(poolUpdateRepository);
    Mockito.verifyNoInteractions(potTransferRepository);
    Mockito.verifyNoInteractions(redeemerRepository);
    Mockito.verifyNoInteractions(redeemerDataRepository);
    Mockito.verifyNoInteractions(referenceInputRepository);
    Mockito.verifyNoInteractions(reserveRepository);
    Mockito.verifyNoInteractions(scriptRepository);
    Mockito.verifyNoInteractions(stakeDeregistrationRepository);
    Mockito.verifyNoInteractions(stakeRegistrationRepository);
    Mockito.verifyNoInteractions(treasuryRepository);
    Mockito.verifyNoInteractions(txInRepository);
    Mockito.verifyNoInteractions(txMetadataRepository);
    Mockito.verifyNoInteractions(txOutRepository);
    Mockito.verifyNoInteractions(withdrawalRepository);
    Mockito.verify(rollbackHistoryRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(rollbackHistoryRepository);
    Mockito.verify(epochService, Mockito.times(1)).rollbackEpochStats(Mockito.anyList());
    Mockito.verifyNoMoreInteractions(epochService);
    Mockito.verifyNoInteractions(addressBalanceService);
    Mockito.verifyNoInteractions(multiAssetService);
  }

  @Test
  @DisplayName("Rollback block with tx")
  void rollbackBlockWithTxTest() { // NOSONAR
    Optional<Block> block = Optional.of(Mockito.mock(Block.class));

    Mockito.when(blockRepository.findBlockByBlockNo(ROLLBACK_BLOCK_NO)).thenReturn(block);
    Mockito.when(blockRepository.findAllByBlockNoGreaterThanOrderByBlockNoDesc(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Mockito.when(txRepository.findAllByBlockIn(Mockito.anyCollection()))
        .thenReturn(List.of(Mockito.mock(Tx.class)));

    victim.rollBackFrom(ROLLBACK_BLOCK_NO);

    Mockito.verify(blockRepository, Mockito.times(1)).findBlockByBlockNo(Mockito.anyLong());
    Mockito.verify(blockRepository, Mockito.times(1))
        .findAllByBlockNoGreaterThanOrderByBlockNoDesc(Mockito.anyLong());
    Mockito.verify(blockRepository, Mockito.times(1)).deleteAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(blockRepository);
    Mockito.verify(txRepository, Mockito.times(1)).findAllByBlockIn(Mockito.anyCollection());
    Mockito.verify(txRepository, Mockito.times(1)).deleteAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(txRepository);
    Mockito.verify(addressTokenRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(addressTokenRepository);
    Mockito.verify(addressTxBalanceRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(addressTxBalanceRepository);
    Mockito.verify(datumRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(datumRepository);
    Mockito.verify(delegationRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(delegationRepository);
    Mockito.verify(extraKeyWitnessRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(extraKeyWitnessRepository);
    Mockito.verify(maTxMintRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(maTxMintRepository);
    Mockito.verify(multiAssetTxOutRepository, Mockito.times(1))
        .deleteAllByTxOutTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(multiAssetTxOutRepository);
    Mockito.verify(epochParamRepository, Mockito.times(1))
        .deleteAllByBlockIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(epochParamRepository);
    Mockito.verify(paramProposalRepository, Mockito.times(1))
        .deleteAllByRegisteredTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(paramProposalRepository);
    Mockito.verify(poolMetadataRefRepository, Mockito.times(1))
        .deleteAllByRegisteredTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(poolMetadataRefRepository);
    Mockito.verify(poolOwnerRepository, Mockito.times(1))
        .deleteAllByPoolUpdateRegisteredTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(poolOwnerRepository);
    Mockito.verify(poolRelayRepository, Mockito.times(1))
        .deleteAllByPoolUpdateRegisteredTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(poolRelayRepository);
    Mockito.verify(poolRetireRepository, Mockito.times(1))
        .deleteAllByAnnouncedTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(poolRetireRepository);
    Mockito.verify(poolUpdateRepository, Mockito.times(1))
        .deleteAllByRegisteredTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(poolUpdateRepository);
    Mockito.verify(potTransferRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(potTransferRepository);
    Mockito.verify(redeemerRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(redeemerRepository);
    Mockito.verify(redeemerDataRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(redeemerDataRepository);
    Mockito.verify(referenceInputRepository, Mockito.times(1))
        .deleteAllByTxInIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(referenceInputRepository);
    Mockito.verify(reserveRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(reserveRepository);
    Mockito.verify(scriptRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(scriptRepository);
    Mockito.verify(stakeDeregistrationRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(stakeDeregistrationRepository);
    Mockito.verify(stakeRegistrationRepository, Mockito.times(1))
        .deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(stakeRegistrationRepository);
    Mockito.verify(treasuryRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(treasuryRepository);
    Mockito.verify(txInRepository, Mockito.times(1)).deleteAllByTxInputIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(txInRepository);
    Mockito.verify(txMetadataRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(txMetadataRepository);
    Mockito.verify(txOutRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(txOutRepository);

    Mockito.verify(withdrawalRepository, Mockito.times(1)).deleteAllByTxIn(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(withdrawalRepository);
    Mockito.verify(rollbackHistoryRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(rollbackHistoryRepository);
    Mockito.verify(epochService, Mockito.times(1)).rollbackEpochStats(Mockito.anyList());
    Mockito.verifyNoMoreInteractions(epochService);
    Mockito.verify(addressBalanceService, Mockito.times(1))
        .rollbackAddressBalances(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(addressBalanceService);
    Mockito.verify(multiAssetService, Mockito.times(1))
        .rollbackMultiAssets(Mockito.anyCollection());
    Mockito.verifyNoMoreInteractions(multiAssetService);
  }
}
