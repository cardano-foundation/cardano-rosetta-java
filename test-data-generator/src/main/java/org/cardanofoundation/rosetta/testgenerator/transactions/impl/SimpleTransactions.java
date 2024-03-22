package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

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

  private Account sender1;
  private Account sender2;

  private String sender1Addr = "addr_test1qz5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxvw2pfap6uqmkj3n6zmlrsgz397md2gt7yqs5p255uygaesx608y5";
  private String sender2Addr = "addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah";

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
    stakeKeyRegistration();

    stakeKeyDeregistration();

    return this.generatedTestData;
  }

  public void simpleTransaction() {
    log.info("Sender1 address: {}", sender1.baseAddress());
    log.info("Sender2 address: {}", sender2.baseAddress());
    Tx tx = new Tx()
        .payToAddress(sender1Addr,
            Amount.ada(BaseFunctions.lovelaceToAda(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT)))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();

    String txHash = complete.getValue();
    log.info("Transaction hash: " + txHash);
    BaseFunctions.checkIfUtxoAvailable(txHash, sender1Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    log.info("Block hash: " + hash);
    if (generatedTestData != null) {
      generatedTestData.setTopUpTxHash(txHash);
      generatedTestData.setTopUpBlockHash(value1.getHash());
      generatedTestData.setTopUpBlockNumber(value1.getHeight());
    }
  }

  public void stakeKeyRegistration() {
    log.info("Sender1 address: {}", sender1.baseAddress());
    log.info("Sender2 address: {}", sender2.baseAddress());
    Tx tx = new Tx()
        .registerStakeAddress(sender2Addr)
        .payToAddress(TestConstants.RECEIVER_3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    log.info("Stake key registration tx: " + complete.getValue());

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
