package org.cardanofoundation.rosetta.testgenerator;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.getBackendService;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.util.JsonUtil;
import java.math.BigInteger;
import java.util.HashMap;
import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;

public class SimpleTransactions {

  public static QuickTxBuilder quickTxBuilder;
  public static BackendService backendService;
  public static Account sender1;
  public static String sender1Mnemonic = "clog book honey force cricket stamp until seed minimum margin denial kind volume undo simple federal then jealous solid legal crucial crazy acoustic thank";
  public static Account sender2;
  public static String sender2Mnemonic = "drive useless envelope shine range ability time copper alarm museum near flee wrist live type device meadow allow churn purity wisdom praise drop code";

  public static String sender1Addr = "addr_test1qz5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxvw2pfap6uqmkj3n6zmlrsgz397md2gt7yqs5p255uygaesx608y5";
  public static String sender2Addr = "addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah";

  public static String receiver1 = "addr_test1qz3s0c370u8zzqn302nppuxl840gm6qdmjwqnxmqxme657ze964mar2m3r5jjv4qrsf62yduqns0tsw0hvzwar07qasqeamp0c";
  public static String receiver2 = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";
  public static String receiver3 = "addr_test1qqqvjp4ffcdqg3fmx0k8rwamnn06wp8e575zcv8d0m3tjn2mmexsnkxp7az774522ce4h3qs4tjp9rxjjm46qf339d9sk33rqn";
  public static HashMap<String, String> map;
  private GeneratedTestDataDTO generatedTestData;

  private static void init() {
    backendService = getBackendService();
    quickTxBuilder = new QuickTxBuilder(backendService);

    sender1 = new Account(Networks.testnet(), sender1Mnemonic);
    sender1Addr = sender1.baseAddress();

    sender2 = new Account(Networks.testnet(), sender2Mnemonic);
    sender2Addr = sender2.baseAddress();

  }

  public GeneratedTestDataDTO runFunctions(GeneratedTestDataDTO generatedTestData) {
    init();
    this.generatedTestData = generatedTestData;

    simpleTransaction();
    stakeKeyRegistration();

    stakeKeyDeregistration();

    return this.generatedTestData;
  }

  private void simpleTransaction() {
    Tx tx = new Tx()
        .payToAddress(sender1Addr, Amount.ada(BaseFunctions.lovelaceToAda(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT)))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();

    String txHash = complete.getValue();
    System.out.println("Transaction hash: " + txHash);
    BaseFunctions.checkIfUtxoAvailable(txHash, sender1Addr);
    Block value1 = BaseFunctions.getBlock(txHash);
    String hash = value1.getHash();
    System.out.println("Block hash: " + hash);
    generatedTestData.setTxHashTopUp(txHash);
    generatedTestData.setBlockHashTopUp(value1.getHash());
    generatedTestData.setBlockHeightTopUp(value1.getHeight());
  }

  private void stakeKeyRegistration() {
    Tx tx = new Tx()
        .registerStakeAddress(sender2Addr)
        .payToAddress(receiver3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    System.out.println("Stake key registration tx: " + complete.getValue());

    String txHash = complete.getValue();
    BaseFunctions.checkIfUtxoAvailable(txHash, sender2Addr);
    Block block = BaseFunctions.getBlock(txHash);

    generatedTestData.setStakeKeyRegistrationTxHash(txHash);
    generatedTestData.setStakeKeyRegistrationBlockHash(block.getHash());
    generatedTestData.setStakeKeyRegistrationBlockHeight(block.getHeight());
  }

  private void stakeKeyDeregistration() {
    Tx tx = new Tx()
        .deregisterStakeAddress(sender2Addr)
        .payToAddress(receiver3, Amount.ada(1.0))
        .from(sender2Addr);

    Result<String> complete = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.stakeKeySignerFrom(sender2))
        .withSigner(SignerProviders.signerFrom(sender2))
        .complete();
    String txHash = complete.getValue();
    BaseFunctions.checkIfUtxoAvailable(txHash, sender2Addr);
    Block block = BaseFunctions.getBlock(txHash);

    generatedTestData.setStakeKeyDeregistrationTxHash(txHash);
    generatedTestData.setStakeKeyDeregistrationBlockHash(block.getHash());
    generatedTestData.setStakeKeyDeregistrationBlockHeight(block.getHeight());
  }


}
