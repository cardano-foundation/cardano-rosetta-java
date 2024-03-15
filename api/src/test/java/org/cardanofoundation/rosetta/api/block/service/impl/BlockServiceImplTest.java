package org.cardanofoundation.rosetta.api.block.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.PartialBlockIdentifier;
import org.openapitools.client.model.SubNetworkIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

  @Mock
  private LedgerDataProviderService ledgerDataProviderService;
  @InjectMocks
  private BlockServiceImpl blockService;

  @Test
  void getBlockByBlockRequest() {

    //given
    long index = 1;
    String hash = "hash1";

    String genesisPath = "../config/preprod/shelley-genesis.json";
    ReflectionTestUtils.setField(blockService , "genesisPath" , genesisPath);

    when(ledgerDataProviderService.findBlock(index, hash)).thenReturn(newBlock());
    when(ledgerDataProviderService.findTransactionsByBlock(index, hash)).thenReturn(
        newTransactionList());

    //when
    Block block = blockService.findBlock(index, hash);

    //then
    assertThat(block).isNotNull();


  }

  private List<Transaction> newTransactionList() {
    Transaction e1 = new Transaction("hash1", "blockHash1", 1L,
        "fee1", 2L, true, 3L,
        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    return List.of(e1);
  }

  private Block newBlock() {
    return new Block(
        "hash1",
        1L,
        2L,
        "prevHashBlock1",
        21L,
        3L,
        "createdAt1",
        4, 5,
        6L, null,
        "poolDeposit1");
  }

  private NetworkIdentifier newNetworkIdentifier() {
    NetworkIdentifier nid = new NetworkIdentifier();
    return NetworkIdentifier
        .builder()
        .network("network1")
        .blockchain("cardano-dev")
        .subNetworkIdentifier(newSubNetworkIdentifier())
        .build();
  }

  private SubNetworkIdentifier newSubNetworkIdentifier() {
    return SubNetworkIdentifier
        .builder()
        .network("network")
        .metadata("metadata")
        .build();
  }

  private PartialBlockIdentifier newPartialBlockIdentifier() {
    return null;
  }


  @Test
  void findTransactionsByBlock() {
  }

  @Test
  void getBlockTransaction() {
  }

  @Test
  void findBlock() {
  }
}