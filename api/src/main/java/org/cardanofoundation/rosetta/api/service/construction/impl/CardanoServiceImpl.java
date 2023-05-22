package org.cardanofoundation.rosetta.api.service.construction.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.cardanofoundation.rosetta.api.construction.data.Metadata;
import org.cardanofoundation.rosetta.api.construction.data.ProcessOperationsDto;
import org.cardanofoundation.rosetta.api.construction.data.ProtocolParametersResponse;
import org.cardanofoundation.rosetta.api.construction.data.Signatures;
import org.cardanofoundation.rosetta.api.construction.data.UnsignedTransaction;
import org.cardanofoundation.rosetta.api.construction.data.type.AddressType;
import org.cardanofoundation.rosetta.api.construction.data.type.CatalystDataIndexes;
import org.cardanofoundation.rosetta.api.construction.data.type.CatalystLabels;
import org.cardanofoundation.rosetta.api.construction.data.type.CatalystSigIndexes;
import org.cardanofoundation.rosetta.api.construction.data.type.EraAddressType;
import org.cardanofoundation.rosetta.api.construction.data.type.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.construction.data.type.NonStakeAddressPrefix;
import org.cardanofoundation.rosetta.api.construction.data.type.OperationType;
import org.cardanofoundation.rosetta.api.construction.data.type.RelayType;
import org.cardanofoundation.rosetta.api.construction.data.type.StakeAddressPrefix;
import org.cardanofoundation.rosetta.api.construction.data.Const;

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
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
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
import org.cardanofoundation.rosetta.api.service.construction.CardanoService;
import org.cardanofoundation.rosetta.api.model.Currency;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Slf4j
@Service("CardanoService")
public class CardanoServiceImpl implements CardanoService {

  @Autowired
  LedgerDataProviderService ledgerDataProviderService;

  @Override
  public String generateAddress(NetworkIdentifierType networkIdentifierType, String publicKeyString,
      String stakingCredentialString, AddressType type)
      throws IllegalAccessException, CborSerializationException {
    log.info(
        "[generateAddress] About to generate address from public key {} and network identifier {}",
        publicKeyString, networkIdentifierType);
    {
      HdPublicKey paymentCredential = new HdPublicKey();
      paymentCredential.setKeyData(HexUtil.decodeHexString(publicKeyString));
      if (!ObjectUtils.isEmpty(type) && type.getValue().equals(AddressType.REWARD.getValue())) {
        return generateRewardAddress(networkIdentifierType, paymentCredential);
      }

      if (!ObjectUtils.isEmpty(type) && type.getValue().equals(AddressType.BASE.getValue())) {
        if (stakingCredentialString == null) {
          log.error("[constructionDerive] No staking key was provided for base address creation");
          throw new IllegalArgumentException("missingStakingKeyError");
        }
        HdPublicKey stakingCredential = new HdPublicKey();
        stakingCredential.setKeyData(HexUtil.decodeHexString(stakingCredentialString));
        return generateBaseAddress(networkIdentifierType, paymentCredential, stakingCredential);
      }

      return generateEnterpriseAddress(paymentCredential, networkIdentifierType);
    }
  }

