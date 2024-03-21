package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.BlockResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

class BlockToBlockResponseTest {

  private ModelMapper modelMapper;

  @BeforeEach
  void setUp() {
    modelMapper = new ModelMapper();
  }

  @Test
  void toDto_Ok() { //TODO how to we agree to name tests? Maybe shouldMapDtoOK?

    BlockToBlockResponse my = new BlockToBlockResponse(modelMapper);
    my.modelMapper.validate();

    Block block = newBlock();
    BlockResponse resp = my.toDto(newBlock());

    assertThat(block.getHash()).isEqualTo(resp.getBlock().getBlockIdentifier().getHash());
    assertThat(block.getNumber()).isEqualTo(resp.getBlock().getBlockIdentifier().getIndex());

    assertThat(block.getPreviousBlockHash()).isEqualTo(
        resp.getBlock().getParentBlockIdentifier().getHash());

    assertThat(block.getPreviousBlockNumber()).isEqualTo(
        resp.getBlock().getParentBlockIdentifier().getIndex());



  }

  private Block newBlock() {
    return new Block(
        "hash",
        1L,
        2L,
        "prevHashBlock",
        21L,
        3L,
        "createdAt",
        4, 5,
        6L, newTransactions(),
        "poolDeposit");
  }

  private List<Transaction> newTransactions() {
    return List.of(new Transaction()); //TODO saa: fill TransactionDto with data
  }


}


