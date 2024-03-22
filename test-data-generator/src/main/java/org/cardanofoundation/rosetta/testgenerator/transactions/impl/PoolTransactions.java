package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.backendService;
import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.checkIfUtxoAvailable;
import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

public class PoolTransactions implements TransactionRunner {

  public static Account sender1;
  public static Account sender2;

  public static String sender1Addr = "addr_test1qz5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxvw2pfap6uqmkj3n6zmlrsgz397md2gt7yqs5p255uygaesx608y5";
  public static String sender2Addr = "addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah";

  private GeneratedTestDataDTO generatedTestData;

  @Override
  public void init() {

    sender1 = new Account(Networks.testnet(), TestConstants.sender1Mnemonic);
    sender1Addr = sender1.baseAddress();

    sender2 = new Account(Networks.testnet(), TestConstants.sender2Mnemonic);
    sender2Addr = sender2.baseAddress();
  }

  @Override
  public GeneratedTestDataDTO runTransactions(GeneratedTestDataDTO generatedTestData) {
    this.generatedTestData = generatedTestData;

    registerPool();

    delegateStakeToPool();

    retirePool();
    return this.generatedTestData;
  }

  public void registerPool() {
    System.out.println("Registering pool");
    ObjectMapper objectMapper = new ObjectMapper();

    SecretKey coldSkey;
    SecretKey stakeSkey;
    VerificationKey stakeVKey;
    PoolRegistration poolRegistration;

    try {
      coldSkey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/cold.skey"), SecretKey.class);
      stakeSkey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/stake.skey"), SecretKey.class);
      stakeVKey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/stake.vkey"), VerificationKey.class);
      String poolRegistrationCborHex = objectMapper.readTree(this.getClass().getResourceAsStream("/pool1/pool-registration.cert")).get("cborHex").asText();
      poolRegistration = PoolRegistration.deserialize(poolRegistrationCborHex);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Address stakeAddr = AddressProvider.getRewardAddress(
        Credential.fromKey(Blake2bUtil.blake2bHash224(stakeVKey.getBytes())), Networks.testnet());
    System.out.println("Stake Addr: " + stakeAddr.toBech32());

    poolRegistration.setPoolMetadataUrl("https://my-pool.com");

    String poolId = Bech32.encode(poolRegistration.getOperator(), "pool");
    System.out.println("Pool ID: " + poolId);

    //pool registrartion
    Tx registerPoolTx = new Tx()
        .registerStakeAddress(stakeAddr)
        .registerPool(poolRegistration)
        .from(sender2Addr);

    Result<String> result = quickTxBuilder.compose(registerPoolTx)
        .withSigner(SignerProviders.signerFrom(coldSkey))
        .withSigner(SignerProviders.signerFrom(stakeSkey))
        .withSigner(SignerProviders.signerFrom(sender2))
        .completeAndWait(System.out::println);

    String txHash = result.getValue();
    checkIfUtxoAvailable(result.getValue(), sender2Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    System.out.println("Block hash: " + hash);
    if(generatedTestData != null) {
      generatedTestData.setPoolRegistrationTxHash(txHash);
      generatedTestData.setPoolRegistrationBlockHash(value1.getHash());
      generatedTestData.setPoolRegistrationBlockNumber(value1.getHeight());
    }
  }

  public void delegateStakeToPool() {
    System.out.println("Delegating stake to pool");
    // registering Stake keys first
    Tx tx = new Tx()
        .registerStakeAddress(sender2Addr)
        .payToAddress(TestConstants.receiver3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> result = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    if(result.isSuccessful())
      BaseFunctions.checkIfUtxoAvailable(result.getValue(), sender2Addr);

    String poolId = getPoolId();

    Tx delegateStake = new Tx()
        .delegateTo(sender2Addr, poolId)
        .payToAddress(TestConstants.receiver3, Amount.ada(2))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(delegateStake)
        .withSigner(SignerProviders.signerFrom(sender2))
        .withSigner(SignerProviders.stakeKeySignerFrom(sender2))
        .complete();

    String txHash = complete.getValue();
    checkIfUtxoAvailable(complete.getValue(), sender2Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    System.out.println("Block hash: " + hash);
    if(generatedTestData != null) {
      generatedTestData.setDelegationTxHash(txHash);
      generatedTestData.setDelegationBlockHash(value1.getHash());
      generatedTestData.setDelegationBlockNumber(value1.getHeight());
    }


  }


  public void retirePool() {
    System.out.println("Retiring pool");
    String poolId = getPoolId();
    ObjectMapper objectMapper = new ObjectMapper();
    SecretKey coldSkey = null;
    try {
      coldSkey = objectMapper.readValue(this.getClass().getResourceAsStream("/pool1/cold.skey"), SecretKey.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
    System.out.println("Block hash: " + hash);
    if(generatedTestData != null) {
      generatedTestData.setPoolRetireTxHash(txHash);
      generatedTestData.setPoolRetireBlockHash(value1.getHash());
      generatedTestData.setPoolRetireBlockNumber(value1.getHeight());
    }
  }

  private String getPoolId() {
    ObjectMapper objectMapper = new ObjectMapper();
    String poolRegistrationCborHex = null;
    try {
      poolRegistrationCborHex = objectMapper.readTree(this.getClass().getResourceAsStream("/pool1/pool-registration.cert")).get("cborHex").asText();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    PoolRegistration poolRegistration = PoolRegistration.deserialize(poolRegistrationCborHex);

    String poolId = poolRegistration.getBech32PoolId();
    return poolId;
  }
}