  @Override
  public String generateRewardAddress(NetworkIdentifierType networkIdentifierType,
      HdPublicKey paymentCredential) throws CborSerializationException {
    log.info(
        "[generateRewardAddress] Deriving cardano reward address from valid public staking key");
    Address rewardAddress = AddressProvider.getRewardAddress(paymentCredential,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
    log.info("[generateRewardAddress] reward address is {}", rewardAddress.toBech32());
    return rewardAddress.toBech32();
  }

  @Override
  public String generateBaseAddress(NetworkIdentifierType networkIdentifierType,
      HdPublicKey paymentCredential, HdPublicKey stakingCredential)
      throws CborSerializationException {
    log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
    Address baseAddress = AddressProvider.getBaseAddress(paymentCredential, stakingCredential,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
    log.info("generateAddress] base address is {}", baseAddress.toBech32());
    return baseAddress.toBech32();
  }

  @Override
  public String generateEnterpriseAddress(HdPublicKey paymentCredential,
      NetworkIdentifierType networkIdentifierType) throws CborSerializationException {
    log.info("[generateAddress] Deriving cardano address from valid public key and staking key");
    Address entAddress = AddressProvider.getEntAddress(paymentCredential,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic()));
    log.info("generateAddress] base address is {}", entAddress.toBech32());
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
  public Double calculateTxSize(NetworkIdentifierType networkIdentifierType,
      ArrayList<Operation> operations, Integer ttl, DepositParameters depositParameters)
      throws IOException, AddressExcepion, CborSerializationException, CborException {
//            const{
//            bytes, addresses, metadata
//        }
    UnsignedTransaction unsignedTransaction = createUnsignedTransaction(
        networkIdentifierType,
        operations,
        ttl,
        !ObjectUtils.isEmpty(depositParameters) ? depositParameters
            : new DepositParameters(Const.DEFAULT_POOL_DEPOSIT.toString(), Const.DEFAULT_KEY_DEPOSIT.toString())
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
          throw new IllegalArgumentException("invalidAddressError" + address);
        }).collect(Collectors.toList());

    String transaction = buildTransaction(unsignedTransaction.getBytes(), signaturesList,
        unsignedTransaction.getMetadata());
    // eslint-disable-next-line no-magic-numbers
    return (double) (transaction.length()
        / 2); // transaction is returned as an hex string and we need size in bytes
  }

  @Override
  public String buildTransaction(String unsignedTransaction,
      List<Signatures> signaturesList, String transactionMetadata) {
    log.info("[buildTransaction] About to signed a transaction with {} signatures",
        signaturesList.size());
    TransactionWitnessSet witnesses = getWitnessesForTransaction(signaturesList);
    try {
      log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
      //co.nstant.in.cbor.model.Map transactionBodyMap = new co.nstant.in.cbor.model.Map();
      DataItem[] dataItems = CborSerializationUtil.deserialize(
          HexUtil.decodeHexString(unsignedTransaction));
      //transactionBodyMap.put(new UnicodeString("unsignedTransaction"), new UnicodeString(unsignedTransaction));
      TransactionBody transactionBody = TransactionBody.deserialize(
          (co.nstant.in.cbor.model.Map) dataItems[0]);
      log.info(
          "[buildTransaction] Creating transaction using transaction body and extracted witnesses");
      AuxiliaryData auxiliaryData = null;
      if (!ObjectUtils.isEmpty(transactionMetadata)) {
        log.info("[buildTransaction] Adding transaction metadata");
        co.nstant.in.cbor.model.Map auxiliaryDataMap = new co.nstant.in.cbor.model.Map();
        auxiliaryDataMap.put(new UnicodeString("transactionMetadata"),
            new ByteString(hexStringToBuffer(transactionMetadata)));
        auxiliaryData = AuxiliaryData.deserialize(auxiliaryDataMap);
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
      ;
      return bytesToHex(CborSerializationUtil.serialize(array));
    } catch (Exception error) {
      log.error(
          error.getMessage() + "[buildTransaction] There was an error building signed transaction");
      throw new IllegalArgumentException("cantBuildSignedTransaction");
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
      signaturesList.stream().forEach(signature -> {
        VerificationKey vkey = new VerificationKey();
        vkey.setCborHex(ObjectUtils.isEmpty(signature) ? null : signature.getPublicKey());
        //      const ed25519Signature: Ed25519Signature = scope.manage(
//                    Ed25519Signature.from_bytes(Buffer.from(signature.signature, 'hex'))
//            );
        if (!ObjectUtils.isEmpty(signature)) {
          if (!ObjectUtils.isEmpty(signature.getAddress())
              && getEraAddressTypeOrNull(signature.getAddress())
              == EraAddressType.Byron) {
            // byron case
            if (ObjectUtils.isEmpty(signature.getChainCode())) {
              log.error(
                  "[getWitnessesForTransaction] Missing chain code for byron address signature");
              throw new IllegalArgumentException("missingChainCodeError");
            }
            String byronAddress = null;
            byronAddress = signature.getAddress();

            BootstrapWitness bootstrap = new BootstrapWitness(
                HexUtil.decodeHexString(vkey.getCborHex()),
                HexUtil.decodeHexString(signature.getSignature()),
                //revise
                hexStringToBuffer(signature.getChainCode()),
                HexUtil.decodeHexString("a0"));
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
      if (vkeyWitnesses.size() > 0) {
        witnesses.setVkeyWitnesses(vkeyWitnesses);
      }
      if (bootstrapWitnesses.size() > 0) {
        witnesses.setBootstrapWitnesses(bootstrapWitnesses);
      }
      return witnesses;
    } catch (Exception error) {
      log.error(error.getMessage()
          + "[getWitnessesForTransaction] There was an error building witnesses set for transaction");
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
  public Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType,
      String address) {
    if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.Shelley)) {
      return new Signatures(Const.SHELLEY_DUMMY_SIGNATURE, Const.SHELLEY_DUMMY_PUBKEY, null,
          address);
    }
    if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.Byron)) {
      return new Signatures(Const.BYRON_DUMMY_SIGNATURE, Const.BYRON_DUMMY_PUBKEY,
          Const.CHAIN_CODE_DUMMY, address);
    }
    if (addressType.getValue().equals(AddressType.POOL_KEY_HASH.getValue())) {
      return new Signatures(Const.COLD_DUMMY_SIGNATURE, Const.COLD_DUMMY_PUBKEY, null,
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
    Map<String, Object> map = processOperations(networkIdentifierType, operations,
        depositParameters);

    log.info("[createUnsignedTransaction] About to create transaction body");
    List<TransactionInput> inputList = (List<TransactionInput>) map.get("transactionInputs");
    List<TransactionOutput> outputList = (List<TransactionOutput>) map.get("transactionOutputs");
    BigInteger fee = BigInteger.valueOf((Long) map.get("fee"));
    TransactionBody transactionBody = TransactionBody.builder().inputs(inputList)
        .outputs(outputList).fee(fee).ttl(ttl.longValue()).build();

    if (!ObjectUtils.isEmpty((AuxiliaryData) map.get("voteRegistrationMetadata"))) {
      log.info(
          "[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
      AuxiliaryData auxiliaryData = (AuxiliaryData) map.get("voteRegistrationMetadata");
      Array array=new Array();
      array.add(auxiliaryData.serialize());
      array.add(new Array());
      transactionBody.setAuxiliaryDataHash(Blake2bUtil.blake2bHash256(CborSerializationUtil.serialize(array)));
    }

    if (!((List<Certificate>) map.get("certificates")).isEmpty()) {
      transactionBody.setCerts((List<Certificate>) map.get("certificates"));
    }
    if (!((List<Withdrawal>) map.get("withdrawals")).isEmpty()) {
      transactionBody.setWithdrawals((List<Withdrawal>) map.get("withdrawals"));
    }
    co.nstant.in.cbor.model.Map mapCbor = transactionBody.serialize();
    if (ttl == 0) {
      mapCbor.put(new UnsignedInteger(3), new UnsignedInteger(0));
    }
    String transactionBytes = hexFormatter(CborSerializationUtil.serialize(mapCbor));
    log.info("[createUnsignedTransaction] Hashing transaction body");
//        String bodyHash = TransactionUtil.getTxHash(HexUtil.decodeHexString(transactionBytes));
    String bodyHash = com.bloxbean.cardano.client.util.HexUtil.encodeHexString(
        Blake2bUtil.blake2bHash256(
            com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(mapCbor)));
    UnsignedTransaction toReturn = new UnsignedTransaction(
        hexFormatter(HexUtil.decodeHexString(bodyHash)),
        transactionBytes, (Set<String>) map.get("addresses"), null);
    if (!ObjectUtils.isEmpty((AuxiliaryData) map.get("voteRegistrationMetadata"))) {
      AuxiliaryData auxiliaryData = (AuxiliaryData) map.get("voteRegistrationMetadata");
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
  public Map<String, Object> processOperations(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations, DepositParameters depositParameters) throws IOException {
    log.info("[processOperations] About to calculate fee");
    ProcessOperationsDto result = convert(networkIdentifierType, operations);
    double refundsSum =
        result.getStakeKeyDeRegistrationsCount() * Double.parseDouble(depositParameters.getKeyDeposit());
    double keyDepositsSum =
        result.getStakeKeyRegistrationsCount() * Double.parseDouble(depositParameters.getKeyDeposit());
    double poolDepositsSum =
        result.getPoolRegistrationsCount() * Double.parseDouble(depositParameters.getPoolDeposit());
    Map<String, Double> depositsSumMap = new HashMap<>();
    depositsSumMap.put("keyRefundsSum", refundsSum);
    depositsSumMap.put("keyDepositsSum", keyDepositsSum);
    depositsSumMap.put("poolDepositsSum", poolDepositsSum);

    long fee = calculateFee(result.getInputAmounts(), result.getOutputAmounts(),
        result.getWithdrawalAmounts(), depositsSumMap);
    log.info("[processOperations] Calculated fee:{}", fee);
    Map<String, Object> map = new HashMap<>();
    map.put("transactionInputs", result.getTransactionInputs());
    map.put("transactionOutputs", result.getTransactionOutputs());
    map.put("certificates", result.getCertificates());
    map.put("withdrawals", result.getWithdrawals());
    Set addresses = new HashSet<>();
    result.getAddresses().stream().forEach(a -> addresses.add(a));
    addresses.removeAll(Collections.singleton(null));
    map.put("addresses", addresses);
    map.put("fee", fee);
    map.put("voteRegistrationMetadata", result.getVoteRegistrationMetadata());
    return map;
  }

  @Override
  public Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts,
      ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap) {
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
    long fee = (long) (inputsSum + withdrawalsSum + depositsSumMap.get("keyRefundsSum") - outputsSum
        - depositsSumMap.get("keyDepositsSum") - depositsSumMap.get("poolDepositsSum"));
    if (fee < 0) {
      throw new IllegalArgumentException("outputsAreBiggerThanInputsError");
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
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      } catch (CborSerializationException e) {
        throw new RuntimeException(e);
      } catch (CborDeserializationException e) {
        throw new RuntimeException(e);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch (SignatureException e) {
        throw new RuntimeException(e);
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException(e);
      } catch (InvalidKeyException e) {
        throw new RuntimeException(e);
      }
      if (ObjectUtils.isEmpty(processor)) {
        log.error("[processOperations] Operation with id {} has invalid type",
            operation.getOperationIdentifier());
        throw new IllegalArgumentException("invalidOperationTypeError");
      }
    }
    ;
    return processor;
  }

  @Override
  public ProcessOperationsDto operationProcessor(Operation operation,
      NetworkIdentifierType networkIdentifierType,
      ProcessOperationsDto resultAccumulator, String type)
      throws IOException, CborSerializationException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {
    if (type.equals(OperationType.INPUT.getValue())) {
      resultAccumulator.getTransactionInputs().add(validateAndParseTransactionInput(operation));
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      resultAccumulator.getAddresses().add(
          ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      resultAccumulator.getInputAmounts().add(
          ObjectUtils.isEmpty(operation.getAmount()) ? null : operation.getAmount().getValue());
      return resultAccumulator;
    }
    if (type.equals(OperationType.OUTPUT.getValue())) {
      resultAccumulator.getTransactionOutputs().add(validateAndParseTransactionOutput(operation));
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      resultAccumulator.getOutputAmounts().add(
          ObjectUtils.isEmpty(operation.getAmount()) ? null : operation.getAmount().getValue());
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
      resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
      resultAccumulator.getAddresses().add((String) map.get("address"));
      double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();
      resultAccumulator.setStakeKeyDeRegistrationsCount(++stakeNumber);
      return resultAccumulator;
    }
    if (type.equals(OperationType.STAKE_DELEGATION.getValue())) {
      Map<String, Object> map = processOperationCertification(networkIdentifierType, operation);
      resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
      resultAccumulator.getAddresses().add((String) map.get("address"));
      return resultAccumulator;
    }
    if (type.equals(OperationType.WITHDRAWAL.getValue())) {
      Map<String, Object> map = processWithdrawal(networkIdentifierType, operation);
      Long withdrawalAmount = ObjectUtils.isEmpty(operation.getAmount()) ? null
          : Long.valueOf(operation.getAmount().getValue());
      resultAccumulator.getWithdrawalAmounts().add(withdrawalAmount);
      resultAccumulator.getWithdrawals()
          .add(new Withdrawal((String) map.get("address"), BigInteger.valueOf(withdrawalAmount)));
      resultAccumulator.getAddresses().add((String) map.get("address"));
      return resultAccumulator;
    }
    if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
      Map<String, Object> map = processPoolRegistration(networkIdentifierType, operation);
      resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
      resultAccumulator.getAddresses()
          .addAll((Collection<? extends String>) map.get("totalAddresses"));
      double poolNumber = resultAccumulator.getPoolRegistrationsCount();
      resultAccumulator.setPoolRegistrationsCount(++poolNumber);
      return resultAccumulator;
    }
    if (type.equals(OperationType.POOL_REGISTRATION_WITH_CERT.getValue())) {
      Map<String, Object> map = processPoolRegistrationWithCert(operation, networkIdentifierType);
      resultAccumulator.getCertificates().add((Certificate) map.get("certificate"));
      Set<String> set = (Set) map.get("address");
      resultAccumulator.getAddresses().addAll(set);
      double poolNumber = resultAccumulator.getPoolRegistrationsCount();
      resultAccumulator.setPoolRegistrationsCount(++poolNumber);
      return resultAccumulator;
    }
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

  Map<String, Object> processPoolRegistrationWithCert(Operation operation,
      NetworkIdentifierType networkIdentifierType)
      throws CborSerializationException {
    OperationMetadata operationMetadata =
        ObjectUtils.isEmpty(operation) ? null : operation.getMetadata();
    AccountIdentifier account = ObjectUtils.isEmpty(operation) ? null : operation.getAccount();
    Map<String, Object> map = validateAndParsePoolRegistrationCert(
        networkIdentifierType,
        ObjectUtils.isEmpty(operationMetadata) ? null : operationMetadata.getPoolRegistrationCert(),
        ObjectUtils.isEmpty(account) ? null : account.getAddress()
    );
    return map;
  }

  @Override
  public Map<String, Object> validateAndParsePoolRegistrationCert(
      NetworkIdentifierType networkIdentifierType, String poolRegistrationCert, String poolKeyHash)
      throws CborSerializationException {
    if (ObjectUtils.isEmpty(poolKeyHash)) {
      log.error(
          "[validateAndParsePoolRegistrationCert] no cold key provided for pool registration");
      throw new IllegalArgumentException("missingPoolKeyError");
    }
    if (ObjectUtils.isEmpty(poolRegistrationCert)) {
      log.error(
          "[validateAndParsePoolRegistrationCert] no pool registration certificate provided for pool registration"
      );
      throw new IllegalArgumentException("missingPoolCertError");
    }
    PoolRegistration parsedCertificate;
    try {
      DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
          HexUtil.decodeHexString(poolRegistrationCert));
      parsedCertificate = PoolRegistration.deserialize(dataItem);
    } catch (Exception error) {
      log.error("[validateAndParsePoolRegistrationCert] invalid pool registration certificate");
      throw new IllegalArgumentException("invalidPoolRegistrationCert" + error.getMessage());
    }
    if (ObjectUtils.isEmpty(parsedCertificate)) {
      log.error("[validateAndParsePoolRegistrationCert] invalid certificate type");
      throw new IllegalArgumentException("invalidPoolRegistrationCertType");
    }
    List<String> ownersAddresses = parsePoolOwners(networkIdentifierType.getValue(),
        parsedCertificate);
    String rewardAddress = parsePoolRewardAccount(networkIdentifierType.getValue(),
        parsedCertificate);
    Map<String, Object> map = new HashMap();
    Set<String> addresses = new HashSet<>();
    addresses.addAll(new HashSet<String>(ownersAddresses));
    addresses.add(poolKeyHash);
    addresses.add(rewardAddress);
    map.put("certificate", parsedCertificate);
    map.put("address", addresses);
    return map;

  }

  @Override
  public AuxiliaryData processVoteRegistration(Operation operation)
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {
    log.info("[processVoteRegistration] About to process vote registration");
    if (!ObjectUtils.isEmpty(operation) && ObjectUtils.isEmpty(operation.getMetadata())) {
      log.error("[processVoteRegistration] Vote registration metadata was not provided");
      throw new IllegalArgumentException("missingVoteRegistrationMetadata");
    }
    if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
        && ObjectUtils.isEmpty(operation.getMetadata().getVoteRegistrationMetadata())) {
      log.error("[processVoteRegistration] Vote registration metadata was not provided");
      throw new IllegalArgumentException("missingVoteRegistrationMetadata");
    }
    Map<String, Object> map = validateAndParseVoteRegistrationMetadata(
        operation.getMetadata().getVoteRegistrationMetadata());

    CBORMetadata metadata = new CBORMetadata();

    CBORMetadataMap map2 = new CBORMetadataMap();
    byte[] votingKeyByte = HexUtil.decodeHexString(((String) map.get("votingKey")));
    map2.put(BigInteger.valueOf(CatalystDataIndexes.VOTING_KEY.getValue()), votingKeyByte);
    map2.put(BigInteger.valueOf(CatalystDataIndexes.STAKE_KEY.getValue()),
        HexUtil.decodeHexString(((String) map.get("stakeKey"))));
    map2.put(BigInteger.valueOf(CatalystDataIndexes.REWARD_ADDRESS.getValue()),
        HexUtil.decodeHexString(((String) map.get("rewardAddress"))));
    map2.put(BigInteger.valueOf(CatalystDataIndexes.VOTING_NONCE.getValue()),
        BigInteger.valueOf(((Integer) map.get("votingNonce"))));
    metadata.put(BigInteger.valueOf(Long.parseLong(CatalystLabels.DATA.getValue())), map2);
    CBORMetadataMap map3 = new CBORMetadataMap();
    map3.put(BigInteger.valueOf(1l), HexUtil.decodeHexString(
        operation.getMetadata().getVoteRegistrationMetadata().getVotingSignature()));
    metadata.put(BigInteger.valueOf(Long.parseLong(CatalystLabels.SIG.getValue())), map3);
    AuxiliaryData auxiliaryData = new AuxiliaryData();
    auxiliaryData.setMetadata(metadata);
    return auxiliaryData;
  }

  @Override
  public Map<String, Object> validateAndParseVoteRegistrationMetadata(
      VoteRegistrationMetadata voteRegistrationMetadata)
      throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {

    log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse voting key");
    String parsedVotingKey = validateAndParseVotingKey(
        voteRegistrationMetadata.getVotingKey());
    log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse stake key");
    if (ObjectUtils.isEmpty(voteRegistrationMetadata.getStakeKey().getHexBytes())) {
      throw new IllegalArgumentException("Staking key is required for this type of address");
    }
    if (!isKeyValid(voteRegistrationMetadata.getStakeKey().getHexBytes(),
        voteRegistrationMetadata.getStakeKey().getCurveType())) {
      throw new IllegalArgumentException("Invalid staking key format");
    }
    ;
    String parsedStakeKey = voteRegistrationMetadata.getStakeKey().getHexBytes();
    log.info(
        "[validateAndParseVoteRegistrationMetadata] About to validate and parse reward address");
    String parsedAddress = null;
    try {
      if (voteRegistrationMetadata.getRewardAddress().startsWith("addr")) {
        throw new IllegalArgumentException("Provided address is invalid");
      }
      Address address = new Address(voteRegistrationMetadata.getRewardAddress());
      parsedAddress = HexUtil.encodeHexString(address.getBytes());
    } catch (Exception exception) {
      throw new IllegalArgumentException("Provided address is invalid");
    }

    log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting nonce");
    if (voteRegistrationMetadata.getVotingNonce() <= 0) {
      log.error("[validateAndParseVoteRegistrationMetadata] Given voting nonce {} is invalid",
          voteRegistrationMetadata.getVotingNonce());
      throw new IllegalArgumentException("votingNonceNotValid");
    }

    log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting signature");
//        if (!isEd25519Signature(voteRegistrationMetadata.getVotingSignature())) {
//            log.error("[validateAndParseVoteRegistrationMetadata] Voting signature has an invalid format");
//            throw new IllegalArgumentException("invalidVotingSignature");
//        }
    if (ObjectUtils.isEmpty(voteRegistrationMetadata.getVotingSignature())) {
      throw new IllegalArgumentException("Invalid voting signature");
    }
    if (!isEd25519KeyHash(voteRegistrationMetadata.getVotingSignature())) {
      log.error(
          "[validateAndParseVoteRegistrationMetadata] Voting signature has an invalid format");
      throw new IllegalArgumentException("invalidVotingSignature");
    }
    String votingKeyHex = add0xPrefix(parsedVotingKey);
    String stakeKeyHex = add0xPrefix(parsedStakeKey);
    String rewardAddressHex = add0xPrefix(parsedAddress);
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
  public String validateAndParseVotingKey(PublicKey votingKey) {
    if (ObjectUtils.isEmpty(votingKey.getHexBytes())) {
      log.error("[validateAndParsePublicKey] Voting key not provided");
      throw new IllegalArgumentException("missingVotingKeyError");
    }
    if (!isKeyValid(votingKey.getHexBytes(), votingKey.getCurveType())) {
      log.info("[validateAndParsePublicKey] Voting key has an invalid format");
      throw new IllegalArgumentException("invalidVotingKeyFormat");
    }
    return votingKey.getHexBytes();
  }

  @Override
  public Map<String, Object> processPoolRetirement(Operation operation) {
    Map<String, Object> map = new HashMap<>();
    log.info("[processPoolRetiring] About to process operation of type ${operation.type}");
    if (!ObjectUtils.isEmpty(operation.getMetadata()) && !ObjectUtils.isEmpty(
        operation.getMetadata().getEpoch())
        && !ObjectUtils.isEmpty(operation.getAccount()) && !ObjectUtils.isEmpty(
        operation.getAccount().getAddress())) {
      double epoch = operation.getMetadata().getEpoch();
      byte[] keyHash = validateAndParsePoolKeyHash(
          ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
      map.put("certificate", new PoolRetirement(keyHash, Math.round(epoch)));
      map.put("poolKeyHash",
          ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
      return map;
    }
    log.error("[processPoolRetiring] Epoch operation metadata is missing");
    throw new IllegalArgumentException("missingMetadataParametersForPoolRetirement" + "Epoch");
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
  public Map<String, Object> processPoolRegistration(NetworkIdentifierType networkIdentifierType,
      Operation operation) {
    log.info("[processPoolRegistration] About to process pool registration operation");

    if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getMetadata())
        && ObjectUtils.isEmpty(operation.getMetadata().getPoolRegistrationParams())) {
      log.error("[processPoolRegistration] Pool_registration was not provided");
      throw new IllegalArgumentException("missingPoolRegistrationParameters");
    }
    PoolRegistrationParams poolRegistrationParams =
        ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getPoolRegistrationParams();

    Map<String, Object> map = validateAndParsePoolRegistationParameters(poolRegistrationParams);
    // eslint-disable-next-line camelcase
    byte[] poolKeyHash = validateAndParsePoolKeyHash(
        ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());

    log.info("[processPoolRegistration] About to validate and parse reward address");
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
        .pledge((BigInteger) map.get("pledge"))
        .cost((BigInteger) map.get("cost"))
        .margin(new UnitInterval((BigInteger) map.get("numerator"),
            (BigInteger) map.get("denominator")))
        .rewardAccount(HexUtil.encodeHexString(bech32Data.data))
        .poolOwners(owners)
        .relays(parsedRelays)
        .poolMetadataUrl(ObjectUtils.isEmpty(poolMetadata) ? null : poolMetadata.getUrl())
        .poolMetadataHash(ObjectUtils.isEmpty(poolMetadata) ? null : poolMetadata.getHash())
        .build();
    log.info("[processPoolRegistration] Generating Pool Registration certificate");
    Certificate certificate = wasmPoolRegistration;
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
    map.put("totalAddresses", totalAddresses);
    map.put("certificate", certificate);
    return map;
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
      throw new IllegalArgumentException("invalidPoolMetadataError");
    }
    return parsedMetadata;
  }

  @Override
  public List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> validateAndParsePoolRelays(
      List<Relay> relays) {
    if (relays.size() == 0) {
      throw new IllegalArgumentException("invalidPoolRelaysError Empty relays received");
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
        throw new IllegalArgumentException("invalidPoolRelayTypeError");
      }
      switch (type) {
        case "single_host_addr": {
          if (relay.getIpv4() != null) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if(!validator.isValidInet4Address(relay.getIpv4())) throw new IllegalArgumentException("ipv4 "+relay.getIpv4()+" invalid");
          }
          Integer port =
              ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10);
          if (port == null) {
            return new SingleHostAddr(0, parseIpv4(relay.getIpv4()), parseIpv6(relay.getIpv6()));
          }
          return new SingleHostAddr(port, parseIpv4(relay.getIpv4()), parseIpv6(relay.getIpv6()));
        }
        case "single_host_name":
          if (ObjectUtils.isEmpty(relay.getDnsName())) {
            throw new IllegalArgumentException("missingDnsNameError");
          }
          Integer port =
              ObjectUtils.isEmpty(relay.getPort()) ? null : Integer.parseInt(relay.getPort(), 10);
          if (port == null) {
            return new SingleHostName(0, relay.getDnsName());
          }
          return new SingleHostName(port, relay.getDnsName());
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
      throw new IllegalArgumentException(error.getMessage());
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
      byte[] parsedIp = bytes;
      return (Inet4Address) Inet4Address.getByAddress(parsedIp);
    }
    return null;
  }

  @Override
  public Inet6Address parseIpv6(String ip) throws UnknownHostException {
    if (!ObjectUtils.isEmpty(ip)) {
      String ipNew = ip.replace(":", "");
      byte[] parsedIp = HexUtil.decodeHexString(ipNew);
      return (Inet6Address) Inet6Address.getByAddress(parsedIp);
    }
    return null;
  }

  @Override
  public void validatePort(String port) {
    try {
      Integer parsedPort = Integer.parseInt(port, 10);
      if (!port.matches(Const.IS_POSITIVE_NUMBER) || parsedPort == null) {
        log.error("[validateAndParsePort] Invalid port {} received", port);
        throw new IllegalArgumentException(
            "invalidPoolRelaysError Invalid port {} received" + port);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "invalidPoolRelaysError Invalid port " + port + " received");
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
    if (ObjectUtils.isEmpty(rewardAddress)) {
      throw new IllegalArgumentException("invalidAddressError");
    }
    return rewardAddress;
  }

  @Override
  public Address parseToRewardAddress(String address) {
//        const wasmAddress = AddressProvider.() scope.manage(CardanoWasm.Address.from_bech32(address));
//        return scope.manage(CardanoWasm.RewardAddress.from_address(wasmAddress));
    return new Address(address);
  }

  @Override
  public Map<String, Object> validateAndParsePoolRegistationParameters(
      PoolRegistrationParams poolRegistrationParameters) {
    HashMap<String, Object> map = new HashMap<>();
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
      throw new IllegalArgumentException(
          "invalidPoolRegistrationParameters Missing margin parameter at pool registration parameters");
    }
//    if (!poolRegistrationParameters.getCost().matches(Const.IS_POSITIVE_NUMBER) ||
//        !poolRegistrationParameters.getPledge().matches(Const.IS_POSITIVE_NUMBER) ||
//        !numerator.matches(Const.IS_POSITIVE_NUMBER) ||
//        !denominator.matches(Const.IS_POSITIVE_NUMBER)) {
//      log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");
//      throw new IllegalArgumentException(
//          "invalidPoolRegistrationParameters Given value is invalid");
//    }
    if (!poolRegistrationParameters.getCost().matches(Const.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");
      throw new IllegalArgumentException(
          "invalidPoolRegistrationParameters Given value " + poolRegistrationParameters.getCost()
              + " is invalid");
    }
    if (!poolRegistrationParameters.getPledge().matches(Const.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");
      throw new IllegalArgumentException(
          "invalidPoolRegistrationParameters Given value " + poolRegistrationParameters.getPledge()
              + " is invalid");
    }
    if (!numerator.matches(Const.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");
      throw new IllegalArgumentException(
          "invalidPoolRegistrationParameters Given value " + numerator + " is invalid");
    }
    if (!denominator.matches(Const.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");
      throw new IllegalArgumentException(
          "invalidPoolRegistrationParameters Given value " + denominator + " is invalid");
    }
    try {
      // eslint-disable-next-line unicorn/prevent-abbreviations
      map.put("cost", BigInteger.valueOf(Long.parseLong(poolRegistrationParameters.getCost())));
      map.put("pledge", BigInteger.valueOf(Long.parseLong(poolRegistrationParameters.getPledge())));
      map.put("numerator", BigInteger.valueOf(Long.parseLong(numerator)));
      map.put("denominator", BigInteger.valueOf(Long.parseLong(denominator)));

      return map;
    } catch (Exception error) {
      log.error("[validateAndParsePoolRegistationParameters] Given pool parameters are invalid");
      throw new IllegalArgumentException("invalidPoolRegistrationParameters" + error);
    }
  }

  @Override
  public Map<String, Object> processWithdrawal(NetworkIdentifierType networkIdentifierType,
      Operation operation) throws CborSerializationException {
    log.info("[processWithdrawal] About to process withdrawal");
    // eslint-disable-next-line camelcase
    StakeCredential credential = getStakingCredentialFromHex(
        ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getStakingCredential());
    HdPublicKey hdPublicKey = new HdPublicKey();
    if (operation.getMetadata() != null &&
        operation.getMetadata().getStakingCredential() != null &&
        operation.getMetadata().getStakingCredential().getHexBytes() != null) {
      hdPublicKey.setKeyData(
          HexUtil.decodeHexString(operation.getMetadata().getStakingCredential().getHexBytes()));
    }
    String address = generateRewardAddress(networkIdentifierType, hdPublicKey);
    HashMap<String, Object> map = new HashMap<>();
    HdPublicKey hdPublicKey1 = new HdPublicKey();
    hdPublicKey1.setKeyData(credential.getHash());
    map.put("reward", AddressProvider.getRewardAddress(hdPublicKey1,
        new Network(networkIdentifierType.getValue(), networkIdentifierType.getProtocolMagic())));
    map.put("address", address);
    return map;
  }

  @Override
  public Map<String, Object> processOperationCertification(
      NetworkIdentifierType networkIdentifierType, Operation operation)
      throws CborSerializationException {
    log.info(
        "[processOperationCertification] About to process operation of type ${operation.type}");
    // eslint-disable-next-line camelcase
    HashMap<String, Object> map = new HashMap<>();
    PublicKey publicKey = ObjectUtils.isEmpty(operation.getMetadata()) ? null
        : operation.getMetadata().getStakingCredential();
    StakeCredential credential = getStakingCredentialFromHex(publicKey);
    HdPublicKey hdPublicKey = new HdPublicKey();
    hdPublicKey.setKeyData(HexUtil.decodeHexString(publicKey.getHexBytes()));
    String address = generateRewardAddress(networkIdentifierType, hdPublicKey);
    if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
      // eslint-disable-next-line camelcase
      if (operation.getMetadata().getPoolKeyHash() == null) {
        throw new IllegalArgumentException("Pool key hash is required to operate");
      }
      Certificate certificate = new StakeDelegation(credential, new StakePoolId(
          ObjectUtils.isEmpty(operation.getMetadata()) ? null
              : HexUtil.decodeHexString(operation.getMetadata().getPoolKeyHash())));
      map.put("certificate", certificate);
      map.put("address", address);
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
    StakeCredential credential = getStakingCredentialFromHex(
        ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getStakingCredential());
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
    HdPublicKey hdPublicKey = new HdPublicKey();
    hdPublicKey.setKeyData(stakingKeyBuffer);
    return hdPublicKey;
  }

  @Override
  public Boolean isKeyValid(String publicKeyBytes, String curveType) {
    if (publicKeyBytes == null) {
      return false;
    }
    return publicKeyBytes.length() == Const.PUBLIC_KEY_BYTES_LENGTH && curveType.equals(
        Const.VALID_CURVE_TYPE);
  }

  @Override
  public TransactionInput validateAndParseTransactionInput(Operation input) {
    if (ObjectUtils.isEmpty(input.getCoinChange())) {
      log.error("[validateAndParseTransactionInput] Input has missing coin_change");
      throw new IllegalArgumentException(
          "transactionInputsParametersMissingError Input has missing coin_change field");
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
      throw new IllegalArgumentException("Input has invalid coin_identifier field");
    }
    if (ObjectUtils.isEmpty(transactionId) || ObjectUtils.isEmpty(index)) {
      log.error("[validateAndParseTransactionInput] Input has missing transactionId and index");
      throw new IllegalArgumentException(
          "transactionInputsParametersMissingError Input has invalid coin_identifier field");
    }
    String value = ObjectUtils.isEmpty(input.getAmount()) ? null : input.getAmount().getValue();
    if (ObjectUtils.isEmpty(value)) {
      log.error("[validateAndParseTransactionInput] Input has missing amount value field");
      throw new IllegalArgumentException(
          "transactionInputsParametersMissingError Input has missing amount value field");
    }
    if (value.matches(Const.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParseTransactionInput] Input has positive value");
      throw new IllegalArgumentException(
          "transactionInputsParametersMissingError Input has positive amount value");
    }
    return new TransactionInput(
        transactionId,
        Integer.parseInt(index));
  }

  @Override
  public TransactionOutput validateAndParseTransactionOutput(Operation output) {
    Object address;
    try {
      address = ObjectUtils.isEmpty(output.getAccount()) ? null
          : generateAddress(output.getAccount().getAddress());
    } catch (Exception error) {
      throw new IllegalArgumentException(
          "transactionOutputDeserializationError " + output.getAccount().getAddress()
              + error.getMessage());
    }
    if (ObjectUtils.isEmpty(address)) {
      log.error("[validateAndParseTransactionOutput] Output has missing address field");
      throw new IllegalArgumentException(
          "transactionOutputsParametersMissingError Output has missing address field");
    }
    String outputValue =
        ObjectUtils.isEmpty(output.getAmount()) ? null : output.getAmount().getValue();
    if (ObjectUtils.isEmpty(output.getAmount()) || outputValue == null) {
      log.error("[validateAndParseTransactionOutput] Output has missing amount value field");
      throw new IllegalArgumentException(
          "transactionOutputsParametersMissingError Output has missing amount value field");
    }
    if (!outputValue.matches(Const.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParseTransactionOutput] Output has negative or invalid value {}",
          outputValue);
      throw new IllegalArgumentException(
          "transactionOutputsParametersMissingError Output has negative amount value");
    }
    Value value = Value.builder().coin(BigInteger.valueOf(Long.valueOf(outputValue))).build();
    if (!ObjectUtils.isEmpty(output.getMetadata()) && !ObjectUtils.isEmpty(
        output.getMetadata().getTokenBundle())) {
      value.setMultiAssets(validateAndParseTokenBundle(output.getMetadata().getTokenBundle()));
    }
    Address address1;
    try {
      address1 = (Address) address;
      return new TransactionOutput(address1.getAddress(), value);
    } catch (Exception error) {
      return new TransactionOutput(((ByronAddress) address).getAddress(), value);
    }
  }

  @Override
  public Object generateAddress(String address) throws AddressExcepion {
    EraAddressType addressType = getEraAddressType(address);
    if (addressType == EraAddressType.Byron) {
      return new ByronAddress(address);
    }
    return new Address(address);
  }

  @Override
  public EraAddressType getEraAddressType(String address) {
    try {
      if (address.startsWith("addr") || address.startsWith("stake")) {
        return EraAddressType.Shelley;
      }
      new Address(address).toBech32();
      return EraAddressType.Byron;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<MultiAsset> validateAndParseTokenBundle(List<TokenBundleItem> tokenBundle) {
    List<MultiAsset> multiAssets = new ArrayList<>();
    tokenBundle.stream().forEach(tokenBundleItem -> {
      if (!isPolicyIdValid(tokenBundleItem.getPolicyId())) {
        log.error("[validateAndParseTokenBundle] PolicyId {} is not valid",
            tokenBundleItem.getPolicyId());
        throw new IllegalArgumentException(
            "transactionOutputsParametersMissingError PolicyId " + tokenBundleItem.getPolicyId()
                + "is not valid");
      }
      List<Asset> assets = new ArrayList<>();
      List<Asset> assetsCheck = new ArrayList<>();
      tokenBundleItem.getTokens().stream().forEach(token -> {
        if (!isTokenNameValid(token.getCurrency().getSymbol())) {
          log.error("validateAndParseTokenBundle] Token name {} is not valid",
              token.getCurrency().getSymbol());
          throw new IllegalArgumentException(
              "transactionOutputsParametersMissingError Token name " + token.getCurrency()
                  .getSymbol() + "is not valid");
        }
        String assetName = token.getCurrency().getSymbol();
        if (assetsCheck.stream().anyMatch(asset -> asset.getName().equals(assetName))) {
          log.error(
              "[validateAndParseTokenBundle] Token name {} has already been added for policy {}",
              token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
          throw new IllegalArgumentException(
              "transactionOutputsParametersMissingError Token name " + token.getCurrency()
                  .getSymbol() + " has already been added for policy "
                  + tokenBundleItem.getPolicyId() + "and will be overriden");
        }
        if (ObjectUtils.isEmpty(token.getValue()) || ObjectUtils.isEmpty(
            token.getValue().charAt(0))) {
          log.error(
              "[validateAndParseTokenBundle] Token with name {} for policy {} has no value or is empty",
              token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
          throw new IllegalArgumentException(
              "Token with name" + token.getCurrency().getSymbol() + "for policy"
                  + tokenBundleItem.getPolicyId() + "has no value or is empty");
        }
        if (!token.getValue().matches(Const.IS_POSITIVE_NUMBER)) {
          log.error("[validateAndParseTokenBundle] Asset {} has negative or invalid value {}",
              token.getCurrency().getSymbol(), token.getValue());
          throw new IllegalArgumentException(
              "transactionOutputsParametersMissingError Asset" + token.getCurrency().getSymbol()
                  + "has negative or invalid value " + token.getValue());
        }
        //revise
        if (token.getCurrency().getSymbol().equals("\\x")) {
          token.getCurrency().setSymbol("");
        }
        assets.add(new Asset(
            token.getCurrency().getSymbol().startsWith("0x") ?
                token.getCurrency().getSymbol() : "0x" + token.getCurrency().getSymbol(),
            BigInteger.valueOf(Long.valueOf(token.getValue()))));
        assetsCheck.add(new Asset(token.getCurrency().getSymbol(),
            BigInteger.valueOf(Long.valueOf(token.getValue()))));
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
    return ObjectUtils.isEmpty(toCheck) ? false : toCheck.equals(Const.EMPTY_HEX);
  }

  @Override
  public byte[] hexStringToBuffer(String input) {
    return isEmptyHexString(input) ? HexUtil.decodeHexString("") : HexUtil.decodeHexString(input);
  }

  @Override
  public NetworkIdentifierType getNetworkIdentifierByRequestParameters(
      NetworkIdentifier networkRequestParameters) {
    if (networkRequestParameters.getNetwork().equals(Const.MAINNET)) {
      return NetworkIdentifierType.CARDANO_MAINNET_NETWORK;
    }
    return NetworkIdentifierType.CARDANO_MAINNET_NETWORK;
  }

  @Override
  public boolean isAddressTypeValid(String type) {
    return Arrays.stream(AddressType.values()).anyMatch(a -> a.getValue().equals(type))
        || type.equals("") || type == null;
  }

  @Override
  public Amount mapAmount(String value, String symbol, Integer decimals,
      Metadata metadata) {
    return new Amount(value,
        new Currency(ObjectUtils.isEmpty(symbol) ? Const.ADA : hexStringFormatter(symbol),
            ObjectUtils.isEmpty(decimals) ? Const.ADA_DECIMALS : decimals, metadata), null);
  }

  @Override
  public String hexStringFormatter(String toFormat) {
    if (ObjectUtils.isEmpty(toFormat)) {
      return Const.EMPTY_HEX;
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
      throws CborSerializationException, CborException {
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
      throw new IllegalArgumentException("blockNotFoundError");
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
      throws JsonProcessingException, CborSerializationException, CborException {
    List<Operation> extraOperations = extraData.getOperations().stream()
        // eslint-disable-next-line camelcase
        .filter(operation -> {
              String coinAction = ObjectUtils.isEmpty(operation.getCoinChange()) ? null
                  : operation.getCoinChange().getCoinAction();
              boolean coinActionStatement =
                  ObjectUtils.isEmpty(coinAction) ? false : coinAction.equals(Const.COIN_SPENT_ACTION);
              boolean statement = coinActionStatement ||
                  Const.StakingOperations.contains(operation.getType()) ||
                  Const.PoolOperations.contains(operation.getType()) ||
                  Const.VoteOperations.contains(operation.getType());
              return statement;
            }
        ).collect(Collectors.toList());
    TransactionExtraData toEncode = new TransactionExtraData();
    toEncode.setOperations(extraOperations);
    if (!ObjectUtils.isEmpty(extraData.getTransactionMetadataHex())) {
      toEncode.setTransactionMetadataHex(extraData.getTransactionMetadataHex());
    }
    co.nstant.in.cbor.model.Map transactionExtraDataMap = new co.nstant.in.cbor.model.Map();
    Array operationArray = new Array();
    extraOperations.stream().forEach(operation -> {
      co.nstant.in.cbor.model.Map operationIdentifierMap = new co.nstant.in.cbor.model.Map();
      Long index = operation.getOperationIdentifier().getIndex();
      if (index != null) {
        operationIdentifierMap.put(new UnicodeString("index"),
            new UnsignedInteger(operation.getOperationIdentifier().getIndex()));
      }
      Long networkIndex = operation.getOperationIdentifier().getNetworkIndex();
      if (networkIndex != null) {
        operationIdentifierMap.put(new UnicodeString("network_index"),
            new UnsignedInteger(networkIndex));
      }
      Array rOperationArray = new Array();
      if (!ObjectUtils.isEmpty(operation.getRelatedOperations())) {
        operation.getRelatedOperations().stream().forEach(rOperation -> {
          co.nstant.in.cbor.model.Map operationIdentifierMapnew = new co.nstant.in.cbor.model.Map();
          if (operation.getOperationIdentifier().getIndex() != null) {
            operationIdentifierMapnew.put(new UnicodeString("index"),
                new UnsignedInteger(operation.getOperationIdentifier().getIndex()));
          }
          Long networkIndex2 = operation.getOperationIdentifier().getNetworkIndex();
          if (networkIndex2 != null) {
            operationIdentifierMapnew.put(new UnicodeString("network_index"),
                new UnsignedInteger(networkIndex2));
          }
          rOperationArray.add(operationIdentifierMapnew);
        });
      }
      co.nstant.in.cbor.model.Map accountIdentifierMap = new co.nstant.in.cbor.model.Map();
      if (operation.getAccount() != null) {
        if (operation.getAccount().getAddress() != null) {
          accountIdentifierMap.put(new UnicodeString("address"),
              new UnicodeString(operation.getAccount().getAddress()));
        }
        if (operation.getAccount().getSubAccount() != null) {
          co.nstant.in.cbor.model.Map subAccountIdentifierMap = new co.nstant.in.cbor.model.Map();
          subAccountIdentifierMap.put(new UnicodeString("address"),
              new UnicodeString(ObjectUtils.isEmpty(operation.getAccount().getSubAccount()) ? null
                  : operation.getAccount().getSubAccount().getAddress()));
          subAccountIdentifierMap.put(new UnicodeString("metadata"),
              ObjectUtils.isEmpty(operation.getAccount().getSubAccount()) ? null
                  : operation.getAccount().getSubAccount().getMetadata());
          accountIdentifierMap.put(new UnicodeString("sub_account"), subAccountIdentifierMap);
        }
        if (operation.getAccount().getMetadata() != null) {
          co.nstant.in.cbor.model.Map accIdMetadataMap = new co.nstant.in.cbor.model.Map();
          accIdMetadataMap.put(new UnicodeString("chain_code"),
              new UnicodeString(ObjectUtils.isEmpty(operation.getAccount().getMetadata()) ? null
                  : operation.getAccount().getMetadata().getChainCode()));
          accountIdentifierMap.put(new UnicodeString("metadata"), accIdMetadataMap);
        }
      }
      co.nstant.in.cbor.model.Map amountMap = getAmountMap(operation.getAmount());
      co.nstant.in.cbor.model.Map coinChangeMap = new co.nstant.in.cbor.model.Map();
      co.nstant.in.cbor.model.Map coinIdentifierMap = new co.nstant.in.cbor.model.Map();
      CoinChange coinChange =
          ObjectUtils.isEmpty(operation.getCoinChange()) ? null : operation.getCoinChange();
      CoinIdentifier coinIdentifier =
          ObjectUtils.isEmpty(coinChange) ? null : coinChange.getCoinIdentifier();
      coinIdentifierMap.put(new UnicodeString("identifier"),
          new UnicodeString(
              ObjectUtils.isEmpty(coinIdentifier) ? null : coinIdentifier.getIdentifier()));
      coinChangeMap.put(new UnicodeString("coin_identifier"), coinIdentifierMap);
      coinChangeMap.put(new UnicodeString("coin_action"),
          new UnicodeString(ObjectUtils.isEmpty(coinChange) ? null : coinChange.getCoinAction()));
      boolean operationMetadataCheck = ObjectUtils.isEmpty(operation.getMetadata());
      co.nstant.in.cbor.model.Map operationMap = new co.nstant.in.cbor.model.Map();

      if (operation.getOperationIdentifier() != null) {
        operationMap.put(new UnicodeString("operation_identifier"), operationIdentifierMap);
      }
      if (!ObjectUtils.isEmpty(operation.getRelatedOperations())) {
        operationMap.put(new UnicodeString("related_operations"), rOperationArray);
      }
      if (!ObjectUtils.isEmpty(operation.getType())) {
        operationMap.put(new UnicodeString("type"), new UnicodeString(operation.getType()));
      }
      if (!ObjectUtils.isEmpty(operation.getStatus())) {
        operationMap.put(new UnicodeString("status"), new UnicodeString(operation.getStatus()));
      }
      if (!ObjectUtils.isEmpty(operation.getAccount())) {
        operationMap.put(new UnicodeString("account"), accountIdentifierMap);
      }
      if (!ObjectUtils.isEmpty(operation.getAmount())) {
        operationMap.put(new UnicodeString("amount"), amountMap);
      }
      if (!ObjectUtils.isEmpty(operation.getCoinChange())) {
        operationMap.put(new UnicodeString("coin_change"), coinChangeMap);
      }

      if (!operationMetadataCheck) {
        co.nstant.in.cbor.model.Map oMetadataMap = new co.nstant.in.cbor.model.Map();
        OperationMetadata operationMetadata =
            ObjectUtils.isEmpty(operation.getMetadata()) ? null : operation.getMetadata();
        if (operationMetadata.getWithdrawalAmount() != null) {
          co.nstant.in.cbor.model.Map withdrawalAmount = getAmountMap(
              operationMetadata.getWithdrawalAmount());
          oMetadataMap.put(new UnicodeString("withdrawal_amount"), withdrawalAmount);
        }
        if (operationMetadata.getDepositAmount() != null) {
          co.nstant.in.cbor.model.Map depositAmount = getAmountMap(
              operationMetadata.getDepositAmount());
          oMetadataMap.put(new UnicodeString("deposit_amount"), depositAmount);
        }
        if (operationMetadata.getRefundAmount() != null) {
          co.nstant.in.cbor.model.Map refundAmount = getAmountMap(
              operationMetadata.getRefundAmount());
          oMetadataMap.put(new UnicodeString("refund_amount"), refundAmount);
        }

        if (operationMetadata.getStakingCredential() != null) {
          co.nstant.in.cbor.model.Map stakingCredentialMap = getPublicKeymap(
              operationMetadata.getStakingCredential());
          oMetadataMap.put(new UnicodeString("staking_credential"), stakingCredentialMap);
        }
        if (operationMetadata.getPoolKeyHash() != null) {
          oMetadataMap.put(new UnicodeString("pool_key_hash"),
              new UnicodeString(operationMetadata.getPoolKeyHash()));
        }
        if (operationMetadata.getEpoch() != null) {
          oMetadataMap.put(new UnicodeString("epoch"),
              new UnsignedInteger(operationMetadata.getEpoch()));
        }
        if (!operationMetadataCheck) {
          if (operation.getMetadata().getTokenBundle() != null) {
            Array tokenBundleArray = new Array();
            operation.getMetadata().getTokenBundle().stream().forEach(tokenbundle -> {
              if (tokenbundle != null) {
                co.nstant.in.cbor.model.Map tokenBundleItemMap = new co.nstant.in.cbor.model.Map();
                if (tokenbundle.getPolicyId() != null) {
                  tokenBundleItemMap.put(new UnicodeString("policyId"),
                      new UnicodeString(tokenbundle.getPolicyId()));
                }
                if (tokenbundle.getTokens() != null) {
                  Array tokensArray = new Array();
                  tokenbundle.getTokens().stream().forEach(amount -> {
                    if (amount != null) {
                      co.nstant.in.cbor.model.Map amountMapNext = getAmountMap(amount);
                      tokensArray.add(amountMapNext);
                    }
                  });
                  tokenBundleItemMap.put(new UnicodeString("tokens"), tokensArray);
                }
                tokenBundleArray.add(tokenBundleItemMap);
              }
            });
            oMetadataMap.put(new UnicodeString("tokenBundle"), tokenBundleArray);
          }
        }
        if (operationMetadata.getPoolRegistrationCert() != null) {
          oMetadataMap.put(new UnicodeString("poolRegistrationCert"),
              new UnicodeString(operationMetadata.getPoolRegistrationCert()));
        }

        if (operationMetadata.getPoolRegistrationParams() != null) {
          co.nstant.in.cbor.model.Map poolRegistrationParamsMap = new co.nstant.in.cbor.model.Map();
          PoolRegistrationParams poolRegistrationParams = operationMetadata.getPoolRegistrationParams();
          if (poolRegistrationParams.getVrfKeyHash() != null) {
            poolRegistrationParamsMap.put(new UnicodeString("vrfKeyHash"),
                new UnicodeString(poolRegistrationParams.getVrfKeyHash()));
          }
          if (poolRegistrationParams.getRewardAddress() != null) {
            poolRegistrationParamsMap.put(new UnicodeString("rewardAddress"),
                new UnicodeString(poolRegistrationParams.getRewardAddress()));
          }
          if (poolRegistrationParams.getPledge() != null) {
            poolRegistrationParamsMap.put(new UnicodeString("pledge"),
                new UnicodeString(poolRegistrationParams.getPledge()));
          }
          if (poolRegistrationParams.getCost() != null) {
            poolRegistrationParamsMap.put(new UnicodeString("cost"),
                new UnicodeString(poolRegistrationParams.getCost()));
          }
          Array poolOwnersArray = new Array();
          if (operationMetadata != null && !ObjectUtils.isEmpty(
              operationMetadata.getPoolRegistrationParams())) {
            if (operation.getMetadata().getPoolRegistrationParams().getPoolOwners() != null) {
              operation.getMetadata().getPoolRegistrationParams().getPoolOwners().stream()
                  .forEach(o -> {
                    DataItem dataItem = new UnicodeString(o);
                    poolOwnersArray.add(dataItem);
                  });
            }
          }
          poolRegistrationParamsMap.put(new UnicodeString("poolOwners"), poolOwnersArray);
          Array relaysArray = new Array();
          if (operationMetadata != null && !ObjectUtils.isEmpty(
              operationMetadata.getPoolRegistrationParams())) {
            if (operationMetadata.getPoolRegistrationParams().getRelays() != null) {
              operation.getMetadata().getPoolRegistrationParams().getRelays().stream()
                  .forEach(r -> {
                    if (r != null) {
                      co.nstant.in.cbor.model.Map relayMap = new co.nstant.in.cbor.model.Map();
                      if (r.getType() != null) {
                        relayMap.put(new UnicodeString("type"), new UnicodeString(r.getType()));
                      }
                      if (r.getIpv4() != null) {
                        relayMap.put(new UnicodeString("ipv4"), new UnicodeString(r.getIpv4()));
                      }
                      if (r.getIpv6() != null) {
                        relayMap.put(new UnicodeString("ipv6"), new UnicodeString(r.getIpv6()));
                      }
                      if (r.getDnsName() != null) {
                        relayMap.put(new UnicodeString("dnsName"),
                            new UnicodeString(r.getDnsName()));
                      }
                      if (r.getPort() != null) {
                        relayMap.put(new UnicodeString("port"), new UnicodeString(r.getPort()));
                      }
                      relaysArray.add(relayMap);
                    }
                  });
              poolRegistrationParamsMap.put(new UnicodeString("relays"), relaysArray);
            }
          }
          PoolMargin poolMargin = poolRegistrationParams.getMargin();
          if (poolMargin != null) {
            co.nstant.in.cbor.model.Map marginMap = new co.nstant.in.cbor.model.Map();
            if (poolMargin.getNumerator() != null) {
              marginMap.put(new UnicodeString("numerator"),
                  new UnicodeString(poolMargin.getNumerator()));
            }
            if (poolMargin.getDenominator() != null) {
              marginMap.put(new UnicodeString("denominator"),
                  new UnicodeString(poolMargin.getDenominator()));
            }
            poolRegistrationParamsMap.put(new UnicodeString("margin"), marginMap);
          }
          if (poolRegistrationParams.getMarginPercentage() != null) {
            poolRegistrationParamsMap.put(new UnicodeString("margin_percentage"),
                new UnicodeString(poolRegistrationParams.getMarginPercentage()));
          }
          co.nstant.in.cbor.model.Map poolMetadataMap = new co.nstant.in.cbor.model.Map();
          PoolMetadata poolMetadata = poolRegistrationParams.getPoolMetadata();
          if (poolMetadata != null) {
            if (poolMetadata.getUrl() != null) {
              poolMetadataMap.put(new UnicodeString("url"),
                  new UnicodeString(poolMetadata.getUrl()));
            }
            if (poolMetadata.getHash() != null) {
              poolMetadataMap.put(new UnicodeString("hash"),
                  new UnicodeString(poolMetadata.getHash()));
            }
            poolRegistrationParamsMap.put(new UnicodeString("poolMetadata"), poolMetadataMap);
          }
          oMetadataMap.put(new UnicodeString("poolRegistrationParams"), poolRegistrationParamsMap);
        }

        if (operationMetadata.getVoteRegistrationMetadata() != null) {
          co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = new co.nstant.in.cbor.model.Map();
          VoteRegistrationMetadata voteRegistrationMetadata =
              operationMetadataCheck ? null : operationMetadata.getVoteRegistrationMetadata();
          co.nstant.in.cbor.model.Map stakeKeyMap = getPublicKeymap(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getStakeKey());
          voteRegistrationMetadataMap.put(new UnicodeString("rewardAddress"), new UnicodeString(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getRewardAddress()));
          voteRegistrationMetadataMap.put(new UnicodeString("stakeKey"), stakeKeyMap);
          co.nstant.in.cbor.model.Map votingKeyMap = getPublicKeymap(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getVotingKey());
          voteRegistrationMetadataMap.put(new UnicodeString("votingKey"), votingKeyMap);
          Integer unsignedIntegerNumber = null;
          unsignedIntegerNumber = ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
              : voteRegistrationMetadata.getVotingNonce();
          UnsignedInteger unsignedInteger = null;
          if (unsignedIntegerNumber != null) {
            unsignedInteger = new UnsignedInteger(
                unsignedIntegerNumber);
          }
          if (unsignedInteger != null) {
            voteRegistrationMetadataMap.put(new UnicodeString("votingNonce"), unsignedInteger);
          }
          voteRegistrationMetadataMap.put(new UnicodeString("votingSignature"), new UnicodeString(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getVotingSignature()));
          oMetadataMap.put(new UnicodeString("voteRegistrationMetadata"),
              voteRegistrationMetadataMap);
        }
        if (!ObjectUtils.isEmpty(operation.getMetadata())) {
          operationMap.put(new UnicodeString("metadata"), oMetadataMap);
        }
      }
      operationArray.add(operationMap);
    });
    transactionExtraDataMap.put(new UnicodeString("operations"), operationArray);
    if (toEncode.getTransactionMetadataHex() != null) {
      transactionExtraDataMap.put(new UnicodeString("transactionMetadataHex"),
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
    DataItem dataItem=com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
        HexUtil.decodeHexString(encoded));
    Array array = (Array) dataItem ;
    return array;
  }

  @Override
  public co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey) {
    co.nstant.in.cbor.model.Map stakingCredentialMap = new co.nstant.in.cbor.model.Map();
    stakingCredentialMap.put(new UnicodeString("hex_bytes"),
        new UnicodeString(ObjectUtils.isEmpty(publicKey) ? null : publicKey.getHexBytes()));
    stakingCredentialMap.put(new UnicodeString("curve_type"),
        new UnicodeString(ObjectUtils.isEmpty(publicKey) ? null : publicKey.getCurveType()));
    return stakingCredentialMap;
  }

  @Override
  public co.nstant.in.cbor.model.Map getAmountMap(Amount amount) {
    co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
    if (!ObjectUtils.isEmpty(amount)) {
      if (amount.getValue() != null) {
        amountMap.put(new UnicodeString("value"), new UnicodeString(amount.getValue()));
      }
      if (amount.getCurrency() != null) {
        co.nstant.in.cbor.model.Map currencyMap = new co.nstant.in.cbor.model.Map();
        if (amount.getCurrency().getSymbol() != null) {
          currencyMap.put(new UnicodeString("symbol"),
              new UnicodeString(amount.getCurrency().getSymbol()));
        }
        if (amount.getCurrency().getDecimals() != null) {
          currencyMap.put(new UnicodeString("decimals"),
              new UnsignedInteger(amount.getCurrency().getDecimals()));
        }
        co.nstant.in.cbor.model.Map addedMetadataMap = new co.nstant.in.cbor.model.Map();
        addedMetadataMap.put(new UnicodeString("metadata"),
            new UnicodeString(ObjectUtils.isEmpty(amount.getCurrency().getMetadata()) ? null
                : amount.getCurrency().getMetadata().getPolicyId()));
        if (amount.getCurrency().getMetadata() != null) {
          currencyMap.put(new UnicodeString("metadata"), addedMetadataMap);
        }
        amountMap.put(new UnicodeString("currency"), currencyMap);
      }
      if (amount.getMetadata() != null) {
        amountMap.put(new UnicodeString("metadata"), amount.getMetadata());
      }
    }
    return amountMap;
  }

  @Override
  public List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
      Set<String> addresses) {
    return addresses.stream().map(
        address -> new SigningPayload(null, new AccountIdentifier(address), transactionBodyHash,
            SignatureType.ED25519)).collect(Collectors.toList());

  }

  @Override
  public TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map) {
    //separator
    TransactionExtraData transactionExtraData = new TransactionExtraData();
    if(map.get(
        new UnicodeString("transactionMetadataHex"))!=null){
      String transactionMetadataHex = ((UnicodeString) map.get(
          new UnicodeString("transactionMetadataHex"))).getString();
      transactionExtraData.setTransactionMetadataHex(transactionMetadataHex);
    }
    List<Operation> operations = new ArrayList<>();
    List<DataItem> operationsListMap = ((Array) map.get(
        new UnicodeString("operations"))).getDataItems();
    operationsListMap.stream().forEach(oDataItem -> {
      co.nstant.in.cbor.model.Map operationMap = (co.nstant.in.cbor.model.Map) oDataItem;
      Operation operation = new Operation();
      if(operationMap.get(new UnicodeString("operation_identifier"))!=null){
        co.nstant.in.cbor.model.Map operationIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(
            new UnicodeString("operation_identifier"));
        OperationIdentifier operationIdentifier = new OperationIdentifier();
        if(operationIdentifierMap.get(new UnicodeString("index"))!=null){
          operationIdentifier.setIndex(((UnsignedInteger) operationIdentifierMap.get(new UnicodeString("index"))).getValue()
              .longValue());
        }
        if(operationIdentifierMap.get(new UnicodeString("index"))!=null){
          operationIdentifier.setNetworkIndex(((UnsignedInteger) operationIdentifierMap.get(new UnicodeString("network_index"))).getValue()
              .longValue());
        };
        operation.setOperationIdentifier(operationIdentifier);
      }
      if(operationMap.get(new UnicodeString("related_operations"))!=null){
        List<OperationIdentifier> relatedOperations = new ArrayList<>();
        List<DataItem> relatedOperationsDI = ((Array) operationMap.get(
            new UnicodeString("related_operations"))).getDataItems();
        relatedOperationsDI.stream().forEach(rDI -> {
          co.nstant.in.cbor.model.Map operationIdentifierMap2 = (co.nstant.in.cbor.model.Map) rDI;

          OperationIdentifier operationIdentifier2 = new OperationIdentifier();
          if(operationIdentifierMap2.get(new UnicodeString("index"))!=null){
            operationIdentifier2.setIndex(((UnsignedInteger) operationIdentifierMap2.get(new UnicodeString("index"))).getValue()
                .longValue());
          }
          if(operationIdentifierMap2.get(new UnicodeString("index"))!=null){
            operationIdentifier2.setNetworkIndex(((UnsignedInteger) operationIdentifierMap2.get(new UnicodeString("network_index"))).getValue()
                .longValue());
          };
          relatedOperations.add(operationIdentifier2);
        });
        operation.setRelatedOperations(relatedOperations);
      }
      if(operationMap.get(new UnicodeString("type"))!=null){
        String type = ((UnicodeString) (operationMap.get(new UnicodeString("type")))).getString();
        operation.setType(type);
      }
      if(operationMap.get(new UnicodeString("status"))!=null){
        String status = ((UnicodeString) (operationMap.get(
            new UnicodeString("status")))).getString();
        operation.setStatus(status);
      }
      if(operationMap.get(new UnicodeString("account"))!=null){
        AccountIdentifier accountIdentifier = new AccountIdentifier();
        co.nstant.in.cbor.model.Map accountIdentifierMap = (co.nstant.in.cbor.model.Map) operationMap.get(
            new UnicodeString("account"));
        if(accountIdentifierMap.get(
            new UnicodeString("address"))!=null){
          String address = ((UnicodeString) accountIdentifierMap.get(
              new UnicodeString("address"))).getString();
          accountIdentifier.setAddress(address);
        }
        if(accountIdentifierMap.get(new UnicodeString("sub_account"))!=null){
          co.nstant.in.cbor.model.Map subAccountIdentifierMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
              new UnicodeString("sub_account"));
          SubAccountIdentifier subAccountIdentifier = new SubAccountIdentifier();
          if(subAccountIdentifierMap.get(new UnicodeString("address"))!=null){
            String addressSub = ((UnicodeString) (subAccountIdentifierMap.get(
                new UnicodeString("address")))).getString();
            subAccountIdentifier.setAddress(addressSub);
          }
          if(subAccountIdentifierMap.get(new UnicodeString("metadata"))!=null){
            co.nstant.in.cbor.model.Map metadataSub = (co.nstant.in.cbor.model.Map) (subAccountIdentifierMap.get(
                new UnicodeString("metadata")));
            subAccountIdentifier.setMetadata(metadataSub);
          }
          accountIdentifier.setSubAccount(subAccountIdentifier);
        }
        if(accountIdentifierMap.get(new UnicodeString("metadata"))!=null){
          co.nstant.in.cbor.model.Map accountIdentifierMetadataMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
              new UnicodeString("metadata"));
          AccountIdentifierMetadata accountIdentifierMetadata = new AccountIdentifierMetadata();
          if(accountIdentifierMetadataMap.get(new UnicodeString("chain_code"))!=null){
            String chainCode = ((UnicodeString) (accountIdentifierMetadataMap.get(
                new UnicodeString("chain_code")))).getString();
            accountIdentifierMetadata.setChainCode(chainCode);
          }
          accountIdentifier.setMetadata(accountIdentifierMetadata);
        }
        operation.setAccount(accountIdentifier);
      }
      if(operationMap.get(new UnicodeString("amount"))!=null){
        co.nstant.in.cbor.model.Map amountMap = (co.nstant.in.cbor.model.Map) operationMap.get(
            new UnicodeString("amount"));
        Amount amount = getAmountFromMap(amountMap);
        operation.setAmount(amount);
      }
      if(operationMap.get(new UnicodeString("coin_change"))!=null){
        co.nstant.in.cbor.model.Map coinChangeMap = (co.nstant.in.cbor.model.Map) operationMap.get(
            new UnicodeString("coin_change"));
        CoinChange coinChange = new CoinChange();
        if(coinChangeMap.get(new UnicodeString("coin_action"))!=null){
          String coinAction = ((UnicodeString) coinChangeMap.get(
              new UnicodeString("coin_action"))).getString();
          coinChange.setCoinAction(coinAction);
        }
        if(coinChangeMap.get(new UnicodeString("coin_identifier"))!=null){
          CoinIdentifier coinIdentifier = new CoinIdentifier();
          co.nstant.in.cbor.model.Map coinIdentifierMap = (co.nstant.in.cbor.model.Map) coinChangeMap.get(
              new UnicodeString("coin_identifier"));
          String identifier = ((UnicodeString) coinIdentifierMap.get(
              new UnicodeString("identifier"))).getString();
          coinIdentifier.setIdentifier(identifier);
          coinChange.setCoinIdentifier(coinIdentifier);
        }
        operation.setCoinChange(coinChange);
      }
      if(operationMap.get(new UnicodeString("metadata"))!=null){
        co.nstant.in.cbor.model.Map metadataMap = (co.nstant.in.cbor.model.Map) operationMap.get(
            new UnicodeString("metadata"));
        OperationMetadata operationMetadata = new OperationMetadata();
        if(metadataMap.get(new UnicodeString("withdrawal_amount"))!=null){
          co.nstant.in.cbor.model.Map withdrawalAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
              new UnicodeString("withdrawal_amount"));
          Amount amountW = getAmountFromMap(withdrawalAmountMap);
          operationMetadata.setWithdrawalAmount(amountW);
        }
        if(metadataMap.get(new UnicodeString("deposit_amount"))!=null){
          co.nstant.in.cbor.model.Map depositAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
              new UnicodeString("deposit_amount"));
          Amount amountD = getAmountFromMap(depositAmountMap);
          operationMetadata.setWithdrawalAmount(amountD);
        }
        if(metadataMap.get(
            new UnicodeString("refund_amount"))!=null){
          co.nstant.in.cbor.model.Map refundAmountMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
              new UnicodeString("refund_amount"));
          Amount amountR = getAmountFromMap(refundAmountMap);
          operationMetadata.setWithdrawalAmount(amountR);
        }
        if(metadataMap.get(new UnicodeString("staking_credential"))!=null){
          co.nstant.in.cbor.model.Map stakingCredentialMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
              new UnicodeString("staking_credential"));
          PublicKey publicKey = getPublicKeyFromMap(stakingCredentialMap);
          operationMetadata.setStakingCredential(publicKey);
        }
        if(metadataMap.get(
            new UnicodeString("pool_key_hash"))!=null){
          String poolKeyHash = ((UnicodeString) metadataMap.get(
              new UnicodeString("pool_key_hash"))).getString();
          operationMetadata.setPoolKeyHash(poolKeyHash);
        }
        if(metadataMap.get(
            new UnicodeString("epoch"))!=null){
          Long epoch = ((UnsignedInteger) metadataMap.get(
              new UnicodeString("epoch"))).getValue().longValue();
          operationMetadata.setEpoch(epoch);
        }
        if(metadataMap.get(new UnicodeString("tokenBundle"))!=null){
          List<DataItem> tokenBundleArray = ((Array) metadataMap.get(
              new UnicodeString("tokenBundle"))).getDataItems();
          List<TokenBundleItem> tokenBundleItems = new ArrayList<>();
          tokenBundleArray.stream().forEach(t -> {
            co.nstant.in.cbor.model.Map tokenBundleMap = (co.nstant.in.cbor.model.Map) t;
            TokenBundleItem tokenBundleItem = new TokenBundleItem();
            if(tokenBundleMap.get(
                new UnicodeString("policyId"))!=null){
              String policyIdT = ((UnicodeString) tokenBundleMap.get(
                  new UnicodeString("policyId"))).getString();
              tokenBundleItem.setPolicyId(policyIdT);
            }

            List<Amount> tokenAList = new ArrayList<>();
            if(tokenBundleMap.get(
                new UnicodeString("tokens"))!=null){
              List<DataItem> tokensItem = ((Array) tokenBundleMap.get(
                  new UnicodeString("tokens"))).getDataItems();
              tokensItem.stream().forEach(tk -> {
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
        if(metadataMap.get(
            new UnicodeString("poolRegistrationCert"))!=null){
          String poolRegistrationCert = ((UnicodeString) metadataMap.get(
              new UnicodeString("poolRegistrationCert"))).getString();
          operationMetadata.setPoolRegistrationCert(poolRegistrationCert);
        }
        if(metadataMap.get(new UnicodeString("poolRegistrationParams"))!=null){
          co.nstant.in.cbor.model.Map poolRegistrationParamsMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
              new UnicodeString("poolRegistrationParams"));
          PoolRegistrationParams poolRegistrationParams = new PoolRegistrationParams();
          if(poolRegistrationParamsMap.get(
              new UnicodeString("vrfKeyHash"))!=null){
            String vrfKeyHash = ((UnicodeString) poolRegistrationParamsMap.get(
                new UnicodeString("vrfKeyHash"))).getString();
            poolRegistrationParams.setVrfKeyHash(vrfKeyHash);
          }
          if(poolRegistrationParamsMap.get(
              new UnicodeString("rewardAddress"))!=null){
            String rewardAddress = ((UnicodeString) poolRegistrationParamsMap.get(
                new UnicodeString("rewardAddress"))).getString();
            poolRegistrationParams.setRewardAddress(rewardAddress);
          }
          if(poolRegistrationParamsMap.get(new UnicodeString("pledge"))!=null){
            String pledge = ((UnicodeString) poolRegistrationParamsMap.get(
                new UnicodeString("pledge"))).getString();
            poolRegistrationParams.setPledge(pledge);
          }
          if(poolRegistrationParamsMap.get(
              new UnicodeString("cost"))!=null){
            String cost = ((UnicodeString) poolRegistrationParamsMap.get(
                new UnicodeString("cost"))).getString();
            poolRegistrationParams.setCost(cost);
          }
          if(poolRegistrationParamsMap.get(
              new UnicodeString("poolOwners"))!=null){
            List<String> stringList = new ArrayList<>();
            List<DataItem> poolOwners = ((Array) poolRegistrationParamsMap.get(
                new UnicodeString("poolOwners"))).getDataItems();
            poolOwners.stream().forEach(p -> {
              if(p!=null){
                stringList.add(((UnicodeString) p).getString());
              }
            });
            poolRegistrationParams.setPoolOwners(stringList);
          }
          if(poolRegistrationParamsMap.get(new UnicodeString("relays"))!=null){
            List<Relay> relayList = new ArrayList<>();
            List<DataItem> relaysArray = ((Array) poolRegistrationParamsMap.get(
                new UnicodeString("relays"))).getDataItems();
            relaysArray.stream().forEach(rA -> {
              co.nstant.in.cbor.model.Map rAMap = (co.nstant.in.cbor.model.Map) rA;
              Relay relay = new Relay();
              if(rAMap.get(new UnicodeString("type"))!=null){
                String typeR = ((UnicodeString) rAMap.get(new UnicodeString("type"))).getString();
                relay.setType(typeR);
              }
              if(rAMap.get(new UnicodeString("ipv4"))!=null){
                String ipv4 = ((UnicodeString) rAMap.get(new UnicodeString("ipv4"))).getString();
                relay.setIpv4(ipv4);
              };
              if(rAMap.get(new UnicodeString("ipv6"))!=null){
                String ipv6 = ((UnicodeString) rAMap.get(new UnicodeString("ipv6"))).getString();
                relay.setIpv6(ipv6);
              }
              if(rAMap.get(new UnicodeString("dnsName"))!=null){
                String dnsName = ((UnicodeString) rAMap.get(
                    new UnicodeString("dnsName"))).getString();
                relay.setDnsName(dnsName);
              }
              relayList.add(relay);
            });
            poolRegistrationParams.setRelays(relayList);
          }
          if(poolRegistrationParamsMap.get(new UnicodeString("margin"))!=null){
            co.nstant.in.cbor.model.Map marginMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(
                new UnicodeString("margin"));
            PoolMargin poolMargin = new PoolMargin();
            if(marginMap.get(
                new UnicodeString("numerator"))!=null){
              String numerator = ((UnicodeString) marginMap.get(
                  new UnicodeString("numerator"))).getString();
              poolMargin.setNumerator(numerator);
            }
            if(marginMap.get(
                new UnicodeString("denominator"))!=null){
              String denominator = ((UnicodeString) marginMap.get(
                  new UnicodeString("denominator"))).getString();
              poolMargin.setDenominator(denominator);
            }
            poolRegistrationParams.setMargin(poolMargin);
          }
          if(poolRegistrationParamsMap.get(
              new UnicodeString("margin_percentage"))!=null){
            String marginPercentage = ((UnicodeString) poolRegistrationParamsMap.get(
                new UnicodeString("margin_percentage"))).getString();
            poolRegistrationParams.setMarginPercentage(marginPercentage);
          }
          if(poolRegistrationParamsMap.get(
              new UnicodeString("poolMetadata"))!=null){
            PoolMetadata poolMetadata = new PoolMetadata();
            co.nstant.in.cbor.model.Map poolMetadataMap = (co.nstant.in.cbor.model.Map) poolRegistrationParamsMap.get(
                new UnicodeString("poolMetadata"));
            if(poolMetadataMap.get(
                new UnicodeString("url"))!=null){
              String url = ((UnicodeString) poolMetadataMap.get(
                  new UnicodeString("url"))).getString();
              poolMetadata.setUrl(url);
            }
            if(poolMetadataMap.get(
                new UnicodeString("hash"))!=null){
              String hash = ((UnicodeString) poolMetadataMap.get(
                  new UnicodeString("hash"))).getString();
              poolMetadata.setHash(hash);
            }
            poolRegistrationParams.setPoolMetadata(poolMetadata);
          }
          operationMetadata.setPoolRegistrationParams(poolRegistrationParams);
        }
        if(metadataMap.get(new UnicodeString("voteRegistrationMetadata"))!=null){
          VoteRegistrationMetadata voteRegistrationMetadata = new VoteRegistrationMetadata();
          co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = (co.nstant.in.cbor.model.Map) metadataMap.get(
              new UnicodeString("voteRegistrationMetadata"));
          if(voteRegistrationMetadataMap.get(new UnicodeString("stakeKey"))!=null){
            co.nstant.in.cbor.model.Map stakeKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(
                new UnicodeString("stakeKey"));
            PublicKey publicKey1 = getPublicKeyFromMap(stakeKeyMap);
            voteRegistrationMetadata.setStakeKey(publicKey1);
          }
          if(voteRegistrationMetadataMap.get(
              new UnicodeString("votingKey"))!=null){
            co.nstant.in.cbor.model.Map votingKeyMap = (co.nstant.in.cbor.model.Map) voteRegistrationMetadataMap.get(
                new UnicodeString("votingKey"));
            ;
            PublicKey publicKey2 = getPublicKeyFromMap(votingKeyMap);
            voteRegistrationMetadata.setVotingKey(publicKey2);
          }
          if(voteRegistrationMetadataMap.get(
              new UnicodeString("rewardAddress"))!=null){
            String rewardAddress2 = ((UnicodeString) voteRegistrationMetadataMap.get(
                new UnicodeString("rewardAddress"))).getString();
            voteRegistrationMetadata.setRewardAddress(rewardAddress2);
          }
          if(voteRegistrationMetadataMap.get(
              new UnicodeString("votingSignature"))!=null){
            String votingSignature = ((UnicodeString) voteRegistrationMetadataMap.get(
                new UnicodeString("votingSignature"))).getString();
            voteRegistrationMetadata.setVotingSignature(votingSignature);
          }
          if(voteRegistrationMetadataMap.get(
              new UnicodeString("votingNonce"))!=null){
            int votingNonce = ((UnsignedInteger) voteRegistrationMetadataMap.get(
                new UnicodeString("votingNonce"))).getValue().intValue();
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
    if(stakingCredentialMap.get(
        new UnicodeString("hex_bytes"))!=null){
      String hexBytes = ((UnicodeString) stakingCredentialMap.get(
          new UnicodeString("hex_bytes"))).getString();
      publicKey.setHexBytes(hexBytes);
    }
    if(stakingCredentialMap.get(
        new UnicodeString("curveType"))!=null){
      String curveType = ((UnicodeString) stakingCredentialMap.get(
          new UnicodeString("curveType"))).getString();
      publicKey.setHexBytes(curveType);
    }
    return publicKey;
  }

  @Override
  public Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap) {
    Amount amount = new Amount();
    if(amountMap.get(new UnicodeString("value"))!=null){
      String value = ((UnicodeString) amountMap.get(new UnicodeString("value"))).getString();
      amount.setValue(value);
    }
    if(amountMap.get(new UnicodeString("metadata"))!=null){
      co.nstant.in.cbor.model.Map metadataAm = (co.nstant.in.cbor.model.Map) amountMap.get(
          new UnicodeString("metadata"));
      amount.setMetadata(metadataAm);
    }
    if(amountMap.get(
        new UnicodeString("currency"))!=null){
      co.nstant.in.cbor.model.Map currencyMap = (co.nstant.in.cbor.model.Map) amountMap.get(
          new UnicodeString("currency"));
      Currency currency = new Currency();
      if(currencyMap.get(new UnicodeString("symbol"))!=null){
        String symbol = ((UnicodeString) currencyMap.get(new UnicodeString("symbol"))).getString();
        currency.setSymbol(symbol);
      }
      if(currencyMap.get(
          new UnicodeString("decimals"))!=null){
        Integer decimals = ((UnsignedInteger) currencyMap.get(
            new UnicodeString("decimals"))).getValue()
            .intValue();
        currency.setDecimals(decimals);
      }

      if(currencyMap.get(new UnicodeString("metadata"))!=null){
        Metadata metadata = new Metadata();
        co.nstant.in.cbor.model.Map addedMetadataMap = (co.nstant.in.cbor.model.Map) currencyMap.get(
            new UnicodeString("metadata"));
        if(addedMetadataMap.get(new UnicodeString("policyId"))!=null){
          String policyId = ((UnicodeString) addedMetadataMap.get(
              new UnicodeString("policyId"))).getString();
          metadata.setPolicyId(policyId);
        }
        currency.setMetadata(metadata);
      }
      amount.setCurrency(currency);
    }
    return amount;
  }

  @Override
  public TransactionParsed parseUnsignedTransaction(NetworkIdentifierType networkIdentifierType,
      String transaction, TransactionExtraData extraData)
      throws CborDeserializationException, UnknownHostException, AddressExcepion, JsonProcessingException {
    try {
      log.info(transaction
          + "[parseUnsignedTransaction] About to create unsigned transaction from bytes");
      byte[] transactionBuffer = HexUtil.decodeHexString(transaction);
      co.nstant.in.cbor.model.Map transactionBodyMap = new co.nstant.in.cbor.model.Map();
      transactionBodyMap.put(new UnicodeString("transactionBuffer"),
          new ByteString(transactionBuffer));
      TransactionBody parsed = TransactionBody.deserialize(transactionBodyMap);
      log.info(
          extraData + "[parseUnsignedTransaction] About to parse operations from transaction body");
      List<Operation> operations = convert(parsed, extraData, networkIdentifierType.getValue());
      log.info(operations + "[parseUnsignedTransaction] Returning ${operations.length} operations");
      return new TransactionParsed(operations, new ArrayList<>());
    } catch (Exception error) {
      log.error(error
          + "[parseUnsignedTransaction] Cant instantiate unsigned transaction from transaction bytes");
      throw new IllegalArgumentException("cantCreateUnsignedTransactionFromBytes");
    }
  }

  @Override
  public TransactionParsed parseSignedTransaction(NetworkIdentifierType networkIdentifierType,
      String transaction, TransactionExtraData extraData) {
    try {
      byte[] transactionBuffer = HexUtil.decodeHexString(transaction);
      log.info("[parseSignedTransaction] About to create signed transaction from bytes");
      Transaction parsed = Transaction.deserialize(transactionBuffer);
      log.info("[parseSignedTransaction] About to parse operations from transaction body");
      List<Operation> operations = convert(parsed.getBody(), extraData,
          networkIdentifierType.getValue());
      log.info("[parseSignedTransaction] About to get signatures from parsed transaction");
      log.info(operations + "[parseSignedTransaction] Returning operations");
      List<String> accum = new ArrayList<>();
      extraData.getOperations().stream().forEach(o ->
          {
            try {
              accum.addAll(getSignerFromOperation(networkIdentifierType, o));
            } catch (CborSerializationException e) {
              throw new RuntimeException(e);
            }
          }
      );
      List<AccountIdentifier> accountIdentifierSigners = getUniqueAccountIdentifiers(accum);
      return new TransactionParsed(operations, accountIdentifierSigners);
    } catch (Exception error) {
      log.error(error
          + "[parseSignedTransaction] Cant instantiate signed transaction from transaction bytes");
      throw new IllegalArgumentException("cantCreateSignedTransactionFromBytes");
    }
  }

  @Override
  public List<String> getSignerFromOperation(NetworkIdentifierType networkIdentifierType,
      Operation operation) throws CborSerializationException {
    if (Const.PoolOperations.contains(operation.getType())) {
      return getPoolSigners(networkIdentifierType, operation);
    }
    if (!ObjectUtils.isEmpty(
        ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress())) {
      return new ArrayList<>(List.of(operation.getAccount().getAddress()));
    }
    if (operation.getType().equals(OperationType.VOTE_REGISTRATION.getValue())) {
      return new ArrayList<>();
    }
    StakeCredential credential = getStakingCredentialFromHex(
        ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getStakingCredential());
    HdPublicKey hdPublicKey = new HdPublicKey();
    hdPublicKey.setKeyData(credential.getHash());
    return new ArrayList<>(List.of(generateRewardAddress(networkIdentifierType, hdPublicKey)));
  }

  @Override
  public List<String> getPoolSigners(NetworkIdentifierType networkIdentifierType,
      Operation operation) throws CborSerializationException {
    List<String> signers = new ArrayList<>();
    switch (operation.getType()) {
      case "poolRegistration": {
        PoolRegistrationParams poolRegistrationParameters =
            ObjectUtils.isEmpty(operation.getMetadata()) ? null
                : operation.getMetadata().getPoolRegistrationParams();
        if (!ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null
            : operation.getAccount().getAddress())) {
          signers.add(operation.getAccount().getAddress());
        }
        if (!ObjectUtils.isEmpty(poolRegistrationParameters)) {
          signers.add(poolRegistrationParameters.getRewardAddress());
          signers.addAll(poolRegistrationParameters.getPoolOwners());
        }
        break;
      }
      case "poolRegistrationWithCert": {
        String poolCertAsHex = ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getPoolRegistrationCert();
        java.util.Map<String, Object> map = validateAndParsePoolRegistrationCert(
            networkIdentifierType,
            poolCertAsHex,
            ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()
        );
        signers.addAll((List<String>) map.get("addresses"));
        break;
      }
      // pool retirement case
      default: {
        if (!ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null
            : operation.getAccount().getAddress())) {
          signers.add(operation.getAccount().getAddress());
        }
        break;
      }
    }
    log.info("[getPoolSigners] About to return {} signers for {} operation", signers.size(),
        operation.getType());
    return signers;
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
  public List<Operation> convert(TransactionBody transactionBody, TransactionExtraData extraData,
      Integer network)
      throws UnknownHostException, JsonProcessingException, AddressExcepion, CborDeserializationException, CborException, CborSerializationException {
    List<Operation> operations = new ArrayList<>();
    List<TransactionInput> inputs = transactionBody.getInputs();
    List<TransactionOutput> outputs = transactionBody.getOutputs();
    log.info("[parseOperationsFromTransactionBody] About to parse ${inputs.len()} inputs");
    List<Operation> inputOperations = extraData.getOperations().stream()
        .filter(o -> o.getType().equals(OperationType.INPUT.getValue()))
        .collect(Collectors.toList());
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
      Operation outputParsed = parseOutputToOperation(output, (long) operations.size(),
          relatedOperations, address);
      operations.add(outputParsed);
    }

    List<Operation> certOps = extraData.getOperations().stream()
        .filter(o -> Const.StakePoolOperations.contains(o)
        ).collect(Collectors.toList());
    List<Operation> parsedCertOperations = parseCertsToOperations(transactionBody, certOps,
        network);
    operations.addAll(parsedCertOperations);
    List<Operation> withdrawalOps = extraData.getOperations().stream()
        .filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue()))
        .collect(Collectors.toList());
    Integer withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0
        : transactionBody.getWithdrawals().size();
    parseWithdrawalsToOperations(withdrawalOps, withdrawalsCount, operations, network);

    List<Operation> voteOp = extraData.getOperations().stream()
        .filter(o -> o.getType().equals(OperationType.VOTE_REGISTRATION.getValue()))
        .collect(Collectors.toList());
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
      throw new IllegalArgumentException("missingVoteRegistrationMetadata");
    }
    co.nstant.in.cbor.model.Map map = new co.nstant.in.cbor.model.Map();
    map.put(new UnicodeString("transactionMetadataHex"), new UnicodeString(transactionMetadataHex));
    AuxiliaryData transactionMetadata = AuxiliaryData.deserialize(map);
    co.nstant.in.cbor.model.Array metadataList = (co.nstant.in.cbor.model.Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
        transactionMetadata.getAuxiliaryDataHash());
    co.nstant.in.cbor.model.Map generalMetadata = (co.nstant.in.cbor.model.Map) metadataList.getDataItems()
        .get(0);
    co.nstant.in.cbor.model.Map data = (co.nstant.in.cbor.model.Map) generalMetadata.get(
        new UnsignedInteger(Long.valueOf(CatalystLabels.DATA.getValue())));
    if (ObjectUtils.isEmpty(data)) {
      throw new IllegalArgumentException("missingVoteRegistrationMetadata");
    }
    co.nstant.in.cbor.model.Map sig = (co.nstant.in.cbor.model.Map) generalMetadata.get(
        new UnsignedInteger(Long.valueOf(CatalystLabels.SIG.getValue())));
    if (ObjectUtils.isEmpty(sig)) {
      throw new IllegalArgumentException("invalidVotingSignature");
    }
    String rewardAddressP = ((UnicodeString) data.get(
        new UnsignedInteger(CatalystDataIndexes.REWARD_ADDRESS.getValue()))).getString();
//need to revise
    Address rewardAddress = getAddressFromHexString(
        remove0xPrefix(rewardAddressP)
    );
    if (rewardAddress == null) {
      throw new IllegalArgumentException("invalidAddressError");
    }

    VoteRegistrationMetadata parsedMetadata = new VoteRegistrationMetadata(
        new PublicKey(remove0xPrefix(((UnicodeString) data.get(
            new UnsignedInteger(CatalystDataIndexes.VOTING_KEY.getValue()))).getString()),
            CurveType.EDWARDS25519.getValue()),
        new PublicKey(remove0xPrefix(((UnicodeString) data.get(
            new UnsignedInteger(CatalystDataIndexes.STAKE_KEY.getValue()))).getString()),
            CurveType.EDWARDS25519.getValue()),
        rewardAddress.toBech32(),
        ((UnsignedInteger) data.get(
            new UnsignedInteger(CatalystDataIndexes.VOTING_NONCE.getValue()))).getValue()
            .intValue(),
        remove0xPrefix(((UnicodeString) sig.get(
            new UnsignedInteger(CatalystSigIndexes.VOTING_SIGNATURE.getValue()))).getString())
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
  public void parseWithdrawalsToOperations(List<Operation> withdrawalOps, Integer withdrawalsCount,
      List<Operation> operations, Integer network) throws CborSerializationException {
    log.info("[parseWithdrawalsToOperations] About to parse {} withdrawals", withdrawalsCount);
    for (int i = 0; i < withdrawalsCount; i++) {
      Operation withdrawalOperation = withdrawalOps.get(i);
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      StakeCredential credential = getStakingCredentialFromHex(
          withdrawalOperation.getMetadata().getStakingCredential());
      HdPublicKey hdPublicKey = new HdPublicKey();
      hdPublicKey.setKeyData(credential.getHash());
      String address = generateRewardAddress(NetworkIdentifierType.find(network), hdPublicKey);
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
        new Amount(value, new Currency(Const.ADA, Const.ADA_DECIMALS, null)),
        new OperationMetadata(new PublicKey(hex, CurveType.EDWARDS25519.getValue()))
    );
  }

  @Override
  public List<Operation> parseCertsToOperations(TransactionBody transactionBody,
      List<Operation> certOps, int network)
      throws UnknownHostException, JsonProcessingException, CborException, CborSerializationException {
    List<Operation> parsedOperations = new ArrayList<>();
    List<Certificate> certs = transactionBody.getCerts();
    Integer certsCount = ObjectUtils.isEmpty(certs) ? 0 : certs.size();
    log.info("[parseCertsToOperations] About to parse {} certs", certsCount);

    for (int i = 0; i < certsCount; i++) {
      Operation certOperation = certOps.get(i);
      if (Const.StakePoolOperations.contains(certOperation.getType())) {
        String hex = ObjectUtils.isEmpty(certOperation.getMetadata()) ? null
            : (ObjectUtils.isEmpty(certOperation.getMetadata().getStakingCredential()) ? null
                : certOperation.getMetadata().getStakingCredential().getHexBytes());
        if (!ObjectUtils.isEmpty(hex)) {
          log.error("[parseCertsToOperations] Staking key not provided");
          throw new IllegalArgumentException("missingStakingKeyError");
        }
        StakeCredential credential = getStakingCredentialFromHex(
            ObjectUtils.isEmpty(certOperation.getMetadata()) ? null
                : certOperation.getMetadata().getStakingCredential());
        HdPublicKey hdPublicKey = new HdPublicKey();
        hdPublicKey.setKeyData(credential.getHash());
        String address = generateRewardAddress(NetworkIdentifierType.find(network), hdPublicKey);
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
  public Operation parsePoolCertToOperation(Integer network, Certificate cert, Long index,
      String type)
      throws UnknownHostException, JsonProcessingException, CborSerializationException, CborException {
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
          operation.getMetadata()
              .setPoolRegistrationParams(parsePoolRegistration(network, poolRegistrationCert));
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
      PoolRegistration poolRegistration) throws UnknownHostException, CborSerializationException {
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
    return new PoolMetadata(poolRegistration.getPoolMetadataHash(),
        poolRegistration.getPoolMetadataUrl());
  }

  @Override
  public PoolMargin parsePoolMargin(PoolRegistration poolRegistration) {
    return new PoolMargin(poolRegistration.getMargin().getDenominator().toString(),
        poolRegistration.getMargin().getNumerator().toString());
  }

  @Override
  public List<Relay> parsePoolRelays(PoolRegistration poolRegistration)
      throws UnknownHostException {
    List<Relay> poolRelays = new ArrayList<>();
    List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> relays = poolRegistration.getRelays();
    Integer relaysCount = relays.size();
    for (int i = 0; i < relaysCount; i++) {
      com.bloxbean.cardano.client.transaction.spec.cert.Relay relay = relays.get(i);
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
        poolRelays.add(
            new Relay(RelayType.MULTI_HOST_NAME.getValue(), multiHostRelay.getDnsName()));
        continue;
      }
      if (!ObjectUtils.isEmpty(singleHostName)) {
        poolRelays.add(
            new Relay(RelayType.SINGLE_HOST_NAME.getValue(), singleHostName.getDnsName(),
                ObjectUtils.isEmpty(singleHostName.getPort()) ? null
                    : String.valueOf(singleHostName.getPort())));
        continue;
      }
      if (!ObjectUtils.isEmpty(singleHostAddr)) {
        Inet4Address ipv4 = parseIpv4(singleHostAddr.getIpv4().getHostAddress());
        Inet6Address ipv6 = parseIpv6(singleHostAddr.getIpv6().getHostAddress());
        poolRelays.add(new Relay(RelayType.SINGLE_HOST_ADDR.getValue(),
            String.valueOf(singleHostName.getPort())));
      }
    }
    return poolRelays;
  }

  @Override
  public List<String> parsePoolOwners(Integer network, PoolRegistration poolRegistration)
      throws CborSerializationException {
    List<String> poolOwners = new ArrayList<>();
    Set<String> owners = poolRegistration.getPoolOwners();
    Integer ownersCount = owners.size();
    for (int i = 0; i < ownersCount; i++) {
      String owner = new ArrayList<String>(owners).get(i);
      byte[] addressByte = new byte[29];
      addressByte[0] = -31;
      byte[] byteCop = HexUtil.decodeHexString(owner);
      System.arraycopy(byteCop, 0, addressByte, 1, addressByte.length - 2);
      Address address = new Address(addressByte);
      poolOwners.add(address.getAddress());
    }
    return poolOwners;
  }

  @Override
  public String parsePoolRewardAccount(Integer network, PoolRegistration poolRegistration)
      throws CborSerializationException {
    byte[] addressByte = new byte[29];
    addressByte[0] = 97;
    byte[] byteCop = HexUtil.decodeHexString(
        "bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb");
    System.arraycopy(byteCop, 0, addressByte, 1, addressByte.length - 1);
    String address = Bech32.encode(addressByte, "addr");
    return address;
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
      operation.getMetadata().setPoolKeyHash(HexUtil.decodeHexString(
          HexUtil.encodeHexString(delegationCert.getStakePoolId().getPoolKeyHash())).toString());
    }
    return operation;
  }

  @Override
  public Operation parseOutputToOperation(TransactionOutput output, Long index,
      List<OperationIdentifier> relatedOperations, String address) {
    OperationIdentifier operationIdentifier = new OperationIdentifier(index, null);
    AccountIdentifier account = new AccountIdentifier(address);
    Amount amount = new Amount(output.getValue().getCoin().toString(),
        new Currency(Const.ADA, Const.ADA_DECIMALS, null), null);
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
          .sorted((tokenA, tokenB) -> tokenA.getPolicyId().compareTo(tokenB.getPolicyId()))
          .collect(Collectors.toList());
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
        .sorted((assetA, assetB) -> assetA.getCurrency().getSymbol()
            .compareTo(assetB.getCurrency().getSymbol())).collect(Collectors.toList());
    return new TokenBundleItem(policyId, tokens);
  }

  @Override
  public Amount parseAsset(List<Asset> assets, String key) throws CborException {
// When getting the key we are obtaining a cbor encoded string instead of the actual name.
    // This might need to be changed in the serialization lib in the future
    String assetSymbol = hexFormatter(
        com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
            new ByteString(HexUtil.decodeHexString(key))));
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
      return network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()
          ? NonStakeAddressPrefix.MAIN.getValue() : NonStakeAddressPrefix.TEST.getValue();
    }
    return network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()
        ? addressPrefix.MAIN.getValue() : addressPrefix.TEST.getValue();
  }

  @Override
  public String parseAddress(String address, String addressPrefix) throws AddressExcepion {
//    ByronAddress byronAddress = new ByronAddress(
//        AddressUtil.bytesToBase58Address(HexUtil.decodeHexString(address)));
//    return !ObjectUtils.isEmpty(byronAddress) ? byronAddress.toBase58()
//        : (new Address(addressPrefix, HexUtil.decodeHexString(address))).toBech32();
    return address;
  }

  @Override
  public List<OperationIdentifier> getRelatedOperationsFromInputs(List<Operation> inputs) {
    return inputs.stream()
        .map(input -> new OperationIdentifier(input.getOperationIdentifier().getIndex(), null))
        .collect(Collectors.toList());
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
      if (parsed == null || body == null ||
          com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(body.serialize())
              == null) {
        hashBuffer = null;
      } else {
        hashBuffer = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
            body.serialize());
      }
      return hexFormatter(hashBuffer);
    } catch (Exception error) {
      log.error(error.getMessage()
          + "[getHashOfSignedTransaction] There was an error parsing signed transaction");
      throw new IllegalArgumentException("parseSignedTransactionError");
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
      owners.stream().forEach(owner -> {
        Address address = new Address(owner);
        Optional<byte[]> bytes = address.getDelegationHash();
        if (bytes.isPresent()) {
          parsedOwners.add(HexUtil.encodeHexString(bytes.get()));
        }
      });
    } catch (Exception error) {
      log.error("[validateAndParsePoolOwners] there was an error parsing pool owners");
      throw new IllegalArgumentException("invalidPoolOwnersError" + error.getMessage());
    }
    if (parsedOwners.size() != owners.size()) {
      throw new IllegalArgumentException(
          "invalidPoolOwnersError Invalid pool owners addresses provided");
    }
    return parsedOwners;
  }

  @Override
  public String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte aByte : bytes) {
      int decimal =
          (int) aByte & 0xff;               // bytes widen to int, need mask, prevent sign extension
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
