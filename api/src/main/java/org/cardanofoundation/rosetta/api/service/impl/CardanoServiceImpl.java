package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.KeyGenUtil;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystSigIndexes;
import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.common.enumeration.StakeAddressPrefix;
import org.cardanofoundation.rosetta.api.model.Metadata;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessOperationsDto;
import org.cardanofoundation.rosetta.api.model.Signatures;
import org.cardanofoundation.rosetta.api.model.UnsignedTransaction;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistationParametersReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistrationCertReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessOperationsReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessPoolRegistrationReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessWithdrawalReturnDto;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.common.enumeration.NonStakeAddressPrefix;
import org.cardanofoundation.rosetta.common.ledgersync.RelayType;

import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.AccountIdentifierMetadata;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.CoinAction;
import org.cardanofoundation.rosetta.api.model.CoinChange;
import org.cardanofoundation.rosetta.api.model.CoinIdentifier;
import org.cardanofoundation.rosetta.api.model.CurveType;
import org.cardanofoundation.rosetta.api.model.DepositParameters;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationIdentifier;
import org.cardanofoundation.rosetta.api.model.OperationMetadata;
import org.cardanofoundation.rosetta.api.model.PoolMargin;
import org.cardanofoundation.rosetta.api.model.PoolMetadata;
import org.cardanofoundation.rosetta.api.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Relay;
import org.cardanofoundation.rosetta.api.model.SignatureType;
import org.cardanofoundation.rosetta.api.model.SigningPayload;
import org.cardanofoundation.rosetta.api.model.SubAccountIdentifier;
import org.cardanofoundation.rosetta.api.model.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionParsed;
import org.cardanofoundation.rosetta.api.model.VoteRegistrationMetadata;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.cardanofoundation.rosetta.api.util.CardanoAddressUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
public class CardanoServiceImpl implements CardanoService {

    final LedgerDataProviderService ledgerDataProviderService;

    public CardanoServiceImpl(LedgerDataProviderService ledgerDataProviderService) {
        this.ledgerDataProviderService = ledgerDataProviderService;
    }

    @Override
    public String generateAddress(NetworkIdentifierType networkIdentifierType, String publicKeyString,
                                  String stakingCredentialString, AddressType type) {
        log.info(
                "[generateAddress] About to generate address from public key {} and network identifier {}",
                publicKeyString, networkIdentifierType);
        HdPublicKey paymentCredential = new HdPublicKey();
        paymentCredential.setKeyData(HexUtil.decodeHexString(publicKeyString));
        if (!ObjectUtils.isEmpty(type) && type.getValue().equals(AddressType.REWARD.getValue())) {
            return generateRewardAddress(networkIdentifierType, paymentCredential);
        }

        if (!ObjectUtils.isEmpty(type) && type.getValue().equals(AddressType.BASE.getValue())) {
            if (stakingCredentialString == null) {
                log.error("[constructionDerive] No staking key was provided for base address creation");
                throw ExceptionFactory.missingStakingKeyError();
            }
            HdPublicKey stakingCredential = new HdPublicKey();
            stakingCredential.setKeyData(HexUtil.decodeHexString(stakingCredentialString));
            return generateBaseAddress(networkIdentifierType, paymentCredential, stakingCredential);
        }

        return generateEnterpriseAddress(paymentCredential, networkIdentifierType);
    }

    @Override
    public String generateRewardAddress(NetworkIdentifierType networkIdentifierType,
                                        HdPublicKey paymentCredential) {
        log.info(
                "[generateRewardAddress] Deriving cardano reward address from valid public staking key");
        Address rewardAddress = AddressProvider.getRewardAddress(paymentCredential,
                new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
        log.info("[generateRewardAddress] reward address is {}", rewardAddress.toBech32());
        return rewardAddress.toBech32();
    }

    @Override
    public String generateBaseAddress(NetworkIdentifierType networkIdentifierType,
                                      HdPublicKey paymentCredential, HdPublicKey stakingCredential) {
        log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
        Address baseAddress = AddressProvider.getBaseAddress(paymentCredential, stakingCredential,
                new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
        log.info("generateAddress] base address is {}", baseAddress.toBech32());
        return baseAddress.toBech32();
    }

    @Override
    public String generateEnterpriseAddress(HdPublicKey paymentCredential,
                                            NetworkIdentifierType networkIdentifierType) {
        log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
        Address entAddress = AddressProvider.getEntAddress(paymentCredential,
                new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
        log.info("generateAddress] base address is {}", entAddress.toBech32());
        return entAddress.toBech32();
    }

    @Override
    public Double calculateRelativeTtl(Double relativeTtl) {
        return Objects.requireNonNullElse(relativeTtl, Constants.DEFAULT_RELATIVE_TTL);
    }

    @Override
    public Double calculateTxSize(NetworkIdentifierType networkIdentifierType,
                                  ArrayList<Operation> operations, Integer ttl, DepositParameters depositParameters)
            throws IOException, AddressExcepion, CborSerializationException, CborException {
        UnsignedTransaction unsignedTransaction = createUnsignedTransaction(
                networkIdentifierType,
                operations,
                ttl,
                !ObjectUtils.isEmpty(depositParameters) ? depositParameters
                        : new DepositParameters(Constants.DEFAULT_POOL_DEPOSIT.toString(), Constants.DEFAULT_KEY_DEPOSIT.toString())
        );
        // eslint-disable-next-line consistent-return
        List<Signatures> signaturesList = (unsignedTransaction.getAddresses()).stream()
                .map(address -> {
                    EraAddressType eraAddressType = getEraAddressType(address);
                    if (eraAddressType != null) {
                        return signatureProcessor(eraAddressType, null, address);
                    }
                    // since pool key hash are passed as address, ed25519 hashes must be included
                    if (isEd25519KeyHash(address)) {
                        return signatureProcessor(null, AddressType.POOL_KEY_HASH, address);
                    }
                    throw ExceptionFactory.invalidAddressError(address);
                }).toList();

        String transaction = buildTransaction(unsignedTransaction.getBytes(), signaturesList,
                unsignedTransaction.getMetadata());
        // eslint-disable-next-line no-magic-numbers
        return  ((double)transaction.length()
                / 2); // transaction is returned as a hex string, and we need size in bytes
    }

    @Override
    public String buildTransaction(String unsignedTransaction,
                                   List<Signatures> signaturesList, String transactionMetadata) {
        log.info("[buildTransaction] About to signed a transaction with {} signatures",
                signaturesList.size());
        TransactionWitnessSet witnesses = getWitnessesForTransaction(signaturesList);

        log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
        DataItem[] dataItems;
        try {
            dataItems = CborSerializationUtil.deserialize(
                    HexUtil.decodeHexString(unsignedTransaction));
        } catch (Exception e) {
            throw ExceptionFactory.cantCreateSignTransaction();
        }
        try {
            TransactionBody transactionBody = TransactionBody.deserialize(
                    (co.nstant.in.cbor.model.Map) dataItems[0]);
            log.info(
                    "[buildTransaction] Creating transaction using transaction body and extracted witnesses");
            AuxiliaryData auxiliaryData = null;
            if (!ObjectUtils.isEmpty(transactionMetadata)) {
                log.info("[buildTransaction] Adding transaction metadata");
                Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(HexUtil.decodeHexString(transactionMetadata));
                auxiliaryData = AuxiliaryData.deserialize(
                        (co.nstant.in.cbor.model.Map) array.getDataItems().get(0));
            }
            Transaction transaction = Transaction.builder().auxiliaryData(auxiliaryData)
                    .witnessSet(witnesses).build();
            transaction.setBody(transactionBody);
            Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
                    transaction.serialize());
            if (transactionBody.getTtl() == 0) {
                co.nstant.in.cbor.model.Map dataItem1 = (co.nstant.in.cbor.model.Map) array.getDataItems()
                        .get(0);
                dataItem1.put(new UnsignedInteger(3), new UnsignedInteger(0));
                array.getDataItems().set(0, dataItem1);
            }
            if (!ObjectUtils.isEmpty(transactionMetadata)) {
                Array metadataArray = new Array();
                metadataArray.add(array.getDataItems().get(3));
                metadataArray.add(new Array());
                array.getDataItems().set(3, metadataArray);
            }
            return bytesToHex(CborSerializationUtil.serialize(array));
        } catch (Exception error) {
            log.error(
                    error.getMessage() + "[buildTransaction] There was an error building signed transaction");
            throw ExceptionFactory.cantBuildSignedTransaction();
        }
    }

    //revise
    @Override
    public TransactionWitnessSet getWitnessesForTransaction(
            List<Signatures> signaturesList) {
        try {
            TransactionWitnessSet witnesses = new TransactionWitnessSet();
            ArrayList<VkeyWitness> vkeyWitnesses = new ArrayList<>();
            ArrayList<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();
            log.info("[getWitnessesForTransaction] Extracting witnesses from signatures");
            signaturesList.forEach(signature -> {
                VerificationKey vkey = new VerificationKey();
                vkey.setCborHex(ObjectUtils.isEmpty(signature) ? null : signature.getPublicKey());
                EraAddressType eraAddressType = getEraAddressTypeOrNull(signature.getAddress());
                if (!ObjectUtils.isEmpty(signature)) {
                    if (!ObjectUtils.isEmpty(signature.getAddress()) && eraAddressType == EraAddressType.BYRON) {
                        // byron case
                        validateChainCode(signature.getChainCode());
                        ByronAddress byronAddress = new ByronAddress(signature.getAddress());
                        String str = HexUtil.encodeHexString(byronAddress.getBytes());
                        String str1 = str.substring(72);
                        StringBuilder str2 = new StringBuilder(str1);
                        StringBuilder str3 = str2.reverse();
                        String str4 = str3.substring(12);
                        StringBuilder result = new StringBuilder(str4);
                        BootstrapWitness bootstrap = new BootstrapWitness(
                                HexUtil.decodeHexString(vkey.getCborHex()),
                                HexUtil.decodeHexString(signature.getSignature()),
                                //revise
                                hexStringToBuffer(signature.getChainCode()),
                                HexUtil.decodeHexString(result.reverse().toString()));
                        bootstrapWitnesses.add(bootstrap);
                    } else {
                        vkeyWitnesses.add(
                                new VkeyWitness(HexUtil.decodeHexString(vkey.getCborHex()),
                                        HexUtil.decodeHexString(signature.getSignature())));
                    }
                }
            });
            log.info("[getWitnessesForTransaction] {} witnesses were extracted to sign transaction",
                    vkeyWitnesses.size());
            if (!vkeyWitnesses.isEmpty()) {
                witnesses.setVkeyWitnesses(vkeyWitnesses);
            }
            if (!bootstrapWitnesses.isEmpty()) {
                witnesses.setBootstrapWitnesses(bootstrapWitnesses);
            }
            return witnesses;
        } catch (Exception error) {
            log.error(error.getMessage()
                    + "[getWitnessesForTransaction] There was an error building witnesses set for transaction");
            throw ExceptionFactory.cantBuildWitnessesSet();
        }
    }

    public void validateChainCode(String chainCode){
        if (ObjectUtils.isEmpty(chainCode)) {
            log.error(
                    "[getWitnessesForTransaction] Missing chain code for byron address signature");
            throw ExceptionFactory.missingChainCodeError();
        }
    }

    @Override
    public EraAddressType getEraAddressTypeOrNull(String address) {
        try {
            return getEraAddressType(address);
        } catch (Exception error) {
            return null;
        }
    }

    @Override
    public boolean isEd25519KeyHash(String hash) {
        try {
            HexUtil.decodeHexString(KeyGenUtil.getKeyHash(HexUtil.decodeHexString(hash)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isEd25519Signature(String hash) {
        byte[] signatureByte = HexUtil.decodeHexString(hash);
        return signatureByte.length >= Constants.Ed25519_Key_Signature_BYTE_LENGTH;
    }

    @Override
    public Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType,
                                         String address) {
        if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.SHELLEY)) {
            return new Signatures(Constants.SHELLEY_DUMMY_SIGNATURE, Constants.SHELLEY_DUMMY_PUBKEY, null,
                    address);
        }
        if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.BYRON)) {
            return new Signatures(Constants.BYRON_DUMMY_SIGNATURE, Constants.BYRON_DUMMY_PUBKEY,
                    Constants.CHAIN_CODE_DUMMY, address);
        }
        if (addressType.getValue().equals(AddressType.POOL_KEY_HASH.getValue())) {
            return new Signatures(Constants.COLD_DUMMY_SIGNATURE, Constants.COLD_DUMMY_PUBKEY, null,
                    address);
        }
        return null;
    }

    @Override
    public UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifierType,
                                                         List<Operation> operations, Integer ttl, DepositParameters depositParameters)
            throws IOException, AddressExcepion, CborSerializationException, CborException {
        log.info(
                "[createUnsignedTransaction] About to create an unsigned transaction with {} operations",
                operations.size());
        ProcessOperationsReturnDto processOperationsReturnDto = processOperations(networkIdentifierType, operations,
                depositParameters);

        log.info("[createUnsignedTransaction] About to create transaction body");
        BigInteger fee = BigInteger.valueOf(processOperationsReturnDto.getFee());
        TransactionBody transactionBody = TransactionBody.builder().inputs(processOperationsReturnDto.getTransactionInputs())
                .outputs(processOperationsReturnDto.getTransactionOutputs()).fee(fee).ttl(ttl.longValue()).build();

        if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
            log.info(
                    "[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
            AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
            Array array = new Array();
            array.add(auxiliaryData.serialize());
            array.add(new Array());
            transactionBody.setAuxiliaryDataHash(Blake2bUtil.blake2bHash256(CborSerializationUtil.serialize(array)));
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
        String transactionBytes = hexFormatter(CborSerializationUtil.serialize(mapCbor));
        log.info("[createUnsignedTransaction] Hashing transaction body");
        String bodyHash = com.bloxbean.cardano.client.util.HexUtil.encodeHexString(
                Blake2bUtil.blake2bHash256(
                        com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(mapCbor)));
        UnsignedTransaction toReturn = new UnsignedTransaction(
                hexFormatter(HexUtil.decodeHexString(bodyHash)),
                transactionBytes, processOperationsReturnDto.getAddresses(), null);
        if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
            AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
            Array array = new Array();
            array.add(auxiliaryData.serialize());
            array.add(new Array());
            toReturn.setMetadata(hex(CborSerializationUtil.serialize(array)));
        }
        log.info(toReturn
                + "[createUnsignedTransaction] Returning unsigned transaction, hash to sign and addresses that will sign hash");
        return toReturn;
    }

    @Override
    public ProcessOperationsReturnDto processOperations(NetworkIdentifierType networkIdentifierType,
                                                        List<Operation> operations, DepositParameters depositParameters) throws IOException {
        log.info("[processOperations] About to calculate fee");
        ProcessOperationsDto result = convert(networkIdentifierType, operations);
        double refundsSum =
                result.getStakeKeyDeRegistrationsCount() * Long.parseLong(depositParameters.getKeyDeposit());
        double keyDepositsSum =
                result.getStakeKeyRegistrationsCount() * Long.parseLong(depositParameters.getKeyDeposit());
        double poolDepositsSum =
                result.getPoolRegistrationsCount() * Long.parseLong(depositParameters.getPoolDeposit());
        Map<String, Double> depositsSumMap = new HashMap<>();
        depositsSumMap.put("keyRefundsSum", refundsSum);
        depositsSumMap.put("keyDepositsSum", keyDepositsSum);
        depositsSumMap.put("poolDepositsSum", poolDepositsSum);

        long fee = calculateFee(result.getInputAmounts(), result.getOutputAmounts(),
                result.getWithdrawalAmounts(), depositsSumMap);
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
    public Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts,
                             ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap) {
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
        long fee = (long) (inputsSum + withdrawalsSum + depositsSumMap.get("keyRefundsSum") - outputsSum
                - depositsSumMap.get("keyDepositsSum") - depositsSumMap.get("poolDepositsSum"));
        if (fee < 0) {
            throw ExceptionFactory.outputsAreBiggerThanInputsError();
        }
        return fee;
    }

    @Override
    public ProcessOperationsDto convert(NetworkIdentifierType networkIdentifierType,
                                        List<Operation> operations) throws IOException {
        ProcessOperationsDto processor = new ProcessOperationsDto();

        for (Operation operation : operations) {
            String type = operation.getType();
            try {
                processor = operationProcessor(operation, networkIdentifierType, processor, type);
            } catch (CborSerializationException | CborDeserializationException |
                     NoSuchAlgorithmException | SignatureException | InvalidKeySpecException |
                     InvalidKeyException e) {
                throw ExceptionFactory.unspecifiedError(e.getMessage());
            }
            if (ObjectUtils.isEmpty(processor)) {
                log.error("[processOperations] Operation with id {} has invalid type",
                        operation.getOperationIdentifier());
                throw ExceptionFactory.invalidOperationTypeError();
            }
        }
        return processor;
    }

