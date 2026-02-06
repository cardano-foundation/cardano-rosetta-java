package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiRuntimeException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.util.PolicyUtil;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;

@Slf4j
public class Step1_SimpleTransactions implements TransactionRunner {

  private Account sender1;
  private Account sender2;

  private String sender1Addr;
  private String sender2Addr;

  private Policy policy;

  @Override
  public int getExecutionOrder() {
    return 1;  // Execute first
  }

  @Override
  public void init() {
    sender1 = new Account(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC);
    sender1Addr = sender1.baseAddress();

    sender2 = new Account(Networks.testnet(), TestConstants.SENDER_2_MNEMONIC);
    sender2Addr = sender2.baseAddress();
  }

  /**
   * Populating the generatedDataMap with the transaction data. On every transaction, map is
   * populated with a new entry - name of the transaction as a key and the TransactionBlockDetails
   * object as a value, where all necessary transaction data is stored.
   *
   * @return a map of transaction block details
   */
  @Override
  public Map<String, TransactionBlockDetails> runTransactions() {
    Map<String, TransactionBlockDetails> generatedDataMap = HashMap.newHashMap(6);
    generatedDataMap.put(TestTransactionNames.SIMPLE_TRANSACTION.getName(), simpleTransaction());
    generatedDataMap.put(TestTransactionNames.SIMPLE_LOVELACE_FIRST_TRANSACTION.getName(),
            simpleLovelaceTransaction());
    generatedDataMap.put(TestTransactionNames.SIMPLE_LOVELACE_SECOND_TRANSACTION.getName(),
            simpleLovelaceTransaction());
    generatedDataMap.put(TestTransactionNames.SIMPLE_NEW_COINS_TRANSACTION.getName(),
            simpleNewCoinsTransaction());
    generatedDataMap.put(TestTransactionNames.SIMPLE_NEW_EMPTY_NAME_COINS_TRANSACTION.getName(),
            simpleNewEmptyNameCoinsTransaction());
    generatedDataMap.put(TestTransactionNames.STAKE_KEY_REGISTRATION_TRANSACTION.getName(),
            stakeKeyRegistrationTransaction());
    generatedDataMap.put(TestTransactionNames.STAKE_KEY_DEREGISTRATION_TRANSACTION.getName(),
            stakeKeyDeregistrationTransaction());
    generatedDataMap.put(TestTransactionNames.SIMPLE_TRANSACTION.getName() + "_2",
            simpleTransaction());

    return generatedDataMap;
  }

  /**
   * Running a simple transaction. SIMPLE_ADDRESS_2 already has some ADA. INITIALLY, it has
   * 1_000_000_000 lovelace (1000 ADA). We will send some lovelace from SIMPLE_ADDRESS_2 to
   * SIMPLE_ADDRESS_1.
   *
   * @return data object with the generated data.
   */
  public TransactionBlockDetails simpleTransaction() {
    double adaToSend = BaseFunctions.lovelaceToAda(TestConstants.ACCOUNT_BALANCE_LOVELACE_AMOUNT);
    log.info("Is about to send {} ADA to the address: \n{}\nfrom the address: \n{}",
            adaToSend, sender1Addr, sender2Addr);
    Tx tx = new Tx()
            .payToAddress(sender1Addr, Amount.ada(adaToSend))
            .from(sender2Addr);

    return completeTransaction(tx, sender2, sender1Addr);
  }

  /**
   * After this transaction, the RECEIVER_1 will have 2 lovelace in two utxo.
   */
  public TransactionBlockDetails simpleLovelaceTransaction() {
    long lovelace = Long.parseLong(TestConstants.ACCOUNT_BALANCE_LOVELACE_SMALL_AMOUNT);
    log.info("Is about to send {} lovelace to the address: \n{}\nfrom the address: \n{}",
            lovelace, TestConstants.RECEIVER_1, sender2Addr);
    Tx tx = new Tx()
            .payToAddress(TestConstants.RECEIVER_1,
                    Amount.lovelace(BigInteger.valueOf(lovelace)))
            .from(sender2Addr);

    return completeTransaction(tx, sender2, TestConstants.RECEIVER_1);
  }

