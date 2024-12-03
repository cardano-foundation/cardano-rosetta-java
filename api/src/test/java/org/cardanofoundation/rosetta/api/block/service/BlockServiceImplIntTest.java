package org.cardanofoundation.rosetta.api.block.service;

import java.math.BigInteger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.ACCOUNT_BALANCE_LOVELACE_AMOUNT;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.SENDER_2_ADDRESS;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.TEST_ACCOUNT_ADDRESS;
import static org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames.SIMPLE_TRANSACTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
class BlockServiceImplIntTest extends IntegrationTest {

  @Autowired
  private BlockService blockService;
  final TransactionBlockDetails simpleTx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private ProtocolParamService protocolParamService;

  @Test
  void getBlockWithTransaction_Test() {
    //given
    //when
    Block block = blockService.findBlock(simpleTx.blockNumber(), simpleTx.blockHash());

    //then
    assertEquals(simpleTx.blockHash(), block.getHash());
    assertEquals(simpleTx.blockNumber(), block.getNumber());
    assertEquals(1, block.getTransactions().size());

    Utxo receiverUtxoDto = block.getTransactions().getFirst().getOutputs().getFirst();
    assertEquals(TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
    assertEquals(simpleTx.txHash(), receiverUtxoDto.getTxHash());
    assertEquals(ACCOUNT_BALANCE_LOVELACE_AMOUNT,
        receiverUtxoDto.getAmounts().getFirst().getQuantity().toString());

  }

  @Test
  void getBlockTransaction_Test() {
    //given
    long blockNo = simpleTx.blockNumber();
    String blockHash = simpleTx.blockHash();
    String blockTxHash = simpleTx.txHash();
    String fee = "172321";
    //when
    BlockTx tx = blockService.getBlockTransaction(blockNo, blockHash, blockTxHash);
    //then
    assertEquals(blockTxHash, tx.getHash());
    assertEquals(blockNo, tx.getBlockNo());
    assertEquals(fee, tx.getFee());
    assertEquals(284, tx.getSize());
    assertEquals(0, tx.getScriptSize());
    assertEquals(1, tx.getInputs().size());
    assertEquals(2, tx.getOutputs().size());
    assertEquals(0, tx.getStakeRegistrations().size());
    assertEquals(0, tx.getPoolRegistrations().size());
    assertEquals(0, tx.getPoolRetirements().size());
    assertEquals(0, tx.getDelegations().size());

    assertNotNull(entityManager);
    BlockEntity fromBlockB = entityManager
        .createQuery("from BlockEntity b where b.number=:block", BlockEntity.class)
        .setParameter("block", simpleTx.blockNumber())
        .getSingleResult();

    Utxo inUtxo = tx.getInputs().getFirst();
    UtxoKey expectedInputKey = fromBlockB.getTransactions().getFirst().getInputKeys().getFirst();

    assertEquals(SENDER_2_ADDRESS, inUtxo.getOwnerAddr());
    assertEquals(expectedInputKey.getTxHash(), inUtxo.getTxHash());
    assertEquals(expectedInputKey.getOutputIndex(), inUtxo.getOutputIndex());

    Utxo outUtxo1 = tx.getOutputs().getFirst();
    assertEquals(TEST_ACCOUNT_ADDRESS, outUtxo1.getOwnerAddr());
    assertEquals(blockTxHash, outUtxo1.getTxHash());
    assertEquals(ACCOUNT_BALANCE_LOVELACE_AMOUNT, outUtxo1.getAmounts().getFirst().getQuantity().toString());

    Utxo outUtxo2 = tx.getOutputs().getLast();
    assertEquals(SENDER_2_ADDRESS, outUtxo2.getOwnerAddr());
    assertEquals(blockTxHash, outUtxo2.getTxHash());

    //  init deposit was 1000 ADA for the account1: addr_test1qp73lju...
    //  (@see test-data-generator/README.md)
    BigInteger initAmountSender1 = BigInteger.valueOf(1000 * 1_000_000); //ADA to lovelace
    BigInteger expected = initAmountSender1
        .add(fromBlockB.getTransactions().getFirst().getFee().negate()) //fee
        .add(new BigInteger(ACCOUNT_BALANCE_LOVELACE_AMOUNT).negate()); //sent amount

    assertEquals(expected, outUtxo2.getAmounts().getFirst().getQuantity());

  }


  @Test
  void getDepositPool_Test() {
    assertThat(protocolParamService.findProtocolParameters().getPoolDeposit())
        .isEqualTo(500000000);
  }

}
