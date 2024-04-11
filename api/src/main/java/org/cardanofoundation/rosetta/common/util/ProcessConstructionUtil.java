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
import com.bloxbean.cardano.client.transaction.spec.cert.Relay;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDelegation;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration;
import com.bloxbean.cardano.client.transaction.spec.cert.StakePoolId;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistationParametersReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRetirement;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessPoolRegistrationReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.StakeCertificate;
import org.openapitools.client.model.AccountIdentifier;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessWithdrawalReturn;
import org.openapitools.client.model.Operation;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openapitools.client.model.VoteRegistrationMetadata;

import static java.math.BigInteger.valueOf;

@Slf4j
public class ProcessConstructionUtil {
    private ProcessConstructionUtil() {

    }

    public static Certificate getStakeRegistrationCertificateFromOperation(Operation operation) {
        log.info("[processStakeKeyRegistration] About to process stake key registration");

        OperationMetadata operationMetadata = operation.getMetadata();
        StakeCredential credential = CardanoAddressUtils.getStakingCredentialFromStakeKey(operationMetadata.getStakingCredential());
        return new StakeRegistration(credential);
    }

    public static StakeCertificate getStakeCertificateFromOperation(
            NetworkIdentifierType networkIdentifierType, Operation operation) {
        log.info(
                "[processOperationCertification] About to process operation of type {}",
                operation.getType());
        OperationMetadata operationMetadata = operation.getMetadata();
        PublicKey publicKey = ObjectUtils.isEmpty(operation.getMetadata()) ? null
                : operationMetadata.getStakingCredential();
        StakeCredential credential = CardanoAddressUtils.getStakingCredentialFromStakeKey(publicKey);
        HdPublicKey hdPublicKey = new HdPublicKey();
        if (publicKey != null) {
            hdPublicKey.setKeyData(HexUtil.decodeHexString(publicKey.getHexBytes()));
        }
        String address = CardanoAddressUtils.generateRewardAddress(networkIdentifierType, hdPublicKey);
        if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
            if (operationMetadata.getPoolKeyHash() == null) {
                throw ExceptionFactory.missingPoolKeyError();
            }
            Certificate certificate = new StakeDelegation(credential, new StakePoolId(
                    ObjectUtils.isEmpty(operation.getMetadata()) ? null
                            : HexUtil.decodeHexString(operationMetadata.getPoolKeyHash())));
            return new StakeCertificate(certificate, address);
        }
        return new StakeCertificate(new StakeDeregistration(credential), address);
    }

    public static ProcessWithdrawalReturn getWithdrawalsReturnFromOperation(
            NetworkIdentifierType networkIdentifierType,
            Operation operation) {
        log.info("[processWithdrawal] About to process withdrawal");
        OperationMetadata operationMetadata = operation.getMetadata();
        HdPublicKey hdPublicKey = new HdPublicKey();
        String address;
        ProcessWithdrawalReturn processWithdrawalReturnDto = new ProcessWithdrawalReturn();
        if (operation.getMetadata() != null &&
                operationMetadata.getStakingCredential() != null &&
                operationMetadata.getStakingCredential().getHexBytes() != null) {
            hdPublicKey.setKeyData(
                    HexUtil.decodeHexString(operationMetadata.getStakingCredential().getHexBytes()));
            address = CardanoAddressUtils.generateRewardAddress(networkIdentifierType, hdPublicKey);
            HdPublicKey hdPublicKey1 = new HdPublicKey();
            hdPublicKey1.setKeyData(
                HexUtil.decodeHexString(operationMetadata.getStakingCredential().getHexBytes()));

            processWithdrawalReturnDto.setReward(AddressProvider.getRewardAddress(hdPublicKey1,
                new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic())));
            processWithdrawalReturnDto.setAddress(address);
        } else if(operation.getAccount() != null && operation.getAccount().getAddress() != null) {
            address = operation.getAccount().getAddress();
            processWithdrawalReturnDto.setAddress(address);
            processWithdrawalReturnDto.setReward(new Address(address));
        } else {
            throw ExceptionFactory.missingStakingKeyError();
        }

        return processWithdrawalReturnDto;
    }


    public static ProcessPoolRegistrationReturn getPoolRegistrationFromOperation(Operation operation) {
        log.info("[processPoolRegistration] About to process pool registration operation");
        OperationMetadata operationMetadata = operation.getMetadata();
        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operationMetadata.getPoolRegistrationParams())) {
            log.error("[processPoolRegistration] Pool_registration was not provided");
            throw ExceptionFactory.missingPoolRegistrationParameters();
        }
        PoolRegistrationParams poolRegistrationParams =
                ObjectUtils.isEmpty(operation.getMetadata()) ? null
                        : operationMetadata.getPoolRegistrationParams();

        PoolRegistationParametersReturn poolRegistationParametersReturn = ValidateParseUtil.validateAndParsePoolRegistationParameters(
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
                .pledge(poolRegistationParametersReturn.pledge())
                .cost(poolRegistationParametersReturn.cost())
                .margin(new UnitInterval(poolRegistationParametersReturn.numerator(),
                        poolRegistationParametersReturn.denominator()))
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
        ProcessPoolRegistrationReturn processPoolRegistrationReturnDto = new ProcessPoolRegistrationReturn();
        processPoolRegistrationReturnDto.setTotalAddresses(totalAddresses);
        processPoolRegistrationReturnDto.setCertificate(wasmPoolRegistration);
        return processPoolRegistrationReturnDto;
    }

    public static PoolRegistrationCertReturn getPoolRegistrationCertFromOperation(Operation operation,
                                                                                NetworkIdentifierType networkIdentifierType) {
        OperationMetadata operationMetadata = operation.getMetadata();
        AccountIdentifier account = operation == null ? null : operation.getAccount();
        return ValidateParseUtil.validateAndParsePoolRegistrationCert(
                networkIdentifierType,
                operationMetadata == null ? null : operationMetadata.getPoolRegistrationCert(),
                account == null ? null : account.getAddress()
        );
    }

    public static PoolRetirement getPoolRetirementFromOperation(Operation operation) {
        log.info("[processPoolRetiring] About to process operation of type {}", operation.getType());
        OperationMetadata operationMetadata = operation.getMetadata();
        if (!ObjectUtils.isEmpty(operation.getMetadata()) && !ObjectUtils.isEmpty(
                operationMetadata.getEpoch())
                && !ObjectUtils.isEmpty(operation.getAccount()) && !ObjectUtils.isEmpty(
                operation.getAccount().getAddress())) {
            double epoch = operationMetadata.getEpoch();
            byte[] keyHash = ValidateParseUtil.validateAndParsePoolKeyHash(
                    ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
            return new PoolRetirement(new com.bloxbean.cardano.client.transaction.spec.cert.PoolRetirement(keyHash, Math.round(epoch)), ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
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
        OperationMetadata operationMetadata = operation.getMetadata();
        if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
                && ObjectUtils.isEmpty(operationMetadata.getVoteRegistrationMetadata())) {
            log.error("[processVoteRegistration] Vote registration metadata was not provided");
            throw ExceptionFactory.missingVoteRegistrationMetadata();
        }
        VoteRegistrationMetadata voteRegistrationMetadata = ValidateParseUtil.validateAndParseVoteRegistrationMetadata(
            operationMetadata.getVoteRegistrationMetadata());

        CBORMetadata metadata = new CBORMetadata();

        CBORMetadataMap map2 = new CBORMetadataMap();
        byte[] votingKeyByte = HexUtil.decodeHexString(voteRegistrationMetadata.getVotingkey().getHexBytes());
        map2.put(valueOf(CatalystDataIndexes.VOTING_KEY.getValue()), votingKeyByte);
        map2.put(valueOf(CatalystDataIndexes.STAKE_KEY.getValue()),
                HexUtil.decodeHexString(voteRegistrationMetadata.getStakeKey().getHexBytes()));
        map2.put(valueOf(CatalystDataIndexes.REWARD_ADDRESS.getValue()),
                HexUtil.decodeHexString(voteRegistrationMetadata.getRewardAddress()));
        map2.put(valueOf(CatalystDataIndexes.VOTING_NONCE.getValue()),
                valueOf(voteRegistrationMetadata.getVotingNonce()));
        metadata.put(valueOf(Long.parseLong(CatalystLabels.DATA.getLabel())), map2);
        CBORMetadataMap map3 = new CBORMetadataMap();
        map3.put(valueOf(1L), HexUtil.decodeHexString(
                operationMetadata.getVoteRegistrationMetadata().getVotingSignature()));
        metadata.put(valueOf(Long.parseLong(CatalystLabels.SIG.getLabel())), map3);
        AuxiliaryData auxiliaryData = new AuxiliaryData();
        auxiliaryData.setMetadata(metadata);
        return auxiliaryData;
    }
}
