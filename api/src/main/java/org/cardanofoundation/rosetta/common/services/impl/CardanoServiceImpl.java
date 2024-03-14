package org.cardanofoundation.rosetta.common.services.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.BootstrapWitness;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import com.bloxbean.cardano.client.transaction.spec.VkeyWitness;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.model.dto.ProcessOperationsDto;
import org.cardanofoundation.rosetta.api.block.model.dto.ProcessOperationsReturnDto;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.common.model.cardano.pool.DepositParameters;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.CardanoService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtil;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.OperationParseUtil;
import org.cardanofoundation.rosetta.common.util.ValidateParseUtil;
import org.openapitools.client.model.Operation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.math.BigInteger.valueOf;


@Slf4j
@Service
@RequiredArgsConstructor
public class CardanoServiceImpl implements CardanoService {

    private final LedgerDataProviderService ledgerDataProviderService;

    @Override
    public Double calculateRelativeTtl(Double relativeTtl) {
        return Objects.requireNonNullElse(relativeTtl, Constants.DEFAULT_RELATIVE_TTL);
    }

    @Override
    public String getHashOfSignedTransaction(String signedTransaction) {
        try {
            log.info("[getHashOfSignedTransaction] About to hash signed transaction {}", signedTransaction);
            byte[] signedTransactionBytes = HexUtil.decodeHexString(signedTransaction);
            log.info("[getHashOfSignedTransaction] About to parse transaction from signed transaction bytes");
            Transaction parsed = Transaction.deserialize(signedTransactionBytes);
            log.info("[getHashOfSignedTransaction] Returning transaction hash");
            TransactionBody body = parsed.getBody();
            byte[] hashBuffer;
            if (body == null || CborSerializationUtil.serialize(body.serialize()) == null) {
                hashBuffer = null;
            } else {
                hashBuffer = Blake2bUtil.blake2bHash256(com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(body.serialize()));
            }
            return CardanoAddressUtil.hexFormatter(hashBuffer);
        } catch (Exception error) {
            log.error(error.getMessage() + "[getHashOfSignedTransaction] There was an error parsing signed transaction");
            throw ExceptionFactory.parseSignedTransactionError();
        }
    }

    @Override
    public Array decodeExtraData(String encoded) {
        try {
            DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(HexUtil.decodeHexString(encoded));
            return (Array) dataItem;
        } catch (Exception e) {
            throw ExceptionFactory.cantBuildSignedTransaction();
        }
    }

    @Override
    public Long calculateTtl(Long ttlOffset) {
        BlockDto latestBlock = ledgerDataProviderService.findLatestBlock();
        return latestBlock.getSlotNo() + ttlOffset;
    }

    @Override
    public Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborException {
        return previousTxSize + com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(new UnsignedInteger(updatedTtl)).length - com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(new UnsignedInteger(previousTtl)).length;
    }

    @Override
    public Long calculateTxMinimumFee(Long transactionSize, ProtocolParams protocolParameters) {
        return protocolParameters.getMinFeeA() * transactionSize + protocolParameters.getMinFeeB();
    }

