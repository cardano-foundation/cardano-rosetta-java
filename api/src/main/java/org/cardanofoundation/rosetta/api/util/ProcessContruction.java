package org.cardanofoundation.rosetta.api.util;

import static java.math.BigInteger.valueOf;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.UnitInterval;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationMetadata;
import org.cardanofoundation.rosetta.api.model.PoolMetadata;
import org.cardanofoundation.rosetta.api.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistationParametersReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistrationCertReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessPoolRegistrationReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessWithdrawalReturnDto;

@Slf4j
public class ProcessContruction {
  private ProcessContruction() {

  }

  public static PoolRegistrationCertReturnDto processPoolRegistrationWithCert(Operation operation,
      NetworkIdentifierType networkIdentifierType) {
    OperationMetadata operationMetadata =
        operation == null ? null : operation.getMetadata();
    AccountIdentifier account = operation == null ? null : operation.getAccount();
    return ValidateOfConstruction.validateAndParsePoolRegistrationCert(
        networkIdentifierType,
        operationMetadata == null ? null : operationMetadata.getPoolRegistrationCert(),
        account == null ? null : account.getAddress()
    );
  }


  public static AuxiliaryData processVoteRegistration(Operation operation) {
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
    Map<String, Object> map = ValidateOfConstruction.validateAndParseVoteRegistrationMetadata(
        operation.getMetadata().getVoteRegistrationMetadata());

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
        operation.getMetadata().getVoteRegistrationMetadata().getVotingSignature()));
    metadata.put(valueOf(Long.parseLong(CatalystLabels.SIG.getLabel())), map3);
    AuxiliaryData auxiliaryData = new AuxiliaryData();
    auxiliaryData.setMetadata(metadata);
    return auxiliaryData;
  }

  public static Map<String, Object> processPoolRetirement(Operation operation) {
    Map<String, Object> map = new HashMap<>();
    log.info("[processPoolRetiring] About to process operation of type {}", operation.getType());
    if (!ObjectUtils.isEmpty(operation.getMetadata()) && !ObjectUtils.isEmpty(
        operation.getMetadata().getEpoch())
        && !ObjectUtils.isEmpty(operation.getAccount()) && !ObjectUtils.isEmpty(
        operation.getAccount().getAddress())) {
      double epoch = operation.getMetadata().getEpoch();
      byte[] keyHash = ValidateOfConstruction.validateAndParsePoolKeyHash(
          ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
      map.put(Constants.CERTIFICATE, new PoolRetirement(keyHash, Math.round(epoch)));
      map.put(Constants.POOL_KEY_HASH,
          ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
      return map;
    }
    log.error("[processPoolRetiring] Epoch operation metadata is missing");
    throw ExceptionFactory.missingMetadataParametersForPoolRetirement();
  }


  public static ProcessPoolRegistrationReturnDto processPoolRegistration(
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

    PoolRegistationParametersReturnDto dto = ValidateOfConstruction.validateAndParsePoolRegistationParameters(
        poolRegistrationParams);
    // eslint-disable-next-line camelcase
    byte[] poolKeyHash = ValidateOfConstruction.validateAndParsePoolKeyHash(
        ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());

    log.info("[processPoolRegistration] About to validate and parse reward address");
    assert poolRegistrationParams != null;
    Address parsedAddress = ValidateOfConstruction.validateAndParseRewardAddress(
        poolRegistrationParams.getRewardAddress());
    Bech32.Bech32Data bech32Data = Bech32.decode(parsedAddress.toBech32());
    log.info("[processPoolRegistration] About to generate pool owners");
    Set<String> owners = ValidateOfConstruction.validateAndParsePoolOwners(
        poolRegistrationParams.getPoolOwners());
    log.info("[processPoolRegistration] About to generate pool relays");
    List<Relay> parsedRelays = ValidateOfConstruction.validateAndParsePoolRelays(
        poolRegistrationParams.getRelays());

    log.info("[processPoolRegistration] About to generate pool metadata");
    PoolMetadata poolMetadata = ValidateOfConstruction.validateAndParsePoolMetadata(
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

  public static ProcessWithdrawalReturnDto processWithdrawal(
      NetworkIdentifierType networkIdentifierType,
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
    String address = CardanoAddressUtils.generateRewardAddress(networkIdentifierType, hdPublicKey);
    HdPublicKey hdPublicKey1 = new HdPublicKey();
    hdPublicKey1.setKeyData(
        HexUtil.decodeHexString(operation.getMetadata().getStakingCredential().getHexBytes()));
    ProcessWithdrawalReturnDto processWithdrawalReturnDto = new ProcessWithdrawalReturnDto();
    processWithdrawalReturnDto.setReward(AddressProvider.getRewardAddress(hdPublicKey1,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic())));
    processWithdrawalReturnDto.setAddress(address);
    return processWithdrawalReturnDto;
  }

  public static Map<String, Object> processOperationCertification(
      NetworkIdentifierType networkIdentifierType, Operation operation) {
    log.info(
        "[processOperationCertification] About to process operation of type {}",
        operation.getType());
    // eslint-disable-next-line camelcase
    HashMap<String, Object> map = new HashMap<>();
    PublicKey publicKey = ObjectUtils.isEmpty(operation.getMetadata()) ? null
        : operation.getMetadata().getStakingCredential();
    StakeCredential credential = CardanoAddressUtils.getStakingCredentialFromHex(publicKey);
    HdPublicKey hdPublicKey = new HdPublicKey();
    if (publicKey != null) {
      hdPublicKey.setKeyData(HexUtil.decodeHexString(publicKey.getHexBytes()));
    }
    String address = CardanoAddressUtils.generateRewardAddress(networkIdentifierType, hdPublicKey);
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

  public static Certificate processStakeKeyRegistration(Operation operation) {
    log.info("[processStakeKeyRegistration] About to process stake key registration");
    // eslint-disable-next-line camelcase
    StakeCredential credential = CardanoAddressUtils.getStakingCredentialFromHex(
        ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getStakingCredential());
    return new StakeRegistration(credential);
  }

}
