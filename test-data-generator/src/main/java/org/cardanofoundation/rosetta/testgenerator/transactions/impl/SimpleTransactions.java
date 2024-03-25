package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import java.math.BigInteger;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.Tx;

import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;

@Slf4j
public class SimpleTransactions implements TransactionRunner {

  private static final String SENDER_1_LOG = "Sender1 address: {}";
  private static final String SENDER_2_LOG = "Sender2 address: {}";
  private Account sender1;
  private Account sender2;

  private String sender1Addr;
  private String sender2Addr;

  private GeneratedTestDataDTO generatedTestData;

  @Override
  public void init() {
    sender1 = new Account(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC);
    sender1Addr = sender1.baseAddress();

    sender2 = new Account(Networks.testnet(), TestConstants.SENDER_2_MNEMONIC);
    sender2Addr = sender2.baseAddress();
  }

  @Override
  public GeneratedTestDataDTO runTransactions(GeneratedTestDataDTO generatedTestData) {
    this.generatedTestData = generatedTestData;

    simpleTransaction();
    simpleLovelaceTransaction();
    stakeKeyRegistration();

    stakeKeyDeregistration();

    return this.generatedTestData;
  }

  public void simpleTransaction() {
    log.info(SENDER_1_LOG, sender1.baseAddress());
    log.info(SENDER_2_LOG, sender2.baseAddress());
    Tx tx = new Tx()
        .payToAddress(sender1Addr,
            Amount.ada(BaseFunctions.lovelaceToAda(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT)))
        .from(sender2Addr);

    completeAndAddToTestDataTransaction(tx, sender2, sender1Addr);
  }

  public void simpleLovelaceTransaction() {
    long halfOfLovelace = Long.parseLong(TestConstants.ACCOUNT_BALANCE_LOVELACE_AMOUNT)/2;
    log.info("1st - Is about to send {} lovelace to the address: \n{}\nfrom the address: \n{}",
        halfOfLovelace, TestConstants.RECEIVER_1, sender2Addr);
    Tx tx = new Tx()
        .payToAddress(TestConstants.RECEIVER_1,
            Amount.lovelace(BigInteger.valueOf(halfOfLovelace)))
        .from(sender2Addr);

    completeAndAddToTestDataTransaction(tx, sender2, TestConstants.RECEIVER_1);

    log.info("2nd - Is about to send {} lovelace to the address: \n{}\nfrom the address: \n{}",
        halfOfLovelace, TestConstants.RECEIVER_1, sender2Addr);
    tx = new Tx()
        .payToAddress(TestConstants.RECEIVER_1,
            Amount.lovelace(BigInteger.valueOf(halfOfLovelace)))
        .from(sender2Addr);

    completeAndAddToTestDataTransaction(tx, sender2, TestConstants.RECEIVER_1);
  }

  private void completeAndAddToTestDataTransaction(Tx tx, Account from, String addressTo) {
    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(from))
        .complete();

    String txHash = complete.getValue();
    log.info("Transaction hash: {}", txHash);
    BaseFunctions.checkIfUtxoAvailable(txHash, addressTo);
    Block transactionBlock = BaseFunctions.getBlock(txHash);
    String hash = transactionBlock.getHash();
    log.info("Block hash: {}", hash);
    if (generatedTestData != null) {
      generatedTestData.setTopUpTxHash(txHash);
      generatedTestData.setTopUpBlockHash(transactionBlock.getHash());
      generatedTestData.setTopUpBlockNumber(transactionBlock.getHeight());
      log.info("Transaction with hash {} added to the generatedTestData", txHash);
    }
  }

  public void stakeKeyRegistration() {
    log.info(SENDER_2_LOG, sender2.baseAddress());
    Tx tx = new Tx()
        .registerStakeAddress(sender2Addr)
        .payToAddress(TestConstants.RECEIVER_3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    log.info("Stake key registration tx: {}", complete.getValue());

    String txHash = complete.getValue();
    BaseFunctions.checkIfUtxoAvailable(txHash, sender2Addr);
    Block block = BaseFunctions.getBlock(txHash);
    if (generatedTestData != null) {
      generatedTestData.setStakeKeyRegistrationTxHash(txHash);
      generatedTestData.setStakeKeyRegistrationBlockHash(block.getHash());
      generatedTestData.setStakeKeyRegistrationBlockSlot(block.getHeight());
    }
  }

  public void stakeKeyDeregistration() {
    Tx tx = new Tx()
        .deregisterStakeAddress(sender2Addr)
        .payToAddress(TestConstants.RECEIVER_3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.stakeKeySignerFrom(sender2))
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    String txHash = complete.getValue();
    BaseFunctions.checkIfUtxoAvailable(txHash, sender2Addr);
    Block block = BaseFunctions.getBlock(txHash);

    if (generatedTestData != null) {
      generatedTestData.setStakeKeyDeregistrationTxHash(txHash);
      generatedTestData.setStakeKeyDeregistrationBlockHash(block.getHash());
      generatedTestData.setStakeKeyDeregistrationBlockNumber(block.getHeight());
    }
  }
}