    @Override
    public Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType, String address) {
        if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.SHELLEY)) {
            return new Signatures(Constants.SHELLEY_DUMMY_SIGNATURE, Constants.SHELLEY_DUMMY_PUBKEY, null, address);
        }
        if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.BYRON)) {
            return new Signatures(Constants.BYRON_DUMMY_SIGNATURE, Constants.BYRON_DUMMY_PUBKEY, Constants.CHAIN_CODE_DUMMY, address);
        }
        if (addressType.getValue().equals(AddressType.POOL_KEY_HASH.getValue())) {
            return new Signatures(Constants.COLD_DUMMY_SIGNATURE, Constants.COLD_DUMMY_PUBKEY, null, address);
        }
        return null;
    }

    @Override
    public Double calculateTxSize(NetworkIdentifierType networkIdentifierType, List<Operation> operations, int ttl, DepositParameters depositParameters) throws IOException, CborException, AddressExcepion, CborSerializationException {
        UnsignedTransaction unsignedTransaction = createUnsignedTransaction(networkIdentifierType, operations, ttl, !ObjectUtils.isEmpty(depositParameters) ? depositParameters : new DepositParameters(Constants.DEFAULT_KEY_DEPOSIT.toString(), Constants.DEFAULT_POOL_DEPOSIT.toString()));
        List<Signatures> signaturesList = (unsignedTransaction.getAddresses()).stream().map(address -> {
            EraAddressType eraAddressType = CardanoAddressUtil.getEraAddressType(address);
            if (eraAddressType != null) {
                return signatureProcessor(eraAddressType, null, address);
            }
            // since pool key hash are passed as address, ed25519 hashes must be included
            if (CardanoAddressUtil.isEd25519KeyHash(address)) {
                return signatureProcessor(null, AddressType.POOL_KEY_HASH, address);
            }
            throw ExceptionFactory.invalidAddressError(address);
        }).toList();

        String transaction = buildTransaction(unsignedTransaction.getBytes(), signaturesList, unsignedTransaction.getMetadata());
        return ((double) transaction.length() / 2);

    }

    @Override
    public String buildTransaction(String unsignedTransaction, List<Signatures> signaturesList, String transactionMetadata) {
        log.info("[buildTransaction] About to signed a transaction with {} signatures", signaturesList.size());
        TransactionWitnessSet witnesses = getWitnessesForTransaction(signaturesList);

        log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
        DataItem[] dataItems;
        try {
            dataItems = com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.deserialize(HexUtil.decodeHexString(unsignedTransaction));
        } catch (Exception e) {
            throw ExceptionFactory.cantCreateSignTransaction();
        }
        try {
            TransactionBody transactionBody = TransactionBody.deserialize((co.nstant.in.cbor.model.Map) dataItems[0]);
            log.info("[buildTransaction] Creating transaction using transaction body and extracted witnesses");
            AuxiliaryData auxiliaryData = null;
            if (!ObjectUtils.isEmpty(transactionMetadata)) {
                log.info("[buildTransaction] Adding transaction metadata");
                Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(HexUtil.decodeHexString(transactionMetadata));
                auxiliaryData = AuxiliaryData.deserialize((co.nstant.in.cbor.model.Map) array.getDataItems().getFirst());
            }
            Transaction transaction = Transaction.builder().auxiliaryData(auxiliaryData).witnessSet(witnesses).build();
            transaction.setBody(transactionBody);
            Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(transaction.serialize());
            if (transactionBody.getTtl() == 0) {
                co.nstant.in.cbor.model.Map dataItem1 = (co.nstant.in.cbor.model.Map) array.getDataItems().getFirst();
                dataItem1.put(new UnsignedInteger(3), new UnsignedInteger(0));
                array.getDataItems().set(0, dataItem1);
            }
            if (!ObjectUtils.isEmpty(transactionMetadata)) {
                Array metadataArray = new Array();
                metadataArray.add(array.getDataItems().get(3));
                metadataArray.add(new Array());
                array.getDataItems().set(3, metadataArray);
            }
            return CardanoAddressUtil.bytesToHex(com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array));
        } catch (Exception error) {
            log.error(error.getMessage() + "[buildTransaction] There was an error building signed transaction");
            throw ExceptionFactory.cantBuildSignedTransaction();
        }
    }

    @Override
    public TransactionWitnessSet getWitnessesForTransaction(List<Signatures> signaturesList) {
        try {
            TransactionWitnessSet witnesses = new TransactionWitnessSet();
            ArrayList<VkeyWitness> vkeyWitnesses = new ArrayList<>();
            ArrayList<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();
            log.info("[getWitnessesForTransaction] Extracting witnesses from signatures");
            signaturesList.forEach(signature -> {
                VerificationKey vkey = new VerificationKey();
                vkey.setCborHex(ObjectUtils.isEmpty(signature) ? null : signature.getPublicKey());
                EraAddressType eraAddressType = CardanoAddressUtil.getEraAddressTypeOrNull(signature.getAddress());
                if (!ObjectUtils.isEmpty(signature)) {
                    if (!ObjectUtils.isEmpty(signature.getAddress()) && eraAddressType == EraAddressType.BYRON) {
                        // byron case
                        ValidateParseUtil.validateChainCode(signature.getChainCode());
                        ByronAddress byronAddress = new ByronAddress(signature.getAddress());
                        String str = HexUtil.encodeHexString(byronAddress.getBytes());
                        String str1 = str.substring(72);
                        StringBuilder str2 = new StringBuilder(str1);
                        StringBuilder str3 = str2.reverse();
                        String str4 = str3.substring(12);
                        StringBuilder result = new StringBuilder(str4);
                        BootstrapWitness bootstrap = new BootstrapWitness(HexUtil.decodeHexString(vkey.getCborHex()), HexUtil.decodeHexString(signature.getSignature()),
                                //revise
                                CardanoAddressUtil.hexStringToBuffer(signature.getChainCode()), HexUtil.decodeHexString(result.reverse().toString()));
                        bootstrapWitnesses.add(bootstrap);
                    } else {
                        vkeyWitnesses.add(new VkeyWitness(HexUtil.decodeHexString(vkey.getCborHex()), HexUtil.decodeHexString(signature.getSignature())));
                    }
                }
            });
            log.info("[getWitnessesForTransaction] {} witnesses were extracted to sign transaction", vkeyWitnesses.size());
            if (!vkeyWitnesses.isEmpty()) {
                witnesses.setVkeyWitnesses(vkeyWitnesses);
            }
            if (!bootstrapWitnesses.isEmpty()) {
                witnesses.setBootstrapWitnesses(bootstrapWitnesses);
            }
            return witnesses;
        } catch (Exception error) {
            log.error(error.getMessage() + "[getWitnessesForTransaction] There was an error building witnesses set for transaction");
            throw ExceptionFactory.cantBuildWitnessesSet();
        }
    }

    private UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifierType, List<Operation> operations, int ttl, DepositParameters depositParameters) throws IOException, CborSerializationException, AddressExcepion, CborException {
        log.info("[createUnsignedTransaction] About to create an unsigned transaction with {} operations", operations.size());
        ProcessOperationsReturnDto processOperationsReturnDto = processOperations(networkIdentifierType, operations, depositParameters);

        log.info("[createUnsignedTransaction] About to create transaction body");
        BigInteger fee = valueOf(processOperationsReturnDto.getFee());
        TransactionBody transactionBody = TransactionBody.builder().inputs(processOperationsReturnDto.getTransactionInputs()).outputs(processOperationsReturnDto.getTransactionOutputs()).fee(fee).ttl(ttl).build();

        if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
            log.info("[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
            AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
            Array array = new Array();
            array.add(auxiliaryData.serialize());
            array.add(new Array());
            transactionBody.setAuxiliaryDataHash(Blake2bUtil.blake2bHash256(com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array)));
        }

        if (!(processOperationsReturnDto.getCertificates()).isEmpty()) {
            transactionBody.setCerts(processOperationsReturnDto.getCertificates());
        }
        if (!ObjectUtils.isEmpty(processOperationsReturnDto.getWithdrawals())) {
            transactionBody.setWithdrawals(processOperationsReturnDto.getWithdrawals());
        }
        co.nstant.in.cbor.model.Map mapCbor = transactionBody.serialize();
        if (ttl == 0) {
            mapCbor.put(new UnsignedInteger(3), new UnsignedInteger(0));
        }
        String transactionBytes = CardanoAddressUtil.hexFormatter(com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(mapCbor));
        log.info("[createUnsignedTransaction] Hashing transaction body");
        String bodyHash = com.bloxbean.cardano.client.util.HexUtil.encodeHexString(Blake2bUtil.blake2bHash256(com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(mapCbor)));
        UnsignedTransaction toReturn = new UnsignedTransaction(CardanoAddressUtil.hexFormatter(HexUtil.decodeHexString(bodyHash)), transactionBytes, processOperationsReturnDto.getAddresses(), null);
        if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
            AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
            Array array = new Array();
            array.add(auxiliaryData.serialize());
            array.add(new Array());
            toReturn.setMetadata(CardanoAddressUtil.hex(com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array)));
        }
        log.info(toReturn + "[createUnsignedTransaction] Returning unsigned transaction, hash to sign and addresses that will sign hash");
        return toReturn;
    }

    private ProcessOperationsReturnDto processOperations(NetworkIdentifierType networkIdentifierType, List<Operation> operations, DepositParameters depositParameters) throws IOException {
        ProcessOperationsDto result = convertRosettaOperations(networkIdentifierType, operations);
        double refundsSum = result.getStakeKeyDeRegistrationsCount() * Long.parseLong(depositParameters.getKeyDeposit());
        double keyDepositsSum = result.getStakeKeyRegistrationsCount() * Long.parseLong(depositParameters.getKeyDeposit());
        double poolDepositsSum = result.getPoolRegistrationsCount() * Long.parseLong(depositParameters.getPoolDeposit());
        Map<String, Double> depositsSumMap = new HashMap<>();
        depositsSumMap.put("keyRefundsSum", refundsSum);
        depositsSumMap.put("keyDepositsSum", keyDepositsSum);
        depositsSumMap.put("poolDepositsSum", poolDepositsSum);
        long fee = calculateFee(result.getInputAmounts(), result.getOutputAmounts(), result.getWithdrawalAmounts(), depositsSumMap);
        log.info("[processOperations] Calculated fee:{}", fee);
        ProcessOperationsReturnDto processOperationsDto = new ProcessOperationsReturnDto();
        processOperationsDto.setTransactionInputs(result.getTransactionInputs());
        processOperationsDto.setTransactionOutputs(result.getTransactionOutputs());
        processOperationsDto.setCertificates(result.getCertificates());
        processOperationsDto.setWithdrawals(result.getWithdrawals());
        Set<String> addresses = new HashSet<>(result.getAddresses());
        processOperationsDto.setAddresses(addresses);
        processOperationsDto.setFee(fee);
        processOperationsDto.setVoteRegistrationMetadata(result.getVoteRegistrationMetadata());
        return processOperationsDto;
    }

    @Override
    public Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts, ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap) {
        double inputsSum = 0;
        for (String i : inputAmounts) {
            inputsSum += Long.parseLong(i);
        }
        inputsSum *= -1;
        double outputsSum = 0;
        for (String i : outputAmounts) {
            outputsSum += Long.parseLong(i);
        }
        double withdrawalsSum = 0;
        for (Long i : withdrawalAmounts) {
            withdrawalsSum += i;
        }
        long fee = (long) (inputsSum + withdrawalsSum + depositsSumMap.get("keyRefundsSum") - outputsSum - depositsSumMap.get("keyDepositsSum") - depositsSumMap.get("poolDepositsSum"));
        if (fee < 0) {
            throw ExceptionFactory.outputsAreBiggerThanInputsError();
        }
        return fee;
    }

    @Override
    public ProcessOperationsDto convertRosettaOperations(NetworkIdentifierType networkIdentifierType, List<Operation> operations) throws IOException {
        ProcessOperationsDto processor = new ProcessOperationsDto();

        for (Operation operation : operations) {
            String type = operation.getType();
            try {
                processor = OperationParseUtil.parseOperation(operation, networkIdentifierType, processor, type);
            } catch (CborSerializationException | CborDeserializationException | NoSuchAlgorithmException |
                     SignatureException | InvalidKeySpecException | InvalidKeyException e) {
                throw ExceptionFactory.unspecifiedError(e.getMessage());
            }
            if (ObjectUtils.isEmpty(processor)) {
                log.error("[processOperations] Operation with id {} has invalid type", operation.getOperationIdentifier());
                throw ExceptionFactory.invalidOperationTypeError();
            }
        }
        return processor;
    }

}
