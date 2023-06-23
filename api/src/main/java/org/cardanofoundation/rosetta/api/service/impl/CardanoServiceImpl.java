package org.cardanofoundation.rosetta.api.service.impl;

import static java.math.BigInteger.*;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.model.Metadata;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessOperationsDto;
import org.cardanofoundation.rosetta.api.model.Signatures;
import org.cardanofoundation.rosetta.api.model.UnsignedTransaction;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistrationCertReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessOperationsReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessPoolRegistrationReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessWithdrawalReturnDto;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.util.ProcessContruction;
import org.cardanofoundation.rosetta.api.util.ValidateOfConstruction;


import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.AccountIdentifierMetadata;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.CoinChange;
import org.cardanofoundation.rosetta.api.model.CoinIdentifier;
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
import java.util.*;
import java.util.Map;


@Slf4j
@Service
public class CardanoServiceImpl implements CardanoService {

  final LedgerDataProviderService ledgerDataProviderService;

  public CardanoServiceImpl(LedgerDataProviderService ledgerDataProviderService) {
    this.ledgerDataProviderService = ledgerDataProviderService;
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
            : new DepositParameters(Constants.DEFAULT_POOL_DEPOSIT.toString(),
                Constants.DEFAULT_KEY_DEPOSIT.toString())
    );
    // eslint-disable-next-line consistent-return
    List<Signatures> signaturesList = (unsignedTransaction.getAddresses()).stream()
        .map(address -> {
          EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressType(address);
          if (eraAddressType != null) {
            return signatureProcessor(eraAddressType, null, address);
          }
          // since pool key hash are passed as address, ed25519 hashes must be included
          if (CardanoAddressUtils.isEd25519KeyHash(address)) {
            return signatureProcessor(null, AddressType.POOL_KEY_HASH, address);
          }
          throw ExceptionFactory.invalidAddressError(address);
        }).toList();

