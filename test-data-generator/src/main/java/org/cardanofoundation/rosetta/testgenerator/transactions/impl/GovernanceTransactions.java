package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.DRep;
import com.bloxbean.cardano.client.util.HexUtil;
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

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.checkIfUtxoAvailable;
import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;

@Slf4j
public class GovernanceTransactions implements TransactionRunner {

  private Account delegatingToAccount;
  private String delegatingToAddress;

  private Account drepAccount;
  private String drepAddr;

  @Override
  public void init() {
    delegatingToAccount = new Account(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC);
    delegatingToAddress = delegatingToAccount.baseAddress();

    log.info("(delegating to a dRep) address: {}", delegatingToAddress);

    drepAccount = new Account(Networks.testnet(), TestConstants.SENDER_2_MNEMONIC);
    drepAddr = drepAccount.baseAddress();

    log.info("(dRep) address: {}", drepAddr);
  }

  @Override
  public Map<String, TransactionBlockDetails> runTransactions() {
    Map<String, TransactionBlockDetails> generatedDataMap = HashMap.newHashMap(3);

    generatedDataMap.put(TestTransactionNames.STAKE_KEY_REGISTRATION_TRANSACTION.getName() + "_2",
            registerDelegatingToStakeAddress());

    generatedDataMap.put(TestTransactionNames.DREP_REGISTER.getName(),
            registerDRep());

    generatedDataMap.put(TestTransactionNames.DREP_VOTE_DELEGATION.getName(),
            delegateToADRep());

    return generatedDataMap;
  }

  private TransactionBlockDetails registerDelegatingToStakeAddress() {
    Tx registerStakeAddress1Tx = new Tx()
            .registerStakeAddress(delegatingToAddress)
            .payToAddress(delegatingToAddress, Amount.lovelace(BigInteger.valueOf(2000000L)))
            .from(delegatingToAddress);

    Result<String> result = quickTxBuilder.compose(registerStakeAddress1Tx)
            .withSigner(SignerProviders.signerFrom(delegatingToAccount))
            .completeAndWait(Duration.ofMinutes(1));

    if (result.isSuccessful()) {
      String txHash = result.getValue();
      checkIfUtxoAvailable(result.getValue(), delegatingToAddress);
      Block value1 = BaseFunctions.getBlock(txHash);
      String hash = value1.getHash();

      log.info("Block hash:%s".formatted(hash));

      return new TransactionBlockDetails(txHash, hash, value1.getHeight());
    }

    throw new RuntimeException("Error in registering stake address, reason: " + result.getResponse());
  }

  public TransactionBlockDetails registerDRep() {
    log.info("dRep registration...");

    var anchor = new Anchor("https://pages.bloxbean.com/cardano-stake/bloxbean-pool.json",
            HexUtil.decodeHexString("bafef700c0039a2efb056a665b3a8bcd94f8670b88d659f7f3db68340f6f0937"));

    Tx drepRegTx = new Tx()
            .registerDRep(drepAccount, anchor)
            .from(drepAddr);

    Result<String> result = quickTxBuilder.compose(drepRegTx)
            .withSigner(SignerProviders.signerFrom(drepAccount))
            .withSigner(SignerProviders.signerFrom(drepAccount.drepHdKeyPair()))
            .complete();

    if (result.isSuccessful()) {
      BaseFunctions.checkIfUtxoAvailable(result.getValue(), drepAddr);

      String txHash = result.getValue();
      checkIfUtxoAvailable(result.getValue(), drepAddr);
      Block value1 = BaseFunctions.getBlock(txHash);
      String hash = value1.getHash();

      log.info("Block hash:%s".formatted(hash));

      return new TransactionBlockDetails(txHash, hash, value1.getHeight());
    }

    throw new RuntimeException("Error in registering dRep, error:" + result.getResponse());
  }

  public TransactionBlockDetails delegateToADRep() {
    log.info("Delegating to a dRep...");

    // Create DRep using the stake credential hash
    // The drepId contains the credential hash that we need
    String drepId = drepAccount.drepId();
    DRep drep = DRep.addrKeyHash(drepId);

    Tx tx = new Tx()
            .delegateVotingPowerTo(delegatingToAddress, drep)
            .from(drepAddr);

    Result<String> result = quickTxBuilder.compose(tx)
            .withSigner(SignerProviders.stakeKeySignerFrom(delegatingToAccount))
            .withSigner(SignerProviders.signerFrom(drepAccount))
            .completeAndWait();

    if (result.isSuccessful()) {
      String txHash = result.getValue();
      checkIfUtxoAvailable(result.getValue(), drepAddr);
      Block value1 = BaseFunctions.getBlock(txHash);
      String hash = value1.getHash();

      log.info("Block hash:%s".formatted(hash));

      return new TransactionBlockDetails(txHash, hash, value1.getHeight());
    }

    throw new RuntimeException("error in delegating to a dRep, reason:" + result.getResponse());
  }

}
