package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.spec.UnitInterval;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.client.transaction.spec.cert.Relay;
import com.bloxbean.cardano.client.transaction.spec.governance.DRep;
import com.bloxbean.cardano.client.transaction.spec.governance.DRepType;
import com.bloxbean.cardano.client.util.HexUtil;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.model.domain.DRepDelegation;
import org.cardanofoundation.rosetta.api.block.model.domain.GovernancePoolVote;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.CertificateWithAddress;
import org.cardanofoundation.rosetta.common.model.cardano.pool.*;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRetirement;
import org.openapitools.client.model.*;

import java.util.*;

@Slf4j
public class ProcessConstructions {

    public static Certificate getStakeRegistrationCertificateFromOperation(Operation operation) {
        log.info("[processStakeKeyRegistration] About to process stake key registration");

        OperationMetadata operationMetadata = operation.getMetadata();
        StakeCredential credential = CardanoAddressUtils.getStakingCredentialFromStakeKey(operationMetadata.getStakingCredential());

        return new StakeRegistration(credential);
    }

    public static Optional<CertificateWithAddress> getCertificateFromOperation(Network network,
                                                                               Operation operation) {
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

        String address = CardanoAddressUtils.generateRewardAddress(network, hdPublicKey);

        if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
            return handleStakeDelegation(operation, operationMetadata, credential, address);
        }
        if (operation.getType().equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue())) {
            return Optional.of(new CertificateWithAddress(new StakeDeregistration(credential), address));
        }

        if (operation.getType().equals(OperationType.VOTE_DREP_DELEGATION.getValue())) {
            return handleDrepVoteDelegation(operationMetadata, credential, address);
        }

        return Optional.empty();
    }

    private static Optional<CertificateWithAddress> handleDrepVoteDelegation(OperationMetadata operationMetadata,
                                                                             StakeCredential credential,
                                                                             String address) {
        DRepParams drep = operationMetadata.getDrep();

        if (drep == null || drep.getType() == null) {
            throw ExceptionFactory.missingDrep();
        }

        DRepDelegation.DRep delegationDrep = DRepDelegation.DRep.convertDRepToRosetta(drep);

        Either<ApiException, Optional<String>> drepIdE = switch (delegationDrep.getDrepType()) {
            case ADDR_KEYHASH, SCRIPTHASH -> {

                if (delegationDrep.getDrepId() == null || delegationDrep.getDrepId().isBlank()) {
                    yield Either.left(ExceptionFactory.missingDRepId());
                }

                if (delegationDrep.getDrepId().length() < 56 || delegationDrep.getDrepId().length() > 58) {
                    yield Either.left(ExceptionFactory.invalidDrepIdLength());
                }

                byte[] idBytes = HexUtil.decodeHexString(delegationDrep.getDrepId());

                if (idBytes.length == 29) {
                    byte tag = idBytes[0];
                    byte[] stripped = Arrays.copyOfRange(idBytes, 1, 29);
                    String strippedHex = HexUtil.encodeHexString(stripped);

                    Either<ApiException, DRepType> dRepHeaderTypeE = switch (tag) {
                        case 0x22 -> Either.right(DRepType.ADDR_KEYHASH);
                        case 0x23 -> Either.right(DRepType.SCRIPTHASH);
                        default -> Either.left(ExceptionFactory.invalidDrepType());
                    };

                    if (dRepHeaderTypeE.isLeft()) {
                        yield Either.left(dRepHeaderTypeE.getLeft());
                    }

                    DRepType dRepHeaderType = dRepHeaderTypeE.get();

                    if (dRepHeaderType != delegationDrep.getDrepType()) {
                        yield Either.left(ExceptionFactory.mismatchDrepType());
                    }

                    drep.setId(strippedHex);

                    yield Either.right(Optional.of(strippedHex));
                }

                yield Either.right(Optional.of(delegationDrep.getDrepId()));
            }

            case ABSTAIN, NO_CONFIDENCE -> Either.right(Optional.empty());
        };

        if (drepIdE.isLeft()) {
            throw drepIdE.getLeft();
        }

        Optional<String> drepIdM = drepIdE.get();
        DRepType drepType = delegationDrep.getDrepType();

        DRep dRep = switch (drepType) {
            case ADDR_KEYHASH -> DRep.addrKeyHash(drepIdM.orElseThrow());
            case SCRIPTHASH -> DRep.scriptHash(drepIdM.orElseThrow());
            case ABSTAIN -> DRep.abstain();
            case NO_CONFIDENCE -> DRep.noConfidence();
        };

        return Optional.of(new CertificateWithAddress(VoteDelegCert.builder()
                .stakeCredential(credential)
                .drep(dRep)
                .build(), address));
    }

    private static Optional<CertificateWithAddress> handleStakeDelegation(Operation operation, OperationMetadata operationMetadata, StakeCredential credential, String address) {
        if (operationMetadata.getPoolKeyHash() == null) {
            throw ExceptionFactory.missingPoolKeyError();
        }
        Certificate certificate = new StakeDelegation(credential, new StakePoolId(
                ObjectUtils.isEmpty(operation.getMetadata()) ? null
                        : HexUtil.decodeHexString(operationMetadata.getPoolKeyHash())));

        return Optional.of(new CertificateWithAddress(certificate, address));
    }

    public static ProcessWithdrawalReturn getWithdrawalsReturnFromOperation(Network network, Operation operation) {
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
            address = CardanoAddressUtils.generateRewardAddress(network, hdPublicKey);
            HdPublicKey hdPublicKey1 = new HdPublicKey();
            hdPublicKey1.setKeyData(
                HexUtil.decodeHexString(operationMetadata.getStakingCredential().getHexBytes()));

            processWithdrawalReturnDto.setReward(AddressProvider.getRewardAddress(hdPublicKey1, network));
            processWithdrawalReturnDto.setAddress(address);
        } else if (operation.getAccount() != null && operation.getAccount().getAddress() != null) {
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

        PoolRegistationParametersReturn poolRegistationParametersReturn = ValidateParseUtil.validateAndParsePoolRegistrationParameters(
                poolRegistrationParams);

        byte[] poolKeyHash = ValidateParseUtil.validateAndParsePoolKeyHash(
                ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());

        log.info("[processPoolRegistration] About to validate and parse reward address");
        Objects.requireNonNull(poolRegistrationParams, "Pool registration params can't be null");
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

    public static PoolRegistrationCertReturn getPoolRegistrationCertFromOperation(Operation operation, Network network) {
        OperationMetadata operationMetadata = null;
        AccountIdentifier account = null;
        if (operation != null) {
            operationMetadata = operation.getMetadata();
            account = operation.getAccount();
        }

        return ValidateParseUtil.validateAndParsePoolRegistrationCert(
                network,
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

    public static GovernancePoolVote processGovernanceVote(Operation operation) {
        log.info("[processCastVote] About to process cast vote operation as a pool operator..");

        OperationMetadata metadata = operation.getMetadata();

        AccountIdentifier account = operation.getAccount();
        String poolKeyHash = account.getAddress();

        if (poolKeyHash.startsWith("pool")) { // it means poolKeyHash is in Bech32 format
            // lets convert this to hex format
            poolKeyHash = HexUtil.encodeHexString(Bech32.decode(poolKeyHash).data);

            // update the account identifier with the hex format pool key hash
            operation.setAccount(new AccountIdentifier(poolKeyHash, account.getSubAccount(), account.getMetadata()));
        }

        PoolGovernanceVoteParams poolGovernanceVoteParams = metadata.getPoolGovernanceVoteParams();
        PublicKey poolCredentialKey = poolGovernanceVoteParams.getPoolCredential();
        if (poolCredentialKey == null) {
            log.error("[processCastVote] pool_credential parameter were not provided");

            throw ExceptionFactory.invalidGovernanceVote("Parameter 'pool_credential' not provided!");
        }
        String poolCredentialKeyHexBytes = poolCredentialKey.getHexBytes();

        if (poolCredentialKeyHexBytes.length() == 68) { // 4 bytes prefix + 64 bytes key
            log.info("[processCastVote] Stripping 2 bytes prefix from pool credential key...");
            poolCredentialKeyHexBytes = poolCredentialKeyHexBytes.substring(4);

            // update the poolCredentialKey with stripped key
            poolGovernanceVoteParams.setPoolCredential(
                    new PublicKey(poolCredentialKeyHexBytes, poolCredentialKey.getCurveType()));
        }

        if (poolCredentialKeyHexBytes.length() != 64) {
            log.error("[processCastVote] Invalid pool credential key length: {}", poolCredentialKeyHexBytes.length());

            throw ExceptionFactory.invalidGovernanceVote("Invalid pool credential key length: " + poolCredentialKeyHexBytes.length());
        }

        String governanceActionHash = poolGovernanceVoteParams.getGovernanceActionHash();
        if (governanceActionHash == null || governanceActionHash.trim().isEmpty()) {
            log.error("[processCastVote] governance action parameters were not provided");

            throw ExceptionFactory.invalidGovernanceVote("Parameter 'governance_action_hash' not provided!");
        }
        
        // Validate the governance action format
        GovActionParamsUtil.parseAndValidate(governanceActionHash);
        GovVoteParams voteParams = poolGovernanceVoteParams.getVote();

        if (voteParams == null) {
            log.error("[processCastVote] Vote not provided, i.e. yes, no or abstain");

            throw ExceptionFactory.invalidGovernanceVote("Parameter 'vote' not provided, i.e. 'yes', 'no' or 'abstain'!");
        }

        var poolVerificationKeyBlake224HashStrippedHexBytes = HexUtil.encodeHexString(Blake2bUtil.blake2bHash224(HexUtil.decodeHexString(poolCredentialKeyHexBytes)));
        if (!poolVerificationKeyBlake224HashStrippedHexBytes.equals(poolKeyHash)) {
            log.error("[processCastVote] Pool key hash does not match with pool credential key hash: {} != {}",
                    poolVerificationKeyBlake224HashStrippedHexBytes, poolKeyHash);

            throw ExceptionFactory.invalidGovernanceVote("Pool key hash passed as account.address does not match with pool credential key hash!");
        }

        return GovernancePoolVote.convertToRosetta(poolGovernanceVoteParams);
    }

}
