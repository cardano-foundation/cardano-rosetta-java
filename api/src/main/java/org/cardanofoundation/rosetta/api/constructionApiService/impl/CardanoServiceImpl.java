package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.util.AddressUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.helper.JsonNoSchemaToMetadataConverter;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.addedClass.AddedSignatures;
import org.cardanofoundation.rosetta.api.addedClass.GeneralMetadata;
import org.cardanofoundation.rosetta.api.addedClass.ProcessOperationsResult;
import org.cardanofoundation.rosetta.api.addedClass.UnsignedTransaction;
import org.cardanofoundation.rosetta.api.addedconsotant.Const;
import org.cardanofoundation.rosetta.api.addedenum.*;

import org.cardanofoundation.rosetta.api.model.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardanoServiceImpl {

    public String generateAddress(NetworkIdentifierEnum networkIdentifierEnum, String publicKeyString, String stakingCredentialString, AddressType type) throws IllegalAccessException {
        log.info("[generateAddress] About to generate address from public key {} and network identifier {}", publicKeyString, networkIdentifierEnum);
        {
            byte[] pub = publicKeyString.getBytes();
            HdPublicKey paymentCredential = HdPublicKey.fromBytes(pub);

            if (type.getValue().equals(AddressType.REWARD.getValue())) {
                return generateRewardAddress(networkIdentifierEnum, paymentCredential);
            }

            if (type.getValue().equals(AddressType.BASE.getValue())) {
                if (stakingCredentialString != null) {
                    log.error("[constructionDerive] No staking key was provided for base address creation");
                    throw new IllegalArgumentException("missingStakingKeyError");
                }
                StakeCredential stakingCredential = StakeCredential.fromKeyHash(stakingCredentialString.getBytes());

                return generateBaseAddress(networkIdentifierEnum, paymentCredential, HdPublicKey.fromBytes(stakingCredentialString.getBytes()));
            }

            return generateEnterpriseAddress(paymentCredential, networkIdentifierEnum);
        }
    }

    public String generateRewardAddress(NetworkIdentifierEnum networkIdentifierEnum, HdPublicKey paymentCredential) {
        log.info("[generateRewardAddress] Deriving cardano reward address from valid public staking key");
        Address rewardAddress = AddressProvider.getRewardAddress(paymentCredential, new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic()));
        log.info("[generateRewardAddress] reward address is ${bech32address}");
        return rewardAddress.toBech32();
    }

    public String generateBaseAddress(NetworkIdentifierEnum networkIdentifierEnum, HdPublicKey paymentCredential, HdPublicKey stakingCredential) {
        log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
        Address baseAddress = AddressProvider.getBaseAddress(paymentCredential, stakingCredential, new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic()));
        log.info("generateAddress] base address is ${bech32address}");
        return baseAddress.toBech32();
    }

    public String generateEnterpriseAddress(HdPublicKey paymentCredential, NetworkIdentifierEnum networkIdentifierEnum) {
        log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
        Address entAddress = AddressProvider.getEntAddress(paymentCredential, new Network(networkIdentifierEnum.getValue(), networkIdentifierEnum.getProtocolMagic()));
        log.info("generateAddress] base address is ${bech32address}");
        return entAddress.toBech32();
    }

    public Double calculateRelativeTtl(Double relativeTtl) {
        if (relativeTtl != null) {
            return relativeTtl;
        } else {
            return Const.DEFAULT_RELATIVE_TTL;
        }
    }

    public Double calculateTxSize(NetworkIdentifierEnum networkIdentifierEnum, ArrayList<Operation> operations, Double ttl, DepositParameters depositParameters) throws IOException {
//            const{
//            bytes, addresses, metadata
//        }
        UnsignedTransaction unsignedTransaction=createUnsignedTransaction(
                networkIdentifierEnum,
                operations,
                ttl,
                !ObjectUtils.isEmpty(depositParameters) ? depositParameters : new DepositParameters(Const.DEFAULT_POOL_DEPOSIT,Const.DEFAULT_KEY_DEPOSIT)
        );
        // eslint-disable-next-line consistent-return
     List<AddedSignatures> addedSignaturesList=(unsignedTransaction.getAddresses()).stream().map(address -> {
      EraAddressType eraAddressType = this.getEraAddressType(address);
        if (eraAddressType != null) {
            return signatureProcessor(eraAddressType,null,address);
        }
        // since pool key hash are passed as address, ed25519 hashes must be included
        if (isEd25519KeyHash(address)) {
            return signatureProcessor(null,AddressType.POOL_KEY_HASH,address);
        }
        throw new IllegalArgumentException("invalidAddressError"+address);
    }).collect(Collectors.toList());
    String transaction = this.buildTransaction(unsignedTransaction.getBytes(), addedSignaturesList, unsignedTransaction.getMetadata());
        // eslint-disable-next-line no-magic-numbers
        return (double) (transaction.length() / 2); // transaction is returned as an hex string and we need size in bytes
    }
    public String buildTransaction(String unsignedTransaction,List<AddedSignatures> addedSignaturesList,String transactionMetadata){
        log.info("[buildTransaction] About to signed a transaction with {} signatures",addedSignaturesList.size());
      TransactionWitnessSet witnesses = getWitnessesForTransaction(addedSignaturesList);
        try {
            log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
//        const transactionBody = scope.manage(
//                    CardanoWasm.TransactionBody.from_bytes(Buffer.from(unsignedTransaction, 'hex'))
//            );
            TransactionBody transactionBody=new TransactionBody();
            log.info("[buildTransaction] Creating transaction using transaction body and extracted witnesses");
            AuxiliaryData  auxiliaryData = null;
            if (ObjectUtils.isEmpty(transactionMetadata)) {
                log.info("[buildTransaction] Adding transaction metadata");
//                auxiliaryData = scope.manage(CardanoWasm.AuxiliaryData.from_bytes(hexStringToBuffer(transactionMetadata)));
                auxiliaryData=AuxiliaryData.builder().build();
            }
            Transaction transaction=Transaction.builder().auxiliaryData(auxiliaryData).witnessSet(witnesses).build();
            ObjectMapper objectMapper=new ObjectMapper();
            return bytesToHex(objectMapper.writeValueAsBytes(transaction));
//            Transaction transaction=
//                    CardanoWasm.Transaction.new(transactionBody, witnesses, auxiliaryData)
//        );
        } catch (Exception error) {
            log.error(error + "[buildTransaction] There was an error building signed transaction");
            throw new IllegalArgumentException("cantBuildSignedTransaction");
        }
    }
    public TransactionWitnessSet getWitnessesForTransaction(List<AddedSignatures> addedSignaturesList){
        try {
    TransactionWitnessSet witnesses = new TransactionWitnessSet();
    ArrayList<VkeyWitness> vkeyWitnesses = new ArrayList<>();
    ArrayList<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();
            log.info("[getWitnessesForTransaction] Extracting witnesses from signatures");
            addedSignaturesList.forEach(signature -> {
                VerificationKey vkey = null;
                try {
                    vkey = VerificationKey.create(signature.getPublicKey().getBytes());
                } catch (CborSerializationException e) {
                    throw new RuntimeException(e);
                }
//      const ed25519Signature: Ed25519Signature = scope.manage(
//                    Ed25519Signature.from_bytes(Buffer.from(signature.signature, 'hex'))
//            );
            if (!ObjectUtils.isEmpty(signature.getAddress()) && getEraAddressTypeOrNull(signature.getAddress()) ==EraAddressType.Byron) {
                // byron case
                if (ObjectUtils.isEmpty(signature.getChainCode())) {
                    log.error("[getWitnessesForTransaction] Missing chain code for byron address signature");
                    throw new IllegalArgumentException("missingChainCodeError");
                }
                String byronAddress = null;
                try {
                    byronAddress = AddressUtil.bytesToBase58Address(signature.getAddress().getBytes());
                } catch (AddressExcepion e) {
                    throw new RuntimeException(e);
                }
                BootstrapWitness bootstrap = new BootstrapWitness(vkey.getBytes(),signature.getSignature().getBytes(),hexStringToBuffer(signature.getChainCode()),byronAddress.getBytes());
                bootstrapWitnesses.add(bootstrap);
            } else {
                vkeyWitnesses.add(new VkeyWitness(vkey.getBytes(), signature.getSignature().getBytes()));
            }
    });
            log.info("[getWitnessesForTransaction] {} witnesses were extracted to sign transaction",vkeyWitnesses.size());
            if (vkeyWitnesses.size() > 0) witnesses.setVkeyWitnesses(vkeyWitnesses);
            if (bootstrapWitnesses.size() > 0) witnesses.setBootstrapWitnesses(bootstrapWitnesses);
            return witnesses;
        } catch (Exception error) {
            log.error(error+ "[getWitnessesForTransaction] There was an error building witnesses set for transaction");
            throw new IllegalArgumentException("cantBuildWitnessesSet");
        }
    }
    public EraAddressType getEraAddressTypeOrNull(String address){
        try {
            return getEraAddressType(address);
        } catch (Exception error) {
            return null;
        }
    }
    public boolean isEd25519KeyHash(String address){
//        let edd25519Hash: CardanoWasm.Ed25519KeyHash;
//        try {
//            edd25519Hash = scope.manage(CardanoWasm.Ed25519KeyHash.from_bytes(Buffer.from(hash, 'hex')));
//        } catch (error) {
//            return false;
//        }
//        return !!edd25519Hash;
        return true;
    }
    public AddedSignatures signatureProcessor(EraAddressType eraAddressType,AddressType addressType,String address){
        if(eraAddressType.equals(EraAddressType.Shelley))
            return new AddedSignatures(Const.SHELLEY_DUMMY_SIGNATURE,Const.SHELLEY_DUMMY_PUBKEY,null,address);
        if(eraAddressType.equals(EraAddressType.Byron))
            return new AddedSignatures(Const.BYRON_DUMMY_SIGNATURE,Const.BYRON_DUMMY_PUBKEY,Const.CHAIN_CODE_DUMMY,address);
        if(addressType.equals(AddressType.POOL_KEY_HASH.getValue()))
            return new AddedSignatures(Const.COLD_DUMMY_SIGNATURE,Const.COLD_DUMMY_PUBKEY,null,address);
        return null;
    }

    public UnsignedTransaction createUnsignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, ArrayList<Operation> operations, Double ttl, DepositParameters depositParameters) throws IOException {
        log.info("[createUnsignedTransaction] About to create an unsigned transaction with ${operations.length} operations");
        Map<String,Object> map=processOperations(networkIdentifierEnum, operations, depositParameters);

        log.info("[createUnsignedTransaction] About to create transaction body");
        List<TransactionInput> inputList= (List<TransactionInput>) map.get("transactionInputs");
        List<TransactionOutput> outputList=(List<TransactionOutput>)map.get("transactionOutputs");
        BigInteger fee=BigInteger.valueOf((Long) map.get("fee"));
        TransactionBody transactionBody = TransactionBody.builder().inputs(inputList).outputs(outputList).fee(fee).ttl(ttl.longValue()).build();

        if (ObjectUtils.isEmpty((AuxiliaryData)map.get("voteRegistrationMetadata"))) {
            log.info("[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
            AuxiliaryData auxiliaryData=(AuxiliaryData)map.get("voteRegistrationMetadata");
            transactionBody.setAuxiliaryDataHash(auxiliaryData.getAuxiliaryDataHash());
        }

        if (((List<Certificate>)map.get("certificates")).size() > 0) transactionBody.setCerts((List<Certificate>)map.get("certificates"));
        if (((List<Withdrawal>)map.get("withdrawals")).size() > 0) transactionBody.setWithdrawals((List<Withdrawal>)map.get("withdrawals"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        ObjectMapper OBJECT_MAPPER=new ObjectMapper();
      String transactionBytes = hexFormatter(OBJECT_MAPPER.writeValueAsBytes(transactionBody));
        log.info("[createUnsignedTransaction] Hashing transaction body");
      String bodyHash = TransactionUtil.getTxHash(transactionBytes.getBytes());
        UnsignedTransaction toReturn=new UnsignedTransaction(hexFormatter(bodyHash.getBytes()),transactionBytes, (Set<String>) map.get("addresses"),null);
        if (!ObjectUtils.isEmpty((AuxiliaryData)map.get("voteRegistrationMetadata"))) {
            toReturn.setMetadata(hex(OBJECT_MAPPER.writeValueAsBytes((AuxiliaryData)map.get("voteRegistrationMetadata"))));
        }
        log.info(toReturn+ "[createUnsignedTransaction] Returning unsigned transaction, hash to sign and addresses that will sign hash");
        return toReturn;
    }
    public static String hex(byte[] bytes) {
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
    public Map<String, Object> processOperations(NetworkIdentifierEnum networkIdentifierEnum, ArrayList<Operation> operations, DepositParameters depositParameters) {
        log.info("[processOperations] About to calculate fee");
        ProcessOperationsResult result = convert(networkIdentifierEnum, operations);
        double refundsSum = result.getStakeKeyDeRegistrationsCount() * Long.valueOf(depositParameters.getKeyDeposit());
        double keyDepositsSum = result.getStakeKeyRegistrationsCount() * Long.valueOf(depositParameters.getKeyDeposit());
        double poolDepositsSum = result.getPoolRegistrationsCount() * Long.valueOf(depositParameters.getPoolDeposit());
        Map<String, Double> depositsSumMap = new HashMap<>();
        depositsSumMap.put("keyRefundsSum", refundsSum);
        depositsSumMap.put("keyDepositsSum", keyDepositsSum);
        depositsSumMap.put("poolDepositsSum", poolDepositsSum);

long fee = calculateFee(result.getInputAmounts(), result.getOutputAmounts(), result.getWithdrawalAmounts(), depositsSumMap);
        log.info("[processOperations] Calculated fee:$ {fee}");
        Map<String,Object> map=new HashMap<>();
        map.put("transactionInputs",result.getTransactionInputs());
        map.put("transactionOutputs",result.getTransactionOutputs());
        map.put("certificates",result.getCertificates());
        map.put("withdrawals",result.getWithdrawals());
        map.put("addresses",new HashSet<String>().addAll(result.getAddresses()));
        map.put("fee",fee);
        map.put("voteRegistrationMetadata",result.getVoteRegistrationMetadata());
        return map;
    }

    public Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts, ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap) {
        double inputsSum = 0;
        for(String i:inputAmounts){
            inputsSum += Long.valueOf(i);
        }
        inputsSum *= -1;
        double outputsSum = 0;
        for(String i:outputAmounts){
            outputsSum += Long.valueOf(i);
        }
        double withdrawalsSum = 0;
        for(Long i:withdrawalAmounts){
            withdrawalsSum += Long.valueOf(i);
        }
        long fee = (long) (inputsSum + withdrawalsSum + depositsSumMap.get("keyRefundsSum") - outputsSum - depositsSumMap.get("keyDepositsSum") - depositsSumMap.get("poolDepositsSum"));
        if (fee < 0) {
            throw new IllegalArgumentException("outputsAreBiggerThanInputsError");
        }
        return fee;
    }

    public ProcessOperationsResult convert(NetworkIdentifierEnum networkIdentifierEnum, ArrayList<Operation> operations) {
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

    public Map<String, Object> validateAndParseVoteRegistrationMetadata(VoteRegistrationMetadata voteRegistrationMetadata) {

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse voting key");
        HdPublicKey parsedVotingKey = validateAndParseVotingKey(voteRegistrationMetadata.getVotingKey());
        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse stake key");
        HdPublicKey parsedStakeKey = getPublicKey(voteRegistrationMetadata.getStakeKey());
        log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse reward address");
        Address parsedAddress = validateAndParseRewardAddress(voteRegistrationMetadata.getRewardAddress());

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting nonce");
        if (voteRegistrationMetadata.getVotingNonce().longValue() <= 0) {
            log.error("[validateAndParseVoteRegistrationMetadata] Given voting nonce {} is invalid", voteRegistrationMetadata.getVotingNonce().longValue());
            throw new IllegalArgumentException("votingNonceNotValid");
        }

        log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting signature");
        if (!isEd25519Signature(voteRegistrationMetadata.getVotingSignature())) {
            log.error("[validateAndParseVoteRegistrationMetadata] Voting signature has an invalid format");
            throw new IllegalArgumentException("invalidVotingSignature");
        }

        String votingKeyHex = add0xPrefix(bytesToHex(parsedVotingKey.getBytes()));
        String stakeKeyHex = add0xPrefix(bytesToHex(parsedStakeKey.getBytes()));
        String rewardAddressHex = add0xPrefix(bytesToHex(parsedAddress.getAddress().getBytes()));
        String votingSignatureHex = add0xPrefix(voteRegistrationMetadata.getVotingSignature());
        HashMap<String, Object> map = new HashMap<>();
        map.put("votingKey", votingKeyHex);
        map.put("stakeKey", stakeKeyHex);
        map.put("rewardAddress", rewardAddressHex);
        map.put("votingNonce", voteRegistrationMetadata.getVotingNonce());
        map.put("votingSignature", votingSignatureHex);

        return map;
    }

    public String bytesToHex(byte[] bytes) {
        return hexFormatter(bytes);
    }

    public String hexFormatter(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String add0xPrefix(String hex) {
        return (hex.startsWith("0x") ? hex : "0x" + hex);
    }

    public boolean isEd25519Signature(String hash) {
//        Ed25519Signature ed25519Signature;
//        try {
//            ed25519Signature = scope.manage(CardanoWasm.Ed25519Signature.from_bytes(hexStringToBuffer(hash)));
//        } catch (Exception error) {
//            return false;
//        }
        return true;
    }

    public HdPublicKey validateAndParseVotingKey(PublicKey votingKey) {
        if (ObjectUtils.isEmpty(votingKey.getHexBytes())) {
            log.error("[validateAndParsePublicKey] Voting key not provided");
            throw new IllegalArgumentException("missingVotingKeyError");
        }
        if (!isKeyValid(votingKey.getHexBytes(), votingKey.getCurveType().getValue())) {
            log.info("[validateAndParsePublicKey] Voting key has an invalid format");
            throw new IllegalArgumentException("invalidVotingKeyFormat");
        }
        byte[] publicKeyBuffer = hexStringToBuffer(votingKey.getHexBytes());
        return HdPublicKey.fromBytes(publicKeyBuffer);
    }

    public Map<String, Object> processPoolRetirement(Operation operation) {
        Map<String, Object> map = new HashMap<>();
        log.info("[processPoolRetiring] About to process operation of type ${operation.type}");
        if (!ObjectUtils.isEmpty(operation.getMetadata()) && ObjectUtils.isEmpty(operation.getMetadata().getEpoch())
                && !ObjectUtils.isEmpty(operation.getAccount()) && ObjectUtils.isEmpty(operation.getAccount().getAddress())) {
            BigDecimal epoch = operation.getMetadata().getEpoch();
            byte[] keyHash = validateAndParsePoolKeyHash(ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            map.put("certificate", new PoolRetirement(keyHash, epoch.longValue()));
            map.put("poolKeyHash", ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            return map;
        }
        log.error("[processPoolRetiring] Epoch operation metadata is missing");
        throw new IllegalArgumentException("missingMetadataParametersForPoolRetirement");
    }

    public byte[] validateAndParsePoolKeyHash(String poolKeyHash) {
        if (ObjectUtils.isEmpty(poolKeyHash)) {
            log.error("[validateAndParsePoolKeyHash] no pool key hash provided");
            throw new IllegalArgumentException("missingPoolKeyError");
        }
        byte[] parsedPoolKeyHash = null;
        try {
            parsedPoolKeyHash = poolKeyHash.getBytes();
        } catch (Exception error) {
            log.error("[validateAndParsePoolKeyHash] invalid pool key hash");
            throw new IllegalArgumentException("invalidPoolKeyError");
        }
        return parsedPoolKeyHash;
    }

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
        set.add(new String(owners.get(), StandardCharsets.UTF_8));
        log.info("[processPoolRegistration] About to generate Pool Registration");
        PoolRegistration wasmPoolRegistration = PoolRegistration.builder()
                .operator(null)
                .vrfKeyHash(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getPoolRegistrationParams().getVrfKeyHash().getBytes())
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

    public Inet4Address parseIpv4(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            byte[] parsedIp = ip.getBytes();
            return (Inet4Address) Inet4Address.getByAddress(parsedIp);
        }
        return null;
    }

    public Inet6Address parseIpv6(String ip) throws UnknownHostException {
        if (!ObjectUtils.isEmpty(ip)) {
            byte[] parsedIp = ip.replace("/:/g", "").getBytes();
            return (Inet6Address) Inet6Address.getByAddress(parsedIp);
        }
        return null;
    }

    public void validatePort(String port) {
        Integer parsedPort = Integer.parseInt(port, 10);
        if (!port.matches(Const.IS_POSITIVE_NUMBER) || parsedPort == null) {
            log.error("[validateAndParsePort] Invalid port ${port} received");
            throw new IllegalArgumentException("invalidPoolRelaysError Invalid port ${port} received");
        }
    }

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

    public Address parseToRewardAddress(String address) {
//        const wasmAddress = AddressProvider.() scope.manage(CardanoWasm.Address.from_bech32(address));
//        return scope.manage(CardanoWasm.RewardAddress.from_address(wasmAddress));
        return new Address(address);
    }

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

    public Map<String, Object> processOperationCertification(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) {
        log.info("[processOperationCertification] About to process operation of type ${operation.type}");
        // eslint-disable-next-line camelcase
        HashMap<String, Object> map = new HashMap<>();
        StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getStakingCredential());
        String address = generateRewardAddress(networkIdentifierEnum, HdPublicKey.fromBytes(credential.getHash()));
        if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
            // eslint-disable-next-line camelcase
            Certificate certificate = new StakeDelegation(credential, new StakePoolId(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getPoolKeyHash().getBytes()));
            map.put("certificate", certificate);

            return map;
        }
        map.put("certificate", new StakeDeregistration(credential));
        map.put("address", address);
        return map;
    }

    public Certificate processStakeKeyRegistration(Operation operation) {
        log.info("[processStakeKeyRegistration] About to process stake key registration");
        // eslint-disable-next-line camelcase
        StakeCredential credential = getStakingCredentialFromHex(ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata().getStakingCredential());
        return new StakeRegistration(credential);
    }

    public StakeCredential getStakingCredentialFromHex(PublicKey staking_credential) {
        HdPublicKey stakingKey = getPublicKey(staking_credential);
        return StakeCredential.fromKeyHash(stakingKey.getKeyHash());
    }

    public HdPublicKey getPublicKey(PublicKey publicKey) {
        if (ObjectUtils.isEmpty(publicKey) || ObjectUtils.isEmpty(publicKey.getHexBytes())) {
            log.error("[getPublicKey] Staking key not provided");
            throw new IllegalArgumentException("missingStakingKeyError");
        }
        if (!isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType().getValue())) {
            log.info("[getPublicKey] Staking key has an invalid format");
            throw new IllegalArgumentException("invalidStakingKeyFormat");
        }
        byte[] stakingKeyBuffer = hexStringToBuffer(publicKey.getHexBytes());
        return HdPublicKey.fromBytes(stakingKeyBuffer);
    }

    public Boolean isKeyValid(String publicKeyBytes, String curveType) {
        return publicKeyBytes.length() == Const.PUBLIC_KEY_BYTES_LENGTH && curveType.equals(Const.VALID_CURVE_TYPE);
    }

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
                    TransactionUtil.getTxHash(transactionId.getBytes()),
                    Integer.parseInt(index));
        } catch (Exception error) {
            throw new IllegalArgumentException("There was an error deserializating transaction input: " + error);
        }
    }

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

    public Address generateAddress(String address) throws AddressExcepion {
        EraAddressType addressType = getEraAddressType(address);

        if (addressType == EraAddressType.Byron) {
            String byronAddress = AddressUtil.bytesToBase58Address(address.getBytes());
            return new Address(byronAddress);
        }
        return new Address(AddressUtil.addressToBytes(address));
    }

    public EraAddressType getEraAddressType(String address) {
        if (AddressUtil.isValidAddress(address)) {
            return EraAddressType.Byron;
        }
        return EraAddressType.Shelley;
    }

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

    public Boolean isPolicyIdValid(String policyId) {
        return policyId.matches(Const.PolicyId_Validation);
    }

    public Boolean isTokenNameValid(String name) {
        return name.matches(Const.Token_Name_Validation) || isEmptyHexString(name);
    }

    public Boolean isEmptyHexString(String toCheck) {
        return toCheck.equals(Const.EMPTY_HEX);
    }

    public byte[] hexStringToBuffer(String input) {
        return isEmptyHexString(input) ? "".getBytes() : input.getBytes();
    }
//    public String getHashOfSignedTransaction(String signedTransaction){
//        try {
//            log.info("[getHashOfSignedTransaction] About to hash signed transaction {}",signedTransaction);
//        byte[] signedTransactionBytes = signedTransaction.getBytes();
//            log.info("[getHashOfSignedTransaction] About to parse transaction from signed transaction bytes");
//        var parsed = CardanoWasm.Transaction.from_bytes(signedTransactionBytes);
//            log.info("[getHashOfSignedTransaction] Returning transaction hash");
//        const body = parsed.body();
//        const hashBuffer = parsed && body && Buffer.from(CardanoWasm.hash_transaction(body).to_bytes());
//            return hexFormatter(hashBuffer);
//        } catch (Exception error) {
//            log.error("{} [getHashOfSignedTransaction] There was an error parsing signed transaction",error);
//            throw new IllegalArgumentException("parseSignedTransactionError");
//        }
//    }
}
