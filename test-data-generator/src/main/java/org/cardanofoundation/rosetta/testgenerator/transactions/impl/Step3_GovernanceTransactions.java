package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.governance.LegacyDRepId;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.DRep;
import com.bloxbean.cardano.client.transaction.spec.governance.DRepType;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.Voter;
import com.bloxbean.cardano.client.transaction.spec.governance.VoterType;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.InfoAction;
import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.checkIfUtxoAvailable;
import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.SENDER_1_MNEMONIC;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.SENDER_2_MNEMONIC;
import static org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames.*;

@Slf4j
public class Step3_GovernanceTransactions implements TransactionRunner {

  private Account delegatingToAccount;
  private String delegatingToAddress;

  private Account drepAccount;
  private String drepAddr;

  private Account poolAccount;
  private String poolAddr;
  private SecretKey poolColdKey;
  private String poolId;
  private byte[] poolOperatorKeyHash;  // Pool cold key hash for voting

  // Store the governance action ID for voting
  private GovActionId createdGovActionId;

  @Override
  public int getExecutionOrder() {
    return 3;  // Execute third (after PoolTransactions - pool must be registered for SPO voting)
  }

  @Override
  public void init() {
    delegatingToAccount = Account.createFromMnemonic(Networks.testnet(), SENDER_1_MNEMONIC);
    delegatingToAddress = delegatingToAccount.baseAddress();

    log.info("(delegating to a dRep) address: {}", delegatingToAddress);

    drepAccount = Account.createFromMnemonic(Networks.testnet(), SENDER_2_MNEMONIC);
    drepAddr = drepAccount.baseAddress();

    log.info("(dRep) address: {}", drepAddr);

    // Initialize pool account (using SENDER_2 as pool operator)
    poolAccount = Account.createFromMnemonic(Networks.testnet(), SENDER_2_MNEMONIC);
    poolAddr = poolAccount.baseAddress();

    // Load pool cold key for signing
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      poolColdKey = objectMapper.readValue(
          this.getClass().getResourceAsStream("/pool1/cold.skey"),
          SecretKey.class);

      String poolRegistrationCborHex = objectMapper.readTree(
              this.getClass().getResourceAsStream("/pool1/pool-registration.cert"))
          .get("cborHex").asText();

      PoolRegistration poolRegistration = PoolRegistration.deserialize(poolRegistrationCborHex);
      poolId = poolRegistration.getBech32PoolId();
      poolOperatorKeyHash = poolRegistration.getOperator();  // This is the pool cold key hash

      log.info("(Pool) address: {}, Pool ID: {}", poolAddr, poolId);
    } catch (IOException e) {
      log.error("Error reading pool keys: {}", e.getMessage());
      throw new MissingResourceException(e.getMessage(), this.getClass().getName(), "pool1 keys");
    }
  }

  @Override
  public Map<String, TransactionBlockDetails> runTransactions() {
    Map<String, TransactionBlockDetails> generatedDataMap = HashMap.newHashMap(6);

    generatedDataMap.put(STAKE_KEY_REGISTRATION_TRANSACTION.getName() + "_2", registerDelegatingToStakeAddress());
    generatedDataMap.put(DREP_REGISTER.getName(), registerDRep());
    generatedDataMap.put(DREP_VOTE_DELEGATION.getName(), delegateToAdRep());
    generatedDataMap.put(GOVERNANCE_ACTION_INFO.getName(), createGovernanceAction());
    generatedDataMap.put(SPO_VOTE_WITH_RATIONALE.getName(), voteSpoWithRationale());
    generatedDataMap.put(SPO_VOTE_WITHOUT_RATIONALE.getName(), voteSpoWithoutRationale());

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

  public TransactionBlockDetails delegateToAdRep() {
    log.info("Delegating to a dRep...");

    // Create DRep using the stake credential hash
    // The drepId contains the credential hash that we need
    String drepKeyHash = HexUtil.encodeHexString(drepAccount.drepKey().verificationKeyHash());


    Tx tx = new Tx()
            .delegateVotingPowerTo(delegatingToAddress, DRep.addrKeyHash(drepKeyHash))
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

  private TransactionBlockDetails createGovernanceAction() {
    log.info("Creating governance info action...");

    // Create an InfoAction (simplest governance action for testing)
    InfoAction govAction = new InfoAction();

    Anchor anchor = new Anchor(
        "https://gov-action.example.com/info.json",
        HexUtil.decodeHexString("1111111111111111111111111111111111111111111111111111111111111111")
    );

    Tx infoPropTx = new Tx()
        .createProposal(govAction, poolAccount.stakeAddress(), anchor)
        .from(poolAddr);

    Result<String> result = quickTxBuilder.compose(infoPropTx)
        .withSigner(SignerProviders.signerFrom(poolAccount))
        .completeAndWait(Duration.ofMinutes(1));

    if (result.isSuccessful()) {
      String txHash = result.getValue();
      checkIfUtxoAvailable(txHash, poolAddr);
      Block block = BaseFunctions.getBlock(txHash);
      String blockHash = block.getHash();

      // Store the governance action ID for use in voting transactions
      // InfoAction is the first (index 0) governance action in the transaction
      createdGovActionId = new GovActionId(txHash, 0);

      log.info("Governance action created. Block hash: {}, GovActionId: {}#{}",
          blockHash, txHash, 0);

      return new TransactionBlockDetails(txHash, blockHash, block.getHeight());
    }

    throw new RuntimeException("Error creating governance action, reason: " + result.getResponse());
  }

  private TransactionBlockDetails voteSpoWithRationale() {
    log.info("SPO voting WITH rationale...");

    if (createdGovActionId == null) {
      throw new RuntimeException("Governance action must be created before voting");
    }

    // Create vote rationale anchor
    Anchor voteRationale = new Anchor(
        "https://pool.example.com/vote-rationale-yes.json",
        HexUtil.decodeHexString("abcd1234567890abcdefabcd1234567890abcdefabcd1234567890abcdefabcd")
    );

    // Create voter (SPO - Stake Pool Operator using pool cold key credential)
    // Use the pool operator key hash from the pool registration certificate
    Credential poolCredential = Credential.fromKey(poolOperatorKeyHash);
    Voter voter = new Voter(VoterType.STAKING_POOL_KEY_HASH, poolCredential);

    Tx voteYesTx = new Tx()
        .createVote(voter, createdGovActionId, Vote.YES, voteRationale)
        .from(poolAddr);

    Result<String> result = quickTxBuilder.compose(voteYesTx)
        .withSigner(SignerProviders.signerFrom(poolAccount))
        .withSigner(SignerProviders.signerFrom(poolColdKey))
        .completeAndWait(Duration.ofMinutes(1));

    if (result.isSuccessful()) {
      String txHash = result.getValue();
      checkIfUtxoAvailable(txHash, poolAddr);
      Block block = BaseFunctions.getBlock(txHash);
      String blockHash = block.getHash();

      log.info("SPO vote WITH rationale created. Block hash: {}", blockHash);

      return new TransactionBlockDetails(txHash, blockHash, block.getHeight());
    }

    throw new RuntimeException("Error creating SPO vote with rationale, reason: " + result.getResponse());
  }

  private TransactionBlockDetails voteSpoWithoutRationale() {
    log.info("SPO voting WITHOUT rationale...");

    if (createdGovActionId == null) {
      throw new RuntimeException("Governance action must be created before voting");
    }

    // Create voter (SPO - Stake Pool Operator using pool cold key credential)
    // Use the pool operator key hash from the pool registration certificate
    Credential poolCredential = Credential.fromKey(poolOperatorKeyHash);
    Voter voter = new Voter(VoterType.STAKING_POOL_KEY_HASH, poolCredential);

    // Vote without anchor (null rationale)
    Tx voteNoTx = new Tx()
        .createVote(voter, createdGovActionId, Vote.NO, null)
        .from(poolAddr);

    Result<String> result = quickTxBuilder.compose(voteNoTx)
        .withSigner(SignerProviders.signerFrom(poolAccount))
        .withSigner(SignerProviders.signerFrom(poolColdKey))
        .completeAndWait(Duration.ofMinutes(1));

    if (result.isSuccessful()) {
      String txHash = result.getValue();
      checkIfUtxoAvailable(txHash, poolAddr);
      Block block = BaseFunctions.getBlock(txHash);
      String blockHash = block.getHash();

      log.info("SPO vote WITHOUT rationale created. Block hash: {}", blockHash);

      return new TransactionBlockDetails(txHash, blockHash, block.getHeight());
    }

    throw new RuntimeException("Error creating SPO vote without rationale, reason: " + result.getResponse());
  }

}