    String transaction = buildTransaction(unsignedTransaction.getBytes(), signaturesList,
        unsignedTransaction.getMetadata());
    // eslint-disable-next-line no-magic-numbers
    return ((double) transaction.length()
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
        Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
            HexUtil.decodeHexString(transactionMetadata));
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
      return CardanoAddressUtils.bytesToHex(CborSerializationUtil.serialize(array));
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
        EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressTypeOrNull(signature.getAddress());
        if (!ObjectUtils.isEmpty(signature)) {
          if (!ObjectUtils.isEmpty(signature.getAddress())
              && eraAddressType == EraAddressType.BYRON) {
            // byron case
            ValidateOfConstruction.validateChainCode(signature.getChainCode());
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
                CardanoAddressUtils.hexStringToBuffer(signature.getChainCode()),
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
    ProcessOperationsReturnDto processOperationsReturnDto = processOperations(networkIdentifierType,
        operations,
        depositParameters);

    log.info("[createUnsignedTransaction] About to create transaction body");
    BigInteger fee = valueOf(processOperationsReturnDto.getFee());
    TransactionBody transactionBody = TransactionBody.builder()
        .inputs(processOperationsReturnDto.getTransactionInputs())
        .outputs(processOperationsReturnDto.getTransactionOutputs()).fee(fee).ttl(ttl.longValue())
        .build();

    if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
      log.info(
          "[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
      AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
      Array array = new Array();
      array.add(auxiliaryData.serialize());
      array.add(new Array());
      transactionBody.setAuxiliaryDataHash(
          Blake2bUtil.blake2bHash256(CborSerializationUtil.serialize(array)));
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
    String transactionBytes = CardanoAddressUtils.hexFormatter(CborSerializationUtil.serialize(mapCbor));
    log.info("[createUnsignedTransaction] Hashing transaction body");
    String bodyHash = com.bloxbean.cardano.client.util.HexUtil.encodeHexString(
        Blake2bUtil.blake2bHash256(
            com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(mapCbor)));
    UnsignedTransaction toReturn = new UnsignedTransaction(
        CardanoAddressUtils.hexFormatter(HexUtil.decodeHexString(bodyHash)),
        transactionBytes, processOperationsReturnDto.getAddresses(), null);
    if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
      AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
      Array array = new Array();
      array.add(auxiliaryData.serialize());
      array.add(new Array());
      toReturn.setMetadata(CardanoAddressUtils.hex(CborSerializationUtil.serialize(array)));
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
        result.getStakeKeyDeRegistrationsCount() * Long.parseLong(
            depositParameters.getKeyDeposit());
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
      resultAccumulator.getTransactionInputs()
          .add(ValidateOfConstruction.validateAndParseTransactionInput(operation));
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      resultAccumulator.getAddresses().add(
          ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress());
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      resultAccumulator.getInputAmounts().add(ValidateOfConstruction.validateValueAmount(operation));
      return resultAccumulator;
    }
    if (type.equals(OperationType.OUTPUT.getValue())) {
      resultAccumulator.getTransactionOutputs()
          .add(ValidateOfConstruction.validateAndParseTransactionOutput(operation));
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      resultAccumulator.getOutputAmounts().add(ValidateOfConstruction.validateValueAmount(operation));
      return resultAccumulator;
    }
    if (type.equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
      resultAccumulator.getCertificates().add(ProcessContruction.processStakeKeyRegistration(operation));
      double stakeNumber = resultAccumulator.getStakeKeyRegistrationsCount();
      resultAccumulator.setStakeKeyRegistrationsCount(++stakeNumber);
      return resultAccumulator;
    }
    if (type.equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue())) {
      Map<String, Object> map = ProcessContruction.processOperationCertification(networkIdentifierType, operation);
      resultAccumulator.getCertificates().add((Certificate) map.get(Constants.CERTIFICATE));
      resultAccumulator.getAddresses().add((String) map.get(Constants.ADDRESS));
      double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();
      resultAccumulator.setStakeKeyDeRegistrationsCount(++stakeNumber);
      return resultAccumulator;
    }
    if (type.equals(OperationType.STAKE_DELEGATION.getValue())) {
      Map<String, Object> map = ProcessContruction.processOperationCertification(networkIdentifierType, operation);
      resultAccumulator.getCertificates().add((Certificate) map.get(Constants.CERTIFICATE));
      resultAccumulator.getAddresses().add((String) map.get(Constants.ADDRESS));
      return resultAccumulator;
    }
    if (type.equals(OperationType.WITHDRAWAL.getValue())) {
      ProcessWithdrawalReturnDto processWithdrawalReturnDto = ProcessContruction.processWithdrawal(
          networkIdentifierType, operation);
      String withdrawalAmountString = ValidateOfConstruction.validateValueAmount(operation);
      Long withdrawalAmount = Long.valueOf(withdrawalAmountString);
      resultAccumulator.getWithdrawalAmounts().add(withdrawalAmount);
      resultAccumulator.getWithdrawals()
          .add(new Withdrawal(processWithdrawalReturnDto.getReward().getAddress(),
              withdrawalAmount == null ? null : valueOf(withdrawalAmount)));
      resultAccumulator.getAddresses().add(processWithdrawalReturnDto.getAddress());
      return resultAccumulator;
    }
    if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
      ProcessPoolRegistrationReturnDto processPoolRegistrationReturnDto = ProcessContruction.processPoolRegistration(
          networkIdentifierType, operation);
      resultAccumulator.getCertificates().add(processPoolRegistrationReturnDto.getCertificate());
      resultAccumulator.getAddresses()
          .addAll(processPoolRegistrationReturnDto.getTotalAddresses());
      double poolNumber = resultAccumulator.getPoolRegistrationsCount();
      resultAccumulator.setPoolRegistrationsCount(++poolNumber);
      return resultAccumulator;
    }
    if (type.equals(OperationType.POOL_REGISTRATION_WITH_CERT.getValue())) {
      PoolRegistrationCertReturnDto dto = ProcessContruction.processPoolRegistrationWithCert(operation,
          networkIdentifierType);
      resultAccumulator.getCertificates().add(dto.getCertificate());
      Set<String> set = dto.getAddress();
      resultAccumulator.getAddresses().addAll(set);
      double poolNumber = resultAccumulator.getPoolRegistrationsCount();
      resultAccumulator.setPoolRegistrationsCount(++poolNumber);
      return resultAccumulator;
    }
    if (type.equals(OperationType.POOL_RETIREMENT.getValue())) {
      Map<String, Object> map = ProcessContruction.processPoolRetirement(operation);
      resultAccumulator.getCertificates().add((Certificate) map.get(Constants.CERTIFICATE));
      resultAccumulator.getAddresses().add((String) map.get(Constants.POOL_KEY_HASH));
      return resultAccumulator;
    }
    if (type.equals(OperationType.VOTE_REGISTRATION.getValue())) {
      AuxiliaryData voteRegistrationMetadata = ProcessContruction.processVoteRegistration(operation);
      resultAccumulator.setVoteRegistrationMetadata(voteRegistrationMetadata);
      return resultAccumulator;
    }
    return null;
  }

  @Override
  public NetworkIdentifierType getNetworkIdentifierByRequestParameters(
      NetworkIdentifier networkRequestParameters) {
    if (networkRequestParameters.getNetwork().equals(Constants.MAINNET)) {
      return NetworkIdentifierType.CARDANO_MAINNET_NETWORK;
    }
    if (networkRequestParameters.getNetwork().equals(Constants.PREPROD)) {
      return NetworkIdentifierType.CARDANO_PREPROD_NETWORK;
    }
    return NetworkIdentifierType.CARDANO_TESTNET_NETWORK;
  }

  @Override
  public boolean isAddressTypeValid(String type) {
    return Arrays.stream(AddressType.values()).anyMatch(a -> a.getValue().equals(type))
        || type.equals("");
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
          if (operation.getAccount().getSubAccount().getAddress() != null) {
            subAccountIdentifierMap.put(new UnicodeString(Constants.ADDRESS),
                new UnicodeString(operation.getAccount().getSubAccount().getAddress()));
          }
          if (operation.getAccount().getSubAccount().getMetadata() != null) {
            subAccountIdentifierMap.put(new UnicodeString(Constants.METADATA),
                operation.getAccount().getSubAccount().getMetadata());
          }
          accountIdentifierMap.put(new UnicodeString(Constants.SUB_ACCOUNT),
              subAccountIdentifierMap);
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
        operationMap.put(new UnicodeString(Constants.STATUS),
            new UnicodeString(operation.getStatus()));
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
                      relayMap.put(new UnicodeString(Constants.TYPE),
                          new UnicodeString(r.getType()));
                    }
                    if (r.getIpv4() != null) {
                      relayMap.put(new UnicodeString(Constants.IPV4),
                          new UnicodeString(r.getIpv4()));
                    }
                    if (r.getIpv6() != null) {
                      relayMap.put(new UnicodeString(Constants.IPV6),
                          new UnicodeString(r.getIpv6()));
                    }
                    if (r.getDnsName() != null) {
                      relayMap.put(new UnicodeString(Constants.DNSNAME),
                          new UnicodeString(r.getDnsName()));
                    }
                    if (r.getPort() != null) {
                      relayMap.put(new UnicodeString(Constants.PORT),
                          new UnicodeString(r.getPort()));
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
            poolRegistrationParamsMap.put(new UnicodeString(Constants.POOLMETADATA),
                poolMetadataMap);
          }
          oMetadataMap.put(new UnicodeString(Constants.POOLREGISTRATIONPARAMS),
              poolRegistrationParamsMap);
        }

        if (operationMetadata != null && operationMetadata.getVoteRegistrationMetadata() != null) {
          co.nstant.in.cbor.model.Map voteRegistrationMetadataMap = new co.nstant.in.cbor.model.Map();
          VoteRegistrationMetadata voteRegistrationMetadata =
              operationMetadata.getVoteRegistrationMetadata();
          co.nstant.in.cbor.model.Map stakeKeyMap = getPublicKeymap(
              ObjectUtils.isEmpty(voteRegistrationMetadata) ? null
                  : voteRegistrationMetadata.getStakeKey());
          voteRegistrationMetadataMap.put(new UnicodeString(Constants.REWARD_ADDRESS),
              new UnicodeString(
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
            voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_NONCE),
                unsignedInteger);
          }
          voteRegistrationMetadataMap.put(new UnicodeString(Constants.VOTING_SIGNATURE),
              new UnicodeString(
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
      getCurrencyMap(amount, amountMap);
      if (amount.getMetadata() != null) {
        amountMap.put(new UnicodeString(Constants.METADATA), amount.getMetadata());
      }
    }
    return amountMap;
  }

  public co.nstant.in.cbor.model.Map getAmountMapV2(Amount amount) {
    co.nstant.in.cbor.model.Map amountMap = new co.nstant.in.cbor.model.Map();
    if (!ObjectUtils.isEmpty(amount)) {
      getCurrencyMap(amount, amountMap);
      if (amount.getValue() != null) {
        amountMap.put(new UnicodeString(Constants.VALUE), new UnicodeString(amount.getValue()));
      }
      if (amount.getMetadata() != null) {
        amountMap.put(new UnicodeString(Constants.METADATA), amount.getMetadata());
      }
    }
    return amountMap;
  }

  public void getCurrencyMap(Amount amount, co.nstant.in.cbor.model.Map amountMap) {
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
          operationIdentifier.setIndex(((UnsignedInteger) operationIdentifierMap.get(
              new UnicodeString(Constants.INDEX))).getValue()
              .longValue());
        }
        if (operationIdentifierMap.get(new UnicodeString(Constants.NETWORK_INDEX)) != null) {
          operationIdentifier.setNetworkIndex(((UnsignedInteger) operationIdentifierMap.get(
              new UnicodeString(Constants.NETWORK_INDEX))).getValue()
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
            operationIdentifier2.setIndex(((UnsignedInteger) operationIdentifierMap2.get(
                new UnicodeString(Constants.INDEX))).getValue()
                .longValue());
          }
          if (operationIdentifierMap2.get(new UnicodeString(Constants.NETWORK_INDEX)) != null) {
            operationIdentifier2.setNetworkIndex(((UnsignedInteger) operationIdentifierMap2.get(
                new UnicodeString(Constants.NETWORK_INDEX))).getValue()
                .longValue());
          }
          relatedOperations.add(operationIdentifier2);
        });
        operation.setRelatedOperations(relatedOperations);
      }
      if (operationMap.get(new UnicodeString(Constants.TYPE)) != null) {
        String type = ((UnicodeString) (operationMap.get(
            new UnicodeString(Constants.TYPE)))).getString();
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
          if (subAccountIdentifierMap.get(new UnicodeString(Constants.METADATA)) != null) {
            co.nstant.in.cbor.model.Map metadataSub = (co.nstant.in.cbor.model.Map) (subAccountIdentifierMap.get(
                new UnicodeString(Constants.METADATA)));
            if (!metadataSub.getValues().isEmpty()) {
              subAccountIdentifier.setMetadata(metadataSub);
            }
          }
          accountIdentifier.setSubAccount(subAccountIdentifier);
        }
        if (accountIdentifierMap.get(new UnicodeString(Constants.METADATA)) != null) {
          co.nstant.in.cbor.model.Map accountIdentifierMetadataMap = (co.nstant.in.cbor.model.Map) accountIdentifierMap.get(
              new UnicodeString(Constants.METADATA));
          AccountIdentifierMetadata accountIdentifierMetadata = new AccountIdentifierMetadata();
          if (accountIdentifierMetadataMap.get(new UnicodeString(Constants.CHAIN_CODE)) != null) {
            String chainCode = null;

            if (accountIdentifierMetadataMap.get(new UnicodeString(Constants.CHAIN_CODE))
                .getMajorType().getValue() == MajorType.UNICODE_STRING.getValue()) {
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
                String typeR = ((UnicodeString) rAMap.get(
                    new UnicodeString(Constants.TYPE))).getString();
                relay.setType(typeR);
              }
              if (rAMap.get(new UnicodeString(Constants.IPV4)) != null) {
                String ipv4 = ((UnicodeString) rAMap.get(
                    new UnicodeString(Constants.IPV4))).getString();
                relay.setIpv4(ipv4);
              }
              if (rAMap.get(new UnicodeString(Constants.IPV6)) != null) {
                String ipv6 = ((UnicodeString) rAMap.get(
                    new UnicodeString(Constants.IPV6))).getString();
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
        String value = ((UnicodeString) amountMap.get(
            new UnicodeString(Constants.VALUE))).getString();
        amount.setValue(value);
      }
      if (amountMap.get(new UnicodeString(Constants.METADATA)) != null) {
        co.nstant.in.cbor.model.Map metadataAm = (co.nstant.in.cbor.model.Map) amountMap.get(
            new UnicodeString(Constants.METADATA));
        amount.setMetadata(metadataAm);
      }
      getCurrencyFromMap(amountMap, amount);
    }
    return amount;
  }

  public void getCurrencyFromMap(co.nstant.in.cbor.model.Map amountMap, Amount amount) {
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
        hashBuffer = Blake2bUtil.blake2bHash256(
            com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                body.serialize()));
      }
      return CardanoAddressUtils.hexFormatter(hashBuffer);
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

}
