package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.checkIfUtxoAvailable;
import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;

@Slf4j
public class PoolTransactions implements TransactionRunner {

  private static final String BLOCK_HASH_MASSAGE = "Block hash: ";
  private Account sender1;
  private Account sender2;

  private String sender2Addr;

  @Override
  public void init() {
    sender1 = new Account(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC);

    sender2 = new Account(Networks.testnet(), TestConstants.SENDER_2_MNEMONIC);
    sender2Addr = sender2.baseAddress();
  }

  @Override
  public Map<String, TransactionBlockDetails> runTransactions() {
    Map<String, TransactionBlockDetails> generatedDataMap = HashMap.newHashMap(3);
    generatedDataMap.put(TestConstants.POOL_REGISTRATION_TRANSACTION_NAME, registerPool());
    generatedDataMap.put(TestConstants.POOL_DELEGATION_TRANSACTION_NAME, delegateStakeToPool());
    generatedDataMap.put(TestConstants.POOL_RETIREMENT_TRANSACTION_NAME, retirePool());
    return generatedDataMap;
  }

  public TransactionBlockDetails registerPool() {
    log.info("Registering pool");
    ObjectMapper objectMapper = new ObjectMapper();

    SecretKey coldSkey;
    SecretKey stakeSkey;
    VerificationKey stakeVKey;
    PoolRegistration poolRegistration;

    try {
      coldSkey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/cold.skey"),
          SecretKey.class);
      stakeSkey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/stake.skey"),
          SecretKey.class);
      stakeVKey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/stake.vkey"),
          VerificationKey.class);
      String poolRegistrationCborHex = objectMapper.readTree(
              this.getClass().getResourceAsStream("/pool1/pool-registration.cert")).get("cborHex")
          .asText();
      poolRegistration = PoolRegistration.deserialize(poolRegistrationCborHex);
    } catch (IOException e) {
      log.error("Error reading and parsing pool registration files");
      throw new MissingResourceException(e.getMessage(), this.getClass().getName(),
          "pool-registration.cert");
    }

    Address stakeAddr = AddressProvider.getRewardAddress(
        Credential.fromKey(Blake2bUtil.blake2bHash224(stakeVKey.getBytes())), Networks.testnet());
    log.info("Stake Addr: {}", stakeAddr.toBech32());

    poolRegistration.setPoolMetadataUrl("https://my-pool.com");

    String poolId = Bech32.encode(poolRegistration.getOperator(), "pool");
    log.info("Pool ID: {}", poolId);

    //pool registration
    log.info("Sender1 address: {}", sender1.baseAddress());
    log.info("Sender2 address: {}", sender2.baseAddress());
    Tx registerPoolTx = new Tx()
        .registerStakeAddress(stakeAddr)
        .registerPool(poolRegistration)
        .from(sender2Addr);

    Result<String> result = quickTxBuilder.compose(registerPoolTx)
        .withSigner(SignerProviders.signerFrom(coldSkey))
        .withSigner(SignerProviders.signerFrom(stakeSkey))
        .withSigner(SignerProviders.signerFrom(sender2))
        .completeAndWait(log::info);

    String txHash = result.getValue();
    checkIfUtxoAvailable(result.getValue(), sender2Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    log.info(BLOCK_HASH_MASSAGE + hash);

    return new TransactionBlockDetails(txHash, hash, value1.getHeight());
  }

  public TransactionBlockDetails delegateStakeToPool() {
    log.info("Delegating stake to pool");
    // registering Stake keys first
    Tx tx = new Tx()
        .registerStakeAddress(sender2Addr)
        .payToAddress(TestConstants.RECEIVER_3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> result = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    if (result.isSuccessful()) {
      BaseFunctions.checkIfUtxoAvailable(result.getValue(), sender2Addr);
    }

    String poolId = getPoolId();

    Tx delegateStake = new Tx()
        .delegateTo(sender2Addr, poolId)
        .payToAddress(TestConstants.RECEIVER_3, Amount.ada(2))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(delegateStake)
        .withSigner(SignerProviders.signerFrom(sender2))
        .withSigner(SignerProviders.stakeKeySignerFrom(sender2))
        .complete();

    String txHash = complete.getValue();
    checkIfUtxoAvailable(complete.getValue(), sender2Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    log.info(BLOCK_HASH_MASSAGE + hash);

    return new TransactionBlockDetails(txHash, hash, value1.getHeight());
  }

  public TransactionBlockDetails retirePool() {
    log.info("Retiring pool");
    String poolId = getPoolId();
    ObjectMapper objectMapper = new ObjectMapper();
    SecretKey coldSkey = null;
    try {
      coldSkey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/cold.skey"),
          SecretKey.class);
    } catch (IOException e) {
      log.error("Error reading and parsing cold.skey file");
      throw new MissingResourceException(e.getMessage(), this.getClass().getName(), "cold.skey");
    }
    log.info("Sender2 address: {}", sender2Addr);
    Tx retirePool = new Tx()
        .retirePool(poolId, 1)
        .from(sender2Addr);
    Result<String> complete = quickTxBuilder.compose(retirePool)
        .withSigner(SignerProviders.signerFrom(sender2))
        .withSigner(SignerProviders.signerFrom(coldSkey))
        .complete();

    String txHash = complete.getValue();
    checkIfUtxoAvailable(complete.getValue(), sender2Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    log.info(BLOCK_HASH_MASSAGE + hash);

    return new TransactionBlockDetails(txHash, hash, value1.getHeight());
  }

  private String getPoolId() {
    ObjectMapper objectMapper = new ObjectMapper();
    String poolRegistrationCborHex = null;
    try {
      poolRegistrationCborHex = objectMapper.readTree(
              this.getClass().getResourceAsStream("/pool1/pool-registration.cert")).get("cborHex")
          .asText();
    } catch (IOException e) {
      log.error("Error reading and parsing pool-registration.cert file");
      throw new MissingResourceException(e.getMessage(), this.getClass().getName(),
          "pool-registration.cert");
    }
    PoolRegistration poolRegistration = PoolRegistration.deserialize(poolRegistrationCborHex);

    return poolRegistration.getBech32PoolId();
  }
}
