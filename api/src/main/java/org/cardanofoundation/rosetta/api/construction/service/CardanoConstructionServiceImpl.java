package org.cardanofoundation.rosetta.api.construction.service;

import java.math.BigInteger;
import java.util.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody.TransactionBodyBuilder;
import com.bloxbean.cardano.client.util.HexUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperationsReturn;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.construction.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.CborArrayToTransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.OperationParseUtil;
import org.cardanofoundation.rosetta.common.util.ValidateParseUtil;

import static java.math.BigInteger.valueOf;
import static org.cardanofoundation.rosetta.common.util.Constants.DEFAULT_RELATIVE_TTL;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardanoConstructionServiceImpl implements CardanoConstructionService {

  private final LedgerBlockService ledgerBlockService;
  private final ProtocolParamService protocolParamService;
  private final OperationService operationService;
  private final RestTemplate restTemplate;

  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

  @Value("${cardano.rosetta.NODE_SUBMIT_API_PORT}")
  private int nodeSubmitApiPort;
  @Value("${cardano.rosetta.CARDANO_NODE_SUBMIT_HOST}")
  private String cardanoNodeSubmitHost;
  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;

  @Override
  public TransactionParsed parseTransaction(Network network, String transaction, boolean signed) {
    Array decodeTransaction = decodeTransaction(transaction);
    try {
      TransactionData convertedTr = CborArrayToTransactionData.convert(decodeTransaction, signed);
      List<Operation> operations = operationService.getOperationsFromTransactionData(convertedTr, network);
      List<AccountIdentifier> accountIdentifierSigners = new ArrayList<>();
      if (signed) {
        log.info("[parseSignedTransaction] About to get signatures from parsed transaction");
        List<String> accumulator = convertedTr.transactionExtraData().operations().stream()
            .map(o -> operationService.getSignerFromOperation(network, o))
            .flatMap(List::stream)
            .toList();
        accountIdentifierSigners = getUniqueAccountIdentifiers(accumulator);
      }
      return new TransactionParsed(operations, accountIdentifierSigners);
    } catch (CborException | CborDeserializationException | CborSerializationException error) {
      log.error("{} [parseTransaction] Cant instantiate transaction from transaction bytes",
          error.getMessage(), error);
      throw ExceptionFactory.invalidTransactionError();
    }
  }

  @Override
  public Integer checkOrReturnDefaultTtl(Integer relativeTtl) {
    return relativeTtl == null ? DEFAULT_RELATIVE_TTL : relativeTtl;
  }

  @Override
  public Array decodeTransaction(String encoded) {
    try {
      DataItem dataItem = CborSerializationUtil.deserialize(HexUtil.decodeHexString(encoded));
      return (Array) dataItem;
    } catch (Exception e) {
      log.error("[decodeTransaction] Cannot decode transaction bytes. Exception: {}",
          e.getMessage());
      throw ExceptionFactory.invalidTransactionError();
    }
  }

  @Override
  public Long calculateTtl(Long ttlOffset) {

    return offlineMode ? ttlOffset : ledgerBlockService.findLatestBlockIdentifier().getSlot() + ttlOffset;
  }

  @Override
  public Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) {
    try {
      return
          previousTxSize + CborSerializationUtil.serialize(new UnsignedInteger(updatedTtl)).length
              - CborSerializationUtil.serialize(new UnsignedInteger(previousTtl)).length;
    } catch (CborException e) {
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
  }

  @Override
  public Long calculateTxMinimumFee(Long transactionSize, ProtocolParams protocolParameters) {
    return protocolParameters.getMinFeeA() * transactionSize + protocolParameters.getMinFeeB();
  }

  @Override
  public Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType,
      String address) {
    if (eraAddressType != null && eraAddressType.equals(EraAddressType.SHELLEY)) {
      return new Signatures(Constants.SHELLEY_DUMMY_SIGNATURE, Constants.SHELLEY_DUMMY_PUBKEY, null,
          address);
    }
    if (eraAddressType != null && eraAddressType.equals(EraAddressType.BYRON)) {
      return new Signatures(Constants.BYRON_DUMMY_SIGNATURE, Constants.BYRON_DUMMY_PUBKEY,
          Constants.CHAIN_CODE_DUMMY, address);
    }
    if (addressType != null && AddressType.POOL_KEY_HASH.getValue().equals(addressType.getValue())) {
      return new Signatures(Constants.COLD_DUMMY_SIGNATURE, Constants.COLD_DUMMY_PUBKEY, null,
          address);
    }
    return null;
  }

  @Override
  public Integer calculateTxSize(Network network, List<Operation> operations, int ttl,
      DepositParameters depositParameters) {
    UnsignedTransaction unsignedTransaction;
    try {
      unsignedTransaction = createUnsignedTransaction(network, operations, ttl,
              depositParameters != null ? depositParameters :
              new DepositParameters(Constants.DEFAULT_KEY_DEPOSIT.toString(),
                  Constants.DEFAULT_POOL_DEPOSIT.toString()));
    } catch (CborSerializationException | AddressExcepion | CborException e) {
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
    List<Signatures> signaturesList = (unsignedTransaction.addresses()).stream()
        .map(this::extractSignaturesFromAddress)
        .toList();

    String transaction = buildTransaction(unsignedTransaction.bytes(), signaturesList,
        unsignedTransaction.metadata());
    // the String transaction represents Hex encoded bytes of the transaction.
    // To get the size of the transaction in bytes, we need to divide the length by two. Because every Hex character represents 4 bits.
    return (transaction.length() / 2);

  }

  @Override
  public String buildTransaction(String unsignedTransaction, List<Signatures> signaturesList,
      String transactionMetadata) {
    log.info("[buildTransaction] About to signed a transaction with {} signatures",
        signaturesList.size());
    CompletableFuture<TransactionBody> transactionBodyFuture =
        CompletableFuture.supplyAsync(() -> deserializeTransactionBody(unsignedTransaction), executorService);
    CompletableFuture<TransactionWitnessSet> witnessesFuture =
        CompletableFuture.supplyAsync(() -> getWitnessesForTransaction(signaturesList), executorService);
    CompletableFuture<AuxiliaryData> auxiliaryDataFuture =
        CompletableFuture.supplyAsync(() -> deserializeAuxiliaryData(transactionMetadata), executorService);
    try {
      log.info(
          "[buildTransaction] Creating transaction using transaction body and extracted witnesses");
      Transaction transaction = new Transaction();
      transaction.setBody(transactionBodyFuture.join());
      transaction.setAuxiliaryData(auxiliaryDataFuture.join());
      transaction.setWitnessSet(witnessesFuture.join());

      Array cborTransactionsArray = (Array) CborSerializationUtil.deserialize(transaction.serialize());

      if (transaction.getBody().getTtl() == 0) {
        log.warn("[buildTransaction] Setting ttl to 0 in transaction body");

        co.nstant.in.cbor.model.Map dataItem1 =
            (co.nstant.in.cbor.model.Map) cborTransactionsArray.getDataItems().getFirst();
        // Position of ttl in transaction body, it will be discarded while serialization if it's 0, but it needs to be in the Data map
        // otherwise a wrong hash will be produced.
        dataItem1.put(new UnsignedInteger(3), new UnsignedInteger(0));
        cborTransactionsArray.getDataItems().set(0, dataItem1);
      }

      if (!ObjectUtils.isEmpty(transactionMetadata)) {
        Array cborMetadataArray = new Array();
        cborMetadataArray.add(cborTransactionsArray.getDataItems().get(3));
        cborMetadataArray.add(new Array());
        cborTransactionsArray.getDataItems().set(3, cborMetadataArray);
      }
      return HexUtil.encodeHexString(
          com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(cborTransactionsArray));
    } catch (CborSerializationException e) {
      log.error("{} [buildTransaction] CborSerializationException while building transaction",
          e.getMessage());
      throw ExceptionFactory.generalSerializationError(e.getMessage());
    }
  }

  @Override
  public TransactionWitnessSet getWitnessesForTransaction(List<Signatures> signaturesList) {
    TransactionWitnessSet witnesses = new TransactionWitnessSet();
    ArrayList<VkeyWitness> vKeyWitnesses = new ArrayList<>();
    ArrayList<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();
    log.info("[getWitnessesForTransaction] Extracting witnesses from signatures");
    signaturesList.forEach(signature -> {
      if (signature == null) {
        return;
      }
      VerificationKey vKey = new VerificationKey(signature.publicKey());
      EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressType(signature.address());
        if (eraAddressType == EraAddressType.BYRON) {
          // byron case
          ValidateParseUtil.validateChainCode(signature.chainCode());
          ByronAddress byronAddress = new ByronAddress(signature.address());
          String byronAddressTail = HexUtil.encodeHexString(byronAddress.getBytes()).substring(72);
          String partOfByronAddress = new StringBuilder(byronAddressTail).reverse().delete(0, 12)
              .reverse().toString();
          BootstrapWitness bootstrap = new BootstrapWitness(
              HexUtil.decodeHexString(vKey.getCborHex()),
              HexUtil.decodeHexString(signature.signature()),
              //revise
              HexUtil.decodeHexString(signature.chainCode()),
              HexUtil.decodeHexString(partOfByronAddress));
          bootstrapWitnesses.add(bootstrap);
        } else {
          vKeyWitnesses.add(new VkeyWitness(HexUtil.decodeHexString(vKey.getCborHex()),
              HexUtil.decodeHexString(signature.signature())));
        }
    });
    log.info("[getWitnessesForTransaction] {} witnesses were extracted to sign transaction",
        vKeyWitnesses.size());
    if (!vKeyWitnesses.isEmpty()) {
      witnesses.setVkeyWitnesses(vKeyWitnesses);
    }
    if (!bootstrapWitnesses.isEmpty()) {
      witnesses.setBootstrapWitnesses(bootstrapWitnesses);
    }
    return witnesses;
  }

  @Override
  public UnsignedTransaction createUnsignedTransaction(Network network, List<Operation> operations, int ttl, DepositParameters depositParameters)
      throws CborSerializationException, AddressExcepion, CborException {

    log.info(
        "[createUnsignedTransaction] About to create an unsigned transaction with {} operations",
        operations.size());
    ProcessOperationsReturn opRetDto = processOperations(network, operations, depositParameters);

    log.info("[createUnsignedTransaction] About to create transaction body");
    TransactionBodyBuilder transactionBodyBuilder = TransactionBody.builder()
        .inputs(opRetDto.getTransactionInputs())
        .outputs(opRetDto.getTransactionOutputs())
        .fee(valueOf(opRetDto.getFee()))
        .ttl(ttl);

    if (opRetDto.getVoteRegistrationMetadata() != null) {
      log.info(
          "[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
      Array cborAuxDataArray = getArrayOfAuxiliaryData(opRetDto);
      transactionBodyBuilder.auxiliaryDataHash(Blake2bUtil.blake2bHash256(
          com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(cborAuxDataArray)));
    }

    if (!(opRetDto.getCertificates()).isEmpty()) {
      transactionBodyBuilder.certs(opRetDto.getCertificates());
    }
    if (!CollectionUtils.isEmpty(opRetDto.getWithdrawals())) {
      transactionBodyBuilder.withdrawals(opRetDto.getWithdrawals());
    }
    TransactionBody transactionBody = transactionBodyBuilder.build();
    co.nstant.in.cbor.model.Map mapCbor = transactionBody.serialize();

    //  If ttl is 0, it will be discarded while serialization, but it needs to be in the Data map
    if (ttl == 0) {
      mapCbor.put(new UnsignedInteger(3), new UnsignedInteger(0));
    }
    byte[] serializedMapCbor = CborSerializationUtil.serialize(mapCbor);
    String transactionBytes = HexUtil.encodeHexString(serializedMapCbor);
    log.info("[createUnsignedTransaction] Hashing transaction body");
    String bodyHash = HexUtil.encodeHexString(Blake2bUtil.blake2bHash256(serializedMapCbor));

    UnsignedTransaction toReturn = new UnsignedTransaction(bodyHash, transactionBytes,
        opRetDto.getAddresses(), getHexEncodedAuxiliaryMetadataArray(opRetDto));
    log.info("[createUnsignedTransaction] Returning unsigned transaction, hash to sign and addresses"
        + " that will sign hash: [{}]", toReturn);
    return toReturn;
  }

  private static String getHexEncodedAuxiliaryMetadataArray(
      ProcessOperationsReturn opRetDto)
      throws CborSerializationException, CborException {
    if (opRetDto.getVoteRegistrationMetadata() != null) {
      Array cborAuxDataArray = getArrayOfAuxiliaryData(opRetDto);
      return HexUtil.encodeHexString(CborSerializationUtil.serialize(cborAuxDataArray));
    }
    return null;
  }

  @NotNull
  private static Array getArrayOfAuxiliaryData(ProcessOperationsReturn processOperationsReturnDto)
      throws CborSerializationException {
    AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
    Array cborArray = new Array();
    cborArray.add(auxiliaryData.serialize());
    cborArray.add(new Array());
    return cborArray;
  }

  @Override
  public List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
      Set<String> addresses) {
    return addresses.stream().map(
        address -> new SigningPayload(null, new AccountIdentifier(address, null, null),
            transactionBodyHash, SignatureType.ED25519)).toList();
  }

  private ProcessOperationsReturn processOperations(Network network, List<Operation> operations, DepositParameters depositParams) {
    ProcessOperations result = convertRosettaOperations(network, operations);
    double refundsSum = result.getStakeKeyDeRegistrationsCount() * Long.parseLong(
        depositParams.getKeyDeposit());
    Map<String, Double> depositsSumMap = getDepositsSumMap(depositParams, result, refundsSum);
    long fee = calculateFee(result.getInputAmounts(), result.getOutputAmounts(),
        result.getWithdrawalAmounts(), depositsSumMap);
    log.info("[processOperations] Calculated fee:{}", fee);
    return fillProcessOperationsReturnObject(result, fee);
  }

  @NotNull
  private Map<String, Double> getDepositsSumMap(DepositParameters depositParameters, ProcessOperations result, double refundsSum) {
    double keyDepositsSum =
        result.getStakeKeyRegistrationsCount() * Long.parseLong(depositParameters.getKeyDeposit());
    double poolDepositsSum =
        result.getPoolRegistrationsCount() * Long.parseLong(depositParameters.getPoolDeposit());
    Map<String, Double> depositsSumMap = new HashMap<>();
    depositsSumMap.put(Constants.KEY_REFUNDS_SUM, refundsSum);
    depositsSumMap.put(Constants.KEY_DEPOSITS_SUM, keyDepositsSum);
    depositsSumMap.put(Constants.POOL_DEPOSITS_SUM, poolDepositsSum);
    return depositsSumMap;
  }

  @NotNull
  private ProcessOperationsReturn fillProcessOperationsReturnObject(ProcessOperations result,
      long fee) {
    ProcessOperationsReturn processOperationsDto = new ProcessOperationsReturn();
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

  /**
   * Fees are calculated based on adding all inputs and subtracting all outputs. Withdrawals will
   * be added as well.
   *
   * @param inputAmounts      Sum of all Input ADA Amounts
   * @param outputAmounts     Sum of all Output ADA Amounts
   * @param withdrawalAmounts Sum of all Withdrawals
   * @param depositsSumMap    Map of refund and deposit values
   * @return Payed Fee
   */
  @Override
  public Long calculateFee(List<BigInteger> inputAmounts, List<BigInteger> outputAmounts,
      List<BigInteger> withdrawalAmounts, Map<String, Double> depositsSumMap) {
    long inputsSum =
        -1 * inputAmounts.stream().reduce(BigInteger.ZERO, BigInteger::add).longValue();
    long outputsSum = outputAmounts.stream().reduce(BigInteger.ZERO, BigInteger::add).longValue();
    long withdrawalsSum = withdrawalAmounts.stream().reduce(BigInteger.ZERO, BigInteger::add)
        .longValue();
    long fee = (long) (inputsSum + withdrawalsSum * (-1) + depositsSumMap.get(
        Constants.KEY_REFUNDS_SUM) - outputsSum
        - depositsSumMap.get(Constants.KEY_DEPOSITS_SUM) - depositsSumMap.get(
        Constants.POOL_DEPOSITS_SUM)); // withdrawals -1 because it's a negative value, but must be added to the
    if (fee < 0) {
      throw ExceptionFactory.outputsAreBiggerThanInputsError();
    }
    return fee;
  }

  @Override
  public ProcessOperations convertRosettaOperations(Network network, List<Operation> operations) {
    ProcessOperations processor = new ProcessOperations();

    for (Operation operation : operations) {
      String type = operation.getType();
      processor = OperationParseUtil.parseOperation(operation, network, processor,
          type);
      if (processor == null) {
        log.error("[processOperations] Operation with id {} has invalid type",
            operation.getOperationIdentifier());
        throw ExceptionFactory.invalidOperationTypeError();
      }
    }
    return processor;
  }

  /**
   * Submits the signed transaction to the preconfigured SubmitAPI. If successful the transaction
   * hash is returned.
   *
   * @param signedTransaction signed transaction in hex format
   * @return transaction hash
   * @throws ApiException if the transaction submission fails, additional information is provided in
   *                      the exception message
   */
  @Override
  public String submitTransaction(String signedTransaction) throws ApiException {
    String submitURL = Constants.PROTOCOL + cardanoNodeSubmitHost + ":" + nodeSubmitApiPort
        + Constants.SUBMIT_API_PATH;
    log.info("[submitTransaction] About to submit transaction to {}", submitURL);

    HttpHeaders headers = new HttpHeaders();
    headers.add(Constants.CONTENT_TYPE_HEADER_KEY, Constants.CBOR_CONTENT_TYPE);
      ResponseEntity<String> exchange = restTemplate.postForEntity(
          submitURL,
          new HttpEntity<>(HexUtil.decodeHexString(signedTransaction), headers),
          String.class);
      if (exchange.getStatusCode().value() == Constants.SUCCESS_SUBMIT_TX_HTTP_CODE) {
        return Optional
            .ofNullable(exchange.getBody())
            .filter(txHash -> txHash.length() == Constants.TX_HASH_LENGTH + 2)
            // removing leading and trailing quotes returned from node API
            .map(txHash -> txHash.substring(1, txHash.length() - 1))
            .orElseThrow(() ->
                ExceptionFactory.sendTransactionError("Transaction hash format error: " +
                    exchange.getBody()));
      }else {
        log.error("[submitTransaction] There was an error submitting transaction");
        throw ExceptionFactory.sendTransactionError("Transaction submit error: " +
            exchange.getBody());
      }
  }

  /**
   * Returns the deposit parameters for the network fetched from the protocol parameters
   *
   * @return Deposit parameters including key- and pool deposit
   */
  @Override
  public DepositParameters getDepositParameters() {
    ProtocolParams pp = protocolParamService.findProtocolParameters();
    return new DepositParameters(pp.getKeyDeposit().toString(), pp.getPoolDeposit().toString());
  }

  /**
   * Extract raw signed transaction and removes the extra data. Transactions build with rosetta
   * contain such data, transaction build with other tools like cardano-cli do not contain this
   * data.
   *
   * @param txWithExtraData transaction with extra data
   * @return raw signed transaction
   */
  @Override
  public String extractTransactionIfNeeded(String txWithExtraData) {
    byte[] bytes = HexUtil.decodeHexString(txWithExtraData);
    Array deserialize = (Array) CborSerializationUtil.deserialize(bytes);
    // Unpack transaction if needed
    if (deserialize.getDataItems().size() == 1 && deserialize.getDataItems().getFirst().getMajorType()
        .equals(MajorType.ARRAY)) {
      deserialize = (Array) deserialize.getDataItems().getFirst();
    }
    if (deserialize.getDataItems().isEmpty()) {
      throw ExceptionFactory.invalidTransactionError();
    }
    // unpack transaction
    if (deserialize.getDataItems().getFirst().getMajorType().equals(MajorType.UNICODE_STRING)) {
      return ((UnicodeString) deserialize.getDataItems().getFirst()).getString();
    } else {
      return txWithExtraData;
    }
  }

  @Override
  public String getCardanoAddress(AddressType addressType, PublicKey stakingCredential,
                                  PublicKey publicKey, NetworkEnum networkEnum) {
    if(publicKey == null) {
      throw ExceptionFactory.publicKeyMissing();
    }
    String address;
    switch (addressType) {
      case BASE:
        log.debug("Deriving base address");
        if(stakingCredential == null) {
          throw ExceptionFactory.missingStakingKeyError();
        }
        log.debug("Deriving base address with staking credential: {}", stakingCredential);
        address = AddressProvider.getBaseAddress(getHdPublicKeyFromRosettaKey(publicKey), getHdPublicKeyFromRosettaKey(stakingCredential),
                networkEnum.getNetwork()).getAddress();
        break;
      case REWARD:
        log.debug("Deriving reward address");
        if(stakingCredential == null) {
          address = AddressProvider
                  .getRewardAddress(getHdPublicKeyFromRosettaKey(publicKey), networkEnum.getNetwork()).getAddress();
          log.debug("Deriving reward address with staking credential: {}", publicKey);
        } else {
          address = AddressProvider
                  .getRewardAddress(getHdPublicKeyFromRosettaKey(stakingCredential), networkEnum.getNetwork()).getAddress();
          log.debug("Deriving reward address with staking credential: {}", stakingCredential);
        }
        break;
      case ENTERPRISE:
        log.info("Deriving enterprise address");
        address = AddressProvider
                .getEntAddress(getHdPublicKeyFromRosettaKey(publicKey), networkEnum.getNetwork()).getAddress();
        break;
      default:
        log.error("Invalid address type: {}", addressType);
        throw ExceptionFactory.invalidAddressTypeError();
    }
    return address;
  }

  public HdPublicKey getHdPublicKeyFromRosettaKey(PublicKey publicKey) {
    byte[] pubKeyBytes = HexUtil.decodeHexString(publicKey.getHexBytes());
    HdPublicKey pubKey;
    if(pubKeyBytes.length == 32) {
      pubKey = new HdPublicKey();
      pubKey.setKeyData(pubKeyBytes);
    } else if(pubKeyBytes.length == 64) {
      pubKey = HdPublicKey.fromBytes(pubKeyBytes);
    } else {
      log.error("Invalid public key length: {}", pubKeyBytes.length);
      throw new IllegalArgumentException("Invalid public key length");
    }
    return pubKey;
  }

  private Signatures extractSignaturesFromAddress(String address) {
    EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressType(address);
    if (eraAddressType != null) {
      return signatureProcessor(eraAddressType, null, address);
    }
    if (CardanoAddressUtils.isEd25519KeyHash(address)) {
      return signatureProcessor(null, AddressType.POOL_KEY_HASH, address);
    }
    throw ExceptionFactory.invalidAddressError(address);
  }

  private TransactionBody deserializeTransactionBody(String unsignedTransaction) {
    log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
    try {
      DataItem[] dataItems = com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.deserialize(
          HexUtil.decodeHexString(unsignedTransaction));
      return TransactionBody.deserialize((co.nstant.in.cbor.model.Map) dataItems[0]);
    } catch (Exception e) {
      log.error("[buildTransaction] Error deserializing unsigned transaction: {}", e.getMessage());
      throw ExceptionFactory.cantCreateSignTransaction();
    }
  }

  private AuxiliaryData deserializeAuxiliaryData(String transactionMetadata) {
    if (ObjectUtils.isEmpty(transactionMetadata)) {
      return null;
    }
    try {
      log.info("[buildTransaction] Adding transaction metadata");
      Array cborArray = (Array) CborSerializationUtil.deserialize(
          HexUtil.decodeHexString(transactionMetadata));
      return AuxiliaryData.deserialize(
          (co.nstant.in.cbor.model.Map) cborArray.getDataItems().getFirst());
    } catch (Exception e) {
      log.error(
          "[buildTransaction] CborDeserializationException while deserializing transactionMetadata: {}",
          e.getMessage());
      throw ExceptionFactory.generalDeserializationError(e.getMessage());
    }
  }

  private List<AccountIdentifier> getUniqueAccountIdentifiers(List<String> addresses) {
    return new HashSet<>(addresses).stream().map(s -> new AccountIdentifier(s, null, null))
        .toList();
  }
}