  /**
   * After this transaction, the sender1 will have 1000 of the new coins (MyAsset) in the utxo.
   */
  public TransactionBlockDetails simpleNewCoinsTransaction() {
    BigInteger quantity = BigInteger.valueOf(1000);
    log.info("Minting {} of the new coins ({}) to the {}", quantity, TestConstants.MY_ASSET_NAME,
            sender1Addr);

    try {
      policy = PolicyUtil.createMultiSigScriptAtLeastPolicy("test_policy", 1, 1);
    } catch (CborSerializationException e) {
      throw new ApiRuntimeException("Error creating policy", e);
    }

    Tx tx = new Tx()
            .mintAssets(policy.getPolicyScript(),
                    new Asset(TestConstants.MY_ASSET_NAME, quantity), sender1Addr)
            .attachMetadata(MessageMetadata.create().add("Minting tx"))
            .from(sender1Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
            .withSigner(SignerProviders.signerFrom(sender1))
            .withSigner(SignerProviders.signerFrom(policy))
            .completeAndWait(Duration.ofMinutes(1));

    return getTransactionOutput(sender1Addr, complete);
  }

  private TransactionBlockDetails simpleNewEmptyNameCoinsTransaction() {
    String emptyAssetName = "";
    BigInteger quantity = BigInteger.valueOf(1000);
    log.info("Minting {} of the new coins ({}) to the {}", quantity, emptyAssetName, sender1Addr);

    Tx tx = new Tx()
            .mintAssets(policy.getPolicyScript(), new Asset(emptyAssetName, quantity), sender1Addr)
            .attachMetadata(MessageMetadata.create().add("Minting tx"))
            .from(sender1Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
            .withSigner(SignerProviders.signerFrom(sender1))
            .withSigner(SignerProviders.signerFrom(policy))
            .completeAndWait(Duration.ofMinutes(1));

    return getTransactionOutput(sender1Addr, complete);
  }

  public TransactionBlockDetails stakeKeyRegistrationTransaction() {
    log.info("Stake key registration transaction for the address: \n{} \nand the address \n{}",
            sender2Addr, TestConstants.RECEIVER_3);
    Tx tx = new Tx()
            .registerStakeAddress(sender2Addr)
            .payToAddress(TestConstants.RECEIVER_3, Amount.ada(1.0))
            .from(sender2Addr);

    Result<String> result = quickTxBuilder.compose(tx)
            .withSigner(SignerProviders.signerFrom(sender2))
            .completeAndWait(Duration.ofMinutes(1));

    log.info("Stake key registration tx: {}", result.getValue());

    if (result.isSuccessful()) {
      String txHash = result.getValue();
      BaseFunctions.checkIfUtxoAvailable(txHash, sender2Addr);
      Block block = BaseFunctions.getBlock(txHash);

      return new TransactionBlockDetails(txHash, block.getHash(), block.getHeight());
    }

    throw new RuntimeException("Error in registering stake key, reason: " + result.getResponse());
  }

  public TransactionBlockDetails stakeKeyDeregistrationTransaction() {
    log.info("Stake key deregistration transaction for the address: \n{} \nand the address \n{}",
            sender2Addr, TestConstants.RECEIVER_3);
    Tx tx = new Tx()
            .deregisterStakeAddress(sender2Addr)
            .payToAddress(TestConstants.RECEIVER_3, Amount.ada(1.0))
            .from(sender2Addr);

    Result<String> result = quickTxBuilder.compose(tx)
            .withSigner(SignerProviders.stakeKeySignerFrom(sender2))
            .withSigner(SignerProviders.signerFrom(sender2))
            .completeAndWait(Duration.ofMinutes(1));

    String txHash = result.getValue();
    BaseFunctions.checkIfUtxoAvailable(txHash, sender2Addr);
    Block block = BaseFunctions.getBlock(txHash);

    return new TransactionBlockDetails(txHash, block.getHash(), block.getHeight());
  }

  private TransactionBlockDetails completeTransaction(Tx tx, Account from,
                                                      String addressTo) {
    Result<String> complete = quickTxBuilder.compose(tx)
            .withSigner(SignerProviders.signerFrom(from))
            .completeAndWait(Duration.ofMinutes(1));

    return getTransactionOutput(addressTo, complete);
  }

  private TransactionBlockDetails getTransactionOutput(String addressTo, Result<String> result) {
    String txHash = result.getValue();
    log.info("Transaction hash: {}", txHash);
    BaseFunctions.checkIfUtxoAvailable(txHash, addressTo);
    Block transactionBlock = BaseFunctions.getBlock(txHash);
    String hash = transactionBlock.getHash();
    log.info("Block hash: {}", hash);

    return new TransactionBlockDetails(txHash, hash, transactionBlock.getHeight());
  }

}
