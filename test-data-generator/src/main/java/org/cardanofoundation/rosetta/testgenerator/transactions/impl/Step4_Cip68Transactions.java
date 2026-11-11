package org.cardanofoundation.rosetta.testgenerator.transactions.impl;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.cip.cip68.CIP68FT;
import com.bloxbean.cardano.client.cip.cip68.CIP68RFT;
import com.bloxbean.cardano.client.cip.cip68.CIP68ReferenceToken;
import com.bloxbean.cardano.client.cip.cip68.common.CIP68TokenTemplate;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;

import org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

import static org.cardanofoundation.rosetta.testgenerator.common.BaseFunctions.quickTxBuilder;

/**
 * Mints CIP-68 tokens so the fixture has cip68_metadata rows (yaci-store assets-ext), paid by the
 * CIP-68 holder account (topped up by the regenerate-testdata-db skill):
 * a fungible token (333, prefix 0014df10) with a logo, and a rich fungible token (444, prefix
 * 001bc280) with an image. Each mint transaction also mints the reference NFT (100, prefix
 * 000643b0) and sends it with the metadata as an inline datum; assets-ext indexes the datum
 * because the same transaction mints the 333 / 444 user token.
 */
@Slf4j
public class Step4_Cip68Transactions implements TransactionRunner {

  private Account sender1;
  private Account holder;
  private String holderAddr;
  private String referenceNftAddr;
  private ScriptPubkey policyScript;
  private String policyId;

  @Override
  public int getExecutionOrder() {
    return 4;  // Execute after GovernanceTransactions
  }

  @Override
  public void init() {
    sender1 = new Account(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC);
    holder = Account.createFromMnemonic(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC,
        TestConstants.CIP68_HOLDER_ACCOUNT, 0);
    holderAddr = holder.baseAddress();
    // Reference NFTs (with the datum) go to a separate address of the holder account: QuickTx merges the
    // change into an output to the change address, and a UTXO with a datum is not used for coin selection,
    // so the holder's funds must not end up next to a datum.
    referenceNftAddr = Account.createFromMnemonic(Networks.testnet(), TestConstants.SENDER_1_MNEMONIC,
        TestConstants.CIP68_HOLDER_ACCOUNT, 1).baseAddress();
    policyScript = cip68PolicyScript(sender1);
    try {
      policyId = policyScript.getPolicyId();
    } catch (CborSerializationException e) {
      throw new IllegalStateException("Could not compute the CIP-68 policy ID", e);
    }
    log.info("CIP-68 policy ID: {}, holder: {}, reference NFTs: {}", policyId, holderAddr, referenceNftAddr);
  }

  /** Single-signature native script on SENDER_1's payment key: the policy ID is stable across regenerations. */
  public static ScriptPubkey cip68PolicyScript(Account sender1) {
    try {
      return ScriptPubkey.create(VerificationKey.create(sender1.publicKeyBytes()));
    } catch (CborSerializationException e) {
      throw new IllegalStateException("Could not create the CIP-68 policy script", e);
    }
  }

  @Override
  public Map<String, TransactionBlockDetails> runTransactions() {
    Map<String, TransactionBlockDetails> generatedDataMap = new HashMap<>();

    CIP68FT ft = CIP68FT.create()
        .name(TestConstants.CIP68_FT_NAME)
        .description(TestConstants.CIP68_FT_DESCRIPTION)
        .ticker(TestConstants.CIP68_FT_TICKER)
        .url(TestConstants.CIP68_FT_URL)
        .decimals(TestConstants.CIP68_FT_DECIMALS)
        .logo(TestConstants.CIP68_FT_LOGO);
    generatedDataMap.put(TestTransactionNames.CIP68_FT_MINT.getName(),
        mint(ft, TestConstants.CIP68_FT_QUANTITY));

    CIP68RFT rft = CIP68RFT.create()
        .name(TestConstants.CIP68_RFT_NAME)
        .description(TestConstants.CIP68_RFT_DESCRIPTION)
        .image(TestConstants.CIP68_RFT_IMAGE)
        .decimals(TestConstants.CIP68_RFT_DECIMALS);
    generatedDataMap.put(TestTransactionNames.CIP68_RFT_MINT.getName(),
        mint(rft, TestConstants.CIP68_RFT_QUANTITY));

    return generatedDataMap;
  }

  private TransactionBlockDetails mint(CIP68TokenTemplate<?> token, long quantity) {
    CIP68ReferenceToken referenceToken = token.getReferenceToken();
    Asset referenceNft = referenceToken.getAsset();
    Asset userToken = token.getAsset(BigInteger.valueOf(quantity));
    log.info("Minting CIP-68 {} ({}) with reference NFT {} to {}",
        userToken.getName(), quantity, referenceNft.getName(), holderAddr);

    Tx tx = new Tx()
        .mintAssets(policyScript, List.of(referenceNft, userToken))
        .payToContract(referenceNftAddr, Amount.asset(policyId, referenceNft.getName(), BigInteger.ONE),
            referenceToken.getDatumAsPlutusData())
        .payToAddress(holderAddr, Amount.asset(policyId, userToken.getName(), BigInteger.valueOf(quantity)))
        .from(holderAddr);

    // The holder pays (it is topped up by the regenerate-testdata-db skill), so SENDER_1's UTXOs,
    // which other tests rely on, are not touched; SENDER_1 only signs as the policy key.
    Result<String> result = quickTxBuilder.compose(tx)
        .withSigner(SignerProviders.signerFrom(holder))
        .withSigner(SignerProviders.signerFrom(sender1))
        .completeAndWait(Duration.ofMinutes(1));
    if (!result.isSuccessful()) {
      throw new IllegalStateException("CIP-68 mint of " + userToken.getName() + " failed: " + result.getResponse());
    }

    String txHash = result.getValue();
    BaseFunctions.checkIfUtxoAvailable(txHash, holderAddr);
    Block block = BaseFunctions.getBlock(txHash);
    log.info("CIP-68 mint {} in block {}", txHash, block.getHash());
    return new TransactionBlockDetails(txHash, block.getHash(), block.getHeight());
  }
}
