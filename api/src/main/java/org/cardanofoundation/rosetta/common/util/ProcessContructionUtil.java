package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.spec.UnitInterval;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRetirement;
import com.bloxbean.cardano.client.transaction.spec.cert.Relay;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDelegation;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration;
import com.bloxbean.cardano.client.transaction.spec.cert.StakePoolId;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.model.dto.PoolRegistrationParams;
import org.cardanofoundation.rosetta.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.metadata.OperationMetadata;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistationParametersReturnDto;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturnDto;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessPoolRegistrationReturnDto;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolMetadata;
import org.openapitools.client.model.AccountIdentifier;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessWithdrawalReturnDto;
import org.openapitools.client.model.Operation;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.openapitools.client.model.PublicKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.math.BigInteger.valueOf;

@Slf4j
public class ProcessContructionUtil {
    private ProcessContructionUtil() {

    }

    public static Certificate processStakeKeyRegistration(Operation operation) {
        log.info("[processStakeKeyRegistration] About to process stake key registration");
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        StakeCredential credential = CardanoAddressUtil.getStakingCredentialFromHex(operationMetadata.getStakingCredential());
        return new StakeRegistration(credential);
    }

    public static Map<String, Object> processOperationCertification(
            NetworkIdentifierType networkIdentifierType, Operation operation) {
        log.info(
                "[processOperationCertification] About to process operation of type {}",
                operation.getType());
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        HashMap<String, Object> map = new HashMap<>();
        PublicKey publicKey = ObjectUtils.isEmpty(operation.getMetadata()) ? null
                : operationMetadata.getStakingCredential();
        StakeCredential credential = CardanoAddressUtil.getStakingCredentialFromHex(publicKey);
        HdPublicKey hdPublicKey = new HdPublicKey();
        if (publicKey != null) {
            hdPublicKey.setKeyData(HexUtil.decodeHexString(publicKey.getHexBytes()));
        }
        String address = CardanoAddressUtil.generateRewardAddress(networkIdentifierType, hdPublicKey);
        if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
            if (operationMetadata.getPoolKeyHash() == null) {
                throw ExceptionFactory.missingPoolKeyError();
            }
            Certificate certificate = new StakeDelegation(credential, new StakePoolId(
                    ObjectUtils.isEmpty(operation.getMetadata()) ? null
                            : HexUtil.decodeHexString(operationMetadata.getPoolKeyHash())));
            map.put(Constants.CERTIFICATE, certificate);
            map.put(Constants.ADDRESS, address);
            return map;
        }
        map.put(Constants.CERTIFICATE, new StakeDeregistration(credential));
        map.put(Constants.ADDRESS, address);
        return map;
    }

    public static ProcessWithdrawalReturnDto processWithdrawal(
            NetworkIdentifierType networkIdentifierType,
            Operation operation) {
        log.info("[processWithdrawal] About to process withdrawal");
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        HdPublicKey hdPublicKey = new HdPublicKey();
        if (operation.getMetadata() != null &&
                operationMetadata.getStakingCredential() != null &&
                operationMetadata.getStakingCredential().getHexBytes() != null) {
            hdPublicKey.setKeyData(
                    HexUtil.decodeHexString(operationMetadata.getStakingCredential().getHexBytes()));
        }
        String address = CardanoAddressUtil.generateRewardAddress(networkIdentifierType, hdPublicKey);
        HdPublicKey hdPublicKey1 = new HdPublicKey();
        hdPublicKey1.setKeyData(
                HexUtil.decodeHexString(operationMetadata.getStakingCredential().getHexBytes()));
        ProcessWithdrawalReturnDto processWithdrawalReturnDto = new ProcessWithdrawalReturnDto();
        processWithdrawalReturnDto.setReward(AddressProvider.getRewardAddress(hdPublicKey1,
                new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic())));
        processWithdrawalReturnDto.setAddress(address);
        return processWithdrawalReturnDto;
    }


    public static ProcessPoolRegistrationReturnDto processPoolRegistration(
            Operation operation) {
        log.info("[processPoolRegistration] About to process pool registration operation");
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operationMetadata.getPoolRegistrationParams())) {
            log.error("[processPoolRegistration] Pool_registration was not provided");
            throw ExceptionFactory.missingPoolRegistrationParameters();
        }
        PoolRegistrationParams poolRegistrationParams =
                ObjectUtils.isEmpty(operation.getMetadata()) ? null
                        : operationMetadata.getPoolRegistrationParams();

        PoolRegistationParametersReturnDto dto = ValidateParseUtil.validateAndParsePoolRegistationParameters(
                poolRegistrationParams);
        byte[] poolKeyHash = ValidateParseUtil.validateAndParsePoolKeyHash(
                ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());

        log.info("[processPoolRegistration] About to validate and parse reward address");
        assert poolRegistrationParams != null;
        Address parsedAddress = ValidateParseUtil.validateAndParseRewardAddress(
                poolRegistrationParams.getRewardAddress());
        Bech32.Bech32Data bech32Data = Bech32.decode(parsedAddress.toBech32());
        log.info("[processPoolRegistration] About to generate pool owners");
        Set<String> owners = ValidateParseUtil.validateAndParsePoolOwners(
                poolRegistrationParams.getPoolOwners());
        log.info("[processPoolRegistration] About to generate pool relays");
        List<Relay> parsedRelays = ValidateParseUtil.validateAndParsePoolRelays(
                poolRegistrationParams.getRelays());

        log.info("[processPoolRegistration] About to generate pool metadata");
        PoolMetadata poolMetadata = ValidateParseUtil.validateAndParsePoolMetadata(
                poolRegistrationParams.getPoolMetadata());

        log.info("[processPoolRegistration] About to generate Pool Registration");
        PoolRegistration wasmPoolRegistration = PoolRegistration.builder()
                .operator(poolKeyHash)
                .vrfKeyHash(ObjectUtils.isEmpty(operation.getMetadata()) ? null : HexUtil.decodeHexString(
                        operationMetadata.getPoolRegistrationParams().getVrfKeyHash()))
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

    public static PoolRegistrationCertReturnDto processPoolRegistrationWithCert(Operation operation,
                                                                                NetworkIdentifierType networkIdentifierType) {
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        AccountIdentifier account = operation == null ? null : operation.getAccount();
        return ValidateParseUtil.validateAndParsePoolRegistrationCert(
                networkIdentifierType,
                operationMetadata == null ? null : operationMetadata.getPoolRegistrationCert(),
                account == null ? null : account.getAddress()
        );
    }

    public static Map<String, Object> processPoolRetirement(Operation operation) {
        Map<String, Object> map = new HashMap<>();
        log.info("[processPoolRetiring] About to process operation of type {}", operation.getType());
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        if (!ObjectUtils.isEmpty(operation.getMetadata()) && !ObjectUtils.isEmpty(
                operationMetadata.getEpoch())
                && !ObjectUtils.isEmpty(operation.getAccount()) && !ObjectUtils.isEmpty(
                operation.getAccount().getAddress())) {
            double epoch = operationMetadata.getEpoch();
            byte[] keyHash = ValidateParseUtil.validateAndParsePoolKeyHash(
                    ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            map.put(Constants.CERTIFICATE, new PoolRetirement(keyHash, Math.round(epoch)));
            map.put(Constants.POOL_KEY_HASH,
                    ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            return map;
        }
        log.error("[processPoolRetiring] Epoch operation metadata is missing");
        throw ExceptionFactory.missingMetadataParametersForPoolRetirement();
    }

    public static AuxiliaryData processVoteRegistration(Operation operation) {
        log.info("[processVoteRegistration] About to process vote registration");
        if (!ObjectUtils.isEmpty(operation) && ObjectUtils.isEmpty(operation.getMetadata())) {
            log.error("[processVoteRegistration] Vote registration metadata was not provided");
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        OperationMetadata operationMetadata = getOperationMetadata(operation);
        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operationMetadata.getVoteRegistrationMetadata())) {
            log.error("[processVoteRegistration] Vote registration metadata was not provided");
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        Map<String, Object> map = ValidateParseUtil.validateAndParseVoteRegistrationMetadata(
                operationMetadata.getVoteRegistrationMetadata());

        CBORMetadata metadata = new CBORMetadata();

        CBORMetadataMap map2 = new CBORMetadataMap();
        byte[] votingKeyByte = HexUtil.decodeHexString(((String) map.get(Constants.VOTING_KEY)));
        map2.put(valueOf(CatalystDataIndexes.VOTING_KEY.getValue()), votingKeyByte);
        map2.put(valueOf(CatalystDataIndexes.STAKE_KEY.getValue()),
                HexUtil.decodeHexString(((String) map.get(Constants.STAKE_KEY))));
        map2.put(valueOf(CatalystDataIndexes.REWARD_ADDRESS.getValue()),
                HexUtil.decodeHexString(((String) map.get(Constants.REWARD_ADDRESS))));
        map2.put(valueOf(CatalystDataIndexes.VOTING_NONCE.getValue()),
                valueOf(((Integer) map.get(Constants.VOTING_NONCE))));
        metadata.put(valueOf(Long.parseLong(CatalystLabels.DATA.getLabel())), map2);
        CBORMetadataMap map3 = new CBORMetadataMap();
        map3.put(valueOf(1L), HexUtil.decodeHexString(
                operationMetadata.getVoteRegistrationMetadata().getVotingSignature()));
        metadata.put(valueOf(Long.parseLong(CatalystLabels.SIG.getLabel())), map3);
        AuxiliaryData auxiliaryData = new AuxiliaryData();
        auxiliaryData.setMetadata(metadata);
        return auxiliaryData;
    }

    public static OperationMetadata getOperationMetadata(Operation operation) {
        OperationMetadata operationMetadata;
        try {
            operationMetadata = operation == null ? null : (OperationMetadata) FileUtils.getObjectFromHashMapObject(operation.getMetadata(), OperationMetadata.class);
        } catch (JsonProcessingException e) {
            log.error("Could not parse operation metadata", e);
            throw new RuntimeException("Could not parse operation metadata");
        }
        return operationMetadata;
    }


}
