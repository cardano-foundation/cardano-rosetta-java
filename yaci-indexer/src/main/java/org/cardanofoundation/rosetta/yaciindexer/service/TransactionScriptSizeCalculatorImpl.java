package org.cardanofoundation.rosetta.yaciindexer.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.script.NativeScript;
import com.bloxbean.cardano.yaci.core.model.PlutusScript;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.cardanofoundation.rosetta.yaciindexer.domain.model.TransactionBuildingConstants;

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;
import static org.cardanofoundation.rosetta.yaciindexer.domain.model.TransactionBuildingConstants.*;

@Service
@Slf4j
public class TransactionScriptSizeCalculatorImpl implements TransactionScriptSizeCalculator {

    @Override
    public int calculateScriptSize(Transaction tx, Map signedTransaction) {
        return addWitnessSetToSignedTransaction(tx, signedTransaction);
    }

    /**
     * Reconstructing the Witness_set from transaction and adding it to signedTransaction
     * CDDL Definition: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L269
     * @param tx
     * @param signedTransaction
     * @return
     */
    private static int addWitnessSetToSignedTransaction(Transaction tx, Map signedTransaction) {
        // adding witnesses to signedTransaction
        Map witnessSet = new Map();
        int scriptSize = 0;
        addvKeyWitnessToWitness(tx, witnessSet);
        addNativeScriptsToWitness(tx, witnessSet);
        addBootstrapToWitness(tx, witnessSet);

        scriptSize += addPlutusToWitness(witnessSet, tx.getWitnesses().getPlutusV1Scripts(),
                TransactionBuildingConstants.PLUTUSV1_WITNESS_INDEX);
        scriptSize += addPlutusToWitness(witnessSet, tx.getWitnesses().getPlutusV2Scripts(),
                TransactionBuildingConstants.PLUTUSV2_WITNESS_INDEX);
        scriptSize += addPlutusToWitness(witnessSet, tx.getWitnesses().getPlutusV3Scripts(),
                TransactionBuildingConstants.PLUTUSV3_WITNESS_INDEX);

        addDatumToWitness(tx, witnessSet);
        addRedeemerToWitness(tx, witnessSet);

        if (!witnessSet.getKeys().isEmpty()) {
            signedTransaction.put(new UnsignedInteger(TransactionBuildingConstants.WITNESS_SET_INDEX), witnessSet);
        }

        return scriptSize;
    }

    /**
     * Adding the datum to Witness set. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L280
     * @param tx transaction to extract the datum
     * @param witnessSet witnessSet to add the datum
     */
    private static void addDatumToWitness(Transaction tx, Map witnessSet) {
        if (!tx.getWitnesses().getDatums().isEmpty()) {
            Array array = new Array();
            // could speed it up by passing an empty array, since we are only interested in the size not the content
            tx.getWitnesses().getDatums().forEach(datum -> array.add(new ByteString(decodeHexString(datum.getCbor()))));
            witnessSet.put(new UnsignedInteger(TransactionBuildingConstants.PLUTUS_DATUM_WITNESS_INDEX), array);
        }
    }

    /**
     * Adding Plutus Script data to witnessSet. Can be used for V1, V2 and V3. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L278
     * @param witnessSet witnessset to add the data
     * @param scripts List of PlutusScripts
     * @param witnessSetIndex Index where to add the datum based on cddl spec
     * @return
     */
    private static int addPlutusToWitness(Map witnessSet, List<PlutusScript> scripts, int witnessSetIndex) {
        int scriptSize = 0;
        Array array = new Array();
        if (!scripts.isEmpty()) {
            scriptSize = scripts.stream().mapToInt(script -> {
                array.add(new ByteString(decodeHexString(script.getContent())));
                return script.getContent().length() / 2;
            }).sum();

            witnessSet.put(new UnsignedInteger(witnessSetIndex), array);
        }
        return scriptSize;
    }

    /**
     * Adding Redemer data to witnessset. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L302
     * @param tx transaction to extract the redeemer data
     * @param witnessSet witnesset to add the data based on cddl spec
     */
    private static void addRedeemerToWitness(Transaction tx, Map witnessSet) {
        if (!tx.getWitnesses().getRedeemers().isEmpty()) {
            Array array = new Array();
            // could speed it up by passing an empty array, since we are only interested in the size not the content
            tx.getWitnesses().getRedeemers().forEach(redeemer -> array.add(new ByteString(decodeHexString(redeemer.getCbor()))));
            witnessSet.put(new UnsignedInteger(REDEEMER_WITNESS_INDEX), array);
        }
    }

    /**
     * Extracting bootstrap data and adding it to witnessSet. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L348
     * @param tx Transcation to get the bootstrap from
     * @param witnessSet witnessSet to add the data
     */
    private static void addBootstrapToWitness(Transaction tx, Map witnessSet) {
        if (!tx.getWitnesses().getBootstrapWitnesses().isEmpty()) {
            Array array = new Array();
            tx.getWitnesses().getBootstrapWitnesses().forEach(bootstrapWitness -> {
                Array witnessArray = new Array();
                witnessArray.add(new ByteString(decodeHexString(bootstrapWitness.getPublicKey())));
                witnessArray.add(new ByteString(decodeHexString(bootstrapWitness.getSignature())));
                witnessArray.add(new ByteString(decodeHexString(bootstrapWitness.getChainCode())));
                witnessArray.add(new ByteString(decodeHexString(bootstrapWitness.getAttributes())));
                array.add(witnessArray);
            });
            witnessSet.put(new UnsignedInteger(BOOTSTRAP_WITNESS_INDEX), array);
        }
    }

    /**
     * Extracting VKey and adding it to witnessSet. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L346
     * @param tx transaction to extract the data
     * @param witnessSet witnessSet to add the data
     */
    private static void addvKeyWitnessToWitness(Transaction tx, Map witnessSet) {
        if (!tx.getWitnesses().getVkeyWitnesses().isEmpty()) {
            Array vKeyWitnessArray = new Array();
            tx.getWitnesses().getVkeyWitnesses().forEach(vkeyWitness -> {
                Array vitnessArray = new Array();
                vitnessArray.add(new ByteString(decodeHexString(vkeyWitness.getKey())));
                vitnessArray.add(new ByteString(decodeHexString(vkeyWitness.getSignature()))); // could speed it up by passing an empty array, since we are only interested in the size not the content
                vKeyWitnessArray.add(vitnessArray);
            });

            witnessSet.put(new UnsignedInteger(VKEY_WITNESS_INDEX), vKeyWitnessArray);
        }
    }

    /**
     * Extracting NativeScript data and adding it to witnessSet. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L355
     * @param tx transaction to extract the data
     * @param witnessSet witnessSet to add the data
     */
    private static void addNativeScriptsToWitness(Transaction tx, Map witnessSet) {
        if (!tx.getWitnesses().getNativeScripts().isEmpty()) {
            Array nativeScripts = new Array();

            tx.getWitnesses().getNativeScripts().forEach(script -> {
                NativeScript nativeScript;
                try {
                    nativeScript = NativeScript.deserializeJson(script.getContent());
                    nativeScripts.add(new ByteString(nativeScript.getScriptHash()));
                } catch (CborDeserializationException | JsonProcessingException |
                         CborSerializationException e) {
                    log.error("Can't parse Native script for Transaction: " + tx.getTxHash());
                }
            });

            witnessSet.put(new UnsignedInteger(NATIVESCRIPT_WITNESS_INDEX), nativeScripts);
        }
    }

}