    @Override
    public ProcessOperationsDto operationProcessor(Operation operation,
                                                   NetworkIdentifierType networkIdentifierType,
                                                   ProcessOperationsDto resultAccumulator,
                                                   String type)
                                                                throws CborSerializationException,
                                                                CborDeserializationException,
                                                                NoSuchAlgorithmException,
                                                                SignatureException,
                                                                InvalidKeySpecException,
                                                                InvalidKeyException {
        if (type.equals(OperationType.INPUT.getValue())) {
            resultAccumulator.getTransactionInputs().add(validateAndParseTransactionInput(operation));
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            resultAccumulator.getAddresses().add(
                    ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            resultAccumulator.getInputAmounts().add(validateValueAmount(operation));
            return resultAccumulator;
        }
        if (type.equals(OperationType.OUTPUT.getValue())) {
            resultAccumulator.getTransactionOutputs().add(validateAndParseTransactionOutput(operation));
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            resultAccumulator.getOutputAmounts().add(validateValueAmount(operation));
            return resultAccumulator;
        }
        if (type.equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
            resultAccumulator.getCertificates().add(processStakeKeyRegistration(operation));
            double stakeNumber = resultAccumulator.getStakeKeyRegistrationsCount();
            resultAccumulator.setStakeKeyRegistrationsCount(++stakeNumber);
            return resultAccumulator;
        }
        if (type.equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue())) {
            Map<String, Object> map = processOperationCertification(networkIdentifierType, operation);
            resultAccumulator.getCertificates().add((Certificate) map.get(Constants.CERTIFICATE));
            resultAccumulator.getAddresses().add((String) map.get(Constants.ADDRESS));
            double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();
            resultAccumulator.setStakeKeyDeRegistrationsCount(++stakeNumber);
            return resultAccumulator;
        }
        if (type.equals(OperationType.STAKE_DELEGATION.getValue())) {
            Map<String, Object> map = processOperationCertification(networkIdentifierType, operation);
            resultAccumulator.getCertificates().add((Certificate) map.get(Constants.CERTIFICATE));
            resultAccumulator.getAddresses().add((String) map.get(Constants.ADDRESS));
            return resultAccumulator;
        }
        if (type.equals(OperationType.WITHDRAWAL.getValue())) {
            ProcessWithdrawalReturnDto processWithdrawalReturnDto = processWithdrawal(networkIdentifierType, operation);
            String withdrawalAmountString=validateValueAmount(operation);
            Long withdrawalAmount = Long.valueOf(withdrawalAmountString);
            resultAccumulator.getWithdrawalAmounts().add(withdrawalAmount);
            resultAccumulator.getWithdrawals()
                    .add(new Withdrawal(processWithdrawalReturnDto.getReward().getAddress(), withdrawalAmount == null ? null : BigInteger.valueOf(withdrawalAmount)));
            resultAccumulator.getAddresses().add(processWithdrawalReturnDto.getAddress());
            return resultAccumulator;
        }
        if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
            ProcessPoolRegistrationReturnDto processPoolRegistrationReturnDto = processPoolRegistration(networkIdentifierType, operation);
            resultAccumulator.getCertificates().add(processPoolRegistrationReturnDto.getCertificate());
            resultAccumulator.getAddresses()
                    .addAll(processPoolRegistrationReturnDto.getTotalAddresses());
            double poolNumber = resultAccumulator.getPoolRegistrationsCount();
            resultAccumulator.setPoolRegistrationsCount(++poolNumber);
            return resultAccumulator;
        }
        if (type.equals(OperationType.POOL_REGISTRATION_WITH_CERT.getValue())) {
            PoolRegistrationCertReturnDto dto = processPoolRegistrationWithCert(operation, networkIdentifierType);
            resultAccumulator.getCertificates().add(dto.getCertificate());
            Set<String> set = dto.getAddress();
            resultAccumulator.getAddresses().addAll(set);
            double poolNumber = resultAccumulator.getPoolRegistrationsCount();
            resultAccumulator.setPoolRegistrationsCount(++poolNumber);
            return resultAccumulator;
        }
        if (type.equals(OperationType.POOL_RETIREMENT.getValue())) {
            Map<String, Object> map = processPoolRetirement(operation);
            resultAccumulator.getCertificates().add((Certificate) map.get(Constants.CERTIFICATE));
            resultAccumulator.getAddresses().add((String) map.get(Constants.POOL_KEY_HASH));
            return resultAccumulator;
        }
        if (type.equals(OperationType.VOTE_REGISTRATION.getValue())) {
            AuxiliaryData voteRegistrationMetadata = processVoteRegistration(operation);
            resultAccumulator.setVoteRegistrationMetadata(voteRegistrationMetadata);
            return resultAccumulator;
        }
        return null;
    }

    public String validateValueAmount(Operation operation){
        return ObjectUtils.isEmpty(operation.getAmount()) ? null : operation.getAmount().getValue();
    }
    PoolRegistrationCertReturnDto processPoolRegistrationWithCert(Operation operation,
                                                                  NetworkIdentifierType networkIdentifierType) {
        OperationMetadata operationMetadata =
                operation==null ? null : operation.getMetadata();
        AccountIdentifier account = operation==null ? null : operation.getAccount();
        return validateAndParsePoolRegistrationCert(
                networkIdentifierType,
                operationMetadata==null ? null : operationMetadata.getPoolRegistrationCert(),
                account==null ? null : account.getAddress()
        );
    }

    @Override
    public PoolRegistrationCertReturnDto validateAndParsePoolRegistrationCert(
            NetworkIdentifierType networkIdentifierType, String poolRegistrationCert, String poolKeyHash) {
        if (ObjectUtils.isEmpty(poolKeyHash)) {
            log.error(
                    "[validateAndParsePoolRegistrationCert] no cold key provided for pool registration");
            throw ExceptionFactory.missingPoolKeyError();
        }
        if (ObjectUtils.isEmpty(poolRegistrationCert)) {
            log.error(
                    "[validateAndParsePoolRegistrationCert] no pool registration certificate provided for pool registration"
            );
            throw ExceptionFactory.missingPoolCertError();
        }
        PoolRegistration parsedCertificate;
        try {
            DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
                    HexUtil.decodeHexString(poolRegistrationCert));
            parsedCertificate = PoolRegistration.deserialize(dataItem);
        } catch (Exception error) {
            log.error("[validateAndParsePoolRegistrationCert] invalid pool registration certificate");
            throw ExceptionFactory.invalidPoolRegistrationCert(error.getMessage());
        }
        if (ObjectUtils.isEmpty(parsedCertificate)) {
            log.error("[validateAndParsePoolRegistrationCert] invalid certificate type");
            throw ExceptionFactory.invalidPoolRegistrationCertType();
        }
        List<String> ownersAddresses = parsePoolOwners(networkIdentifierType.getValue(),
                parsedCertificate);
        String rewardAddress = parsePoolRewardAccount(networkIdentifierType.getValue(),
                parsedCertificate);
        Set<String> addresses = new HashSet<>(new HashSet<>(ownersAddresses));
        addresses.add(poolKeyHash);
        addresses.add(rewardAddress);
        PoolRegistrationCertReturnDto poolRegistrationCertReturnDto = new PoolRegistrationCertReturnDto();
        poolRegistrationCertReturnDto.setCertificate(parsedCertificate);
        poolRegistrationCertReturnDto.setAddress(addresses);
        return poolRegistrationCertReturnDto;

    }

    @Override
    public AuxiliaryData processVoteRegistration(Operation operation) {
        log.info("[processVoteRegistration] About to process vote registration");
        if (!ObjectUtils.isEmpty(operation) && ObjectUtils.isEmpty(operation.getMetadata())) {
            log.error("[processVoteRegistration] Vote registration metadata was not provided");
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operation.getMetadata().getVoteRegistrationMetadata())) {
            log.error("[processVoteRegistration] Vote registration metadata was not provided");
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        Map<String, Object> map = validateAndParseVoteRegistrationMetadata(
                operation.getMetadata().getVoteRegistrationMetadata());

        CBORMetadata metadata = new CBORMetadata();

        CBORMetadataMap map2 = new CBORMetadataMap();
        byte[] votingKeyByte = HexUtil.decodeHexString(((String) map.get(Constants.VOTING_KEY)));
        map2.put(BigInteger.valueOf(CatalystDataIndexes.VOTING_KEY.getValue()), votingKeyByte);
        map2.put(BigInteger.valueOf(CatalystDataIndexes.STAKE_KEY.getValue()),
                HexUtil.decodeHexString(((String) map.get(Constants.STAKE_KEY))));
        map2.put(BigInteger.valueOf(CatalystDataIndexes.REWARD_ADDRESS.getValue()),
                HexUtil.decodeHexString(((String) map.get(Constants.REWARD_ADDRESS))));
        map2.put(BigInteger.valueOf(CatalystDataIndexes.VOTING_NONCE.getValue()),
                BigInteger.valueOf(((Integer) map.get(Constants.VOTING_NONCE))));
        metadata.put(BigInteger.valueOf(Long.parseLong(CatalystLabels.DATA.getLabel())), map2);
        CBORMetadataMap map3 = new CBORMetadataMap();
        map3.put(BigInteger.valueOf(1L), HexUtil.decodeHexString(
                operation.getMetadata().getVoteRegistrationMetadata().getVotingSignature()));
        metadata.put(BigInteger.valueOf(Long.parseLong(CatalystLabels.SIG.getLabel())), map3);
        AuxiliaryData auxiliaryData = new AuxiliaryData();
        auxiliaryData.setMetadata(metadata);
        return auxiliaryData;
    }

    @Override
    public Map<String, Object> validateAndParseVoteRegistrationMetadata(
            VoteRegistrationMetadata voteRegistrationMetadata) {

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse voting key");
        String parsedVotingKey = validateAndParseVotingKey(
                voteRegistrationMetadata.getVotingKey());
        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse stake key");
        if (ObjectUtils.isEmpty(voteRegistrationMetadata.getStakeKey().getHexBytes())) {
            throw ExceptionFactory.missingStakingKeyError();
        }
        boolean checkKey = isKeyValid(voteRegistrationMetadata.getStakeKey().getHexBytes(),
                voteRegistrationMetadata.getStakeKey().getCurveType());
        if (!checkKey) {
            throw ExceptionFactory.invalidStakingKeyFormat();
        }
        String parsedStakeKey = voteRegistrationMetadata.getStakeKey().getHexBytes();
        log.info(
                "[validateAndParseVoteRegistrationMetadata] About to validate and parse reward address");
        String parsedAddress;
        try {
            if (voteRegistrationMetadata.getRewardAddress().startsWith("addr")) {
                throw ExceptionFactory.invalidAddressError();
            }
            Address address = new Address(voteRegistrationMetadata.getRewardAddress());
            parsedAddress = HexUtil.encodeHexString(address.getBytes());
        } catch (Exception exception) {
            throw ExceptionFactory.invalidAddressError();
        }

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting nonce");
        if (voteRegistrationMetadata.getVotingNonce() <= 0) {
            log.error("[validateAndParseVoteRegistrationMetadata] Given voting nonce {} is invalid",
                    voteRegistrationMetadata.getVotingNonce());
            throw ExceptionFactory.votingNonceNotValid();
        }

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting signature");
        if (ObjectUtils.isEmpty(voteRegistrationMetadata.getVotingSignature())) {
            throw ExceptionFactory.invalidVotingSignature();
        }
        if (!isEd25519Signature(voteRegistrationMetadata.getVotingSignature())) {
            log.error(
                    "[validateAndParseVoteRegistrationMetadata] Voting signature has an invalid format");
            throw ExceptionFactory.invalidVotingSignature();
        }
        String votingKeyHex = add0xPrefix(parsedVotingKey);
        String stakeKeyHex = add0xPrefix(parsedStakeKey);
        String rewardAddressHex = add0xPrefix(parsedAddress);
        String votingSignatureHex = add0xPrefix(voteRegistrationMetadata.getVotingSignature());
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.VOTING_KEY, votingKeyHex);
        map.put(Constants.STAKE_KEY, stakeKeyHex);
        map.put(Constants.REWARD_ADDRESS, rewardAddressHex);
        map.put(Constants.VOTING_NONCE, voteRegistrationMetadata.getVotingNonce());
        map.put(Constants.VOTING_SIGNATURE, votingSignatureHex);

        return map;
    }

    @Override
    public String bytesToHex(byte[] bytes) {
        return hexFormatter(bytes);
    }

    @Override
    public String hexFormatter(byte[] bytes) {
        return HexUtil.encodeHexString(bytes);
    }

    @Override
    public String add0xPrefix(String hex) {
        return (hex.startsWith("0x") ? hex : Constants.EMPTY_SYMBOl + hex);
    }

    @Override
    public String validateAndParseVotingKey(PublicKey votingKey) {
        if (ObjectUtils.isEmpty(votingKey.getHexBytes())) {
            log.error("[validateAndParsePublicKey] Voting key not provided");
            throw ExceptionFactory.missingVotingKeyError();
        }
        boolean checkKey = isKeyValid(votingKey.getHexBytes(), votingKey.getCurveType());
        if (!checkKey) {
            log.info("[validateAndParsePublicKey] Voting key has an invalid format");
            throw ExceptionFactory.invalidVotingKeyFormat();
        }
        return votingKey.getHexBytes();
    }

    @Override
    public Map<String, Object> processPoolRetirement(Operation operation) {
        Map<String, Object> map = new HashMap<>();
        log.info("[processPoolRetiring] About to process operation of type {}", operation.getType());
        if (!ObjectUtils.isEmpty(operation.getMetadata()) && !ObjectUtils.isEmpty(
                operation.getMetadata().getEpoch())
                && !ObjectUtils.isEmpty(operation.getAccount()) && !ObjectUtils.isEmpty(
                operation.getAccount().getAddress())) {
            double epoch = operation.getMetadata().getEpoch();
            byte[] keyHash = validateAndParsePoolKeyHash(
                    ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            map.put(Constants.CERTIFICATE, new PoolRetirement(keyHash, Math.round(epoch)));
            map.put(Constants.POOL_KEY_HASH,
                    ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            return map;
        }
        log.error("[processPoolRetiring] Epoch operation metadata is missing");
        throw ExceptionFactory.missingMetadataParametersForPoolRetirement();
    }

    @Override
    public byte[] validateAndParsePoolKeyHash(String poolKeyHash) {
        if (ObjectUtils.isEmpty(poolKeyHash)) {
            log.error("[validateAndParsePoolKeyHash] no pool key hash provided");
            throw ExceptionFactory.missingPoolKeyError();
        }
        byte[] parsedPoolKeyHash;
        try {
            parsedPoolKeyHash = HexUtil.decodeHexString(poolKeyHash);

        } catch (Exception error) {
            log.error("[validateAndParsePoolKeyHash] invalid pool key hash");
            throw ExceptionFactory.invalidPoolKeyError();
        }
        return parsedPoolKeyHash;
    }

    @Override
    public ProcessPoolRegistrationReturnDto processPoolRegistration(NetworkIdentifierType networkIdentifierType,
                                                                    Operation operation) {
        log.info("[processPoolRegistration] About to process pool registration operation");

        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operation.getMetadata().getPoolRegistrationParams())) {
            log.error("[processPoolRegistration] Pool_registration was not provided");
            throw ExceptionFactory.missingPoolRegistrationParameters();
        }
        PoolRegistrationParams poolRegistrationParams =
                ObjectUtils.isEmpty(operation.getMetadata()) ? null
                        : operation.getMetadata().getPoolRegistrationParams();

        PoolRegistationParametersReturnDto dto = validateAndParsePoolRegistationParameters(poolRegistrationParams);
        // eslint-disable-next-line camelcase
        byte[] poolKeyHash = validateAndParsePoolKeyHash(
                ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());

        log.info("[processPoolRegistration] About to validate and parse reward address");
        assert poolRegistrationParams != null;
        Address parsedAddress = validateAndParseRewardAddress(
                poolRegistrationParams.getRewardAddress());
        Bech32.Bech32Data bech32Data = Bech32.decode(parsedAddress.toBech32());
        log.info("[processPoolRegistration] About to generate pool owners");
        Set<String> owners = validateAndParsePoolOwners(poolRegistrationParams.getPoolOwners());
        log.info("[processPoolRegistration] About to generate pool relays");
        List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> parsedRelays = validateAndParsePoolRelays(
                poolRegistrationParams.getRelays());

        log.info("[processPoolRegistration] About to generate pool metadata");
        PoolMetadata poolMetadata = validateAndParsePoolMetadata(
                poolRegistrationParams.getPoolMetadata());

        log.info("[processPoolRegistration] About to generate Pool Registration");
        PoolRegistration wasmPoolRegistration = PoolRegistration.builder()
                .operator(poolKeyHash)
                .vrfKeyHash(ObjectUtils.isEmpty(operation.getMetadata()) ? null : HexUtil.decodeHexString(
                        operation.getMetadata().getPoolRegistrationParams().getVrfKeyHash()))
                .pledge(dto.getPledge())
                .cost(dto.getCost())
                .margin(new UnitInterval(dto.getNumerator(),
                        dto.getDenominator()))
                .rewardAccount(HexUtil.encodeHexString(bech32Data.data))
                .poolOwners(owners)
                .relays(parsedRelays)
                .poolMetadataUrl(ObjectUtils.isEmpty(poolMetadata) ? null : poolMetadata.getUrl())
                .poolMetadataHash(ObjectUtils.isEmpty(poolMetadata) ? null : poolMetadata.getHash())
                .build();
        log.info("[processPoolRegistration] Generating Pool Registration certificate");
        log.info("[processPoolRegistration] Successfully created Pool Registration certificate");

        List<String> totalAddresses = new ArrayList<>();
        if (!ObjectUtils.isEmpty(poolRegistrationParams.getPoolOwners())) {
            totalAddresses.addAll(poolRegistrationParams.getPoolOwners());
        }
        if (!ObjectUtils.isEmpty(parsedAddress.getAddress())) {
            totalAddresses.add(parsedAddress.getAddress());
        }
        if (!ObjectUtils.isEmpty(operation.getAccount().getAddress())) {
            totalAddresses.add(operation.getAccount().getAddress());
        }
        ProcessPoolRegistrationReturnDto processPoolRegistrationReturnDto = new ProcessPoolRegistrationReturnDto();
        processPoolRegistrationReturnDto.setTotalAddresses(totalAddresses);
        processPoolRegistrationReturnDto.setCertificate(wasmPoolRegistration);
        return processPoolRegistrationReturnDto;
    }

    @Override
    public PoolMetadata validateAndParsePoolMetadata(PoolMetadata metadata) {
        PoolMetadata parsedMetadata = null;
        try {
            if (!ObjectUtils.isEmpty(metadata)) {
                parsedMetadata = new PoolMetadata(metadata.getUrl(), metadata.getHash());
                return parsedMetadata;
            }
        } catch (Exception error) {
            log.error("[validateAndParsePoolMetadata] invalid pool metadata");
            throw ExceptionFactory.invalidPoolMetadataError();
        }
        return parsedMetadata;
    }

    @Override
    public List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> validateAndParsePoolRelays(
            List<Relay> relays) {
        if (relays.isEmpty()) {
            throw ExceptionFactory.invalidPoolRelaysError();
        }
        List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> generatedRelays = new ArrayList<>();
        for (Relay relay : relays) {
            if (!ObjectUtils.isEmpty(relay.getPort())) {
                validatePort(relay.getPort());
            }
            com.bloxbean.cardano.client.transaction.spec.cert.Relay generatedRelay = generateSpecificRelay(
                    relay);
            generatedRelays.add(generatedRelay);
        }

        return generatedRelays;
    }

    @Override
    public com.bloxbean.cardano.client.transaction.spec.cert.Relay generateSpecificRelay(
            Relay relay) {
        try {
            String type = relay.getType();
            if (type == null) {
                throw ExceptionFactory.invalidPoolRelayTypeError();
            }
            switch (type) {
                case "single_host_addr" -> {
                    if (relay.getIpv4() != null) {
                        InetAddressValidator validator = InetAddressValidator.getInstance();
                        if (!validator.isValidInet4Address(relay.getIpv4()))
                            throw ExceptionFactory.invalidIpv4();
                    }
                    Integer port =
                            ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10);
                    return new SingleHostAddr(Objects.requireNonNullElse(port, 0), parseIpv4(relay.getIpv4()),
                            parseIpv6(relay.getIpv6()));
                }
                case "single_host_name" -> {
                    validateDnsName(relay.getDnsName());
                    Integer port =
                            ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10);
                    return new SingleHostName(Objects.requireNonNullElse(port, 0), relay.getDnsName());
                }
                case "multi_host_name" -> {
                    validateDnsName(relay.getDnsName());
                    return new MultiHostName(relay.getDnsName());
                }
                default -> throw ExceptionFactory.invalidPoolRelayTypeError();
            }
        } catch (Exception error) {
            log.error("[validateAndParsePoolRelays] invalid pool relay");
            throw ExceptionFactory.invalidPoolRelaysError(error.getMessage());
        }
    }
    public void validateDnsName(String dnsName){
        if (ObjectUtils.isEmpty(dnsName)) {
            throw ExceptionFactory.missingDnsNameError();
        }
    }

    @Override
    public Inet4Address parseIpv4(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            String[] ipNew = ip.split("\\.");
            byte[] bytes = new byte[ipNew.length];
            for (int i = 0; i < ipNew.length; i++) {
                bytes[i] = Byte.parseByte(ipNew[i]);
            }
            return (Inet4Address) InetAddress.getByAddress(bytes);
        }
        return null;
    }

    @Override
    public Inet6Address parseIpv6(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            String ipNew = ip.replace(":", "");
            byte[] parsedIp = HexUtil.decodeHexString(ipNew);
            return (Inet6Address) InetAddress.getByAddress(parsedIp);
        }
        return null;
    }

    @Override
    public void validatePort(String port) {
        try {
            if (!port.matches(Constants.IS_POSITIVE_NUMBER)) {
                log.error("[validateAndParsePort] Invalid port {} received", port);
                throw ExceptionFactory.invalidPoolRelaysError("Invalid port " + port + " received");
            }
        } catch (Exception e) {
            throw ExceptionFactory.invalidPoolRelaysError("Invalid port " + port + " received");
        }
    }

    @Override
    public Address validateAndParseRewardAddress(String rwrdAddress) {
        Address rewardAddress = null;
        try {
            rewardAddress = parseToRewardAddress(rwrdAddress);
        } catch (Exception error) {
            log.error("[validateAndParseRewardAddress] invalid reward address {}", rewardAddress);
            throw ExceptionFactory.invalidAddressError();
        }
        if (ObjectUtils.isEmpty(rewardAddress)) {
            throw ExceptionFactory.invalidAddressError();
        }
        return rewardAddress;
    }

    @Override
    public Address parseToRewardAddress(String address) {
        return new Address(address);
    }

    @Override
    public PoolRegistationParametersReturnDto validateAndParsePoolRegistationParameters(
            PoolRegistrationParams poolRegistrationParameters) {
        String denominator = null;
        String numerator = null;
        if (!ObjectUtils.isEmpty(poolRegistrationParameters) && !ObjectUtils.isEmpty(
                poolRegistrationParameters.getMargin())) {
            denominator = poolRegistrationParameters.getMargin().getDenominator();
            numerator = poolRegistrationParameters.getMargin().getNumerator();
        }

        if (ObjectUtils.isEmpty(denominator) || ObjectUtils.isEmpty(numerator)) {
            log.error(
                    "[validateAndParsePoolRegistationParameters] Missing margin parameter at pool registration parameters"
            );
            throw ExceptionFactory.invalidPoolRegistrationParameters("Missing margin parameter at pool registration parameters");
        }
        logInvalidValue(denominator,numerator,poolRegistrationParameters);
        if (!poolRegistrationParameters.getCost().matches(Constants.IS_POSITIVE_NUMBER)) {
            throw ExceptionFactory.invalidPoolRegistrationParameters(poolRegistrationParameters.getCost());
        }
        if (!poolRegistrationParameters.getPledge().matches(Constants.IS_POSITIVE_NUMBER)) {
            throw ExceptionFactory.invalidPoolRegistrationParameters(poolRegistrationParameters.getPledge());
        }
        if(numerator!=null&&denominator!=null){
            if (!numerator.matches(Constants.IS_POSITIVE_NUMBER)) {
                throw ExceptionFactory.invalidPoolRegistrationParameters(numerator);
            }
            if (!denominator.matches(Constants.IS_POSITIVE_NUMBER)) {
                throw ExceptionFactory.invalidPoolRegistrationParameters(denominator);
            }
        }
        try {
            PoolRegistationParametersReturnDto poolRegistationParametersReturnDto = new PoolRegistationParametersReturnDto();
            poolRegistationParametersReturnDto.setCost(BigInteger.valueOf(Long.parseLong(poolRegistrationParameters.getCost())));
            poolRegistationParametersReturnDto.setPledge(BigInteger.valueOf(Long.parseLong(poolRegistrationParameters.getPledge())));
            poolRegistationParametersReturnDto.setNumerator(BigInteger.valueOf(Long.parseLong(numerator)));
            poolRegistationParametersReturnDto.setDenominator(BigInteger.valueOf(Long.parseLong(denominator)));

            return poolRegistationParametersReturnDto;
        } catch (Exception error) {
            log.error("[validateAndParsePoolRegistationParameters] Given pool parameters are invalid");
            throw ExceptionFactory.invalidPoolRegistrationParameters(error.getMessage());
        }
    }
    public void logInvalidValue(String denominator,
                                String numerator,
                                PoolRegistrationParams poolRegistrationParameters){
        if(numerator!=null&&denominator!=null && (!poolRegistrationParameters.getCost().matches(Constants.IS_POSITIVE_NUMBER) ||
                    !poolRegistrationParameters.getPledge().matches(Constants.IS_POSITIVE_NUMBER) ||
                    !numerator.matches(Constants.IS_POSITIVE_NUMBER) ||
                    !denominator.matches(Constants.IS_POSITIVE_NUMBER))) {
                log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");

        }
    }
    @Override
    public ProcessWithdrawalReturnDto processWithdrawal(NetworkIdentifierType networkIdentifierType,
                                                        Operation operation) {
        log.info("[processWithdrawal] About to process withdrawal");
        // eslint-disable-next-line camelcase
        HdPublicKey hdPublicKey = new HdPublicKey();
        if (operation.getMetadata() != null &&
                operation.getMetadata().getStakingCredential() != null &&
                operation.getMetadata().getStakingCredential().getHexBytes() != null) {
            hdPublicKey.setKeyData(
                    HexUtil.decodeHexString(operation.getMetadata().getStakingCredential().getHexBytes()));
        }
        String address = generateRewardAddress(networkIdentifierType, hdPublicKey);
        HdPublicKey hdPublicKey1 = new HdPublicKey();
        hdPublicKey1.setKeyData(HexUtil.decodeHexString(operation.getMetadata().getStakingCredential().getHexBytes()));
        ProcessWithdrawalReturnDto processWithdrawalReturnDto = new ProcessWithdrawalReturnDto();
        processWithdrawalReturnDto.setReward(AddressProvider.getRewardAddress(hdPublicKey1,
                new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic())));
        processWithdrawalReturnDto.setAddress(address);
        return processWithdrawalReturnDto;
    }

    @Override
    public Map<String, Object> processOperationCertification(
            NetworkIdentifierType networkIdentifierType, Operation operation) {
        log.info(
                "[processOperationCertification] About to process operation of type {}", operation.getType());
        // eslint-disable-next-line camelcase
        HashMap<String, Object> map = new HashMap<>();
        PublicKey publicKey = ObjectUtils.isEmpty(operation.getMetadata()) ? null
                : operation.getMetadata().getStakingCredential();
        StakeCredential credential = getStakingCredentialFromHex(publicKey);
        HdPublicKey hdPublicKey = new HdPublicKey();
        if (publicKey != null) {
            hdPublicKey.setKeyData(HexUtil.decodeHexString(publicKey.getHexBytes()));
        }
        String address = generateRewardAddress(networkIdentifierType, hdPublicKey);
        if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
            // eslint-disable-next-line camelcase
            if (operation.getMetadata().getPoolKeyHash() == null) {
                throw ExceptionFactory.missingPoolKeyError();
            }
            Certificate certificate = new StakeDelegation(credential, new StakePoolId(
                    ObjectUtils.isEmpty(operation.getMetadata()) ? null
                            : HexUtil.decodeHexString(operation.getMetadata().getPoolKeyHash())));
            map.put(Constants.CERTIFICATE, certificate);
            map.put(Constants.ADDRESS, address);
            return map;
        }
        map.put(Constants.CERTIFICATE, new StakeDeregistration(credential));
        map.put(Constants.ADDRESS, address);
        return map;
    }

    @Override
    public Certificate processStakeKeyRegistration(Operation operation) {
        log.info("[processStakeKeyRegistration] About to process stake key registration");
        // eslint-disable-next-line camelcase
        StakeCredential credential = getStakingCredentialFromHex(
                ObjectUtils.isEmpty(operation.getMetadata()) ? null
                        : operation.getMetadata().getStakingCredential());
        return new StakeRegistration(credential);
    }

    @Override
    public StakeCredential getStakingCredentialFromHex(PublicKey stakingCredential) {
        HdPublicKey stakingKey = getPublicKey(stakingCredential);
        return StakeCredential.fromKeyHash(stakingKey.getKeyHash());
    }

    @Override
    public HdPublicKey getPublicKey(PublicKey publicKey) {
        if (ObjectUtils.isEmpty(publicKey) || ObjectUtils.isEmpty(publicKey.getHexBytes())) {
            log.error("[getPublicKey] Staking key not provided");
            throw ExceptionFactory.missingStakingKeyError();
        }
        boolean checkKey = isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType());
        if (!checkKey) {
            log.info("[getPublicKey] Staking key has an invalid format");
            throw ExceptionFactory.invalidStakingKeyFormat();
        }
        byte[] stakingKeyBuffer = hexStringToBuffer(publicKey.getHexBytes());
        HdPublicKey hdPublicKey = new HdPublicKey();
        hdPublicKey.setKeyData(stakingKeyBuffer);
        return hdPublicKey;
    }

    @Override
    public Boolean isKeyValid(String publicKeyBytes, String curveType) {
        if (publicKeyBytes == null) {
            return false;
        }
        return publicKeyBytes.length() == Constants.PUBLIC_KEY_BYTES_LENGTH && curveType.equals(
                Constants.VALID_CURVE_TYPE);
    }

    @Override
    public TransactionInput validateAndParseTransactionInput(Operation input) {
        if (ObjectUtils.isEmpty(input.getCoinChange())) {
            log.error("[validateAndParseTransactionInput] Input has missing coin_change");
            throw ExceptionFactory.transactionInputsParametersMissingError("Input has missing coin_change field");
        }
        String transactionId = null;
        String index = null;
        try {
            if (!ObjectUtils.isEmpty(input.getCoinChange())) {
                String[] array = input.getCoinChange().getCoinIdentifier().getIdentifier().split(":");
                transactionId = array[0];
                index = array[1];
            }
        } catch (Exception exception) {
            throw ExceptionFactory.transactionInputsParametersMissingError("Input has invalid coin_identifier field");
        }
        if (ObjectUtils.isEmpty(transactionId) || ObjectUtils.isEmpty(index)) {
            log.error("[validateAndParseTransactionInput] Input has missing transactionId and index");
            throw ExceptionFactory.transactionInputsParametersMissingError("Input has invalid coin_identifier field");
        }
        String value = ObjectUtils.isEmpty(input.getAmount()) ? null : input.getAmount().getValue();
        if (ObjectUtils.isEmpty(value)) {
            log.error("[validateAndParseTransactionInput] Input has missing amount value field");
            throw ExceptionFactory.transactionInputsParametersMissingError("Input has missing amount value field");
        }
        if (value != null && value.matches(Constants.IS_POSITIVE_NUMBER)) {
            log.error("[validateAndParseTransactionInput] Input has positive value");
            throw ExceptionFactory.transactionInputsParametersMissingError("Input has positive amount value");
        }
        try {
            return new TransactionInput(
                    HexUtil.encodeHexString(HexUtil.decodeHexString(transactionId)),
                    Integer.parseInt(index));
        } catch (Exception e) {
            throw ExceptionFactory.deserializationError(e.getMessage());
        }
    }

    @Override
    public TransactionOutput validateAndParseTransactionOutput(Operation output) {
        Object address;
        try {
            address = ObjectUtils.isEmpty(output.getAccount()) ? null
                    : generateAddress(output.getAccount().getAddress());
        } catch (Exception error) {
            throw ExceptionFactory.transactionOutputDeserializationError("Invalid input: " + output.getAccount().getAddress() + " "
                    + error.getMessage());
        }
        if (ObjectUtils.isEmpty(address)) {
            log.error("[validateAndParseTransactionOutput] Output has missing address field");
            throw ExceptionFactory.transactionOutputDeserializationError("Output has missing address field");
        }
        String outputValue =
                ObjectUtils.isEmpty(output.getAmount()) ? null : output.getAmount().getValue();
        if (ObjectUtils.isEmpty(output.getAmount()) || outputValue == null) {
            log.error("[validateAndParseTransactionOutput] Output has missing amount value field");
            throw ExceptionFactory.transactionOutputDeserializationError("Output has missing amount field");
        }
        if (!outputValue.matches(Constants.IS_POSITIVE_NUMBER)) {
            log.error("[validateAndParseTransactionOutput] Output has negative or invalid value {}",
                    outputValue);
            throw ExceptionFactory.transactionOutputDeserializationError("Output has negative amount value");
        }
        Value value = Value.builder().coin(BigInteger.valueOf(Long.parseLong(outputValue))).build();
        if (!ObjectUtils.isEmpty(output.getMetadata()) && !ObjectUtils.isEmpty(
                output.getMetadata().getTokenBundle())) {
            value.setMultiAssets(validateAndParseTokenBundle(output.getMetadata().getTokenBundle()));
        }
        Address address1;
        try {
            address1 = (Address) address;
            if(address1!=null){
                return new TransactionOutput(address1.getAddress(), value);
            }else{
                return new TransactionOutput(null, value);
            }
        } catch (Exception error) {
            if(address!=null){
                return new TransactionOutput(((ByronAddress) address).getAddress(), value);
            }else{
                return new TransactionOutput(null, value);
            }
        }
    }

    @Override
    public Object generateAddress(String address) {
        EraAddressType addressType = getEraAddressType(address);
        if (addressType == EraAddressType.BYRON) {
            return new ByronAddress(address);
        }
        return new Address(address);
    }

    @Override
    public EraAddressType getEraAddressType(String address) {
        try {
            if (address.startsWith("addr") || address.startsWith("stake")) {
                return EraAddressType.SHELLEY;
            }
            new ByronAddress(address).getAddress();
            return EraAddressType.BYRON;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<MultiAsset> validateAndParseTokenBundle(List<TokenBundleItem> tokenBundle) {
        List<MultiAsset> multiAssets = new ArrayList<>();
        tokenBundle.forEach(tokenBundleItem -> {
            validateCheckKey(tokenBundleItem.getPolicyId());
            List<Asset> assets = new ArrayList<>();
            List<Asset> assetsCheck = new ArrayList<>();
            tokenBundleItem.getTokens().forEach(token -> {
                validateTokenName(token.getCurrency().getSymbol());
                String assetName = token.getCurrency().getSymbol();
                if (assetsCheck.stream().anyMatch(asset -> asset.getName().equals(assetName))) {
                    log.error(
                            "[validateAndParseTokenBundle] Token name {} has already been added for policy {}",
                            token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
                    throw ExceptionFactory.transactionOutputsParametersMissingError("Token name " + token.getCurrency().getSymbol() + " has already been added for policy " +
                            tokenBundleItem.getPolicyId() + " and will be overriden");
                }
                validateTokenValue(token,tokenBundleItem);
                //revise
                if (token.getCurrency().getSymbol().equals(Constants.SYMBOL_REGEX)) {
                    token.getCurrency().setSymbol("");
                }
                assets.add(new Asset(
                        token.getCurrency().getSymbol().startsWith(Constants.EMPTY_SYMBOl) ?
                                token.getCurrency().getSymbol() : Constants.EMPTY_SYMBOl + token.getCurrency().getSymbol(),
                        BigInteger.valueOf(Long.parseLong(token.getValue()))));
                assetsCheck.add(new Asset(token.getCurrency().getSymbol(),
                        BigInteger.valueOf(Long.parseLong(token.getValue()))));
            });
            multiAssets.add(new MultiAsset(tokenBundleItem.getPolicyId(), assets));
        });
        return multiAssets;
    }

    public void validateTokenValue(Amount token,TokenBundleItem tokenBundleItem){
        if (ObjectUtils.isEmpty(token.getValue()) || ObjectUtils.isEmpty(
                token.getValue().charAt(0))) {
            log.error(
                    "[validateAndParseTokenBundle] Token with name {} for policy {} has no value or is empty",
                    token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
            throw ExceptionFactory.transactionOutputsParametersMissingError("Token with name " + token.getCurrency().getSymbol() + " for policy "
                    + tokenBundleItem.getPolicyId() + " has no value or is empty");
        }
        if (!token.getValue().matches(Constants.IS_POSITIVE_NUMBER)) {
            log.error("[validateAndParseTokenBundle] Asset {} has negative or invalid value {}",
                    token.getCurrency().getSymbol(), token.getValue());
            throw ExceptionFactory.transactionOutputsParametersMissingError("Asset " + token.getCurrency().getSymbol()
                    + " has negative or invalid value " + token.getValue());
        }
    }
    public void validateCheckKey(String policyId){
        boolean checckKey = isPolicyIdValid(policyId);
        if (!checckKey) {
            log.error("[validateAndParseTokenBundle] PolicyId {} is not valid",
                    policyId);
            throw ExceptionFactory.transactionOutputsParametersMissingError("PolicyId " + policyId
                    + " is not valid");
        }
    }

    public void validateTokenName(String tokenName){
        boolean checkTokenName = isTokenNameValid(tokenName);
        if (!checkTokenName) {
            log.error("validateAndParseTokenBundle] Token name {} is not valid",
                    tokenName);
            throw ExceptionFactory.transactionOutputsParametersMissingError("Token name " + tokenName + " is not valid");
        }
    }
    @Override
    public Boolean isPolicyIdValid(String policyId) {
        return policyId.matches(Constants.PolicyId_Validation);
    }

    @Override
    public Boolean isTokenNameValid(String name) {
        return name.matches(Constants.Token_Name_Validation) || isEmptyHexString(name);
    }

    @Override
    public Boolean isEmptyHexString(String toCheck) {
        return !ObjectUtils.isEmpty(toCheck) && toCheck.equals(Constants.EMPTY_HEX);
    }

    @Override
    public byte[] hexStringToBuffer(String input) {
        boolean checkEmptyHexString = isEmptyHexString(input);
        return checkEmptyHexString ? HexUtil.decodeHexString("") : HexUtil.decodeHexString(input);
    }

    @Override
    public NetworkIdentifierType getNetworkIdentifierByRequestParameters(
            NetworkIdentifier networkRequestParameters) {
        if (networkRequestParameters.getNetwork().equals(Constants.MAINNET))
            return NetworkIdentifierType.CARDANO_MAINNET_NETWORK;
        if (networkRequestParameters.getNetwork().equals(Constants.PREPROD))
            return NetworkIdentifierType.CARDANO_PREPROD_NETWORK;
        return NetworkIdentifierType.CARDANO_TESTNET_NETWORK;
    }

    @Override
    public boolean isAddressTypeValid(String type) {
        return Arrays.stream(AddressType.values()).anyMatch(a -> a.getValue().equals(type))
                || type.equals("");
    }

    @Override
    public Amount mapAmount(String value, String symbol, Integer decimals,
                            Metadata metadata) {
        return new Amount(value,
                new Currency(ObjectUtils.isEmpty(symbol) ? Constants.ADA : hexStringFormatter(symbol),
                        ObjectUtils.isEmpty(decimals) ? Constants.ADA_DECIMALS : decimals, metadata), null);
    }

    @Override
    public String hexStringFormatter(String toFormat) {
        if (ObjectUtils.isEmpty(toFormat)) {
            return Constants.EMPTY_HEX;
        } else {
            return toFormat;
        }
    }

    @Override
    public Long calculateTxMinimumFee(Long transactionSize,
                                      ProtocolParameters protocolParameters) {
        return protocolParameters.getMinFeeCoefficient() * transactionSize
                + protocolParameters.getMinFeeConstant();
    }

    @Override
    public ProtocolParameters getProtocolParameters() {
        log.debug("[getLinearFeeParameters] About to run findProtocolParameters query");
        return ledgerDataProviderService.findProtocolParameters();
    }

    @Override
    public Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl)
            throws CborException {
        return previousTxSize + com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                new UnsignedInteger(updatedTtl)).length -
                com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                        new UnsignedInteger(previousTtl)).length;
    }

    @Override
    public Long calculateTtl(Long ttlOffset) {
        BlockDto latestBlock = getLatestBlock();
        return latestBlock.getSlotNo() + ttlOffset;
    }

    @Override
    public BlockDto getLatestBlock() {
        log.info("[getLatestBlock] About to look for latest block");
        Long latestBlockNumber = findLatestBlockNumber();
        log.info("[getLatestBlock] Latest block number is {}", latestBlockNumber);
        BlockDto latestBlock = ledgerDataProviderService.findBlock(latestBlockNumber, null);
        if (ObjectUtils.isEmpty(latestBlock)) {
            log.error("[getLatestBlock] Latest block not found");
            throw ExceptionFactory.blockNotFoundException();
        }
        log.debug(latestBlock + "[getLatestBlock] Returning latest block");
        return latestBlock;
    }

    @Override
    public Long findLatestBlockNumber() {
        log.debug("[findLatestBlockNumber] About to run findLatestBlockNumber query");
        Long latestBlockNumber = ledgerDataProviderService.findLatestBlockNumber();
        log.debug("[findLatestBlockNumber] Latest block number is {}", latestBlockNumber);
        return latestBlockNumber;
    }


    @Override
    public String encodeExtraData(String transaction, TransactionExtraData extraData)
            throws CborException {
        List<Operation> extraOperations = extraData.getOperations().stream()
                // eslint-disable-next-line camelcase
                .filter(operation -> {
                            String coinAction = ObjectUtils.isEmpty(operation.getCoinChange()) ? null
                                    : operation.getCoinChange().getCoinAction();
                            boolean coinActionStatement =
                                    !ObjectUtils.isEmpty(coinAction) && coinAction.equals(Constants.COIN_SPENT_ACTION);
                            return coinActionStatement ||
                                    Constants.StakingOperations.contains(operation.getType()) ||
                                    Constants.PoolOperations.contains(operation.getType()) ||
                                    Constants.VoteOperations.contains(operation.getType());
                        }
                ).toList();
        TransactionExtraData toEncode = new TransactionExtraData();
        toEncode.setOperations(extraOperations);
        if (!ObjectUtils.isEmpty(extraData.getTransactionMetadataHex())) {
            toEncode.setTransactionMetadataHex(extraData.getTransactionMetadataHex());
        }
        co.nstant.in.cbor.model.Map transactionExtraDataMap = new co.nstant.in.cbor.model.Map();
        Array operationArray = new Array();
        extraOperations.forEach(operation -> {
            co.nstant.in.cbor.model.Map operationIdentifierMap = new co.nstant.in.cbor.model.Map();
            Long index = operation.getOperationIdentifier().getIndex();
            if (index != null) {
                operationIdentifierMap.put(new UnicodeString(Constants.INDEX),
                        new UnsignedInteger(operation.getOperationIdentifier().getIndex()));
            }
            Long networkIndex = operation.getOperationIdentifier().getNetworkIndex();
            if (networkIndex != null) {
                operationIdentifierMap.put(new UnicodeString(Constants.NETWORK_INDEX),
                        new UnsignedInteger(networkIndex));
            }
            Array rOperationArray = new Array();
            if (!ObjectUtils.isEmpty(operation.getRelatedOperations())) {
                operation.getRelatedOperations().forEach(rOperation -> {
                    co.nstant.in.cbor.model.Map operationIdentifierMapnew = new co.nstant.in.cbor.model.Map();
                    if (operation.getOperationIdentifier().getIndex() != null) {
                        operationIdentifierMapnew.put(new UnicodeString(Constants.INDEX),
                                new UnsignedInteger(operation.getOperationIdentifier().getIndex()));
                    }
                    Long networkIndex2 = operation.getOperationIdentifier().getNetworkIndex();
                    if (networkIndex2 != null) {
                        operationIdentifierMapnew.put(new UnicodeString(Constants.NETWORK_INDEX),
                                new UnsignedInteger(networkIndex2));
                    }
                    rOperationArray.add(operationIdentifierMapnew);
                });
            }
            co.nstant.in.cbor.model.Map accountIdentifierMap = new co.nstant.in.cbor.model.Map();
            if (operation.getAccount() != null) {
                if (operation.getAccount().getAddress() != null) {
                    accountIdentifierMap.put(new UnicodeString(Constants.ADDRESS),
                            new UnicodeString(operation.getAccount().getAddress()));
                }
                if (operation.getAccount().getSubAccount() != null) {
                    co.nstant.in.cbor.model.Map subAccountIdentifierMap = new co.nstant.in.cbor.model.Map();
                    if (operation.getAccount().getSubAccount().getAddress() != null)
                        subAccountIdentifierMap.put(new UnicodeString(Constants.ADDRESS),
                                new UnicodeString(operation.getAccount().getSubAccount().getAddress()));
                    if (operation.getAccount().getSubAccount().getMetadata() != null)
                        subAccountIdentifierMap.put(new UnicodeString(Constants.METADATA),
                                operation.getAccount().getSubAccount().getMetadata());
                    accountIdentifierMap.put(new UnicodeString(Constants.SUB_ACCOUNT), subAccountIdentifierMap);
                }
                if (operation.getAccount().getMetadata() != null) {
                    co.nstant.in.cbor.model.Map accIdMetadataMap = new co.nstant.in.cbor.model.Map();
                    accIdMetadataMap.put(new UnicodeString(Constants.CHAIN_CODE),
                            new UnicodeString(ObjectUtils.isEmpty(operation.getAccount().getMetadata()) ? null
                                    : operation.getAccount().getMetadata().getChainCode()));
                    accountIdentifierMap.put(new UnicodeString(Constants.METADATA), accIdMetadataMap);
                }
            }
            co.nstant.in.cbor.model.Map amountMap = getAmountMap(operation.getAmount());
            co.nstant.in.cbor.model.Map coinChangeMap = new co.nstant.in.cbor.model.Map();
            co.nstant.in.cbor.model.Map coinIdentifierMap = new co.nstant.in.cbor.model.Map();
            CoinChange coinChange =
                    ObjectUtils.isEmpty(operation.getCoinChange()) ? null : operation.getCoinChange();
            CoinIdentifier coinIdentifier =
                    ObjectUtils.isEmpty(coinChange) ? null : coinChange.getCoinIdentifier();
            coinIdentifierMap.put(new UnicodeString(Constants.IDENTIFIER),
                    new UnicodeString(
                            ObjectUtils.isEmpty(coinIdentifier) ? null : coinIdentifier.getIdentifier()));
            coinChangeMap.put(new UnicodeString(Constants.COIN_IDENTIFIER), coinIdentifierMap);
            coinChangeMap.put(new UnicodeString(Constants.COIN_ACTION),
                    new UnicodeString(ObjectUtils.isEmpty(coinChange) ? null : coinChange.getCoinAction()));
            boolean operationMetadataCheck = ObjectUtils.isEmpty(operation.getMetadata());
            co.nstant.in.cbor.model.Map operationMap = new co.nstant.in.cbor.model.Map();

            if (operation.getOperationIdentifier() != null) {
                operationMap.put(new UnicodeString(Constants.OPERATION_IDENTIFIER), operationIdentifierMap);
            }
            if (!ObjectUtils.isEmpty(operation.getRelatedOperations())) {
                operationMap.put(new UnicodeString(Constants.RELATED_OPERATION), rOperationArray);
            }
            if (!ObjectUtils.isEmpty(operation.getType())) {
                operationMap.put(new UnicodeString(Constants.TYPE), new UnicodeString(operation.getType()));
            }
            if (!ObjectUtils.isEmpty(operation.getStatus())) {
                operationMap.put(new UnicodeString(Constants.STATUS), new UnicodeString(operation.getStatus()));
            }
            if (!ObjectUtils.isEmpty(operation.getAccount())) {
                operationMap.put(new UnicodeString(Constants.ACCOUNT), accountIdentifierMap);
            }
            if (!ObjectUtils.isEmpty(operation.getAmount())) {
                operationMap.put(new UnicodeString(Constants.AMOUNT), amountMap);
            }
            if (!ObjectUtils.isEmpty(operation.getCoinChange())) {
                operationMap.put(new UnicodeString(Constants.COIN_CHANGE), coinChangeMap);
            }

            if (!operationMetadataCheck) {
                co.nstant.in.cbor.model.Map oMetadataMap = new co.nstant.in.cbor.model.Map();
                OperationMetadata operationMetadata =
                        ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata();
                if (operationMetadata != null && operationMetadata.getStakingCredential() != null) {
                    co.nstant.in.cbor.model.Map stakingCredentialMap = getPublicKeymap(
                            operationMetadata.getStakingCredential());
                    oMetadataMap.put(new UnicodeString(Constants.STAKING_CREDENTIAL), stakingCredentialMap);
                }
                if (operationMetadata != null && operationMetadata.getWithdrawalAmount() != null) {
                    co.nstant.in.cbor.model.Map withdrawalAmount = getAmountMapV2(
                            operationMetadata.getWithdrawalAmount());
                    oMetadataMap.put(new UnicodeString(Constants.WITHDRAWALAMOUNT), withdrawalAmount);
                }
                if (operationMetadata != null && operationMetadata.getDepositAmount() != null) {
                    co.nstant.in.cbor.model.Map depositAmount = getAmountMapV2(
                            operationMetadata.getDepositAmount());
                    oMetadataMap.put(new UnicodeString(Constants.DEPOSITAMOUNT), depositAmount);
                }
                if (operationMetadata != null && operationMetadata.getRefundAmount() != null) {
                    co.nstant.in.cbor.model.Map refundAmount = getAmountMap(
                            operationMetadata.getRefundAmount());
                    oMetadataMap.put(new UnicodeString(Constants.REFUNDAMOUNT), refundAmount);
                }


                if (operationMetadata != null && operationMetadata.getPoolKeyHash() != null) {
                    oMetadataMap.put(new UnicodeString(Constants.POOL_KEY_HASH),
                            new UnicodeString(operationMetadata.getPoolKeyHash()));
                }
                if (operationMetadata != null && operationMetadata.getEpoch() != null) {
                    oMetadataMap.put(new UnicodeString(Constants.EPOCH),
                            new UnsignedInteger(operationMetadata.getEpoch()));
                }
                if (operation.getMetadata() != null && operation.getMetadata().getTokenBundle() != null) {
                    Array tokenBundleArray = new Array();
                    operation.getMetadata().getTokenBundle().forEach(tokenbundle -> {
                        if (tokenbundle != null) {
                            co.nstant.in.cbor.model.Map tokenBundleItemMap = new co.nstant.in.cbor.model.Map();
                            if (tokenbundle.getPolicyId() != null) {
                                tokenBundleItemMap.put(new UnicodeString(Constants.POLICYID),
                                        new UnicodeString(tokenbundle.getPolicyId()));
                            }
                            if (tokenbundle.getTokens() != null) {
                                Array tokensArray = new Array();
                                tokenbundle.getTokens().forEach(amount -> {
                                    if (amount != null) {
                                        co.nstant.in.cbor.model.Map amountMapNext = getAmountMap(amount);
                                        tokensArray.add(amountMapNext);
                                    }
                                });
                                tokenBundleItemMap.put(new UnicodeString(Constants.TOKENS), tokensArray);
                            }
                            tokenBundleArray.add(tokenBundleItemMap);
                        }
                    });
                    oMetadataMap.put(new UnicodeString(Constants.TOKENBUNDLE), tokenBundleArray);
                }
                if (operationMetadata != null && operationMetadata.getPoolRegistrationCert() != null) {
                    oMetadataMap.put(new UnicodeString(Constants.POOLREGISTRATIONCERT),
                            new UnicodeString(operationMetadata.getPoolRegistrationCert()));
                }

                if (operationMetadata != null && operationMetadata.getPoolRegistrationParams() != null) {
                    co.nstant.in.cbor.model.Map poolRegistrationParamsMap = new co.nstant.in.cbor.model.Map();
                    PoolRegistrationParams poolRegistrationParams = operationMetadata.getPoolRegistrationParams();
                    if (poolRegistrationParams.getVrfKeyHash() != null) {
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.VRFKEYHASH),
                                new UnicodeString(poolRegistrationParams.getVrfKeyHash()));
                    }
                    if (poolRegistrationParams.getRewardAddress() != null) {
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.REWARD_ADDRESS),
                                new UnicodeString(poolRegistrationParams.getRewardAddress()));
                    }
                    if (poolRegistrationParams.getPledge() != null) {
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.PLEDGE),
                                new UnicodeString(poolRegistrationParams.getPledge()));
                    }
                    if (poolRegistrationParams.getCost() != null) {
                        poolRegistrationParamsMap.put(new UnicodeString("cost"),
                                new UnicodeString(poolRegistrationParams.getCost()));
                    }
                    Array poolOwnersArray = new Array();
                    if (!ObjectUtils.isEmpty(operationMetadata.getPoolRegistrationParams())
                            && operation.getMetadata().getPoolRegistrationParams().getPoolOwners() != null) {
                        operation.getMetadata().getPoolRegistrationParams().getPoolOwners()
                                .forEach(o -> {
                                    DataItem dataItem = new UnicodeString(o);
                                    poolOwnersArray.add(dataItem);
                                });
                    }
                    poolRegistrationParamsMap.put(new UnicodeString(Constants.POOLOWNERS), poolOwnersArray);
                    Array relaysArray = new Array();
                    if (!ObjectUtils.isEmpty(operationMetadata.getPoolRegistrationParams())
                            && operationMetadata.getPoolRegistrationParams().getRelays() != null) {
                        operation.getMetadata().getPoolRegistrationParams().getRelays()
                                .forEach(r -> {
                                    if (r != null) {
                                        co.nstant.in.cbor.model.Map relayMap = new co.nstant.in.cbor.model.Map();
                                        if (r.getType() != null) {
                                            relayMap.put(new UnicodeString(Constants.TYPE), new UnicodeString(r.getType()));
                                        }
                                        if (r.getIpv4() != null) {
                                            relayMap.put(new UnicodeString(Constants.IPV4), new UnicodeString(r.getIpv4()));
                                        }
                                        if (r.getIpv6() != null) {
                                            relayMap.put(new UnicodeString(Constants.IPV6), new UnicodeString(r.getIpv6()));
                                        }
                                        if (r.getDnsName() != null) {
                                            relayMap.put(new UnicodeString(Constants.DNSNAME),
                                                    new UnicodeString(r.getDnsName()));
                                        }
                                        if (r.getPort() != null) {
                                            relayMap.put(new UnicodeString(Constants.PORT), new UnicodeString(r.getPort()));
                                        }
                                        relaysArray.add(relayMap);
                                    }
                                });
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.RELAYS), relaysArray);
                    }
                    PoolMargin poolMargin = poolRegistrationParams.getMargin();
                    if (poolMargin != null) {
                        co.nstant.in.cbor.model.Map marginMap = new co.nstant.in.cbor.model.Map();
                        if (poolMargin.getNumerator() != null) {
                            marginMap.put(new UnicodeString(Constants.NUMERATOR),
                                    new UnicodeString(poolMargin.getNumerator()));
                        }
                        if (poolMargin.getDenominator() != null) {
                            marginMap.put(new UnicodeString(Constants.DENOMINATOR),
                                    new UnicodeString(poolMargin.getDenominator()));
                        }
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.MARGIN), marginMap);
                    }
                    if (poolRegistrationParams.getMarginPercentage() != null) {
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.MARGIN_PERCENTAGE),
                                new UnicodeString(poolRegistrationParams.getMarginPercentage()));
                    }
                    co.nstant.in.cbor.model.Map poolMetadataMap = new co.nstant.in.cbor.model.Map();
                    PoolMetadata poolMetadata = poolRegistrationParams.getPoolMetadata();
                    if (poolMetadata != null) {
                        if (poolMetadata.getUrl() != null) {
                            poolMetadataMap.put(new UnicodeString(Constants.URL),
                                    new UnicodeString(poolMetadata.getUrl()));
                        }
                        if (poolMetadata.getHash() != null) {
                            poolMetadataMap.put(new UnicodeString(Constants.HASH),
                                    new UnicodeString(poolMetadata.getHash()));
                        }
                        poolRegistrationParamsMap.put(new UnicodeString(Constants.POOLMETADATA), poolMetadataMap);
                    }
                    oMetadataMap.put(new UnicodeString(Constants.POOLREGISTRATIONPARAMS), poolRegistrationParamsMap);
                }

                if (operationMetadata != null && operationMetadata.getVoteRegistrationMetadata() != null) {
                    co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = new co.nstant.in.cbor.model.Map();
                    VoteRegistrationMetadata voteRegistrationMetadata =
                            operationMetadata.getVoteRegistrationMetadata();
                    co.nstant.in.cbor.model.Map stakeKeyMap = getPublicKeymap(
                            ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                                    : voteRegistrationMetadata.getStakeKey());
                    voteRegistrationMetadataMap.put(new UnicodeString(Constants.REWARD_ADDRESS), new UnicodeString(
                            ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                                    : voteRegistrationMetadata.getRewardAddress()));
                    voteRegistrationMetadataMap.put(new UnicodeString(Constants.STAKE_KEY), stakeKeyMap);
                    co.nstant.in.cbor.model.Map votingKeyMap = getPublicKeymap(
                            ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                                    : voteRegistrationMetadata.getVotingKey());
                    voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_KEY), votingKeyMap);
                    Integer unsignedIntegerNumber;
                    unsignedIntegerNumber = ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                            : voteRegistrationMetadata.getVotingNonce();
                    UnsignedInteger unsignedInteger = null;
                    if (unsignedIntegerNumber != null) {
                        unsignedInteger = new UnsignedInteger(
                                unsignedIntegerNumber);
                    }
                    if (unsignedInteger != null) {
                        voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_NONCE), unsignedInteger);
                    }
                    voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_SIGNATURE), new UnicodeString(
                            ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                                    : voteRegistrationMetadata.getVotingSignature()));
                    oMetadataMap.put(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA),
                            voteRegistrationMetadataMap);
                }
                if (!ObjectUtils.isEmpty(operation.getMetadata())) {
                    operationMap.put(new UnicodeString(Constants.METADATA), oMetadataMap);
                }
            }
            operationArray.add(operationMap);
        });
        transactionExtraDataMap.put(new UnicodeString(Constants.OPERATIONS), operationArray);
        if (toEncode.getTransactionMetadataHex() != null) {
            transactionExtraDataMap.put(new UnicodeString(Constants.TRANSACTIONMETADATAHEX),
                    new UnicodeString(toEncode.getTransactionMetadataHex()));
        }
        Array outputArray = new Array();
        outputArray.add(new UnicodeString(transaction));
        outputArray.add(transactionExtraDataMap);
        return HexUtil.encodeHexString(
                com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(outputArray,
                        false));
    }

    @Override
    public Array decodeExtraData(String encoded) {
        try {
            DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
                    HexUtil.decodeHexString(encoded));
            return (Array) dataItem;
        } catch (Exception e) {
            throw ExceptionFactory.cantBuildSignedTransaction();
        }
    }

    @Override
    public co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey) {
        co.nstant.in.cbor.model.Map stakingCredentialMap = new co.nstant.in.cbor.model.Map();
        stakingCredentialMap.put(new UnicodeString(Constants.HEX_BYTES),
                new UnicodeString(ObjectUtils.isEmpty(publicKey) ? null : publicKey.getHexBytes()));
        stakingCredentialMap.put(new UnicodeString(Constants.CURVE_TYPE),
                new UnicodeString(ObjectUtils.isEmpty(publicKey) ? null : publicKey.getCurveType()));
        return stakingCredentialMap;
    }

    @Override
    public co.nstant.in.cbor.model.Map getAmountMap(Amount amount) {
        co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
        if (!ObjectUtils.isEmpty(amount)) {
            if (amount.getValue() != null) {
                amountMap.put(new UnicodeString(Constants.VALUE), new UnicodeString(amount.getValue()));
            }
            getCurrencyMap(amount,amountMap);
            if (amount.getMetadata() != null) {
                amountMap.put(new UnicodeString(Constants.METADATA), amount.getMetadata());
            }
        }
        return amountMap;
    }

    public co.nstant.in.cbor.model.Map getAmountMapV2(Amount amount) {
        co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
        if (!ObjectUtils.isEmpty(amount)) {
            getCurrencyMap(amount,amountMap);
            if (amount.getValue() != null) {
                amountMap.put(new UnicodeString(Constants.VALUE), new UnicodeString(amount.getValue()));
            }
            if (amount.getMetadata() != null) {
                amountMap.put(new UnicodeString(Constants.METADATA), amount.getMetadata());
            }
        }
        return amountMap;
    }
    public void getCurrencyMap(Amount amount,co.nstant.in.cbor.model.Map amountMap){
        if (amount.getCurrency() != null) {
            co.nstant.in.cbor.model.Map currencyMap = new co.nstant.in.cbor.model.Map();
            if (amount.getCurrency().getSymbol() != null) {
                currencyMap.put(new UnicodeString(Constants.SYMBOL),
                        new UnicodeString(amount.getCurrency().getSymbol()));
            }
            if (amount.getCurrency().getDecimals() != null) {
                currencyMap.put(new UnicodeString(Constants.DECIMALS),
                        new UnsignedInteger(amount.getCurrency().getDecimals()));
            }
            co.nstant.in.cbor.model.Map addedMetadataMap = new co.nstant.in.cbor.model.Map();
            addedMetadataMap.put(new UnicodeString(Constants.METADATA),
                    new UnicodeString(ObjectUtils.isEmpty(amount.getCurrency().getMetadata()) ? null
                            : amount.getCurrency().getMetadata().getPolicyId()));
            if (amount.getCurrency().getMetadata() != null) {
                currencyMap.put(new UnicodeString(Constants.METADATA), addedMetadataMap);
            }
            amountMap.put(new UnicodeString(Constants.CURRENCY), currencyMap);
        }
    }
    @Override
    public List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
                                                                    Set<String> addresses) {
        return addresses.stream().map(
                address -> new SigningPayload(null, new AccountIdentifier(address), transactionBodyHash,
                        SignatureType.ED25519)).toList();

    }

    @Override
    public TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map) {
        //separator
        TransactionExtraData transactionExtraData = new TransactionExtraData();
        if (map.get(
                new UnicodeString(Constants.TRANSACTIONMETADATAHEX)) != null) {
            String transactionMetadataHex = ((UnicodeString) map.get(
                    new UnicodeString(Constants.TRANSACTIONMETADATAHEX))).getString();
            transactionExtraData.setTransactionMetadataHex(transactionMetadataHex);
        }
        List<Operation> operations = new ArrayList<>();
        List<DataItem> operationsListMap = ((Array) map.get(
                new UnicodeString(Constants.OPERATIONS))).getDataItems();
        operationsListMap.forEach(oDataItem -> {
            co.nstant.in.cbor.model.Map operationMap = (co.nstant.in.cbor.model.Map) oDataItem;
            Operation operation = new Operation();
            if (operationMap.get(new UnicodeString(Constants.OPERATION_IDENTIFIER)) != null) {
                co.nstant.in.cbor.model.Map operationIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(
                        new UnicodeString(Constants.OPERATION_IDENTIFIER));
                OperationIdentifier operationIdentifier = new OperationIdentifier();
                if (operationIdentifierMap.get(new UnicodeString(Constants.INDEX)) != null) {
                    operationIdentifier.setIndex(((UnsignedInteger) operationIdentifierMap.get(new UnicodeString(Constants.INDEX))).getValue()
                            .longValue());
                }
                if (operationIdentifierMap.get(new UnicodeString(Constants.NETWORK_INDEX)) != null) {
                    operationIdentifier.setNetworkIndex(((UnsignedInteger) operationIdentifierMap.get(new UnicodeString(Constants.NETWORK_INDEX))).getValue()
                            .longValue());
                }
                operation.setOperationIdentifier(operationIdentifier);
            }
            if (operationMap.get(new UnicodeString(Constants.RELATED_OPERATION)) != null) {
                List<OperationIdentifier> relatedOperations = new ArrayList<>();
                List<DataItem> relatedOperationsDI = ((Array) operationMap.get(
                        new UnicodeString(Constants.RELATED_OPERATION))).getDataItems();
                relatedOperationsDI.forEach(rDI -> {
                    co.nstant.in.cbor.model.Map operationIdentifierMap2 = (co.nstant.in.cbor.model.Map) rDI;

                    OperationIdentifier operationIdentifier2 = new OperationIdentifier();
                    if (operationIdentifierMap2.get(new UnicodeString(Constants.INDEX)) != null) {
                        operationIdentifier2.setIndex(((UnsignedInteger) operationIdentifierMap2.get(new UnicodeString(Constants.INDEX))).getValue()
                                .longValue());
                    }
                    if (operationIdentifierMap2.get(new UnicodeString(Constants.NETWORK_INDEX)) != null) {
                        operationIdentifier2.setNetworkIndex(((UnsignedInteger) operationIdentifierMap2.get(new UnicodeString(Constants.NETWORK_INDEX))).getValue()
                                .longValue());
                    }
                    relatedOperations.add(operationIdentifier2);
                });
                operation.setRelatedOperations(relatedOperations);
            }
            if (operationMap.get(new UnicodeString(Constants.TYPE)) != null) {
                String type = ((UnicodeString) (operationMap.get(new UnicodeString(Constants.TYPE)))).getString();
                operation.setType(type);
            }
            if (operationMap.get(new UnicodeString(Constants.STATUS)) != null) {
                String status = ((UnicodeString) (operationMap.get(
                        new UnicodeString(Constants.STATUS)))).getString();
                operation.setStatus(status);
            }
            if (operationMap.get(new UnicodeString(Constants.ACCOUNT)) != null) {
                AccountIdentifier accountIdentifier = new AccountIdentifier();
                co.nstant.in.cbor.model.Map accountIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(
                        new UnicodeString(Constants.ACCOUNT));
                if (accountIdentifierMap.get(
                        new UnicodeString(Constants.ADDRESS)) != null) {
                    String address = ((UnicodeString) accountIdentifierMap.get(
                            new UnicodeString(Constants.ADDRESS))).getString();
                    accountIdentifier.setAddress(address);
                }
                if (accountIdentifierMap.get(new UnicodeString(Constants.SUB_ACCOUNT)) != null) {
                    co.nstant.in.cbor.model.Map subAccountIdentifierMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
                            new UnicodeString(Constants.SUB_ACCOUNT));
                    SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
                    if (subAccountIdentifierMap.get(new UnicodeString(Constants.ADDRESS)) != null) {
                        String addressSub = ((UnicodeString) (subAccountIdentifierMap.get(
                                new UnicodeString(Constants.ADDRESS)))).getString();
                        subAccountIdentifier.setAddress(addressSub);
                    }
                    if ( subAccountIdentifierMap.get(new UnicodeString(Constants.METADATA))!=null) {
                        co.nstant.in.cbor.model.Map metadataSub = (co.nstant.in.cbor.model.Map) (subAccountIdentifierMap.get(
                                new UnicodeString(Constants.METADATA)));
                        subAccountIdentifier.setMetadata(metadataSub);
                    }
                    accountIdentifier.setSubAccount(subAccountIdentifier);
                }
                if (accountIdentifierMap.get(new UnicodeString(Constants.METADATA)) != null) {
                    co.nstant.in.cbor.model.Map accountIdentifierMetadataMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
                            new UnicodeString(Constants.METADATA));
                    AccountIdentifierMetadata accountIdentifierMetadata = new AccountIdentifierMetadata();
                    if (accountIdentifierMetadataMap.get(new UnicodeString(Constants.CHAIN_CODE)) != null) {
                        String chainCode=null;

                        if(accountIdentifierMetadataMap.get(new UnicodeString(Constants.CHAIN_CODE)).getMajorType().getValue()==MajorType.UNICODE_STRING.getValue()){
                            chainCode = ((UnicodeString) (accountIdentifierMetadataMap.get(
                                new UnicodeString(Constants.CHAIN_CODE)))).getString();
                        }

                        accountIdentifierMetadata.setChainCode(chainCode);
                    }
                    accountIdentifier.setMetadata(accountIdentifierMetadata);
                }
                operation.setAccount(accountIdentifier);
            }
            if (operationMap.get(new UnicodeString(Constants.AMOUNT)) != null) {
                co.nstant.in.cbor.model.Map amountMap = (co.nstant.in.cbor.model.Map) operationMap.get(
                        new UnicodeString(Constants.AMOUNT));
                Amount amount = getAmountFromMap(amountMap);
                operation.setAmount(amount);
            }
            if (operationMap.get(new UnicodeString(Constants.COIN_CHANGE)) != null) {
                co.nstant.in.cbor.model.Map coinChangeMap = (co.nstant.in.cbor.model.Map) operationMap.get(
                        new UnicodeString(Constants.COIN_CHANGE));
                CoinChange coinChange = new CoinChange();
                if (coinChangeMap.get(new UnicodeString(Constants.COIN_ACTION)) != null) {
                    String coinAction = ((UnicodeString) coinChangeMap.get(
                            new UnicodeString(Constants.COIN_ACTION))).getString();
                    coinChange.setCoinAction(coinAction);
                }
                if (coinChangeMap.get(new UnicodeString(Constants.COIN_IDENTIFIER)) != null) {
                    CoinIdentifier coinIdentifier = new CoinIdentifier();
                    co.nstant.in.cbor.model.Map coinIdentifierMap = (co.nstant.in.cbor.model.Map) coinChangeMap.get(
                            new UnicodeString(Constants.COIN_IDENTIFIER));
                    String identifier = ((UnicodeString) coinIdentifierMap.get(
                            new UnicodeString(Constants.IDENTIFIER))).getString();
                    coinIdentifier.setIdentifier(identifier);
                    coinChange.setCoinIdentifier(coinIdentifier);
                }
                operation.setCoinChange(coinChange);
            }
            if (operationMap.get(new UnicodeString(Constants.METADATA)) != null) {
                co.nstant.in.cbor.model.Map metadataMap = (co.nstant.in.cbor.model.Map) operationMap.get(
                        new UnicodeString(Constants.METADATA));
                OperationMetadata operationMetadata = new OperationMetadata();
                if (metadataMap.get(new UnicodeString(Constants.WITHDRAWALAMOUNT)) != null) {
                    co.nstant.in.cbor.model.Map withdrawalAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
                            new UnicodeString(Constants.WITHDRAWALAMOUNT));
                    Amount amountW = getAmountFromMap(withdrawalAmountMap);
                    operationMetadata.setWithdrawalAmount(amountW);
                }
                if (metadataMap.get(new UnicodeString(Constants.DEPOSITAMOUNT)) != null) {
                    co.nstant.in.cbor.model.Map depositAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
                            new UnicodeString(Constants.DEPOSITAMOUNT));
                    Amount amountD = getAmountFromMap(depositAmountMap);
                    operationMetadata.setDepositAmount(amountD);
                }
                if (metadataMap.get(
                        new UnicodeString(Constants.REFUNDAMOUNT)) != null) {
                    co.nstant.in.cbor.model.Map refundAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
                            new UnicodeString(Constants.REFUNDAMOUNT));
                    Amount amountR = getAmountFromMap(refundAmountMap);
                    operationMetadata.setRefundAmount(amountR);
                }
                if (metadataMap.get(new UnicodeString(Constants.STAKING_CREDENTIAL)) != null) {
                    co.nstant.in.cbor.model.Map stakingCredentialMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
                            new UnicodeString(Constants.STAKING_CREDENTIAL));
                    PublicKey publicKey = getPublicKeyFromMap(stakingCredentialMap);
                    operationMetadata.setStakingCredential(publicKey);
                }
                if (metadataMap.get(
                        new UnicodeString(Constants.POOL_KEY_HASH)) != null) {
                    String poolKeyHash = ((UnicodeString) metadataMap.get(
                            new UnicodeString(Constants.POOL_KEY_HASH))).getString();
                    operationMetadata.setPoolKeyHash(poolKeyHash);
                }
                if (metadataMap.get(
                        new UnicodeString(Constants.EPOCH)) != null) {
                    Long epoch = ((UnsignedInteger) metadataMap.get(
                            new UnicodeString(Constants.EPOCH))).getValue().longValue();
                    operationMetadata.setEpoch(epoch);
                }
                if (metadataMap.get(new UnicodeString(Constants.TOKENBUNDLE)) != null) {
                    List<DataItem> tokenBundleArray = ((Array) metadataMap.get(
                            new UnicodeString(Constants.TOKENBUNDLE))).getDataItems();
                    List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
                    tokenBundleArray.forEach(t -> {
                        co.nstant.in.cbor.model.Map tokenBundleMap = (co.nstant.in.cbor.model.Map) t;
                        TokenBundleItem tokenBundleItem = new TokenBundleItem();
                        if (tokenBundleMap.get(
                                new UnicodeString(Constants.POLICYID)) != null) {
                            String policyIdT = ((UnicodeString) tokenBundleMap.get(
                                    new UnicodeString(Constants.POLICYID))).getString();
                            tokenBundleItem.setPolicyId(policyIdT);
                        }

                        List<Amount> tokenAList = new ArrayList<>();
                        if (tokenBundleMap.get(
                                new UnicodeString(Constants.TOKENS)) != null) {
                            List<DataItem> tokensItem = ((Array) tokenBundleMap.get(
                                    new UnicodeString(Constants.TOKENS))).getDataItems();
                            tokensItem.forEach(tk -> {
                                co.nstant.in.cbor.model.Map tokenAmountMap = (co.nstant.in.cbor.model.Map) tk;
                                Amount amount1 = getAmountFromMap(tokenAmountMap);
                                tokenAList.add(amount1);
                            });
                        }
                        tokenBundleItem.setTokens(tokenAList);
                        tokenBundleItems.add(tokenBundleItem);
                    });
                    operationMetadata.setTokenBundle(tokenBundleItems);
                }
                if (metadataMap.get(
                        new UnicodeString(Constants.POOLREGISTRATIONCERT)) != null) {
                    String poolRegistrationCert = ((UnicodeString) metadataMap.get(
                            new UnicodeString(Constants.POOLREGISTRATIONCERT))).getString();
                    operationMetadata.setPoolRegistrationCert(poolRegistrationCert);
                }
                if (metadataMap.get(new UnicodeString(Constants.POOLREGISTRATIONPARAMS)) != null) {
                    co.nstant.in.cbor.model.Map poolRegistrationParamsMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
                            new UnicodeString(Constants.POOLREGISTRATIONPARAMS));
                    PoolRegistrationParams poolRegistrationParams = new PoolRegistrationParams();
                    if (poolRegistrationParamsMap.get(
                            new UnicodeString(Constants.VRFKEYHASH)) != null) {
                        String vrfKeyHash = ((UnicodeString) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.VRFKEYHASH))).getString();
                        poolRegistrationParams.setVrfKeyHash(vrfKeyHash);
                    }
                    if (poolRegistrationParamsMap.get(
                            new UnicodeString(Constants.REWARD_ADDRESS)) != null) {
                        String rewardAddress = ((UnicodeString) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.REWARD_ADDRESS))).getString();
                        poolRegistrationParams.setRewardAddress(rewardAddress);
                    }
                    if (poolRegistrationParamsMap.get(new UnicodeString(Constants.PLEDGE)) != null) {
                        String pledge = ((UnicodeString) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.PLEDGE))).getString();
                        poolRegistrationParams.setPledge(pledge);
                    }
                    if (poolRegistrationParamsMap.get(
                            new UnicodeString(Constants.COST)) != null) {
                        String cost = ((UnicodeString) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.COST))).getString();
                        poolRegistrationParams.setCost(cost);
                    }
                    if (poolRegistrationParamsMap.get(
                            new UnicodeString(Constants.POOLOWNERS)) != null) {
                        List<String> stringList = new ArrayList<>();
                        List<DataItem> poolOwners = ((Array) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.POOLOWNERS))).getDataItems();
                        poolOwners.forEach(p -> {
                            if (p != null) {
                                stringList.add(((UnicodeString) p).getString());
                            }
                        });
                        poolRegistrationParams.setPoolOwners(stringList);
                    }
                    if (poolRegistrationParamsMap.get(new UnicodeString(Constants.RELAYS)) != null) {
                        List<Relay> relayList = new ArrayList<>();
                        List<DataItem> relaysArray = ((Array) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.RELAYS))).getDataItems();
                        relaysArray.forEach(rA -> {
                            co.nstant.in.cbor.model.Map rAMap = (co.nstant.in.cbor.model.Map) rA;
                            Relay relay = new Relay();
                            if (rAMap.get(new UnicodeString(Constants.TYPE)) != null) {
                                String typeR = ((UnicodeString) rAMap.get(new UnicodeString(Constants.TYPE))).getString();
                                relay.setType(typeR);
                            }
                            if (rAMap.get(new UnicodeString(Constants.IPV4)) != null) {
                                String ipv4 = ((UnicodeString) rAMap.get(new UnicodeString(Constants.IPV4))).getString();
                                relay.setIpv4(ipv4);
                            }
                            if (rAMap.get(new UnicodeString(Constants.IPV6)) != null) {
                                String ipv6 = ((UnicodeString) rAMap.get(new UnicodeString(Constants.IPV6))).getString();
                                relay.setIpv6(ipv6);
                            }
                            if (rAMap.get(new UnicodeString(Constants.DNSNAME)) != null) {
                                String dnsName = ((UnicodeString) rAMap.get(
                                        new UnicodeString(Constants.DNSNAME))).getString();
                                relay.setDnsName(dnsName);
                            }
                            relayList.add(relay);
                        });
                        poolRegistrationParams.setRelays(relayList);
                    }
                    if (poolRegistrationParamsMap.get(new UnicodeString(Constants.MARGIN)) != null) {
                        co.nstant.in.cbor.model.Map marginMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.MARGIN));
                        PoolMargin poolMargin = new PoolMargin();
                        if (marginMap.get(
                                new UnicodeString(Constants.NUMERATOR)) != null) {
                            String numerator = ((UnicodeString) marginMap.get(
                                    new UnicodeString(Constants.NUMERATOR))).getString();
                            poolMargin.setNumerator(numerator);
                        }
                        if (marginMap.get(
                                new UnicodeString(Constants.DENOMINATOR)) != null) {
                            String denominator = ((UnicodeString) marginMap.get(
                                    new UnicodeString(Constants.DENOMINATOR))).getString();
                            poolMargin.setDenominator(denominator);
                        }
                        poolRegistrationParams.setMargin(poolMargin);
                    }
                    if (poolRegistrationParamsMap.get(
                            new UnicodeString(Constants.MARGIN_PERCENTAGE)) != null) {
                        String marginPercentage = ((UnicodeString) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.MARGIN_PERCENTAGE))).getString();
                        poolRegistrationParams.setMarginPercentage(marginPercentage);
                    }
                    if (poolRegistrationParamsMap.get(
                            new UnicodeString(Constants.POOLMETADATA)) != null) {
                        PoolMetadata poolMetadata = new PoolMetadata();
                        co.nstant.in.cbor.model.Map poolMetadataMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(
                                new UnicodeString(Constants.POOLMETADATA));
                        if (poolMetadataMap.get(
                                new UnicodeString(Constants.URL)) != null) {
                            String url = ((UnicodeString) poolMetadataMap.get(
                                    new UnicodeString(Constants.URL))).getString();
                            poolMetadata.setUrl(url);
                        }
                        if (poolMetadataMap.get(
                                new UnicodeString(Constants.HASH)) != null) {
                            String hash = ((UnicodeString) poolMetadataMap.get(
                                    new UnicodeString(Constants.HASH))).getString();
                            poolMetadata.setHash(hash);
                        }
                        poolRegistrationParams.setPoolMetadata(poolMetadata);
                    }
                    operationMetadata.setPoolRegistrationParams(poolRegistrationParams);
                }
                if (metadataMap.get(new UnicodeString(Constants.VOTEREGISTRATIONMETADATA)) != null) {
                    VoteRegistrationMetadata voteRegistrationMetadata = new VoteRegistrationMetadata();
                    co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
                            new UnicodeString(Constants.VOTEREGISTRATIONMETADATA));
                    if (voteRegistrationMetadataMap.get(new UnicodeString(Constants.STAKE_KEY)) != null) {
                        co.nstant.in.cbor.model.Map stakeKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(
                                new UnicodeString(Constants.STAKE_KEY));
                        PublicKey publicKey1 = getPublicKeyFromMap(stakeKeyMap);
                        voteRegistrationMetadata.setStakeKey(publicKey1);
                    }
                    if (voteRegistrationMetadataMap.get(
                            new UnicodeString(Constants.VOTING_KEY)) != null) {
                        co.nstant.in.cbor.model.Map votingKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(
                                new UnicodeString(Constants.VOTING_KEY));
                        PublicKey publicKey2 = getPublicKeyFromMap(votingKeyMap);
                        voteRegistrationMetadata.setVotingKey(publicKey2);
                    }
                    if (voteRegistrationMetadataMap.get(
                            new UnicodeString(Constants.REWARD_ADDRESS)) != null) {
                        String rewardAddress2 = ((UnicodeString) voteRegistrationMetadataMap.get(
                                new UnicodeString(Constants.REWARD_ADDRESS))).getString();
                        voteRegistrationMetadata.setRewardAddress(rewardAddress2);
                    }
                    if (voteRegistrationMetadataMap.get(
                            new UnicodeString(Constants.VOTING_SIGNATURE)) != null) {
                        String votingSignature = ((UnicodeString) voteRegistrationMetadataMap.get(
                                new UnicodeString(Constants.VOTING_SIGNATURE))).getString();
                        voteRegistrationMetadata.setVotingSignature(votingSignature);
                    }
                    if (voteRegistrationMetadataMap.get(
                            new UnicodeString(Constants.VOTING_NONCE)) != null) {
                        int votingNonce = ((UnsignedInteger) voteRegistrationMetadataMap.get(
                                new UnicodeString(Constants.VOTING_NONCE))).getValue().intValue();
                        voteRegistrationMetadata.setVotingNonce(votingNonce);
                    }
                    operationMetadata.setVoteRegistrationMetadata(voteRegistrationMetadata);
                }
                operation.setMetadata(operationMetadata);
            }
            operations.add(operation);
        });
        transactionExtraData.setOperations(operations);
        return transactionExtraData;
    }

    @Override
    public PublicKey getPublicKeyFromMap(co.nstant.in.cbor.model.Map stakingCredentialMap) {
        PublicKey publicKey = new PublicKey();
        if (stakingCredentialMap.get(
                new UnicodeString(Constants.HEX_BYTES)) != null) {
            String hexBytes = ((UnicodeString) stakingCredentialMap.get(
                    new UnicodeString(Constants.HEX_BYTES))).getString();
            publicKey.setHexBytes(hexBytes);
        }
        if (stakingCredentialMap.get(
                new UnicodeString(Constants.CURVE_TYPE)) != null) {
            String curveType = ((UnicodeString) stakingCredentialMap.get(
                    new UnicodeString(Constants.CURVE_TYPE))).getString();
            publicKey.setCurveType(curveType);
        }
        return publicKey;
    }

    @Override
    public Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap) {
        Amount amount = new Amount();
        if (amountMap != null) {
            if (amountMap.get(new UnicodeString(Constants.VALUE)) != null) {
                String value = ((UnicodeString) amountMap.get(new UnicodeString(Constants.VALUE))).getString();
                amount.setValue(value);
            }
            if (amountMap.get(new UnicodeString(Constants.METADATA)) != null) {
                co.nstant.in.cbor.model.Map metadataAm = (co.nstant.in.cbor.model.Map) amountMap.get(
                        new UnicodeString(Constants.METADATA));
                amount.setMetadata(metadataAm);
            }
            getCurrencyFromMap(amountMap,amount);
        }
        return amount;
    }

    public void getCurrencyFromMap(co.nstant.in.cbor.model.Map amountMap,Amount amount){
        if (amountMap.get(
                new UnicodeString(Constants.CURRENCY)) != null) {
            co.nstant.in.cbor.model.Map currencyMap = (co.nstant.in.cbor.model.Map) amountMap.get(
                    new UnicodeString(Constants.CURRENCY));
            Currency currency = new Currency();
            if (currencyMap.get(new UnicodeString(Constants.SYMBOL)) != null) {
                String symbol = ((UnicodeString) currencyMap.get(
                        new UnicodeString(Constants.SYMBOL))).getString();
                currency.setSymbol(symbol);
            }
            if (currencyMap.get(
                    new UnicodeString(Constants.DECIMALS)) != null) {
                Integer decimals = ((UnsignedInteger) currencyMap.get(
                        new UnicodeString(Constants.DECIMALS))).getValue()
                        .intValue();
                currency.setDecimals(decimals);
            }

            if (currencyMap.get(new UnicodeString(Constants.METADATA)) != null) {
                Metadata metadata = new Metadata();
                co.nstant.in.cbor.model.Map addedMetadataMap = (co.nstant.in.cbor.model.Map) currencyMap.get(
                        new UnicodeString(Constants.METADATA));
                if (addedMetadataMap.get(new UnicodeString(Constants.POLICYID)) != null) {
                    String policyId = ((UnicodeString) addedMetadataMap.get(
                            new UnicodeString(Constants.POLICYID))).getString();
                    metadata.setPolicyId(policyId);
                }
                currency.setMetadata(metadata);
            }
            amount.setCurrency(currency);
        }
    }

    @Override
    public TransactionParsed parseUnsignedTransaction(NetworkIdentifierType networkIdentifierType,
                                                      String transaction, TransactionExtraData extraData) {
        try {
            log.info(transaction
                    + "[parseUnsignedTransaction] About to create unsigned transaction from bytes");
            byte[] transactionBuffer = HexUtil.decodeHexString(transaction);
            TransactionBody parsed = TransactionBody.deserialize((co.nstant.in.cbor.model.Map) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(transactionBuffer));
            log.info(
                    extraData + "[parseUnsignedTransaction] About to parse operations from transaction body");
            List<Operation> operations = convert(parsed, extraData, networkIdentifierType.getValue());
            log.info(operations + "[parseUnsignedTransaction] Returning ${operations.length} operations");
            return new TransactionParsed(operations, new ArrayList<>());
        } catch (Exception error) {
            log.error(error
                    + "[parseUnsignedTransaction] Cant instantiate unsigned transaction from transaction bytes");
            throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
        }
    }

    @Override
    public TransactionParsed parseSignedTransaction(NetworkIdentifierType networkIdentifierType,
                                                    String transaction, TransactionExtraData extraData) {
        try {
            byte[] transactionBuffer = HexUtil.decodeHexString(transaction);
            List<DataItem> dataItemList = CborDecoder.decode(transactionBuffer);
            Array array = (Array) dataItemList.get(0);
            if (dataItemList.size() >= 2 && array.getDataItems().size() == 3) array.add(dataItemList.get(1));
            log.info("[parseSignedTransaction] About to create signed transaction from bytes");
            Transaction parsed = Transaction.deserialize(CborSerializationUtil.serialize(array));
            log.info("[parseSignedTransaction] About to parse operations from transaction body");
            List<Operation> operations = convert(parsed.getBody(), extraData,
                    networkIdentifierType.getValue());
            log.info("[parseSignedTransaction] About to get signatures from parsed transaction");
            log.info(operations + "[parseSignedTransaction] Returning operations");
            List<String> accum = new ArrayList<>();
            extraData.getOperations().forEach(o ->
                    {
                        try {
                            List<String> list = getSignerFromOperation(networkIdentifierType, o);
                            accum.addAll(list);
                        } catch (CborSerializationException e) {
                          throw ExceptionFactory.cantCreateSignedTransactionFromBytes();
                        }
                    }
            );
            List<AccountIdentifier> accountIdentifierSigners = getUniqueAccountIdentifiers(accum);
            return new TransactionParsed(operations, accountIdentifierSigners);
        } catch (Exception error) {
            log.error(error
                    + "[parseSignedTransaction] Cant instantiate signed transaction from transaction bytes");
            throw ExceptionFactory.cantCreateSignedTransactionFromBytes();
        }
    }

    @Override
    public List<String> getSignerFromOperation(NetworkIdentifierType networkIdentifierType,
                                               Operation operation) throws CborSerializationException {
        if (Constants.PoolOperations.contains(operation.getType())) {
            return getPoolSigners(networkIdentifierType, operation);
        }
        if (!ObjectUtils.isEmpty(
                ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress())) {
            return new ArrayList<>(List.of(operation.getAccount().getAddress()));
        }
        if (operation.getType().equals(OperationType.VOTE_REGISTRATION.getValue())) {
            return new ArrayList<>();
        }
        HdPublicKey hdPublicKey = new HdPublicKey();
        hdPublicKey.setKeyData(HexUtil.decodeHexString(operation.getMetadata().getStakingCredential().getHexBytes()));
        return new ArrayList<>(List.of(generateRewardAddress(networkIdentifierType, hdPublicKey)));
    }

    @Override
    public List<String> getPoolSigners(NetworkIdentifierType networkIdentifierType,
                                       Operation operation) {
        List<String> signers = new ArrayList<>();
        switch (operation.getType()) {
            case "poolRegistration" -> {
                PoolRegistrationParams poolRegistrationParameters =
                        ObjectUtils.isEmpty(operation.getMetadata()) ? null
                                : operation.getMetadata().getPoolRegistrationParams();
                if (validateAddressPresence(operation)) {
                    signers.add(operation.getAccount().getAddress());
                }
                if (poolRegistrationParameters!=null) {
                    signers.add(poolRegistrationParameters.getRewardAddress());
                    signers.addAll(poolRegistrationParameters.getPoolOwners());
                }
            }
            case "poolRegistrationWithCert" -> {
                String poolCertAsHex = ObjectUtils.isEmpty(operation.getMetadata()) ? null
                        : operation.getMetadata().getPoolRegistrationCert();
                PoolRegistrationCertReturnDto dto = validateAndParsePoolRegistrationCert(
                        networkIdentifierType,
                        poolCertAsHex,
                        ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()
                );
                signers.addAll(dto.getAddress());
            }

            // pool retirement case
            default -> {
                if (validateAddressPresence(operation)) {
                    signers.add(operation.getAccount().getAddress());
                }
            }
        }
        log.info("[getPoolSigners] About to return {} signers for {} operation", signers.size(),
                operation.getType());
        return signers;
    }

    public boolean validateAddressPresence(Operation operation){
        return !ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null
                : operation.getAccount().getAddress());
    }

    @Override
    public List<AccountIdentifier> getUniqueAccountIdentifiers(List<String> addresses) {
        return addressesToAccountIdentifiers(new HashSet<>(addresses));
    }

    @Override
    public List<AccountIdentifier> addressesToAccountIdentifiers(Set<String> uniqueAddresses) {
        return uniqueAddresses.stream().map(AccountIdentifier::new).toList();
    }

    @Override
    public List<Operation> convert(TransactionBody transactionBody, TransactionExtraData extraData,
                                   Integer network)
            throws UnknownHostException, JsonProcessingException, AddressExcepion, CborDeserializationException, CborException, CborSerializationException {
        List<Operation> operations = new ArrayList<>();
        List<TransactionInput> inputs = transactionBody.getInputs();
        List<TransactionOutput> outputs = transactionBody.getOutputs();
        log.info("[parseOperationsFromTransactionBody] About to parse {} inputs", inputs.size());
        List<Operation> inputOperations = extraData.getOperations().stream()
                .filter(o -> o.getType().equals(OperationType.INPUT.getValue()))
                .toList();
        for (int i = 0; i < inputs.size(); i++) {

            if (!inputOperations.isEmpty() && inputOperations.size() <= inputs.size()) {
                Operation operation = new Operation();
                operation.setOperationIdentifier(inputOperations.get(i).getOperationIdentifier());
                operation.setRelatedOperations(inputOperations.get(i).getRelatedOperations());
                operation.setType(inputOperations.get(i).getType());
                operation.setStatus("");
                operation.setAccount(inputOperations.get(i).getAccount());
                operation.setAmount(inputOperations.get(i).getAmount());
                operation.setCoinChange(inputOperations.get(i).getCoinChange());
                operation.setMetadata(inputOperations.get(i).getMetadata());
                operations.add(operation);
            } else {
                TransactionInput input = inputs.get(i);
                Operation inputParsed = parseInputToOperation(input, (long) operations.size());
                Operation operation = new Operation();
                operation.setOperationIdentifier(inputParsed.getOperationIdentifier());
                operation.setRelatedOperations(inputParsed.getRelatedOperations());
                operation.setType(inputParsed.getType());
                operation.setStatus("");
                operation.setAccount(inputParsed.getAccount());
                operation.setAmount(inputParsed.getAmount());
                operation.setCoinChange(inputParsed.getCoinChange());
                operation.setMetadata(inputParsed.getMetadata());
                operations.add(operation);
            }
        }
        // till this line operations only contains inputs
        List<OperationIdentifier> relatedOperations = getRelatedOperationsFromInputs(operations);
        log.info("[parseOperationsFromTransactionBody] About to parse {} outputs", outputs.size());
        for (TransactionOutput output : outputs) {
            String address = parseAddress(output.getAddress(), getAddressPrefix(network, null));
            Operation outputParsed = parseOutputToOperation(output, (long) operations.size(),
                    relatedOperations, address);
            operations.add(outputParsed);
        }

        List<Operation> certOps = extraData.getOperations().stream()
                .filter(o -> Constants.StakePoolOperations.contains(o.getType())
                ).toList();
        List<Operation> parsedCertOperations = parseCertsToOperations(transactionBody, certOps,
                network);
        operations.addAll(parsedCertOperations);
        List<Operation> withdrawalOps = extraData.getOperations().stream()
                .filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue()))
                .toList();
        Integer withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0
                : transactionBody.getWithdrawals().size();
        parseWithdrawalsToOperations(withdrawalOps, withdrawalsCount, operations, network);

        List<Operation> voteOp = extraData.getOperations().stream()
                .filter(o -> o.getType().equals(OperationType.VOTE_REGISTRATION.getValue()))
                .toList();
        if (!ObjectUtils.isEmpty(voteOp)) {
            Operation parsedVoteOperations = parseVoteMetadataToOperation(
                    voteOp.get(0).getOperationIdentifier().getIndex(),
                    extraData.getTransactionMetadataHex()
            );
            operations.add(parsedVoteOperations);
        }

        return operations;
    }

    //need to revise
    @Override
    public Operation parseVoteMetadataToOperation(Long index, String transactionMetadataHex)
            throws CborDeserializationException {
        log.info("[parseVoteMetadataToOperation] About to parse a vote registration operation");
        if (ObjectUtils.isEmpty(transactionMetadataHex)) {
            log.error("[parseVoteMetadataToOperation] Missing vote registration metadata");
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(HexUtil.decodeHexString(transactionMetadataHex));
        AuxiliaryData transactionMetadata = AuxiliaryData.deserialize((co.nstant.in.cbor.model.Map) array.getDataItems().get(0));
        CBORMetadata metadata = (CBORMetadata) transactionMetadata.getMetadata();
        CBORMetadataMap data = (CBORMetadataMap) metadata.get(BigInteger.valueOf(Long.parseLong(CatalystLabels.DATA.getLabel())));
        CBORMetadataMap sig = (CBORMetadataMap) metadata.get(BigInteger.valueOf(Long.parseLong(CatalystLabels.SIG.getLabel())));
        if (ObjectUtils.isEmpty(data)) {
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        if (ObjectUtils.isEmpty(sig)) {
            throw ExceptionFactory.invalidVotingSignature();
        }
        byte[] rewardAddressP = (byte[]) data.get(BigInteger.valueOf(CatalystDataIndexes.REWARD_ADDRESS.getValue()));
//need to revise
        Address rewardAddress = getAddressFromHexString(
                remove0xPrefix(HexUtil.encodeHexString(rewardAddressP))
        );
        if (rewardAddress == null) {
            throw ExceptionFactory.invalidAddressError();
        }
        BigInteger votingNonce = (BigInteger) data.get(BigInteger.valueOf(CatalystDataIndexes.VOTING_NONCE.getValue()));
        VoteRegistrationMetadata parsedMetadata = new VoteRegistrationMetadata(
                new PublicKey(remove0xPrefix(HexUtil.encodeHexString((byte[]) data.get(BigInteger.valueOf(CatalystDataIndexes.STAKE_KEY.getValue())))),
                        CurveType.EDWARDS25519.getValue()),
                new PublicKey(remove0xPrefix(HexUtil.encodeHexString((byte[]) data.get(BigInteger.valueOf(CatalystDataIndexes.VOTING_KEY.getValue())))),
                        CurveType.EDWARDS25519.getValue()),
                rewardAddress.toBech32(), votingNonce.intValue(),
                remove0xPrefix(HexUtil.encodeHexString((byte[]) sig.get(BigInteger.valueOf(
                        CatalystSigIndexes.VOTING_SIGNATURE.getValue())))
                ));

        return new Operation(
                new OperationIdentifier(index, null),
                OperationType.VOTE_REGISTRATION.getValue(),
                "",
                new OperationMetadata(parsedMetadata)
        );
    }

    @Override
    public String remove0xPrefix(String hex) {
        return (hex.startsWith("0x") ? hex.substring("0x".length()) : hex);
    }

    //need to revise
    @Override
    public Address getAddressFromHexString(String hex) {
        return new Address(hexStringToBuffer(hex));
    }

    @Override
    public void parseWithdrawalsToOperations(List<Operation> withdrawalOps, Integer withdrawalsCount,
                                             List<Operation> operations, Integer network) {
        log.info("[parseWithdrawalsToOperations] About to parse {} withdrawals", withdrawalsCount);
        for (int i = 0; i < withdrawalsCount; i++) {
            Operation withdrawalOperation = withdrawalOps.get(i);
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            HdPublicKey hdPublicKey = new HdPublicKey();
            hdPublicKey.setKeyData(HexUtil.decodeHexString(withdrawalOperation.getMetadata().getStakingCredential().getHexBytes()));
            String address = generateRewardAddress(
                    Objects.requireNonNull(NetworkIdentifierType.find(network)), hdPublicKey);
            Operation parsedOperation = parseWithdrawalToOperation(
                    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                    withdrawalOperation.getAmount().getValue(),
                    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                    withdrawalOperation.getMetadata().getStakingCredential().getHexBytes(),
                    withdrawalOperation.getOperationIdentifier().getIndex(),
                    address
            );
            operations.add(parsedOperation);
        }
    }

    @Override
    public Operation parseWithdrawalToOperation(String value, String hex, Long index,
                                                String address) {
        return new Operation(
                new OperationIdentifier(index, null),
                OperationType.WITHDRAWAL.getValue(),
                "",
                new AccountIdentifier(address),
                new Amount(value, new Currency(Constants.ADA, Constants.ADA_DECIMALS, null)),
                new OperationMetadata(new PublicKey(hex, CurveType.EDWARDS25519.getValue()))
        );
    }

    @Override
    public List<Operation> parseCertsToOperations(TransactionBody transactionBody,
                                                  List<Operation> certOps, int network)
            throws CborException, CborSerializationException {
        List<Operation> parsedOperations = new ArrayList<>();
        List<Certificate> certs = transactionBody.getCerts();
        int certsCount = getCertSize(certs);
        log.info("[parseCertsToOperations] About to parse {} certs", certsCount);

        for (int i = 0; i < certsCount; i++) {
            Operation certOperation = certOps.get(i);
            if (Constants.StakingOperations.contains(certOperation.getType())) {
                String hex = null;
                if (checkStakeCredential(certOperation)) {
                    hex = certOperation.getMetadata().getStakingCredential().getHexBytes();
                }
                validateHex(hex);
                HdPublicKey hdPublicKey = new HdPublicKey();
                hdPublicKey.setKeyData(HexUtil.decodeHexString(hex));
                String address = generateRewardAddress(
                        Objects.requireNonNull(NetworkIdentifierType.find(network)), hdPublicKey);
                Certificate cert = validateCert(certs,i);
                if (!ObjectUtils.isEmpty(cert)) {
                    Operation parsedOperation = parseCertToOperation(
                            cert,
                            certOperation.getOperationIdentifier().getIndex(),
                            hex,
                            certOperation.getType(),
                            address
                    );
                    parsedOperations.add(parsedOperation);
                }
            }else{
                Certificate cert = validateCert(certs,i);
                if (!ObjectUtils.isEmpty(cert)) {
                    Operation parsedOperation = parsePoolCertToOperation(
                            network,
                            cert,
                            certOperation.getOperationIdentifier().getIndex(),
                            certOperation.getType()
                    );
                    parsedOperation.setAccount(certOperation.getAccount());
                    parsedOperations.add(parsedOperation);
                }
            }

        }

        return parsedOperations;
    }
    public void validateHex(String hex){
        if (ObjectUtils.isEmpty(hex)) {
            log.error("[parseCertsToOperations] Staking key not provided");
            throw ExceptionFactory.missingStakingKeyError();
        }
    }
    public boolean checkStakeCredential(Operation certOperation){
        return certOperation.getMetadata() != null && certOperation.getMetadata().getStakingCredential() != null;
    }
    public int getCertSize(List<Certificate> certs){
        return ObjectUtils.isEmpty(certs) ? 0 : certs.size();
    }
    public Certificate validateCert(List<Certificate> certs,int i){
        return ObjectUtils.isEmpty(certs) ? null : certs.get(i);
    }
    @Override
    public Operation parsePoolCertToOperation(Integer network, Certificate cert, Long index,
                                              String type)
            throws CborSerializationException, CborException {
        Operation operation = new Operation(new OperationIdentifier(index, null), type, "", new OperationMetadata());

        if (type.equals(OperationType.POOL_RETIREMENT.getValue())) {
            PoolRetirement poolRetirementCert = (PoolRetirement) cert;
            if (!ObjectUtils.isEmpty(poolRetirementCert)) {
                operation.getMetadata().setEpoch(poolRetirementCert.getEpoch());
            }
        } else {
            PoolRegistration poolRegistrationCert = null;
            try {
                poolRegistrationCert = (PoolRegistration) cert;
            } catch (Exception e) {
                log.info("Not a PoolRegistration");
            }
            if (!ObjectUtils.isEmpty(poolRegistrationCert)) {
                if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
                    PoolRegistrationParams poolRegistrationParams = parsePoolRegistration(network, poolRegistrationCert);
                    operation.getMetadata().setPoolRegistrationParams(poolRegistrationParams);
                } else {
                    String parsedPoolCert = HexUtil.encodeHexString(
                            com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                                    poolRegistrationCert.serialize()));
                    operation.getMetadata().setPoolRegistrationCert(parsedPoolCert);
                }
            }
        }
        return operation;
    }

    @Override
    public PoolRegistrationParams parsePoolRegistration(Integer network,
                                                        PoolRegistration poolRegistration) {
        return new PoolRegistrationParams(
                HexUtil.encodeHexString(poolRegistration.getVrfKeyHash()),
                parsePoolRewardAccount(network, poolRegistration),
                poolRegistration.getPledge().toString(),
                poolRegistration.getCost().toString(),
                parsePoolOwners(network, poolRegistration),
                parsePoolRelays(poolRegistration),
                parsePoolMargin(poolRegistration), null,
                parsePoolMetadata(poolRegistration)
        );
    }

    @Override
    public PoolMetadata parsePoolMetadata(PoolRegistration poolRegistration) {
        if (poolRegistration.getPoolMetadataUrl() != null || poolRegistration.getPoolMetadataHash() != null) {
            return new PoolMetadata(poolRegistration.getPoolMetadataHash(),
                    poolRegistration.getPoolMetadataUrl());
        }
        return null;
    }

    @Override
    public PoolMargin parsePoolMargin(PoolRegistration poolRegistration) {
        return new PoolMargin(poolRegistration.getMargin().getDenominator().toString(),
                poolRegistration.getMargin().getNumerator().toString());
    }

    @Override
    public List<Relay> parsePoolRelays(PoolRegistration poolRegistration) {
        List<Relay> poolRelays = new ArrayList<>();
        List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> relays = poolRegistration.getRelays();
        for (com.bloxbean.cardano.client.transaction.spec.cert.Relay relay : relays) {
            MultiHostName multiHostRelay = getMultiHostRelay(relay);
            SingleHostName singleHostName = getSingleHostName(relay);
            SingleHostAddr singleHostAddr = getSingleHostAddr(relay);
            if (!ObjectUtils.isEmpty(multiHostRelay) || !ObjectUtils.isEmpty(singleHostName)) {
                addRelayToPoolReLayOfTypeMultiHostOrSingleHostName(poolRelays,multiHostRelay,singleHostName);
                continue;
            }
            if (!ObjectUtils.isEmpty(singleHostAddr)) {
                addRelayToPoolReLayOfTypeSingleHostAddr(poolRelays,singleHostAddr);
            }
        }
        return poolRelays;
    }
    public void addRelayToPoolReLayOfTypeSingleHostAddr(List<Relay> poolRelays,
                                                        SingleHostAddr singleHostAddr){
        Relay relay1 = new Relay(RelayType.SINGLE_HOST_ADDR.getValue(),
                singleHostAddr.getIpv4().getHostAddress(), singleHostAddr.getIpv6().getHostAddress(),
                null, String.valueOf(singleHostAddr.getPort()));
        poolRelays.add(relay1);
    }
    public void addRelayToPoolReLayOfTypeMultiHostOrSingleHostName(List<Relay> poolRelays,
                                                                   MultiHostName multiHostRelay,
                                                                   SingleHostName singleHostName){
        if (!ObjectUtils.isEmpty(multiHostRelay)) {
            poolRelays.add(
                    new Relay(RelayType.MULTI_HOST_NAME.getValue(), multiHostRelay.getDnsName()));
        }
        if (!ObjectUtils.isEmpty(singleHostName)) {
            poolRelays.add(
                    new Relay(RelayType.SINGLE_HOST_NAME.getValue(), singleHostName.getDnsName(),
                            ObjectUtils.isEmpty(singleHostName.getPort()) ? null
                                    : String.valueOf(singleHostName.getPort())));
        }
    }
    public MultiHostName getMultiHostRelay(
                                  com.bloxbean.cardano.client.transaction.spec.cert.Relay relay){
        try {
           return (MultiHostName) relay;
        } catch (Exception e) {
            log.info("not a MultiHostName");
            return null;
        }
    }
    public SingleHostName getSingleHostName(
                                  com.bloxbean.cardano.client.transaction.spec.cert.Relay relay){
        try {
            return (SingleHostName) relay;
        } catch (Exception e) {
            log.info("not a SingleHostName");
            return null;
        }
    }
    public SingleHostAddr getSingleHostAddr(
                                  com.bloxbean.cardano.client.transaction.spec.cert.Relay relay){
        try {
            return(SingleHostAddr) relay;
        } catch (Exception e) {
            log.info("not a SingleHostAddr");
            return null;
        }
    }
    @Override
    public List<String> parsePoolOwners(Integer network, PoolRegistration poolRegistration) {
        List<String> poolOwners = new ArrayList<>();
        Set<String> owners = poolRegistration.getPoolOwners();
        int ownersCount = owners.size();
        for (int i = 0; i < ownersCount; i++) {
            String owner = new ArrayList<>(owners).get(i);
            byte[] addressByte = new byte[29];
            addressByte[0] = -31;
            byte[] byteCop = HexUtil.decodeHexString(owner);
            System.arraycopy(byteCop, 0, addressByte, 1, addressByte.length - 1);
            Address address = new Address(addressByte);
            poolOwners.add(address.getAddress());
        }
        return poolOwners;
    }

    @Override
    public String parsePoolRewardAccount(Integer network, PoolRegistration poolRegistration) {
        byte[] addressByte = new byte[29];
        addressByte[0] = 97;
        byte[] byteCop = HexUtil.decodeHexString(poolRegistration.getRewardAccount().substring(2));
        System.arraycopy(byteCop, 0, addressByte, 1, addressByte.length - 1);
        return Bech32.encode(addressByte, "addr");
    }

    @Override
    public Operation parseCertToOperation(Certificate cert, Long index, String hash, String type,
                                          String address) {
        Operation operation = new Operation(new OperationIdentifier(index, null), null, type, "",
                new AccountIdentifier(address), null, null,
                new OperationMetadata(new PublicKey(hash, CurveType.EDWARDS25519.getValue())));
        StakeDelegation delegationCert = null;
        try {
            delegationCert = (StakeDelegation) cert;
        } catch (Exception e) {
            log.info("not a StakeDelegation");
        }
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        if (!ObjectUtils.isEmpty(delegationCert)) {
            operation.getMetadata().setPoolKeyHash(HexUtil.encodeHexString(delegationCert.getStakePoolId().getPoolKeyHash()));
        }
        return operation;
    }

    @Override
    public Operation parseOutputToOperation(TransactionOutput output, Long index,
                                            List<OperationIdentifier> relatedOperations, String address) {
        OperationIdentifier operationIdentifier = new OperationIdentifier(index, null);
        AccountIdentifier account = new AccountIdentifier(address);
        Amount amount = new Amount(output.getValue().getCoin().toString(),
                new Currency(Constants.ADA, Constants.ADA_DECIMALS, null), null);
        return new Operation(operationIdentifier, relatedOperations, OperationType.OUTPUT.getValue(),
                "",
                account, amount, null, parseTokenBundle(output));
    }

    @Override
    public OperationMetadata parseTokenBundle(TransactionOutput output) {
        List<MultiAsset> multiassets = output.getValue().getMultiAssets();
        List<TokenBundleItem> tokenBundle = new ArrayList<>();
        if (!ObjectUtils.isEmpty(multiassets)) {
            log.info("[parseTokenBundle] About to parse {} multiassets from token bundle",
                    multiassets.size());
            tokenBundle = multiassets.stream()
                    .map(key -> parseTokenAsset(multiassets, key.getPolicyId()))
                    .sorted(Comparator.comparing(TokenBundleItem::getPolicyId))
                    .toList();
        }

        return !ObjectUtils.isEmpty(multiassets) ? new OperationMetadata(tokenBundle) : null;
    }


    @Override
    public List<String> keys(List<Asset> collection) {
        List<String> keysArray = new ArrayList<>();
        for (Asset asset : collection) {
            keysArray.add(asset.getName());
        }
        return keysArray;
    }

    @Override
    public TokenBundleItem parseTokenAsset(List<MultiAsset> multiAssets, String policyId) {
        MultiAsset mergedMultiAssets = multiAssets.get(0);
        for (int i = 1; i < multiAssets.size(); i++) {
            mergedMultiAssets.plus(multiAssets.get(i));
        }
        if (ObjectUtils.isEmpty(mergedMultiAssets.getAssets())) {
            log.error("[parseTokenBundle] assets for policyId: {} not provided", policyId);
            throw ExceptionFactory.tokenBundleAssetsMissingError();
        }
        List<Amount> tokens = (keys(mergedMultiAssets.getAssets())).stream()
                .map(key -> {
                  try {
                    return parseAsset(mergedMultiAssets.getAssets(), key);
                  } catch (CborException e) {
                    throw ExceptionFactory.unspecifiedError(e.getMessage());
                  }
                })
                .sorted(Comparator.comparing(assetA -> assetA.getCurrency().getSymbol())).toList();
        return new TokenBundleItem(policyId, tokens);
    }

    @Override
    public Amount parseAsset(List<Asset> assets, String key) throws CborException {
// When getting the key we are obtaining a cbor encoded string instead of the actual name.
        // This might need to be changed in the serialization lib in the future
        for (Asset a : assets) {
            if (a.getName().startsWith("0x")) {
                a.setName(a.getName().substring(2));
                if (a.getName().equals("")) a.setName("\\x");
            }
        }
        if (key.startsWith("0x")) {
            key = key.substring(2);
        }
        if (key.equals("")) key = "\\x";
        String assetSymbol = key;
        AtomicLong assetValue = new AtomicLong();
        for (Asset a : assets) {
            if (a.getName().equals(key) && !ObjectUtils.isEmpty(a.getValue())) {
                assetValue.addAndGet(a.getValue().longValue());
            }
        }
        if (assetValue.get() == 0) {
            log.error("[parseTokenBundle] asset value for symbol: {} not provided", assetSymbol);
            throw ExceptionFactory.tokenAssetValueMissingError();
        }
        return mapAmount(assetValue.toString(), assetSymbol, 0, null);
    }

    @Override
    public String getAddressPrefix(Integer network, StakeAddressPrefix addressPrefix) {
        if (ObjectUtils.isEmpty(addressPrefix)) {
            return network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()
                    ? NonStakeAddressPrefix.MAIN.getValue() : NonStakeAddressPrefix.TEST.getValue();
        }
        return network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()
                ? StakeAddressPrefix.MAIN.getPrefix() : StakeAddressPrefix.TEST.getPrefix();
    }

    @Override
    public String parseAddress(String address, String addressPrefix) {
        return address;
    }

    @Override
    public List<OperationIdentifier> getRelatedOperationsFromInputs(List<Operation> inputs) {
        return inputs.stream()
                .map(input -> new OperationIdentifier(input.getOperationIdentifier().getIndex(), null))
                .toList();
    }

    @Override
    public Operation parseInputToOperation(TransactionInput input, Long index) {
        return new Operation(new OperationIdentifier(index, null), null, OperationType.INPUT.getValue(),
                "", null, null,
                new CoinChange(new CoinIdentifier(
                        hexFormatter(HexUtil.decodeHexString(input.getTransactionId())) + ":"
                                + input.getIndex()), CoinAction.SPENT.getValue()), null);
    }

    @Override
    public String getHashOfSignedTransaction(String signedTransaction) {
        try {
            log.info("[getHashOfSignedTransaction] About to hash signed transaction {}",
                    signedTransaction);
            byte[] signedTransactionBytes = HexUtil.decodeHexString(signedTransaction);
            log.info(
                    "[getHashOfSignedTransaction] About to parse transaction from signed transaction bytes");
            Transaction parsed = Transaction.deserialize(signedTransactionBytes);
            log.info("[getHashOfSignedTransaction] Returning transaction hash");
            TransactionBody body = parsed.getBody();
            byte[] hashBuffer;
            if (body == null ||
                    com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(body.serialize())
                            == null) {
                hashBuffer = null;
            } else {
                hashBuffer = Blake2bUtil.blake2bHash256(com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                        body.serialize()));
            }
            return hexFormatter(hashBuffer);
        } catch (Exception error) {
            log.error(error.getMessage()
                    + "[getHashOfSignedTransaction] There was an error parsing signed transaction");
            throw ExceptionFactory.parseSignedTransactionError();
        }
    }

    @Override
    public TransactionIdentifierResponse mapToConstructionHashResponse(String transactionHash) {
        return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash));
    }

    @Override
    public Set<String> validateAndParsePoolOwners(List<String> owners) {
        Set<String> parsedOwners = new HashSet<>();
        try {
            owners.forEach(owner -> {
                Address address = new Address(owner);
                Optional<byte[]> bytes = address.getDelegationHash();
                bytes.ifPresent(value -> parsedOwners.add(HexUtil.encodeHexString(value)));
            });
        } catch (Exception error) {
            log.error("[validateAndParsePoolOwners] there was an error parsing pool owners");
            throw ExceptionFactory.invalidPoolOwnersError(error.getMessage());
        }
        if (parsedOwners.size() != owners.size()) {
            throw ExceptionFactory.invalidPoolOwnersError("Invalid pool owners addresses provided");
        }
        return parsedOwners;
    }

    @Override
    public boolean isStakeAddress(String address) {
        return CardanoAddressUtils.isStakeAddress(address);
    }

    @Override
    public String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            int decimal =
                    aByte & 0xff;               // bytes widen to int, need mask, prevent sign extension
            // get last 8 bits
            String hex = Integer.toHexString(decimal);
            if (hex.length() % 2 == 1) {                    // if half hex, pad with zero, e.g \t
                hex = "0" + hex;
            }
            result.append(hex);
        }
        return result.toString();
    }
}
