package org.cardanofoundation.rosetta.api.block.service;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.BlockRepository;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

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
  private ProtocolParamService protocolParamService;
  @Mock
  private BlockRepository blockRepository;
  @Mock
  private AddressUtxoRepository addressUtxoRepository;

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

}
