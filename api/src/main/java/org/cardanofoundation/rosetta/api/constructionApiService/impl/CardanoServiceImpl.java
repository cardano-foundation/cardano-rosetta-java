package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.address.util.AddressUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.helper.JsonNoSchemaToMetadataConverter;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adabox.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.addedClass.*;
import org.cardanofoundation.rosetta.api.addedRepo.BlockRepository;
import org.cardanofoundation.rosetta.api.addedconsotant.Const;
import org.cardanofoundation.rosetta.api.addedenum.*;

import org.cardanofoundation.rosetta.api.constructionApiService.CardanoService;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CardanoServiceImpl implements CardanoService {
    @Autowired
    BlockRepository blockRepository;

    @Override
    public String generateAddress(NetworkIdentifierEnum networkIdentifierEnum, String publicKeyString, String stakingCredentialString, AddressType type) throws IllegalAccessException {
        log.info("[generateAddress] About to generate address from public key {} and network identifier {}", publicKeyString, networkIdentifierEnum);
        {
            byte[] pub = HexUtil.decodeHexString(publicKeyString);
            HdPublicKey paymentCredential = HdPublicKey.fromBytes(pub);

            if (type.getValue().equals(AddressType.REWARD.getValue())) {
                return generateRewardAddress(networkIdentifierEnum, paymentCredential);
            }

            if (type.getValue().equals(AddressType.BASE.getValue())) {
                if (stakingCredentialString != null) {
                    log.error("[constructionDerive] No staking key was provided for base address creation");
                    throw new IllegalArgumentException("missingStakingKeyError");
                }
                return generateBaseAddress(networkIdentifierEnum, paymentCredential, HdPublicKey.fromBytes(HexUtil.decodeHexString(stakingCredentialString)));
            }

            return generateEnterpriseAddress(paymentCredential, networkIdentifierEnum);
        }
    }

    @Override
    public String generateRewardAddress(NetworkIdentifierEnum networkIdentifierEnum, HdPublicKey paymentCredential) {
        log.info("[generateRewardAddress] Deriving cardano reward address from valid public staking key");
        Address rewardAddress = AddressProvider.getRewardAddress(paymentCredential, new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic()));
        log.info("[generateRewardAddress] reward address is ${bech32address}");
        return rewardAddress.toBech32();
    }

    @Override
    public String generateBaseAddress(NetworkIdentifierEnum networkIdentifierEnum, HdPublicKey paymentCredential, HdPublicKey stakingCredential) {
        log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
        Address baseAddress = AddressProvider.getBaseAddress(paymentCredential, stakingCredential, new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic()));
        log.info("generateAddress] base address is ${bech32address}");
        return baseAddress.toBech32();
    }

    @Override
    public String generateEnterpriseAddress(HdPublicKey paymentCredential, NetworkIdentifierEnum networkIdentifierEnum) {
        log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
        Address entAddress = AddressProvider.getEntAddress(paymentCredential, new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic()));
        log.info("generateAddress] base address is ${bech32address}");
        return entAddress.toBech32();
    }

    @Override
    public Double calculateRelativeTtl(Double relativeTtl) {
        if (relativeTtl != null) {
            return relativeTtl;
        } else {
            return Const.DEFAULT_RELATIVE_TTL;
        }
    }

    @Override
    public Double calculateTxSize(NetworkIdentifierEnum networkIdentifierEnum, ArrayList<Operation> operations, Integer ttl, DepositParameters depositParameters) throws IOException, AddressExcepion, CborSerializationException {
//            const{
//            bytes, addresses, metadata
//        }
        UnsignedTransaction unsignedTransaction = createUnsignedTransaction(
                networkIdentifierEnum,
                operations,
                ttl,
                !ObjectUtils.isEmpty(depositParameters) ? depositParameters : new DepositParameters(Const.DEFAULT_POOL_DEPOSIT, Const.DEFAULT_KEY_DEPOSIT)
        );
        // eslint-disable-next-line consistent-return
        List<AddedSignatures> addedSignaturesList = (unsignedTransaction.getAddresses()).stream().map(address -> {
            EraAddressType eraAddressType = this.getEraAddressType(address);
            if (eraAddressType != null) {
                return signatureProcessor(eraAddressType, null, address);
            }
            // since pool key hash are passed as address, ed25519 hashes must be included
            if (isEd25519KeyHash(address)) {
                return signatureProcessor(null, AddressType.POOL_KEY_HASH, address);
            }
            throw new IllegalArgumentException("invalidAddressError" + address);
        }).collect(Collectors.toList());
        String transaction = this.buildTransaction(unsignedTransaction.getBytes(), addedSignaturesList, unsignedTransaction.getMetadata());
        // eslint-disable-next-line no-magic-numbers
        return (double) (transaction.length() / 2); // transaction is returned as an hex string and we need size in bytes
    }

    @Override
    public String buildTransaction(String unsignedTransaction, List<AddedSignatures> addedSignaturesList, String transactionMetadata) {
        log.info("[buildTransaction] About to signed a transaction with {} signatures", addedSignaturesList.size());
        TransactionWitnessSet witnesses = getWitnessesForTransaction(addedSignaturesList);
        try {
            log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
            co.nstant.in.cbor.model.Map transactionBodyMap = new co.nstant.in.cbor.model.Map();
            transactionBodyMap.put(new UnicodeString("unsignedTransaction"), new UnicodeString(unsignedTransaction));
            TransactionBody transactionBody = TransactionBody.deserialize(transactionBodyMap);
            log.info("[buildTransaction] Creating transaction using transaction body and extracted witnesses");
            AuxiliaryData auxiliaryData = null;
            if (ObjectUtils.isEmpty(transactionMetadata)) {
                log.info("[buildTransaction] Adding transaction metadata");
                co.nstant.in.cbor.model.Map auxiliaryDataMap = new co.nstant.in.cbor.model.Map();
                auxiliaryDataMap.put(new UnicodeString("transactionMetadata"), new ByteString(hexStringToBuffer(transactionMetadata)));
                auxiliaryData = AuxiliaryData.deserialize(auxiliaryDataMap);
            }
            Transaction transaction = Transaction.builder().auxiliaryData(auxiliaryData).witnessSet(witnesses).build();
            transaction.setBody(transactionBody);
            return bytesToHex(transaction.serialize());
        } catch (Exception error) {
            log.error(error.getMessage() + "[buildTransaction] There was an error building signed transaction");
            throw new IllegalArgumentException("cantBuildSignedTransaction");
        }
    }

    @Override
    public TransactionWitnessSet getWitnessesForTransaction(List<AddedSignatures> addedSignaturesList) {
        try {
            TransactionWitnessSet witnesses = new TransactionWitnessSet();
            ArrayList<VkeyWitness> vkeyWitnesses = new ArrayList<>();
            ArrayList<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();
            log.info("[getWitnessesForTransaction] Extracting witnesses from signatures");
            addedSignaturesList.forEach(signature -> {
                VerificationKey vkey = null;
                try {
                    vkey = VerificationKey.create(HexUtil.decodeHexString(signature.getPublicKey()));
                } catch (CborSerializationException e) {
                    throw new RuntimeException(e);
                }
//      const ed25519Signature: Ed25519Signature = scope.manage(
//                    Ed25519Signature.from_bytes(Buffer.from(signature.signature, 'hex'))
//            );
                if (!ObjectUtils.isEmpty(signature.getAddress()) && getEraAddressTypeOrNull(signature.getAddress()) == EraAddressType.Byron) {
                    // byron case
                    if (ObjectUtils.isEmpty(signature.getChainCode())) {
                        log.error("[getWitnessesForTransaction] Missing chain code for byron address signature");
                        throw new IllegalArgumentException("missingChainCodeError");
                    }
                    String byronAddress = null;
                    try {
                        byronAddress = AddressUtil.bytesToBase58Address(HexUtil.decodeHexString(signature.getAddress()));
                    } catch (AddressExcepion e) {
                        throw new RuntimeException(e);
                    }
                    BootstrapWitness bootstrap = new BootstrapWitness(vkey.getBytes(), signature.getSignature().getBytes(), hexStringToBuffer(signature.getChainCode()), byronAddress.getBytes());
                    bootstrapWitnesses.add(bootstrap);
                } else {
                    vkeyWitnesses.add(new VkeyWitness(vkey.getBytes(), signature.getSignature().getBytes()));
                }
            });
            log.info("[getWitnessesForTransaction] {} witnesses were extracted to sign transaction", vkeyWitnesses.size());
            if (vkeyWitnesses.size() > 0) witnesses.setVkeyWitnesses(vkeyWitnesses);
            if (bootstrapWitnesses.size() > 0) witnesses.setBootstrapWitnesses(bootstrapWitnesses);
            return witnesses;
        } catch (Exception error) {
            log.error(error.getMessage() + "[getWitnessesForTransaction] There was an error building witnesses set for transaction");
            throw new IllegalArgumentException("cantBuildWitnessesSet");
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
    public boolean isEd25519KeyHash(String address) {
//        let edd25519Hash: CardanoWasm.Ed25519KeyHash;
//        try {
//            edd25519Hash = scope.manage(CardanoWasm.Ed25519KeyHash.from_bytes(Buffer.from(hash, 'hex')));
//        } catch (error) {
//            return false;
//        }
//        return !!edd25519Hash;
        return true;
    }

    @Override
    public AddedSignatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType, String address) {
        if (eraAddressType.equals(EraAddressType.Shelley))
            return new AddedSignatures(Const.SHELLEY_DUMMY_SIGNATURE, Const.SHELLEY_DUMMY_PUBKEY, null, address);
        if (eraAddressType.equals(EraAddressType.Byron))
            return new AddedSignatures(Const.BYRON_DUMMY_SIGNATURE, Const.BYRON_DUMMY_PUBKEY, Const.CHAIN_CODE_DUMMY, address);
        if (addressType.equals(AddressType.POOL_KEY_HASH.getValue()))
            return new AddedSignatures(Const.COLD_DUMMY_SIGNATURE, Const.COLD_DUMMY_PUBKEY, null, address);
        return null;
    }

    @Override
    public UnsignedTransaction createUnsignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, List<Operation> operations, Integer ttl, DepositParameters depositParameters) throws IOException, AddressExcepion, CborSerializationException {
        log.info("[createUnsignedTransaction] About to create an unsigned transaction with ${operations.length} operations");
        Map<String, Object> map = processOperations(networkIdentifierEnum, operations, depositParameters);

        log.info("[createUnsignedTransaction] About to create transaction body");
        List<TransactionInput> inputList = (List<TransactionInput>) map.get("transactionInputs");
        List<TransactionOutput> outputList = (List<TransactionOutput>) map.get("transactionOutputs");
        BigInteger fee = BigInteger.valueOf((Long) map.get("fee"));
        TransactionBody transactionBody = TransactionBody.builder().inputs(inputList).outputs(outputList).fee(fee).ttl(ttl.longValue()).build();

        if (ObjectUtils.isEmpty((AuxiliaryData) map.get("voteRegistrationMetadata"))) {
            log.info("[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
            AuxiliaryData auxiliaryData = (AuxiliaryData) map.get("voteRegistrationMetadata");
            transactionBody.setAuxiliaryDataHash(auxiliaryData.getAuxiliaryDataHash());
        }

        if (((List<Certificate>) map.get("certificates")).size() > 0)
            transactionBody.setCerts((List<Certificate>) map.get("certificates"));
        if (((List<Withdrawal>) map.get("withdrawals")).size() > 0)
            transactionBody.setWithdrawals((List<Withdrawal>) map.get("withdrawals"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        String transactionBytes = hexFormatter(CborSerializationUtil.serialize(transactionBody.serialize()));
        log.info("[createUnsignedTransaction] Hashing transaction body");
        String bodyHash = TransactionUtil.getTxHash(HexUtil.decodeHexString(transactionBytes));
        UnsignedTransaction toReturn = new UnsignedTransaction(hexFormatter(HexUtil.decodeHexString(bodyHash)), transactionBytes, (Set<String>) map.get("addresses"), null);
        if (!ObjectUtils.isEmpty((AuxiliaryData) map.get("voteRegistrationMetadata"))) {
            toReturn.setMetadata(hex(CborSerializationUtil.serialize(((AuxiliaryData) map.get("voteRegistrationMetadata")).serialize())));
        }
        log.info(toReturn + "[createUnsignedTransaction] Returning unsigned transaction, hash to sign and addresses that will sign hash");
        return toReturn;
    }

    @Override
    public Map<String, Object> processOperations(NetworkIdentifierEnum networkIdentifierEnum, List<Operation> operations, DepositParameters depositParameters) {
        log.info("[processOperations] About to calculate fee");
        ProcessOperationsResult result = convert(networkIdentifierEnum, operations);
        double refundsSum = result.getStakeKeyDeRegistrationsCount() * depositParameters.getKeyDeposit().longValue();
        double keyDepositsSum = result.getStakeKeyRegistrationsCount() * depositParameters.getKeyDeposit().longValue();
        double poolDepositsSum = result.getPoolRegistrationsCount() * depositParameters.getPoolDeposit().longValue();
        Map<String, Double> depositsSumMap = new HashMap<>();
        depositsSumMap.put("keyRefundsSum", refundsSum);
        depositsSumMap.put("keyDepositsSum", keyDepositsSum);
        depositsSumMap.put("poolDepositsSum", poolDepositsSum);

        long fee = calculateFee(result.getInputAmounts(), result.getOutputAmounts(), result.getWithdrawalAmounts(), depositsSumMap);
        log.info("[processOperations] Calculated fee:$ {fee}");
        Map<String, Object> map = new HashMap<>();
        map.put("transactionInputs", result.getTransactionInputs());
        map.put("transactionOutputs", result.getTransactionOutputs());
        map.put("certificates", result.getCertificates());
        map.put("withdrawals", result.getWithdrawals());
        map.put("addresses", new HashSet<String>().addAll(result.getAddresses()));
        map.put("fee", fee);
        map.put("voteRegistrationMetadata", result.getVoteRegistrationMetadata());
        return map;
    }

    @Override
    public Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts, ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap) {
        double inputsSum = 0;
        for (String i : inputAmounts) {
            inputsSum += Long.valueOf(i);
        }
        inputsSum *= -1;
        double outputsSum = 0;
        for (String i : outputAmounts) {
            outputsSum += Long.valueOf(i);
        }
        double withdrawalsSum = 0;
        for (Long i : withdrawalAmounts) {
            withdrawalsSum += Long.valueOf(i);
        }
        long fee = (long) (inputsSum + withdrawalsSum + depositsSumMap.get("keyRefundsSum") - outputsSum - depositsSumMap.get("keyDepositsSum") - depositsSumMap.get("poolDepositsSum"));
        if (fee < 0) {
            throw new IllegalArgumentException("outputsAreBiggerThanInputsError");
        }
        return fee;
    }

    @Override
    public ProcessOperationsResult convert(NetworkIdentifierEnum networkIdentifierEnum, List<Operation> operations) {
        ProcessOperationsResult result = new ProcessOperationsResult();


        operations.stream().forEach(operation -> {
            String type = operation.getType();
            ProcessOperationsResult processor = null;
            try {
                processor = operationProcessor(operation, networkIdentifierEnum, result, type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if (ObjectUtils.isEmpty(processor)) {
                log.error("[processOperations] Operation with id $ { operation.operation_identifier} has invalid type");
                throw new IllegalArgumentException("invalidOperationTypeError");
            }
        });
        return result;
    }

    @Override
    public ProcessOperationsResult operationProcessor(Operation operation, NetworkIdentifierEnum networkIdentifierEnum,
                                                      ProcessOperationsResult resultAccumulator, String type) throws JsonProcessingException {
        if (type.equals(OperationType.INPUT.getValue())) {
            resultAccumulator.getTransactionInputs().add(validateAndParseTransactionInput(operation));
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            resultAccumulator.getAddresses().add(ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            resultAccumulator.getInputAmounts().add(ObjectUtils.isEmpty(operation.getAmount()) ? null : operation.getAmount().getValue());
            return resultAccumulator;
        }
        if (type.equals(OperationType.OUTPUT.getValue())) {
            resultAccumulator.getTransactionOutputs().add(validateAndParseTransactionOutput(operation));
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            resultAccumulator.getOutputAmounts().add(ObjectUtils.isEmpty(operation.getAmount()) ? null : operation.getAmount().getValue());
            return resultAccumulator;
        }
        if (type.equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
            resultAccumulator.getCertificates().add(processStakeKeyRegistration(operation));
            double stakeNumber = resultAccumulator.getStakeKeyRegistrationsCount();
            resultAccumulator.setStakeKeyRegistrationsCount(stakeNumber++);
            return resultAccumulator;
        }
        if (type.equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue())) {
            Map<String, Object> map = processOperationCertification(networkIdentifierEnum, operation);
            resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
            resultAccumulator.getAddresses().add((String) map.get("address"));
            double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();
            resultAccumulator.setStakeKeyDeRegistrationsCount(stakeNumber++);
            return resultAccumulator;
        }
        if (type.equals(OperationType.STAKE_DELEGATION.getValue())) {
            Map<String, Object> map = processOperationCertification(networkIdentifierEnum, operation);
            resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
            resultAccumulator.getAddresses().add((String) map.get("address"));
            return resultAccumulator;
        }
        if (type.equals(OperationType.WITHDRAWAL.getValue())) {
            Map<String, Object> map = processWithdrawal(networkIdentifierEnum, operation);
            Long withdrawalAmount = ObjectUtils.isEmpty(operation.getAmount()) ? null : Long.valueOf(operation.getAmount().getValue());
            resultAccumulator.getWithdrawalAmounts().add(withdrawalAmount);
            resultAccumulator.getWithdrawals().add(new Withdrawal((String) map.get("address"), BigInteger.valueOf(withdrawalAmount)));
            resultAccumulator.getAddresses().add((String) map.get("address"));
            return resultAccumulator;
        }
        if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
            Map<String, Object> map = processPoolRegistration(networkIdentifierEnum, operation);
            resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
            resultAccumulator.getAddresses().addAll((Collection<? extends String>) map.get("totalAddresses"));
            double poolNumber = resultAccumulator.getPoolRegistrationsCount();
            resultAccumulator.setPoolRegistrationsCount(poolNumber++);
            return resultAccumulator;
        }
//        if (type.equals(OperationType.POOL_REGISTRATION_WITH_CERT.getValue())) {
//            const { certificate, addresses } = processPoolRegistrationWithCert(scope, logger, network, operation);
//            resultAccumulator.certificates.add(certificate);
//            resultAccumulator.addresses.push(...addresses);
//            resultAccumulator.poolRegistrationsCount++;
//            return resultAccumulator;
//        }
        if (type.equals(OperationType.POOL_RETIREMENT.getValue())) {
            Map<String, Object> map = processPoolRetirement(operation);
            resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
            resultAccumulator.getAddresses().add((String) map.get("poolKeyHash"));
            return resultAccumulator;
        }
        if (type.equals(OperationType.VOTE_REGISTRATION.getValue())) {
            AuxiliaryData voteRegistrationMetadata = processVoteRegistration(operation);
            resultAccumulator.setVoteRegistrationMetadata(voteRegistrationMetadata);
            return resultAccumulator;
        }
        return null;
    }

    @Override
    public AuxiliaryData processVoteRegistration(Operation operation) throws JsonProcessingException {
        log.info("[processVoteRegistration] About to process vote registration");
        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operation.getMetadata().getVoteRegistrationMetadata())) {
            log.error("[processVoteRegistration] Vote registration metadata was not provided");
            throw new IllegalArgumentException("missingVoteRegistrationMetadata");
        }
//const { , , , , votingSignature }
        Map<String, Object> map = validateAndParseVoteRegistrationMetadata(operation.getMetadata().getVoteRegistrationMetadata());
        HashMap<CatalystDataIndexes, Object> map2 = new HashMap<>();
        map2.put(CatalystDataIndexes.VOTING_KEY, map.get("votingKey"));
        map2.put(CatalystDataIndexes.REWARD_ADDRESS, map.get("stakeKey"));
        map2.put(CatalystDataIndexes.STAKE_KEY, map.get("rewardAddress"));
        map2.put(CatalystDataIndexes.VOTING_NONCE, map.get("votingNonce"));
        String json = new ObjectMapper().writeValueAsString(map2);
        Metadata registrationMetadata = JsonNoSchemaToMetadataConverter.jsonToCborMetadata(json);

        HashMap<CatalystSigIndexes, Object> map3 = new HashMap<>();
        map3.put(CatalystSigIndexes.VOTING_SIGNATURE, map.get("votingSignature"));
        String json2 = new ObjectMapper().writeValueAsString(map3);
        Metadata signatureMetadata = JsonNoSchemaToMetadataConverter.jsonToCborMetadata(json2);

        List<GeneralMetadata> generalMetadata = new ArrayList<>();
        Metadata mergeMetaData = registrationMetadata.merge(signatureMetadata);
//        generalMetadata.add(new GeneralMetadata(Long.valueOf(CatalystLabels.DATA.getValue()), registrationMetadata));
//        generalMetadata.add(new GeneralMetadata(Long.valueOf(CatalystLabels.SIG.getValue()), signatureMetadata));

        return AuxiliaryData.builder().metadata(mergeMetaData).build();
    }

    @Override
    public Map<String, Object> validateAndParseVoteRegistrationMetadata(VoteRegistrationMetadata voteRegistrationMetadata) {

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse voting key");
        HdPublicKey parsedVotingKey = validateAndParseVotingKey(voteRegistrationMetadata.getVotingKey());
        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse stake key");
        HdPublicKey parsedStakeKey = getPublicKey(voteRegistrationMetadata.getStakeKey());
        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse reward address");
        Address parsedAddress = validateAndParseRewardAddress(voteRegistrationMetadata.getRewardAddress());

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting nonce");
        if (voteRegistrationMetadata.getVotingNonce() <= 0) {
            log.error("[validateAndParseVoteRegistrationMetadata] Given voting nonce {} is invalid", voteRegistrationMetadata.getVotingNonce());
            throw new IllegalArgumentException("votingNonceNotValid");
        }

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting signature");
//        if (!isEd25519Signature(voteRegistrationMetadata.getVotingSignature())) {
//            log.error("[validateAndParseVoteRegistrationMetadata] Voting signature has an invalid format");
//            throw new IllegalArgumentException("invalidVotingSignature");
//        }

        String votingKeyHex = add0xPrefix(bytesToHex(parsedVotingKey.getBytes()));
        String stakeKeyHex = add0xPrefix(bytesToHex(parsedStakeKey.getBytes()));
        String rewardAddressHex = add0xPrefix(bytesToHex(HexUtil.decodeHexString(parsedAddress.getAddress())));
        String votingSignatureHex = add0xPrefix(voteRegistrationMetadata.getVotingSignature());
        HashMap<String, Object> map = new HashMap<>();
        map.put("votingKey", votingKeyHex);
        map.put("stakeKey", stakeKeyHex);
        map.put("rewardAddress", rewardAddressHex);
        map.put("votingNonce", voteRegistrationMetadata.getVotingNonce());
        map.put("votingSignature", votingSignatureHex);

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
        return (hex.startsWith("0x") ? hex : "0x" + hex);
    }

//    public boolean isEd25519Signature(String hash) {
////         SignatureSpi.Ed25519  ed25519Signature;
////        SignatureSpi.Ed25519
////        try {
////            ed25519Signature = scope.manage(CardanoWasm.Ed25519Signature.from_bytes(hexStringToBuffer(hash)));
////        } catch (Exception error) {
////            return false;
////        }
//        return true;
//    }

    @Override
    public HdPublicKey validateAndParseVotingKey(PublicKey votingKey) {
        if (ObjectUtils.isEmpty(votingKey.getHexBytes())) {
            log.error("[validateAndParsePublicKey] Voting key not provided");
            throw new IllegalArgumentException("missingVotingKeyError");
        }
        if (!isKeyValid(votingKey.getHexBytes(), votingKey.getCurveType())) {
            log.info("[validateAndParsePublicKey] Voting key has an invalid format");
            throw new IllegalArgumentException("invalidVotingKeyFormat");
        }
        byte[] publicKeyBuffer = hexStringToBuffer(votingKey.getHexBytes());
        return HdPublicKey.fromBytes(publicKeyBuffer);
    }

    @Override
    public Map<String, Object> processPoolRetirement(Operation operation) {
        Map<String, Object> map = new HashMap<>();
        log.info("[processPoolRetiring] About to process operation of type ${operation.type}");
        if (!ObjectUtils.isEmpty(operation.getMetadata()) && ObjectUtils.isEmpty(operation.getMetadata().getEpoch())
                && !ObjectUtils.isEmpty(operation.getAccount()) && ObjectUtils.isEmpty(operation.getAccount().getAddress())) {
            double epoch = operation.getMetadata().getEpoch();
            byte[] keyHash = validateAndParsePoolKeyHash(ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            map.put("certificate", new PoolRetirement(keyHash, Math.round(epoch)));
            map.put("poolKeyHash", ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            return map;
        }
        log.error("[processPoolRetiring] Epoch operation metadata is missing");
        throw new IllegalArgumentException("missingMetadataParametersForPoolRetirement");
    }

    @Override
    public byte[] validateAndParsePoolKeyHash(String poolKeyHash) {
        if (ObjectUtils.isEmpty(poolKeyHash)) {
            log.error("[validateAndParsePoolKeyHash] no pool key hash provided");
            throw new IllegalArgumentException("missingPoolKeyError");
        }
        byte[] parsedPoolKeyHash = null;
        try {
            parsedPoolKeyHash = HexUtil.decodeHexString(poolKeyHash);
        } catch (Exception error) {
            log.error("[validateAndParsePoolKeyHash] invalid pool key hash");
            throw new IllegalArgumentException("invalidPoolKeyError");
        }
        return parsedPoolKeyHash;
    }

    @Override
    public Map<String, Object> processPoolRegistration(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) {
        log.info("[processPoolRegistration] About to process pool registration operation");

        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata()) && ObjectUtils.isEmpty(operation.getMetadata().getPoolRegistrationParams())) {
            log.error("[processPoolRegistration] Pool_registration was not provided");
            throw new IllegalArgumentException("missingPoolRegistrationParameters");
        }
        PoolRegistrationParams poolRegistrationParams = ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getPoolRegistrationParams();

        Map<String, Object> map = validateAndParsePoolRegistationParameters(poolRegistrationParams);
        // eslint-disable-next-line camelcase
//  const poolKeyHash = validateAndParsePoolKeyHash(scope, logger, operation.account ?.address);

        log.info("[processPoolRegistration] About to validate and parse reward address");
        Address parsedAddress = validateAndParseRewardAddress(poolRegistrationParams.getRewardAddress());

        log.info("[processPoolRegistration] About to generate pool owners");
        Optional<byte[]> owners = parsedAddress.getPaymentKeyHash();
//                validateAndParsePoolOwners(poolRegistrationParams.getPoolOwners());

        log.info("[processPoolRegistration] About to generate pool relays");
        List<Relay> parsedRelays = validateAndParsePoolRelays(poolRegistrationParams.getRelays());

        log.info("[processPoolRegistration] About to generate pool metadata");
        PoolMetadata poolMetadata = validateAndParsePoolMetadata(poolRegistrationParams.getPoolMetadata());
        Set<String> set = new HashSet<>();
        set.add(HexUtil.encodeHexString(owners.get()));
        log.info("[processPoolRegistration] About to generate Pool Registration");
        PoolRegistration wasmPoolRegistration = PoolRegistration.builder()
                .operator(null)
                .vrfKeyHash(ObjectUtils.isEmpty(operation.getMetadata()) ? null : HexUtil.decodeHexString(operation.getMetadata().getPoolRegistrationParams().getVrfKeyHash()))
                .pledge((BigInteger) map.get("pledge"))
                .cost((BigInteger) map.get("cost"))
                .margin(new UnitInterval((BigInteger) map.get("numerator"), (BigInteger) map.get("denominator")))
                .rewardAccount(parsedAddress.getAddress())
                .poolOwners(set)
                .relays(parsedRelays)
                .poolMetadataUrl(poolMetadata.getUrl())
                .poolMetadataHash(poolMetadata.getHash())
                .build();
        log.info("[processPoolRegistration] Generating Pool Registration certificate");
        Certificate certificate = wasmPoolRegistration;
        log.info("[processPoolRegistration] Successfully created Pool Registration certificate");

        List<String> totalAddresses = new ArrayList<>();
        totalAddresses.addAll(set);
        totalAddresses.add(parsedAddress.getAddress());
        totalAddresses.add(operation.getAccount().getAddress());
        map.put("totalAddresses", totalAddresses);
        map.put("certificate", certificate);
        return map;
    }

    @Override
    public PoolMetadata validateAndParsePoolMetadata(PoolMetadata metadata) {
        PoolMetadata parsedMetadata = new PoolMetadata();
        try {
            if (!ObjectUtils.isEmpty(metadata))
                parsedMetadata = new PoolMetadata(metadata.getHash(), metadata.getUrl());
        } catch (Exception error) {
            log.error("[validateAndParsePoolMetadata] invalid pool metadata");
            throw new IllegalArgumentException("invalidPoolMetadataError");
        }
        return parsedMetadata;
    }

    @Override
    public List<Relay> validateAndParsePoolRelays(List<Relay1> relays) {
        if (relays.size() == 0) throw new IllegalArgumentException("invalidPoolRelaysError Empty relays received");
        List<Relay> generatedRelays = new ArrayList<>();
        for (Relay1 relay : relays) {
            if (!ObjectUtils.isEmpty(relay.getPort())) validatePort(relay.getPort());
            Relay generatedRelay = generateSpecificRelay(relay);
            generatedRelays.add(generatedRelay);
        }

        return generatedRelays;
    }

    @Override
    public Relay generateSpecificRelay(Relay1 relay) {
        try {
            switch (relay.getType()) {
                case "single_host_addr": {
                    return new SingleHostAddr(ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10)
                            , parseIpv4(relay.getIpv4()), parseIpv6(relay.getIpv6()));
                }
                case "single_host_name": {
                    if (ObjectUtils.isEmpty(relay.getDnsName()))
                        throw new IllegalArgumentException("missingDnsNameError");
                }
                return new SingleHostName(ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10), relay.getDnsName());

                case "multi_host_name":
                    if (ObjectUtils.isEmpty(relay.getDnsName())) {
                        throw new IllegalArgumentException("missingDnsNameError");
                    }
                    return new MultiHostName(relay.getDnsName());
                default: {
                    throw new IllegalArgumentException("invalidPoolRelayTypeError");
                }
            }
        } catch (Exception error) {
            log.error("[validateAndParsePoolRelays] invalid pool relay");
            throw new IllegalArgumentException("invalidPoolRelaysError");
        }
    }

    @Override
    public Inet4Address parseIpv4(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            byte[] parsedIp = HexUtil.decodeHexString(ip);
            return (Inet4Address) Inet4Address.getByAddress(parsedIp);
        }
        return null;
    }

    @Override
    public Inet6Address parseIpv6(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            byte[] parsedIp = HexUtil.decodeHexString(ip.replace("/:/g", ""));
            return (Inet6Address) Inet6Address.getByAddress(parsedIp);
        }
        return null;
    }

    @Override
    public void validatePort(String port) {
        Integer parsedPort = Integer.parseInt(port, 10);
        if (!port.matches(Const.IS_POSITIVE_NUMBER) || parsedPort == null) {
            log.error("[validateAndParsePort] Invalid port ${port} received");
            throw new IllegalArgumentException("invalidPoolRelaysError Invalid port ${port} received");
        }
    }

    @Override
    public Address validateAndParseRewardAddress(String rwrdAddress) {
        Address rewardAddress = null;
        try {
            rewardAddress = parseToRewardAddress(rwrdAddress);
        } catch (Exception error) {
            log.error("[validateAndParseRewardAddress] invalid reward address {}", rewardAddress);
            throw new IllegalArgumentException("invalidAddressError");
        }
        if (ObjectUtils.isEmpty(rewardAddress)) throw new IllegalArgumentException("invalidAddressError");
        return rewardAddress;
    }

    @Override
    public Address parseToRewardAddress(String address) {
//        const wasmAddress = AddressProvider.() scope.manage(CardanoWasm.Address.from_bech32(address));
//        return scope.manage(CardanoWasm.RewardAddress.from_address(wasmAddress));
        return new Address(address);
    }

    @Override
    public Map<String, Object> validateAndParsePoolRegistationParameters(PoolRegistrationParams poolRegistrationParameters) {
        HashMap<String, Object> map = new HashMap<>();
        String denominator = null;
        String numerator = null;
        if (!ObjectUtils.isEmpty(poolRegistrationParameters) && !ObjectUtils.isEmpty(poolRegistrationParameters.getMargin())) {
            denominator = poolRegistrationParameters.getMargin().getDenominator();
            numerator = poolRegistrationParameters.getMargin().getNumerator();
        }

        if (ObjectUtils.isEmpty(denominator) || ObjectUtils.isEmpty(numerator)) {
            log.error(
                    "[validateAndParsePoolRegistationParameters] Missing margin parameter at pool registration parameters"
            );
            throw new IllegalArgumentException("invalidPoolRegistrationParameters Missing margin parameter at pool registration parameters");
        }
        if (!poolRegistrationParameters.getCost().matches(Const.IS_POSITIVE_NUMBER) ||
                !poolRegistrationParameters.getPledge().matches(Const.IS_POSITIVE_NUMBER) ||
                !numerator.matches(Const.IS_POSITIVE_NUMBER) ||
                !denominator.matches(Const.IS_POSITIVE_NUMBER)) {
            log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");
            throw new IllegalArgumentException("invalidPoolRegistrationParameters Given value is invalid");
        }
        try {
            // eslint-disable-next-line unicorn/prevent-abbreviations
            map.put("cost", BigInteger.valueOf(Long.valueOf(poolRegistrationParameters.getCost())));
            map.put("pledge", BigInteger.valueOf(Long.valueOf(poolRegistrationParameters.getPledge())));
            map.put("numerator", BigInteger.valueOf(Long.valueOf(numerator)));
            map.put("denominator", BigInteger.valueOf(Long.valueOf(denominator)));

            return map;
        } catch (Exception error) {
            log.error("[validateAndParsePoolRegistationParameters] Given pool parameters are invalid");
            throw new IllegalArgumentException("invalidPoolRegistrationParameters" + error);
        }
    }

    @Override
    public Map<String, Object> processWithdrawal(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) {
        log.info("[processWithdrawal] About to process withdrawal");
        // eslint-disable-next-line camelcase
        StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getStakingCredential());
        String address = generateRewardAddress(networkIdentifierEnum, HdPublicKey.fromBytes(credential.getHash()));
        HashMap<String, Object> map = new HashMap<>();
        map.put("reward", AddressProvider.getRewardAddress(HdPublicKey.fromBytes(credential.getHash()), new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic())));
        map.put("address", address);
        return map;
    }

    @Override
    public Map<String, Object> processOperationCertification(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) {
        log.info("[processOperationCertification] About to process operation of type ${operation.type}");
        // eslint-disable-next-line camelcase
        HashMap<String, Object> map = new HashMap<>();
        StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getStakingCredential());
        String address = generateRewardAddress(networkIdentifierEnum, HdPublicKey.fromBytes(credential.getHash()));
        if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
            // eslint-disable-next-line camelcase
            Certificate certificate = new StakeDelegation(credential, new StakePoolId(ObjectUtils.isEmpty(operation.getMetadata()) ? null : HexUtil.decodeHexString(operation.getMetadata().getPoolKeyHash())));
            map.put("certificate", certificate);

            return map;
        }
        map.put("certificate", new StakeDeregistration(credential));
        map.put("address", address);
        return map;
    }

    @Override
    public Certificate processStakeKeyRegistration(Operation operation) {
        log.info("[processStakeKeyRegistration] About to process stake key registration");
        // eslint-disable-next-line camelcase
        StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getStakingCredential());
        return new StakeRegistration(credential);
    }

    @Override
    public StakeCredential getStakingCredentialFromHex(PublicKey staking_credential) {
        HdPublicKey stakingKey = getPublicKey(staking_credential);
        return StakeCredential.fromKeyHash(stakingKey.getKeyHash());
    }

    @Override
    public HdPublicKey getPublicKey(PublicKey publicKey) {
        if (ObjectUtils.isEmpty(publicKey) || ObjectUtils.isEmpty(publicKey.getHexBytes())) {
            log.error("[getPublicKey] Staking key not provided");
            throw new IllegalArgumentException("missingStakingKeyError");
        }
        if (!isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType())) {
            log.info("[getPublicKey] Staking key has an invalid format");
            throw new IllegalArgumentException("invalidStakingKeyFormat");
        }
        byte[] stakingKeyBuffer = hexStringToBuffer(publicKey.getHexBytes());
        return HdPublicKey.fromBytes(stakingKeyBuffer);
    }

    @Override
    public Boolean isKeyValid(String publicKeyBytes, String curveType) {
        return publicKeyBytes.length() == Const.PUBLIC_KEY_BYTES_LENGTH && curveType.equals(Const.VALID_CURVE_TYPE);
    }

    @Override
    public TransactionInput validateAndParseTransactionInput(Operation input) {
        if (ObjectUtils.isEmpty(input.getCoinChange())) {
            log.error("[validateAndParseTransactionInput] Input has missing coin_change");
            throw new IllegalArgumentException("transactionInputsParametersMissingError Input has missing coin_change field");
        }
        String transactionId = null;
        String index = null;
        if (ObjectUtils.isEmpty(input.getCoinChange())) {
            String[] array = input.getCoinChange().getCoinIdentifier().getIdentifier().split(":");
            transactionId = array[0];
            index = array[1];
        }
        if (ObjectUtils.isEmpty(transactionId) || ObjectUtils.isEmpty(index)) {
            log.error("[validateAndParseTransactionInput] Input has missing transactionId and index");
            throw new IllegalArgumentException("transactionInputsParametersMissingError Input has invalid coin_identifier field");
        }
        String value = ObjectUtils.isEmpty(input.getAmount()) ? null : input.getAmount().getValue();
        if (ObjectUtils.isEmpty(value)) {
            log.error("[validateAndParseTransactionInput] Input has missing amount value field");
            throw new IllegalArgumentException("transactionInputsParametersMissingError Input has missing amount value field");
        }
        if (!value.matches(Const.IS_POSITIVE_NUMBER)) {
            log.error("[validateAndParseTransactionInput] Input has positive value");
            throw new IllegalArgumentException("transactionInputsParametersMissingError Input has positive amount value");
        }
        try {
            return new TransactionInput(
                    TransactionUtil.getTxHash(HexUtil.decodeHexString(transactionId)),
                    Integer.parseInt(index));
        } catch (Exception error) {
            throw new IllegalArgumentException("There was an error deserializating transaction input: " + error);
        }
    }

    @Override
    public TransactionOutput validateAndParseTransactionOutput(Operation output) {
        Address address;
        try {
            address = ObjectUtils.isEmpty(output.getAccount()) ? null : generateAddress(output.getAccount().getAddress());
        } catch (Exception error) {
            throw new IllegalArgumentException("transactionOutputDeserializationError" + output.getAccount().getAddress() + error);
        }
        if (ObjectUtils.isEmpty(address)) {
            log.error("[validateAndParseTransactionOutput] Output has missing address field");
            throw new IllegalArgumentException("transactionOutputsParametersMissingError Output has missing address field");
        }
        String outputValue = ObjectUtils.isEmpty(output.getAmount()) ? null : output.getAmount().getValue();
        if (ObjectUtils.isEmpty(output.getAmount()) || outputValue == null) {
            log.error("[validateAndParseTransactionOutput] Output has missing amount value field");
            throw new IllegalArgumentException("transactionOutputsParametersMissingError Output has missing amount value field");
        }
        if (!outputValue.matches(Const.IS_POSITIVE_NUMBER)) {
            log.error("[validateAndParseTransactionOutput] Output has negative or invalid value {}", outputValue);
            throw new IllegalArgumentException("transactionOutputsParametersMissingError Output has negative amount value");
        }
        Value value = Value.builder().coin(BigInteger.valueOf(Long.valueOf(outputValue))).build();
        if (!ObjectUtils.isEmpty(output.getMetadata()) && ObjectUtils.isEmpty(output.getMetadata().getTokenBundle()))
            value.setMultiAssets(validateAndParseTokenBundle(output.getMetadata().getTokenBundle()));
        try {
            return new TransactionOutput(address.getAddress(), value);
        } catch (Exception error) {
            throw new IllegalArgumentException("transactionOutputDeserializationError Invalid input: " + output.getAccount().getAddress() + error);
        }
    }

    @Override
    public Address generateAddress(String address) throws AddressExcepion {
        EraAddressType addressType = getEraAddressType(address);

        if (addressType == EraAddressType.Byron) {
            String byronAddress = AddressUtil.bytesToBase58Address(HexUtil.decodeHexString(address));
            return new Address(byronAddress);
        }
        return new Address(AddressUtil.addressToBytes(address));
    }

    @Override
    public EraAddressType getEraAddressType(String address) {
        if (AddressUtil.isValidAddress(address)) {
            return EraAddressType.Byron;
        }
        return EraAddressType.Shelley;
    }

    @Override
    public List<MultiAsset> validateAndParseTokenBundle(List<TokenBundleItem> tokenBundle) {
        List<MultiAsset> multiAssets = new ArrayList<>();
        tokenBundle.stream().forEach(tokenBundleItem -> {
            if (!isPolicyIdValid(tokenBundleItem.getPolicyId())) {
                log.error("[validateAndParseTokenBundle] PolicyId {} is not valid", tokenBundleItem.getPolicyId());
                throw new IllegalArgumentException("transactionOutputsParametersMissingError PolicyId " + tokenBundleItem.getPolicyId() + "is not valid");
            }
            List<Asset> assets = new ArrayList<>();
            tokenBundleItem.getTokens().stream().forEach(token -> {
                if (!isTokenNameValid(token.getCurrency().getSymbol())) {
                    log.error("validateAndParseTokenBundle] Token name {} is not valid", token.getCurrency().getSymbol());
                    throw new IllegalArgumentException("transactionOutputsParametersMissingError Token name " + token.getCurrency().getSymbol() + "is not valid");
                }
                String assetName = token.getCurrency().getSymbol();
                if (assets.stream().anyMatch(asset -> !ObjectUtils.isEmpty(asset.getName()))) {
                    log.error("[validateAndParseTokenBundle] Token name {} has already been added for policy {}", token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
                    throw new IllegalArgumentException("transactionOutputsParametersMissingError Token name " + token.getCurrency().getSymbol() + " has already been added for policy " + tokenBundleItem.getPolicyId() + "and will be overriden");
                }
                if (ObjectUtils.isEmpty(token.getValue()) || ObjectUtils.isEmpty(token.getValue().charAt(0))) {
                    log.error("[validateAndParseTokenBundle] Token with name {} for policy {} has no value or is empty", token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
                    throw new IllegalArgumentException("Token with name" + token.getCurrency().getSymbol() + "for policy" + tokenBundleItem.getPolicyId() + "has no value or is empty");
                }
                if (!token.getValue().matches(Const.IS_POSITIVE_NUMBER)) {
                    log.error("[validateAndParseTokenBundle] Asset {} has negative or invalid value {}", token.getCurrency().getSymbol(), token.getValue());
                    throw new IllegalArgumentException("transactionOutputsParametersMissingError Asset" + token.getCurrency().getSymbol() + "has negative or invalid value " + token.getValue());
                }
                assets.add(new Asset(token.getCurrency().getSymbol(), BigInteger.valueOf(Long.valueOf(token.getValue()))));
            });
            multiAssets.add(new MultiAsset(tokenBundleItem.getPolicyId(), assets));
        });
        return multiAssets;
    }

    @Override
    public Boolean isPolicyIdValid(String policyId) {
        return policyId.matches(Const.PolicyId_Validation);
    }

    @Override
    public Boolean isTokenNameValid(String name) {
        return name.matches(Const.Token_Name_Validation) || isEmptyHexString(name);
    }

    @Override
    public Boolean isEmptyHexString(String toCheck) {
        return toCheck.equals(Const.EMPTY_HEX);
    }

    @Override
    public byte[] hexStringToBuffer(String input) {
        return isEmptyHexString(input) ? HexUtil.decodeHexString("") : HexUtil.decodeHexString(input);
    }

    @Override
    public NetworkIdentifierEnum getNetworkIdentifierByRequestParameters(NetworkIdentifier networkRequestParameters) {
        if (networkRequestParameters.getNetwork().equals(Const.MAINNET)) {
            return NetworkIdentifierEnum.CARDANO_MAINNET_NETWORK;
        }
        return NetworkIdentifierEnum.CARDANO_MAINNET_NETWORK;
    }

    @Override
    public boolean isAddressTypeValid(String type) {
        return Arrays.stream(AddressType.values()).anyMatch(a -> a.getValue().equals(type)) || type.equals("") || type == null;
    }

    @Override
    public Amount mapAmount(String value, String symbol, Integer decimals, AddedMetadata addedMetadata) {
        return new Amount(value, new Currency(ObjectUtils.isEmpty(symbol) ? Const.ADA : hexStringFormatter(symbol),
                ObjectUtils.isEmpty(decimals) ? Const.ADA_DECIMALS : decimals, addedMetadata), null);
    }

    @Override
    public String hexStringFormatter(String toFormat) {
        if (ObjectUtils.isEmpty(toFormat)) return Const.EMPTY_HEX;
        else return toFormat;
    }

    @Override
    public Long calculateTxMinimumFee(Long transactionSize, ProtocolParametersResponse protocolParameters) {
        return protocolParameters.getMinFeeCoefficient() * transactionSize + protocolParameters.getMinFeeConstant();
    }

    @Override
    public ProtocolParametersResponse getProtocolParameters() {
        log.debug("[getLinearFeeParameters] About to run findProtocolParameters query");
        ProtocolParametersResponse protocolParametersResponse = blockRepository.findProtocolParameters();
        return protocolParametersResponse;
    }

    @Override
    public Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborSerializationException, CborException {
        return previousTxSize + com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(new UnsignedInteger(updatedTtl)).length -
                com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(new UnsignedInteger(previousTtl)).length;
    }

    @Override
    public Long calculateTtl(Long ttlOffset) {
        BlockResponse latestBlock = getLatestBlock();
        return latestBlock.getSlotNo() + ttlOffset;
    }

    @Override
    public BlockResponse getLatestBlock() {
        log.info("[getLatestBlock] About to look for latest block");
        Long latestBlockNumber = findLatestBlockNumber();
        log.info("[getLatestBlock] Latest block number is ${latestBlockNumber}");
        BlockResponse latestBlock = blockRepository.findBlock(latestBlockNumber, null);
        if (ObjectUtils.isEmpty(latestBlock)) {
            log.error("[getLatestBlock] Latest block not found");
            throw new IllegalArgumentException("blockNotFoundError");
        }
        log.debug(latestBlock + "[getLatestBlock] Returning latest block");
        return latestBlock;
    }

    @Override
    public Long findLatestBlockNumber() {
        log.debug("[findLatestBlockNumber] About to run findLatestBlockNumber query");
        Long latestBlockNumber = blockRepository.findLatestBlockNumber();
        log.debug("[findLatestBlockNumber] Latest block number is {}", latestBlockNumber);
        return latestBlockNumber;
    }

    @Override
    public BlockResponse findBlock(Long blockNumber, String blockHash) {
        BlockResponse result = blockRepository.findBlock(blockNumber, blockHash);
        log.debug("[findBlock] Parameters received for run query blockNumber: {}, blockHash: {}", blockNumber, blockHash);
        if (!ObjectUtils.isEmpty(result)) {
            log.debug("[findBlock] Block found!");
            return result;
        }
        log.debug("[findBlock] No block was found");
        return null;
    }

    @Override
    public String encodeExtraData(String transaction, TransactionExtraData extraData) throws JsonProcessingException, CborSerializationException, CborException {
        List<Operation> extraOperations = extraData.getOperations().stream()
                // eslint-disable-next-line camelcase
                .filter(operation -> {
                            return ObjectUtils.isEmpty(operation.getCoinChange()) ? null : operation.getCoinChange().getCoinAction().equals(Const.COIN_SPENT_ACTION) ||
                                    Const.StakingOperations.contains(operation.getType()) ||
                                    Const.PoolOperations.contains(operation.getType()) ||
                                    Const.VoteOperations.contains(operation.getType());
                        }

                ).collect(Collectors.toList());
        TransactionExtraData toEncode = new TransactionExtraData();
        toEncode.setOperations(extraOperations);
        if (!ObjectUtils.isEmpty(extraData.getTransactionMetadataHex())) {
            toEncode.setTransactionMetadataHex(extraData.getTransactionMetadataHex());
        }
        co.nstant.in.cbor.model.Map transactionExtraDataMap = new co.nstant.in.cbor.model.Map();
        Array operationArray = new Array();
        extraData.getOperations().stream().forEach(operation -> {
            co.nstant.in.cbor.model.Map operationMap = new co.nstant.in.cbor.model.Map();
            co.nstant.in.cbor.model.Map operationIdentifierMap = new co.nstant.in.cbor.model.Map();
            operationIdentifierMap.put(new UnicodeString("index"), new UnsignedInteger(operation.getOperationIdentifier().getIndex()));
            operationIdentifierMap.put(new UnicodeString("networkIndex"), new UnsignedInteger(operation.getOperationIdentifier().getNetworkIndex()));
            Array rOperationArray = new Array();
            operation.getRelatedOperations().stream().forEach(rOperation -> {
                co.nstant.in.cbor.model.Map operationIdentifierMapnew = new co.nstant.in.cbor.model.Map();
                operationIdentifierMapnew.put(new UnicodeString("index"), new UnsignedInteger(operation.getOperationIdentifier().getIndex()));
                operationIdentifierMapnew.put(new UnicodeString("networkIndex"), new UnsignedInteger(operation.getOperationIdentifier().getNetworkIndex()));
                rOperationArray.add(operationIdentifierMapnew);
            });
            co.nstant.in.cbor.model.Map accountIdentifierMap = new co.nstant.in.cbor.model.Map();
            accountIdentifierMap.put(new UnicodeString("address"), new UnicodeString(operation.getAccount().getAddress()));
            co.nstant.in.cbor.model.Map subAccountIdentifierMap = new co.nstant.in.cbor.model.Map();
            subAccountIdentifierMap.put(new UnicodeString("address"), new UnicodeString(operation.getAccount().getSubAccount().getAddress()));
            subAccountIdentifierMap.put(new UnicodeString("metadata"), operation.getAccount().getSubAccount().getMetadata());
            co.nstant.in.cbor.model.Map accIdMetadataMap = new co.nstant.in.cbor.model.Map();
            accIdMetadataMap.put(new UnicodeString("chainCode"), new UnicodeString(operation.getAccount().getMetadata().getChainCode()));
            accountIdentifierMap.put(new UnicodeString("subAccount"), subAccountIdentifierMap);
            accountIdentifierMap.put(new UnicodeString("metadata"), accIdMetadataMap);
            co.nstant.in.cbor.model.Map amountMap = getAmountMap(operation.getAmount());
            co.nstant.in.cbor.model.Map coinChangeMap = new co.nstant.in.cbor.model.Map();
            co.nstant.in.cbor.model.Map coinIdentifierMap = new co.nstant.in.cbor.model.Map();
            coinIdentifierMap.put(new UnicodeString("identifier"), new UnicodeString(operation.getCoinChange().getCoinIdentifier().getIdentifier()));
            coinChangeMap.put(new UnicodeString("coinIdentifier"), coinIdentifierMap);
            coinChangeMap.put(new UnicodeString("coinAction"), new UnicodeString(operation.getCoinChange().getCoinAction()));
            co.nstant.in.cbor.model.Map oMetadataMap = new co.nstant.in.cbor.model.Map();
            co.nstant.in.cbor.model.Map withdrawalAmount = getAmountMap(operation.getMetadata().getWithdrawalAmount());
            co.nstant.in.cbor.model.Map depositAmount = getAmountMap(operation.getMetadata().getDepositAmount());
            co.nstant.in.cbor.model.Map refundAmount = getAmountMap(operation.getMetadata().getRefundAmount());
            oMetadataMap.put(new UnicodeString("withdrawalAmount"), withdrawalAmount);
            oMetadataMap.put(new UnicodeString("depositAmount"), depositAmount);
            oMetadataMap.put(new UnicodeString("refundAmount"), refundAmount);
            co.nstant.in.cbor.model.Map stakingCredentialMap = getPublicKeymap(operation.getMetadata().getStakingCredential());
            oMetadataMap.put(new UnicodeString("stakingCredential"), stakingCredentialMap);
            oMetadataMap.put(new UnicodeString("poolKeyHash"), new UnicodeString(operation.getMetadata().getPoolKeyHash()));
            oMetadataMap.put(new UnicodeString("epoch"), new DoublePrecisionFloat(operation.getMetadata().getEpoch()));
            Array tokenBundleArray = new Array();
            operation.getMetadata().getTokenBundle().stream().forEach(tokenbundle -> {
                co.nstant.in.cbor.model.Map tokenBundleItemMap = new co.nstant.in.cbor.model.Map();
                tokenBundleItemMap.put(new UnicodeString("policyId"), new UnicodeString(tokenbundle.getPolicyId()));
                Array tokensArray = new Array();
                tokenbundle.getTokens().stream().forEach(amount -> {
                    co.nstant.in.cbor.model.Map amountMapNext = getAmountMap(amount);
                    tokensArray.add(amountMapNext);
                });
                tokenBundleItemMap.put(new UnicodeString("tokens"), tokensArray);
                tokenBundleArray.add(tokenBundleItemMap);
            });
            oMetadataMap.put(new UnicodeString("tokenBundle"), tokenBundleArray);
            oMetadataMap.put(new UnicodeString("poolRegistrationCert"), new UnicodeString(operation.getMetadata().getPoolRegistrationCert()));
            co.nstant.in.cbor.model.Map poolRegistrationParamsMap = new co.nstant.in.cbor.model.Map();
            poolRegistrationParamsMap.put(new UnicodeString("vrfKeyHash"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getVrfKeyHash()));
            poolRegistrationParamsMap.put(new UnicodeString("rewardAddress"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getRewardAddress()));
            poolRegistrationParamsMap.put(new UnicodeString("pledge"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getPledge()));
            poolRegistrationParamsMap.put(new UnicodeString("cost"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getCost()));
            Array poolOwnersArray = new Array();
            operation.getMetadata().getPoolRegistrationParams().getPoolOwners().stream().forEach(o -> {
                DataItem dataItem = new UnicodeString(o);
                poolOwnersArray.add(dataItem);
            });
            poolRegistrationParamsMap.put(new UnicodeString("poolOwners"), poolOwnersArray);
            Array relaysArray = new Array();
            operation.getMetadata().getPoolRegistrationParams().getRelays().stream().forEach(r -> {
                co.nstant.in.cbor.model.Map relayMap = new co.nstant.in.cbor.model.Map();
                relayMap.put(new UnicodeString("type"), new UnicodeString(r.getType()));
                relayMap.put(new UnicodeString("ipv4"), new UnicodeString(r.getIpv4()));
                relayMap.put(new UnicodeString("ipv6"), new UnicodeString(r.getIpv6()));
                relayMap.put(new UnicodeString("dnsName"), new UnicodeString(r.getDnsName()));
                relayMap.put(new UnicodeString("port"), new UnicodeString(r.getPort()));
                relaysArray.add(relayMap);
            });
            poolRegistrationParamsMap.put(new UnicodeString("relays"), relaysArray);
            co.nstant.in.cbor.model.Map marginMap = new co.nstant.in.cbor.model.Map();
            marginMap.put(new UnicodeString("numerator"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getMargin().getNumerator()));
            marginMap.put(new UnicodeString("denominator"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getMargin().getDenominator()));
            poolRegistrationParamsMap.put(new UnicodeString("margin"), marginMap);
            poolRegistrationParamsMap.put(new UnicodeString("marginPercentage"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getMarginPercentage()));
            co.nstant.in.cbor.model.Map poolMetadataMap = new co.nstant.in.cbor.model.Map();
            marginMap.put(new UnicodeString("url"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getPoolMetadata().getUrl()));
            marginMap.put(new UnicodeString("hash"), new UnicodeString(operation.getMetadata().getPoolRegistrationParams().getPoolMetadata().getHash()));
            poolRegistrationParamsMap.put(new UnicodeString("poolMetadata"), poolMetadataMap);
            oMetadataMap.put(new UnicodeString("poolRegistrationParams"), poolRegistrationParamsMap);
            co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = new co.nstant.in.cbor.model.Map();
            co.nstant.in.cbor.model.Map stakeKeyMap = getPublicKeymap(operation.getMetadata().getVoteRegistrationMetadata().getStakeKey());
            voteRegistrationMetadataMap.put(new UnicodeString("stakeKey"), stakeKeyMap);
            co.nstant.in.cbor.model.Map votingKeyMap = getPublicKeymap(operation.getMetadata().getVoteRegistrationMetadata().getVotingKey());
            voteRegistrationMetadataMap.put(new UnicodeString("votingKey"), votingKeyMap);
            voteRegistrationMetadataMap.put(new UnicodeString("rewardAddress"), new UnicodeString(operation.getMetadata().getVoteRegistrationMetadata().getRewardAddress()));
            voteRegistrationMetadataMap.put(new UnicodeString("votingNonce"), new DoublePrecisionFloat(operation.getMetadata().getVoteRegistrationMetadata().getVotingNonce()));
            voteRegistrationMetadataMap.put(new UnicodeString("votingSignature"), new UnicodeString(operation.getMetadata().getVoteRegistrationMetadata().getVotingSignature()));
            oMetadataMap.put(new UnicodeString("voteRegistrationMetadata"), voteRegistrationMetadataMap);
            operationMap.put(new UnicodeString("operationIdentifier"), operationIdentifierMap);
            operationMap.put(new UnicodeString("relatedOperations"), rOperationArray);
            operationMap.put(new UnicodeString("type"), new UnicodeString(operation.getType()));
            operationMap.put(new UnicodeString("status"), new UnicodeString(operation.getStatus()));
            operationMap.put(new UnicodeString("account"), accountIdentifierMap);
            operationMap.put(new UnicodeString("amount"), amountMap);
            operationMap.put(new UnicodeString("coinChange"), coinChangeMap);
            operationMap.put(new UnicodeString("metadata"), oMetadataMap);
            operationArray.add(operationMap);
        });
        transactionExtraDataMap.put(new UnicodeString("operations"), operationArray);
        transactionExtraDataMap.put(new UnicodeString("transactionMetadataHex"), new UnicodeString(toEncode.getTransactionMetadataHex()));
        co.nstant.in.cbor.model.Map outputMap = new co.nstant.in.cbor.model.Map();
        outputMap.put(new UnicodeString("transaction"), new UnicodeString(transaction));
        outputMap.put(new UnicodeString("extraData"), transactionExtraDataMap);
        return HexUtil.encodeHexString(com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(outputMap));

    }

    @Override
    public co.nstant.in.cbor.model.Map decodeExtraData(String encoded) {
        co.nstant.in.cbor.model.Map map = (co.nstant.in.cbor.model.Map) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(HexUtil.decodeHexString(encoded));
        return map;
    }

    @Override
    public co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey) {
        co.nstant.in.cbor.model.Map stakingCredentialMap = new co.nstant.in.cbor.model.Map();
        stakingCredentialMap.put(new UnicodeString("hexBytes"), new UnicodeString(publicKey.getHexBytes()));
        stakingCredentialMap.put(new UnicodeString("curveType"), new UnicodeString(publicKey.getCurveType()));
        return stakingCredentialMap;
    }

    @Override
    public co.nstant.in.cbor.model.Map getAmountMap(Amount amount) {
        co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
        amountMap.put(new UnicodeString("value"), new UnicodeString(amount.getValue()));
        co.nstant.in.cbor.model.Map currencyMap = new co.nstant.in.cbor.model.Map();
        currencyMap.put(new UnicodeString("symbol"), new UnicodeString(amount.getCurrency().getSymbol()));
        currencyMap.put(new UnicodeString("decimals"), new UnsignedInteger(amount.getCurrency().getDecimals()));
        co.nstant.in.cbor.model.Map addedMetadataMap = new co.nstant.in.cbor.model.Map();
        addedMetadataMap.put(new UnicodeString("metadata"), new UnicodeString(amount.getCurrency().getMetadata().getPolicyId()));
        currencyMap.put(new UnicodeString("metadata"), addedMetadataMap);
        amountMap.put(new UnicodeString("currency"), currencyMap);
        amountMap.put(new UnicodeString("metadata"), amount.getMetadata());
        return amountMap;
    }

    @Override
    public List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash, Set<String> addresses) {
        return addresses.stream().map(address -> new SigningPayload(null, new AccountIdentifier(address), transactionBodyHash, SignatureType.ED25519)).collect(Collectors.toList());

    }

    @Override
    public TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map) {
        //separator
        TransactionExtraData transactionExtraData = new TransactionExtraData();
        String transactionMetadataHex = ((UnicodeString) map.get(new UnicodeString("transactionMetadataHex"))).getString();
        transactionExtraData.setTransactionMetadataHex(transactionMetadataHex);
        List<Operation> operations = new ArrayList<>();
        List<DataItem> operationsListMap = ((Array) map.get(new UnicodeString("operations"))).getDataItems();
        operationsListMap.stream().forEach(oDataItem -> {
            co.nstant.in.cbor.model.Map operationMap = (co.nstant.in.cbor.model.Map) oDataItem;
            Operation operation = new Operation();
            co.nstant.in.cbor.model.Map operationIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(new UnicodeString("operationIdentifier"));
            OperationIdentifier operationIdentifier = new OperationIdentifier(
                    ((UnsignedInteger) operationIdentifierMap.get(new UnicodeString("index"))).getValue().longValue(),
                    ((UnsignedInteger) operationIdentifierMap.get(new UnicodeString("networkIndex"))).getValue().longValue()
            );
            operation.setOperationIdentifier(operationIdentifier);
            List<OperationIdentifier> relatedOperations = new ArrayList<>();
            List<DataItem> relatedOperationsDI = ((Array) operationMap.get(new UnicodeString("relatedOperations"))).getDataItems();
            relatedOperationsDI.stream().forEach(rDI -> {
                co.nstant.in.cbor.model.Map operationIdentifierMap2 = (co.nstant.in.cbor.model.Map) rDI;
                OperationIdentifier operationIdentifier2 = new OperationIdentifier(
                        ((UnsignedInteger) operationIdentifierMap2.get(new UnicodeString("index"))).getValue().longValue(),
                        ((UnsignedInteger) operationIdentifierMap2.get(new UnicodeString("networkIndex"))).getValue().longValue()
                );
                relatedOperations.add(operationIdentifier2);
            });
            operation.setRelatedOperations(relatedOperations);
            String type = ((UnicodeString) (operationMap.get(new UnicodeString("type")))).getString();
            String status = ((UnicodeString) (operationMap.get(new UnicodeString("status")))).getString();
            operation.setType(type);
            operation.setStatus(status);
            AccountIdentifier accountIdentifier = new AccountIdentifier();
            co.nstant.in.cbor.model.Map accountIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(new UnicodeString("account"));
            String address = ((UnicodeString) accountIdentifierMap.get(new UnicodeString("address"))).getString();
            accountIdentifier.setAddress(address);
            co.nstant.in.cbor.model.Map subAccountIdentifierMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(new UnicodeString("subAccount"));
            SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
            String addressSub = ((UnicodeString) (subAccountIdentifierMap.get(new UnicodeString("address")))).getString();
            co.nstant.in.cbor.model.Map metadataSub = (co.nstant.in.cbor.model.Map) (subAccountIdentifierMap.get(new UnicodeString("metadata")));
            subAccountIdentifier.setAddress(addressSub);
            subAccountIdentifier.setMetadata(metadataSub);
            accountIdentifier.setSubAccount(subAccountIdentifier);
            co.nstant.in.cbor.model.Map accountIdentifierMetadataMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(new UnicodeString("metadata"));
            AccountIdentifierMetadata accountIdentifierMetadata = new AccountIdentifierMetadata();
            String chainCode = ((UnicodeString) (accountIdentifierMetadataMap.get(new UnicodeString("chainCode")))).getString();
            accountIdentifierMetadata.setChainCode(chainCode);
            accountIdentifier.setMetadata(accountIdentifierMetadata);
            operation.setAccount(accountIdentifier);
            co.nstant.in.cbor.model.Map amountMap = (co.nstant.in.cbor.model.Map) operationMap.get(new UnicodeString("amount"));
            Amount amount = getAmountFromMap(amountMap);
            operation.setAmount(amount);
            co.nstant.in.cbor.model.Map coinChangeMap = (co.nstant.in.cbor.model.Map) operationMap.get(new UnicodeString("coinChange"));
            CoinChange coinChange = new CoinChange();
            String coinAction = ((UnicodeString) coinChangeMap.get(new UnicodeString("coinAction"))).getString();
            coinChange.setCoinAction(coinAction);
            CoinIdentifier coinIdentifier = new CoinIdentifier();
            co.nstant.in.cbor.model.Map coinIdentifierMap = (co.nstant.in.cbor.model.Map) coinChangeMap.get(new UnicodeString("coinIdentifier"));
            String identifier = ((UnicodeString) coinIdentifierMap.get(new UnicodeString("identifier"))).getString();
            coinIdentifier.setIdentifier(identifier);
            coinChange.setCoinIdentifier(coinIdentifier);
            operation.setCoinChange(coinChange);
            co.nstant.in.cbor.model.Map metadataMap = (co.nstant.in.cbor.model.Map) operationMap.get(new UnicodeString("metadata"));
            OperationMetadata operationMetadata = new OperationMetadata();
            co.nstant.in.cbor.model.Map withdrawalAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnicodeString("withdrawalAmount"));
            Amount amountW = getAmountFromMap(withdrawalAmountMap);
            operationMetadata.setWithdrawalAmount(amountW);
            co.nstant.in.cbor.model.Map depositAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnicodeString("withdrawalAmount"));
            Amount amountD = getAmountFromMap(depositAmountMap);
            operationMetadata.setWithdrawalAmount(amountD);
            co.nstant.in.cbor.model.Map refundAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnicodeString("withdrawalAmount"));
            Amount amountR = getAmountFromMap(refundAmountMap);
            operationMetadata.setWithdrawalAmount(amountR);
            co.nstant.in.cbor.model.Map stakingCredentialMap = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnicodeString("stakingCredential"));
            PublicKey publicKey = getPublicKeyFromMap(stakingCredentialMap);
            operationMetadata.setStakingCredential(publicKey);
            String poolKeyHash = ((UnicodeString) metadataMap.get(new UnicodeString("poolKeyHash"))).getString();
            operationMetadata.setPoolKeyHash(poolKeyHash);
            Double epoch = ((DoublePrecisionFloat) metadataMap.get(new UnicodeString("epoch"))).getValue();
            operationMetadata.setEpoch(epoch);
            List<DataItem> tokenBundleArray = ((Array) metadataMap.get(new UnicodeString("tokenBundle"))).getDataItems();
            List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
            tokenBundleArray.stream().forEach(t -> {
                co.nstant.in.cbor.model.Map tokenBundleMap = (co.nstant.in.cbor.model.Map) t;
                TokenBundleItem tokenBundleItem = new TokenBundleItem();
                String policyIdT = ((UnicodeString) tokenBundleMap.get(new UnicodeString("policyId"))).getString();
                tokenBundleItem.setPolicyId(policyIdT);
                List<DataItem> tokensItem = ((Array) tokenBundleMap.get(new UnicodeString("tokens"))).getDataItems();
                List<Amount> tokenAList = new ArrayList<>();
                tokensItem.stream().forEach(tk -> {
                    co.nstant.in.cbor.model.Map tokenAmountMap = (co.nstant.in.cbor.model.Map) tk;
                    Amount amount1 = getAmountFromMap(tokenAmountMap);
                    tokenAList.add(amount1);
                });
                tokenBundleItem.setTokens(tokenAList);
                tokenBundleItems.add(tokenBundleItem);
            });
            operationMetadata.setTokenBundle(tokenBundleItems);
            String poolRegistrationCert = ((UnicodeString) metadataMap.get(new UnicodeString("poolRegistrationCert"))).getString();
            operationMetadata.setPoolRegistrationCert(poolRegistrationCert);
            co.nstant.in.cbor.model.Map poolRegistrationParamsMap = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnicodeString("poolRegistrationParams"));
            PoolRegistrationParams poolRegistrationParams = new PoolRegistrationParams();
            String vrfKeyHash = ((UnicodeString) poolRegistrationParamsMap.get(new UnicodeString("vrfKeyHash"))).getString();
            poolRegistrationParams.setVrfKeyHash(vrfKeyHash);
            String rewardAddress = ((UnicodeString) poolRegistrationParamsMap.get(new UnicodeString("rewardAddress"))).getString();
            poolRegistrationParams.setRewardAddress(rewardAddress);
            String pledge = ((UnicodeString) poolRegistrationParamsMap.get(new UnicodeString("pledge"))).getString();
            poolRegistrationParams.setPledge(pledge);
            String cost = ((UnicodeString) poolRegistrationParamsMap.get(new UnicodeString("cost"))).getString();
            poolRegistrationParams.setCost(cost);
            List<String> stringList = new ArrayList<>();
            List<DataItem> poolOwners = ((Array) poolRegistrationParamsMap.get(new UnicodeString("poolOwners"))).getDataItems();
            poolOwners.stream().forEach(p -> {
                stringList.add(((UnicodeString) p).getString());
            });
            poolRegistrationParams.setPoolOwners(stringList);
            List<Relay1> relay1List = new ArrayList<>();
            List<DataItem> relaysArray = ((Array) poolRegistrationParamsMap.get(new UnicodeString("relays"))).getDataItems();
            relaysArray.stream().forEach(rA -> {
                co.nstant.in.cbor.model.Map rAMap = (co.nstant.in.cbor.model.Map) rA;
                Relay1 relay1 = new Relay1();
                String typeR = ((UnicodeString) rAMap.get(new UnicodeString("type"))).getString();
                relay1.setType(typeR);
                String ipv4 = ((UnicodeString) rAMap.get(new UnicodeString("ipv4"))).getString();
                relay1.setIpv4(ipv4);
                String ipv6 = ((UnicodeString) rAMap.get(new UnicodeString("ipv6"))).getString();
                relay1.setIpv6(ipv6);
                String dnsName = ((UnicodeString) rAMap.get(new UnicodeString("dnsName"))).getString();
                relay1.setDnsName(dnsName);
                relay1List.add(relay1);
            });
            poolRegistrationParams.setRelays(relay1List);
            co.nstant.in.cbor.model.Map marginMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(new UnicodeString("margin"));
            PoolMargin poolMargin = new PoolMargin();
            String numerator = ((UnicodeString) marginMap.get(new UnicodeString("numerator"))).getString();
            poolMargin.setNumerator(numerator);
            String denominator = ((UnicodeString) marginMap.get(new UnicodeString("denominator"))).getString();
            poolMargin.setNumerator(denominator);
            poolRegistrationParams.setMargin(poolMargin);
            String marginPercentage = ((UnicodeString) poolRegistrationParamsMap.get(new UnicodeString("marginPercentage"))).getString();
            poolRegistrationParams.setMarginPercentage(marginPercentage);
            PoolMetadata poolMetadata = new PoolMetadata();
            co.nstant.in.cbor.model.Map poolMetadataMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(new UnicodeString("poolMetadata"));
            String url = ((UnicodeString) poolMetadataMap.get(new UnicodeString("url"))).getString();
            poolMetadata.setUrl(url);
            String hash = ((UnicodeString) poolMetadataMap.get(new UnicodeString("hash"))).getString();
            poolMetadata.setHash(hash);
            poolRegistrationParams.setPoolMetadata(poolMetadata);
            operationMetadata.setPoolRegistrationParams(poolRegistrationParams);
            VoteRegistrationMetadata voteRegistrationMetadata = new VoteRegistrationMetadata();
            co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnicodeString("voteRegistrationMetadata"));
            co.nstant.in.cbor.model.Map stakeKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(new UnicodeString("stakeKey"));
            PublicKey publicKey1 = getPublicKeyFromMap(stakeKeyMap);
            voteRegistrationMetadata.setStakeKey(publicKey1);
            co.nstant.in.cbor.model.Map votingKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(new UnicodeString("votingKey"));
            ;
            PublicKey publicKey2 = getPublicKeyFromMap(votingKeyMap);
            voteRegistrationMetadata.setVotingKey(publicKey2);
            String rewardAddress2 = ((UnicodeString) voteRegistrationMetadataMap.get(new UnicodeString("rewardAddress"))).getString();
            voteRegistrationMetadata.setRewardAddress(rewardAddress2);
            String votingSignature = ((UnicodeString) voteRegistrationMetadataMap.get(new UnicodeString("votingSignature"))).getString();
            voteRegistrationMetadata.setVotingSignature(votingSignature);
            Double votingNonce = ((DoublePrecisionFloat) voteRegistrationMetadataMap.get(new UnicodeString("votingSignature"))).getValue();
            voteRegistrationMetadata.setVotingNonce(votingNonce);
            operationMetadata.setVoteRegistrationMetadata(voteRegistrationMetadata);
            operation.setMetadata(operationMetadata);
            operations.add(operation);
        });
        transactionExtraData.setOperations(operations);
        return transactionExtraData;
    }

    @Override
    public PublicKey getPublicKeyFromMap(co.nstant.in.cbor.model.Map stakingCredentialMap) {
        PublicKey publicKey = new PublicKey();
        String hexBytes = ((UnicodeString) stakingCredentialMap.get(new UnicodeString("hexBytes"))).getString();
        publicKey.setHexBytes(hexBytes);
        String curveType = ((UnicodeString) stakingCredentialMap.get(new UnicodeString("curveType"))).getString();
        publicKey.setHexBytes(curveType);
        return publicKey;
    }

    @Override
    public Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap) {
        Amount amount = new Amount();
        String value = ((UnicodeString) amountMap.get(new UnicodeString("value"))).getString();
        amount.setValue(value);
        co.nstant.in.cbor.model.Map metadataAm = (co.nstant.in.cbor.model.Map) amountMap.get(new UnicodeString("metadata"));
        amount.setMetadata(metadataAm);
        co.nstant.in.cbor.model.Map currencyMap = (co.nstant.in.cbor.model.Map) amountMap.get(new UnicodeString("currency"));
        Currency currency = new Currency();
        String symbol = ((UnicodeString) currencyMap.get(new UnicodeString("symbol"))).getString();
        currency.setSymbol(symbol);
        Integer decimals = ((UnsignedInteger) currencyMap.get(new UnicodeString("decimals"))).getValue().intValue();
        currency.setDecimals(decimals);
        AddedMetadata addedMetadata = new AddedMetadata();
        co.nstant.in.cbor.model.Map addedMetadataMap = (co.nstant.in.cbor.model.Map) currencyMap.get(new UnicodeString("metadata"));
        String policyId = ((UnicodeString) addedMetadataMap.get(new UnicodeString("policyId"))).getString();
        addedMetadata.setPolicyId(policyId);
        currency.setMetadata(addedMetadata);
        amount.setCurrency(currency);
        return amount;
    }

    @Override
    public TransactionParsed parseUnsignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, String transaction, TransactionExtraData extraData) throws CborDeserializationException, UnknownHostException, AddressExcepion, JsonProcessingException {
        try {
            log.info(transaction + "[parseUnsignedTransaction] About to create unsigned transaction from bytes");
            byte[] transactionBuffer = HexUtil.decodeHexString(transaction);
            co.nstant.in.cbor.model.Map transactionBodyMap = new co.nstant.in.cbor.model.Map();
            transactionBodyMap.put(new UnicodeString("transactionBuffer"), new ByteString(transactionBuffer));
            TransactionBody parsed = TransactionBody.deserialize(transactionBodyMap);
            log.info(extraData + "[parseUnsignedTransaction] About to parse operations from transaction body");
            List<Operation> operations = convert(parsed, extraData, networkIdentifierEnum.getValue());
            log.info(operations + "[parseUnsignedTransaction] Returning ${operations.length} operations");
            return new TransactionParsed(operations, new ArrayList<>());
        } catch (Exception error) {
            log.error(error + "[parseUnsignedTransaction] Cant instantiate unsigned transaction from transaction bytes");
            throw new IllegalArgumentException("cantCreateUnsignedTransactionFromBytes");
        }
    }

    @Override
    public TransactionParsed parseSignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, String transaction, TransactionExtraData extraData) {
        try {
            byte[] transactionBuffer = HexUtil.decodeHexString(transaction);
            log.info("[parseSignedTransaction] About to create signed transaction from bytes");
            Transaction parsed = Transaction.deserialize(transactionBuffer);
            log.info("[parseSignedTransaction] About to parse operations from transaction body");
            List<Operation> operations = convert(parsed.getBody(), extraData, networkIdentifierEnum.getValue());
            log.info("[parseSignedTransaction] About to get signatures from parsed transaction");
            log.info(operations + "[parseSignedTransaction] Returning operations");
            List<String> accum = new ArrayList<>();
            extraData.getOperations().stream().forEach(o ->
                    accum.addAll(getSignerFromOperation(networkIdentifierEnum, o))
            );
            List<AccountIdentifier> accountIdentifierSigners = getUniqueAccountIdentifiers(accum);
            return new TransactionParsed(operations, accountIdentifierSigners);
        } catch (Exception error) {
            log.error(error + "[parseSignedTransaction] Cant instantiate signed transaction from transaction bytes");
            throw new IllegalArgumentException("cantCreateSignedTransactionFromBytes");
        }
    }

    @Override
    public List<String> getSignerFromOperation(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) {
        if (Const.PoolOperations.contains(operation.getType())) {
            return getPoolSigners(networkIdentifierEnum, operation);
        }
        if (!ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress())) {
            return new ArrayList<>(List.of(operation.getAccount().getAddress()));
        }
        if (operation.getType().equals(OperationType.VOTE_REGISTRATION.getValue())) {
            return new ArrayList<>();
        }
        StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getStakingCredential());
        return new ArrayList<>(List.of(generateRewardAddress(networkIdentifierEnum, HdPublicKey.fromBytes(credential.getHash()))));
    }

    @Override
    public List<String> getPoolSigners(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) {
        List<String> signers = new ArrayList<>();
        switch (operation.getType()) {
            case "poolRegistration": {
                PoolRegistrationParams poolRegistrationParameters = ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getPoolRegistrationParams();
                if (!ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()))
                    signers.add(operation.getAccount().getAddress());
                if (!ObjectUtils.isEmpty(poolRegistrationParameters)) {
                    signers.add(poolRegistrationParameters.getRewardAddress());
                    signers.addAll(poolRegistrationParameters.getPoolOwners());
                }
                break;
            }
            case "poolRegistrationWithCert": {
                String poolCertAsHex = ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getPoolRegistrationCert();
                java.util.Map<String, Object> map = validateAndParsePoolRegistrationCert(
                        networkIdentifierEnum,
                        poolCertAsHex,
                        ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()
                );
                signers.addAll((List<String>) map.get("addresses"));
                break;
            }
            // pool retirement case
            default: {
                if (!ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()))
                    signers.add(operation.getAccount().getAddress());
                break;
            }
        }
        log.info("[getPoolSigners] About to return {} signers for {} operation", signers.size(), operation.getType());
        return signers;
    }

    @Override
    public java.util.Map<String, Object> validateAndParsePoolRegistrationCert(NetworkIdentifierEnum networkIdentifierEnum, String poolRegistrationCert, String poolKeyHash) {
        if (ObjectUtils.isEmpty(poolKeyHash)) {
            log.error("[validateAndParsePoolRegistrationCert] no cold key provided for pool registration");
            throw new IllegalArgumentException("missingPoolKeyError");
        }
        if (ObjectUtils.isEmpty(poolRegistrationCert)) {
            log.error(
                    "[validateAndParsePoolRegistrationCert] no pool registration certificate provided for pool registration"
            );
            throw new IllegalArgumentException("missingPoolCertError");
        }
        Certificate parsedCertificate;
        try {
            parsedCertificate = PoolRegistration.deserialize(new UnicodeString(poolRegistrationCert));
        } catch (Exception error) {
            log.error("[validateAndParsePoolRegistrationCert] invalid pool registration certificate");
            throw new IllegalArgumentException("invalidPoolRegistrationCert");
        }
        PoolRegistration poolRegistration = (PoolRegistration) parsedCertificate;
        if (ObjectUtils.isEmpty(poolRegistration)) {
            log.error("[validateAndParsePoolRegistrationCert] invalid certificate type");
            throw new IllegalArgumentException("invalidPoolRegistrationCertType");
        }
        List<String> ownersAddresses = parsePoolOwners(networkIdentifierEnum.getValue(), poolRegistration);
        String rewardAddress = parsePoolRewardAccount(networkIdentifierEnum.getValue(), poolRegistration);
        java.util.Map<String, Object> map = new HashMap<>();
        map.put("certificate", parsedCertificate);
        List<String> list = new ArrayList<>();
        list.addAll(ownersAddresses);
        list.add(poolKeyHash);
        list.add(rewardAddress);
        map.put("addresses", list);
        return map;
    }

    @Override
    public List<AccountIdentifier> getUniqueAccountIdentifiers(List<String> addresses) {
        return addressesToAccountIdentifiers(new HashSet<String>(addresses));
    }

    @Override
    public List<AccountIdentifier> addressesToAccountIdentifiers(Set<String> uniqueAddresses) {
        return uniqueAddresses.stream().map(a -> new AccountIdentifier(a)).collect(Collectors.toList());
    }

    @Override
    public List<Operation> convert(TransactionBody transactionBody, TransactionExtraData extraData, Integer network) throws UnknownHostException, JsonProcessingException, AddressExcepion, CborDeserializationException, CborException, CborSerializationException {
        List<Operation> operations = new ArrayList<>();
        List<TransactionInput> inputs = transactionBody.getInputs();
        List<TransactionOutput> outputs = transactionBody.getOutputs();
        log.info("[parseOperationsFromTransactionBody] About to parse ${inputs.len()} inputs");
        List<Operation> inputOperations = extraData.getOperations().stream().filter(o -> o.getType().equals(OperationType.INPUT.getValue())).collect(Collectors.toList());
        for (int i = 0; i < inputs.size(); i++) {
            TransactionInput input = inputs.get(i);
            Operation inputParsed = parseInputToOperation(input, (long) operations.size());
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
        }
        // till this line operations only contains inputs
        List<OperationIdentifier> relatedOperations = getRelatedOperationsFromInputs(operations);
        log.info("[parseOperationsFromTransactionBody] About to parse {} outputs", outputs.size());
        for (int i = 0; i < outputs.size(); i++) {
            TransactionOutput output = outputs.get(i);
            String address = parseAddress(output.getAddress(), getAddressPrefix(network, null));
            Operation outputParsed = parseOutputToOperation(output, (long) operations.size(), relatedOperations, address);
            operations.add(outputParsed);
        }

        List<Operation> certOps = extraData.getOperations().stream().filter(o -> Const.StakePoolOperations.contains(o)
        ).collect(Collectors.toList());
        List<Operation> parsedCertOperations = parseCertsToOperations(transactionBody, certOps, network);
        operations.addAll(parsedCertOperations);
        List<Operation> withdrawalOps = extraData.getOperations().stream().filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue())).collect(Collectors.toList());
        Integer withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0 : transactionBody.getWithdrawals().size();
        parseWithdrawalsToOperations(withdrawalOps, withdrawalsCount, operations, network);

        List<Operation> voteOp = extraData.getOperations().stream().filter(o -> o.getType().equals(OperationType.VOTE_REGISTRATION.getValue())).collect(Collectors.toList());
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
    public Operation parseVoteMetadataToOperation(Long index, String transactionMetadataHex) throws CborDeserializationException {
        log.info("[parseVoteMetadataToOperation] About to parse a vote registration operation");
        if (ObjectUtils.isEmpty(transactionMetadataHex)) {
            log.error("[parseVoteMetadataToOperation] Missing vote registration metadata");
            throw new IllegalArgumentException("missingVoteRegistrationMetadata");
        }
        co.nstant.in.cbor.model.Map map = new co.nstant.in.cbor.model.Map();
        map.put(new UnicodeString("transactionMetadataHex"), new UnicodeString(transactionMetadataHex));
        AuxiliaryData transactionMetadata = AuxiliaryData.deserialize(map);
        co.nstant.in.cbor.model.Map metadataMap = (co.nstant.in.cbor.model.Map) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(transactionMetadata.getAuxiliaryDataHash());
        co.nstant.in.cbor.model.Map generalMetadata = (co.nstant.in.cbor.model.Map) metadataMap.get(new UnsignedInteger(0));
        co.nstant.in.cbor.model.Map data = (co.nstant.in.cbor.model.Map) generalMetadata.get(new UnsignedInteger(Long.valueOf(CatalystLabels.DATA.getValue())));
        if (ObjectUtils.isEmpty(data)) throw new IllegalArgumentException("missingVoteRegistrationMetadata");
        co.nstant.in.cbor.model.Map sig = (co.nstant.in.cbor.model.Map) generalMetadata.get(new UnsignedInteger(Long.valueOf(CatalystLabels.SIG.getValue())));
        if (ObjectUtils.isEmpty(sig)) throw new IllegalArgumentException("invalidVotingSignature");
        String rewardAddressP = ((UnicodeString) data.get(new UnsignedInteger(CatalystDataIndexes.REWARD_ADDRESS.getValue()))).getString();
//need to revise
        Address rewardAddress = getAddressFromHexString(
                remove0xPrefix(rewardAddressP)
        );
        if (rewardAddress == null) throw new IllegalArgumentException("invalidAddressError");

        VoteRegistrationMetadata parsedMetadata = new VoteRegistrationMetadata(
                new PublicKey(remove0xPrefix(((UnicodeString) data.get(new UnsignedInteger(CatalystDataIndexes.VOTING_KEY.getValue()))).getString()), CurveType.EDWARDS25519.getValue()),
                new PublicKey(remove0xPrefix(((UnicodeString) data.get(new UnsignedInteger(CatalystDataIndexes.STAKE_KEY.getValue()))).getString()), CurveType.EDWARDS25519.getValue()),
                rewardAddress.toBech32(),
                ((DoublePrecisionFloat) data.get(new UnsignedInteger(CatalystDataIndexes.VOTING_NONCE.getValue()))).getValue(),
                remove0xPrefix(((UnicodeString) sig.get(new UnsignedInteger(CatalystSigIndexes.VOTING_SIGNATURE.getValue()))).getString())
        );

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
    public void parseWithdrawalsToOperations(List<Operation> withdrawalOps, Integer withdrawalsCount, List<Operation> operations, Integer network) {
        log.info("[parseWithdrawalsToOperations] About to parse {} withdrawals", withdrawalsCount);
        for (int i = 0; i < withdrawalsCount; i++) {
            Operation withdrawalOperation = withdrawalOps.get(i);
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            StakeCredential credential = getStakingCredentialFromHex(withdrawalOperation.getMetadata().getStakingCredential());
            String address = generateRewardAddress(NetworkIdentifierEnum.find(network), HdPublicKey.fromBytes(credential.getHash()));
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
    public Operation parseWithdrawalToOperation(String value, String hex, Long index, String address) {
        return new Operation(
                new OperationIdentifier(index, null),
                OperationType.WITHDRAWAL.getValue(),
                "",
                new AccountIdentifier(address),
                new Amount(value, new Currency(Const.ADA, Const.ADA_DECIMALS, null)),
                new OperationMetadata(new PublicKey(hex, CurveType.EDWARDS25519.getValue()))
        );
    }

    @Override
    public List<Operation> parseCertsToOperations(TransactionBody transactionBody, List<Operation> certOps, int network) throws UnknownHostException, JsonProcessingException, CborException, CborSerializationException {
        List<Operation> parsedOperations = new ArrayList<>();
        List<Certificate> certs = transactionBody.getCerts();
        Integer certsCount = ObjectUtils.isEmpty(certs) ? 0 : certs.size();
        log.info("[parseCertsToOperations] About to parse {} certs", certsCount);

        for (int i = 0; i < certsCount; i++) {
            Operation certOperation = certOps.get(i);
            if (Const.StakePoolOperations.contains(certOperation.getType())) {
                String hex = ObjectUtils.isEmpty(certOperation.getMetadata()) ? null : (ObjectUtils.isEmpty(certOperation.getMetadata().getStakingCredential()) ? null : certOperation.getMetadata().getStakingCredential().getHexBytes());
                if (!ObjectUtils.isEmpty(hex)) {
                    log.error("[parseCertsToOperations] Staking key not provided");
                    throw new IllegalArgumentException("missingStakingKeyError");
                }
                StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(certOperation.getMetadata()) ? null : certOperation.getMetadata().getStakingCredential());
                String address = generateRewardAddress(NetworkIdentifierEnum.find(network), HdPublicKey.fromBytes(credential.getHash()));
                Certificate cert = ObjectUtils.isEmpty(certs) ? null : certs.get(i);
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
            } else {
                Certificate cert = ObjectUtils.isEmpty(certs) ? null : certs.get(i);
                if (!ObjectUtils.isEmpty(cert)) {
                    Operation parsedOperation = parsePoolCertToOperation(
                            network,
                            cert,
                            certOperation.getOperationIdentifier().getIndex(),
                            certOperation.getType()
                    );
                    parsedOperation.setAccount(certOperation.getAccount());
                }
            }
        }

        return parsedOperations;
    }

    @Override
    public Operation parsePoolCertToOperation(Integer network, Certificate cert, Long index, String type) throws UnknownHostException, JsonProcessingException, CborSerializationException, CborException {
        Operation operation = new Operation(new OperationIdentifier(index, null), type, "", null);

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
                    operation.getMetadata().setPoolRegistrationParams(parsePoolRegistration(network, poolRegistrationCert));
                } else {
                    String parsedPoolCert = HexUtil.encodeHexString(com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(poolRegistrationCert.serialize()));
                    operation.getMetadata().setPoolRegistrationCert(parsedPoolCert);
                }
            }
        }
        return operation;
    }

    @Override
    public PoolRegistrationParams parsePoolRegistration(Integer network, PoolRegistration poolRegistration) throws UnknownHostException {
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
        return new PoolMetadata(poolRegistration.getPoolMetadataHash(), poolRegistration.getPoolMetadataUrl());
    }

    @Override
    public PoolMargin parsePoolMargin(PoolRegistration poolRegistration) {
        return new PoolMargin(poolRegistration.getMargin().getDenominator().toString(), poolRegistration.getMargin().getNumerator().toString());
    }

    @Override
    public List<Relay1> parsePoolRelays(PoolRegistration poolRegistration) throws UnknownHostException {
        List<Relay1> poolRelays = new ArrayList<>();
        List<Relay> relays = poolRegistration.getRelays();
        Integer relaysCount = relays.size();
        for (int i = 0; i < relaysCount; i++) {
            Relay relay = relays.get(i);
            MultiHostName multiHostRelay = null;
            SingleHostName singleHostName = null;
            SingleHostAddr singleHostAddr = null;
            try {
                multiHostRelay = (MultiHostName) relay;
            } catch (Exception e) {
                log.info("not a MultiHostName");
            }
            try {
                singleHostName = (SingleHostName) relay;
            } catch (Exception e) {
                log.info("not a SingleHostName");
            }
            try {
                singleHostAddr = (SingleHostAddr) relay;
            } catch (Exception e) {
                log.info("not a SingleHostAddr");
            }

            if (!ObjectUtils.isEmpty(multiHostRelay)) {
                poolRelays.add(new Relay1(RelayType.MULTI_HOST_NAME.getValue(), multiHostRelay.getDnsName()));
                continue;
            }
            if (!ObjectUtils.isEmpty(singleHostName)) {
                poolRelays.add(new Relay1(RelayType.SINGLE_HOST_NAME.getValue(), singleHostName.getDnsName(),
                        ObjectUtils.isEmpty(singleHostName.getPort()) ? null : String.valueOf(singleHostName.getPort())));
                continue;
            }
            if (!ObjectUtils.isEmpty(singleHostAddr)) {
                Inet4Address ipv4 = parseIpv4(singleHostAddr.getIpv4().getHostAddress());
                Inet6Address ipv6 = parseIpv6(singleHostAddr.getIpv6().getHostAddress());
                poolRelays.add(new Relay1(RelayType.SINGLE_HOST_ADDR.getValue(), String.valueOf(singleHostName.getPort())));
            }
        }
        return poolRelays;
    }

    @Override
    public List<String> parsePoolOwners(Integer network, PoolRegistration poolRegistration) {
        List<String> poolOwners = new ArrayList<>();
        Set<String> owners = poolRegistration.getPoolOwners();
        Integer ownersCount = owners.size();
        for (int i = 0; i < ownersCount; i++) {
            String owner = new ArrayList<String>(owners).get(i);
            String address = generateRewardAddress(
                    NetworkIdentifierEnum.find(network),
                    HdPublicKey.fromBytes(HexUtil.decodeHexString(owner))
            );
            poolOwners.add(address);
        }
        return poolOwners;
    }

    @Override
    public String parsePoolRewardAccount(Integer network, PoolRegistration poolRegistration) {
        return generateRewardAddress(NetworkIdentifierEnum.find(network),
                HdPublicKey.fromBytes(HexUtil.decodeHexString(poolRegistration.getRewardAccount())));
    }

    @Override
    public Operation parseCertToOperation(Certificate cert, Long index, String hash, String type, String address) {
        Operation operation = new Operation(new OperationIdentifier(index, null), null, type, "", new AccountIdentifier(address), null, null,
                new OperationMetadata(new PublicKey(hash, CurveType.EDWARDS25519.getValue())));
        StakeDelegation delegationCert = null;
        try {
            delegationCert = (StakeDelegation) cert;
        } catch (Exception e) {
            log.info("not a StakeDelegation");
        }
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        if (!ObjectUtils.isEmpty(delegationCert))
            operation.getMetadata().setPoolKeyHash(HexUtil.decodeHexString(HexUtil.encodeHexString(delegationCert.getStakePoolId().getPoolKeyHash())).toString());
        return operation;
    }

    @Override
    public Operation parseOutputToOperation(TransactionOutput output, Long index, List<OperationIdentifier> relatedOperations, String address) {
        OperationIdentifier operationIdentifier = new OperationIdentifier(index, null);
        AccountIdentifier account = new AccountIdentifier(address);
        Amount amount = new Amount(output.getValue().toString(), new Currency(Const.ADA, Const.ADA_DECIMALS, null), null);
        return new Operation(operationIdentifier, relatedOperations, OperationType.OUTPUT.getValue(), "",
                account, amount, null, parseTokenBundle(output));
    }

    @Override
    public OperationMetadata parseTokenBundle(TransactionOutput output) {
        List<MultiAsset> multiassets = output.getValue().getMultiAssets();
        List<TokenBundleItem> tokenBundle = new ArrayList<>();
        if (!ObjectUtils.isEmpty(multiassets)) {
            log.info("[parseTokenBundle] About to parse {} multiassets from token bundle", multiassets.size());
            tokenBundle = multiassets.stream()
                    .map(key -> parseTokenAsset(multiassets, key.getPolicyId()))
                    .sorted((tokenA, tokenB) -> tokenA.getPolicyId().compareTo(tokenB.getPolicyId())).collect(Collectors.toList());
        }

        return !ObjectUtils.isEmpty(multiassets) ? new OperationMetadata(tokenBundle) : null;
    }


    @Override
    public List<String> keys(List<Asset> collection) {
        List<String> keysArray = new ArrayList();
        for (int j = 0; j < collection.size(); j++) {
            keysArray.add(collection.get(j).getName());
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
            throw new IllegalArgumentException("tokenBundleAssetsMissingError");
        }
        List<Amount> tokens = (keys(mergedMultiAssets.getAssets())).stream()
                .map(key -> {
                    try {
                        return parseAsset(mergedMultiAssets.getAssets(), key);
                    } catch (CborException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted((assetA, assetB) -> assetA.getCurrency().getSymbol().compareTo(assetB.getCurrency().getSymbol())).collect(Collectors.toList());
        return new TokenBundleItem(policyId, tokens);
    }

    @Override
    public Amount parseAsset(List<Asset> assets, String key) throws CborException {
// When getting the key we are obtaining a cbor encoded string instead of the actual name.
        // This might need to be changed in the serialization lib in the future
        String assetSymbol = hexFormatter(com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(new ByteString(HexUtil.decodeHexString(key))));
        AtomicLong assetValue = new AtomicLong();
        assets.forEach(a -> {
            if (a.getName().equals(key) && !ObjectUtils.isEmpty(a.getValue())) {
                assetValue.addAndGet(a.getValue().longValue());
            }
        });
        if (assetValue.get() == 0) {
            log.error("[parseTokenBundle] asset value for symbol: {} not provided", assetSymbol);
            throw new IllegalArgumentException("tokenAssetValueMissingError");
        }
        return mapAmount(assetValue.toString(), assetSymbol, 0, null);
    }

    @Override
    public String getAddressPrefix(Integer network, StakeAddressPrefix addressPrefix) {
        if (ObjectUtils.isEmpty(addressPrefix)) {
            return network == NetworkIdentifierEnum.CARDANO_MAINNET_NETWORK.getValue() ? NonStakeAddressPrefix.MAIN.getValue() : NonStakeAddressPrefix.TEST.getValue();
        }
        return network == NetworkIdentifierEnum.CARDANO_MAINNET_NETWORK.getValue() ? addressPrefix.MAIN.getValue() : addressPrefix.TEST.getValue();
    }

    @Override
    public String parseAddress(String address, String addressPrefix) throws AddressExcepion {
        ByronAddress byronAddress = new ByronAddress(AddressUtil.bytesToBase58Address(HexUtil.decodeHexString(address)));
        return !ObjectUtils.isEmpty(byronAddress) ? byronAddress.toBase58() : (new Address(addressPrefix, HexUtil.decodeHexString(address))).toBech32();
    }

    @Override
    public List<OperationIdentifier> getRelatedOperationsFromInputs(List<Operation> inputs) {
        return inputs.stream().map(input -> new OperationIdentifier(input.getOperationIdentifier().getIndex(), null)).collect(Collectors.toList());
    }

    @Override
    public Operation parseInputToOperation(TransactionInput input, Long index) {
        return new Operation(new OperationIdentifier(index, null), null, OperationType.INPUT.getValue(), "", null, null,
                new CoinChange(new CoinIdentifier(hexFormatter(HexUtil.decodeHexString(input.getTransactionId())) + ":" + input.getIndex()), CoinAction.SPENT.getValue()), null);
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
            if (parsed == null || body == null || com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(body.serialize()) == null) {
                hashBuffer = null;
            } else {
                hashBuffer = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(body.serialize());
            }
            return hexFormatter(hashBuffer);
        } catch (Exception error) {
            log.error(error.getMessage() + "[getHashOfSignedTransaction] There was an error parsing signed transaction");
            throw new IllegalArgumentException("parseSignedTransactionError");
        }
    }

    @Override
    public TransactionIdentifierResponse mapToConstructionHashResponse(String transactionHash) {
        return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash));
    }
    @Override
    public String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            int decimal = (int) aByte & 0xff;               // bytes widen to int, need mask, prevent sign extension
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
