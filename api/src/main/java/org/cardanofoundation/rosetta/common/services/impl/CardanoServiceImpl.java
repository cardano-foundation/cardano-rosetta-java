package org.cardanofoundation.rosetta.common.services.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.BootstrapWitness;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import com.bloxbean.cardano.client.transaction.spec.VkeyWitness;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperationsReturn;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.common.mapper.CborArrayToTransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.CardanoService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.mapper.TransactionDataToOperations;
import org.cardanofoundation.rosetta.common.util.OperationParseUtil;
import org.cardanofoundation.rosetta.common.util.ValidateParseUtil;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.DepositParameters;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.SignatureType;
import org.openapitools.client.model.SigningPayload;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.math.BigInteger.valueOf;


@Slf4j
@Service
@RequiredArgsConstructor
public class CardanoServiceImpl implements CardanoService {

  private final LedgerDataProviderService ledgerDataProviderService;

  @Override
  public TransactionParsed parseTransaction(NetworkIdentifierType networkIdentifierType,
      String transaction, boolean signed) {
    try {

      Array decodeTransaction = decodeTransaction(transaction);

      TransactionData convert = CborArrayToTransactionData.convert(decodeTransaction, signed);
      List<Operation> operations = TransactionDataToOperations.convert(convert,
          networkIdentifierType.getValue());
      List<AccountIdentifier> accountIdentifierSigners = new ArrayList<>();
      if (signed) {
        log.info("[parseSignedTransaction] About to get signatures from parsed transaction");
        List<String> accum = new ArrayList<>();
        convert.transactionExtraData().operations().forEach(o -> {
          List<String> list = TransactionDataToOperations.getSignerFromOperation(
              networkIdentifierType, o);
          accum.addAll(list);
        });
        accountIdentifierSigners = TransactionDataToOperations.getUniqueAccountIdentifiers(accum);
      }
      return new TransactionParsed(operations, accountIdentifierSigners);
    } catch (Exception error) {
      log.error(error
          + "[parseSignedTransaction] Cant instantiate signed transaction from transaction bytes");
      throw ExceptionFactory.cantCreateSignedTransactionFromBytes();
    }
  }

  @Override
  public Double checkOrReturnDefaultTtl(Integer relativeTtl) {
    return relativeTtl == null ? Constants.DEFAULT_RELATIVE_TTL : relativeTtl.doubleValue();
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
      if (body == null || CborSerializationUtil.serialize(body.serialize()) == null) {
        hashBuffer = null;
      } else {
        hashBuffer = Blake2bUtil.blake2bHash256(CborSerializationUtil.serialize(body.serialize()));
      }
      return HexUtil.encodeHexString(hashBuffer);
    } catch (Exception error) {
      log.error(error.getMessage()
          + "[getHashOfSignedTransaction] There was an error parsing signed transaction");
      throw ExceptionFactory.parseSignedTransactionError();
    }
  }

  @Override
  public Array decodeTransaction(String encoded) {
    try {
      DataItem dataItem = CborSerializationUtil.deserialize(HexUtil.decodeHexString(encoded));
      return (Array) dataItem;
    } catch (Exception e) {
      throw ExceptionFactory.cantBuildSignedTransaction();
    }
  }

  @Override
  public Long calculateTtl(Long ttlOffset) {
    Block latestBlock = ledgerDataProviderService.findLatestBlock();
    return latestBlock.getSlotNo() + ttlOffset;
  }

  @Override
  public Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) {
    try {
      return previousTxSize + CborSerializationUtil.serialize(new UnsignedInteger(updatedTtl)).length
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
    if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.SHELLEY)) {
      return new Signatures(Constants.SHELLEY_DUMMY_SIGNATURE, Constants.SHELLEY_DUMMY_PUBKEY, null,
          address);
    }
    if (!ObjectUtils.isEmpty(eraAddressType) && eraAddressType.equals(EraAddressType.BYRON)) {
      return new Signatures(Constants.BYRON_DUMMY_SIGNATURE, Constants.BYRON_DUMMY_PUBKEY,
          Constants.CHAIN_CODE_DUMMY, address);
    }
    if (AddressType.POOL_KEY_HASH.getValue().equals(addressType.getValue())) {
      return new Signatures(Constants.COLD_DUMMY_SIGNATURE, Constants.COLD_DUMMY_PUBKEY, null,
          address);
    }
    return null;
  }

  @Override
  public Double calculateTxSize(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations, int ttl, DepositParameters depositParameters) {
    UnsignedTransaction unsignedTransaction = null;
    try {
      unsignedTransaction = createUnsignedTransaction(networkIdentifierType,
          operations, ttl, !ObjectUtils.isEmpty(depositParameters) ? depositParameters
              : new DepositParameters(Constants.DEFAULT_KEY_DEPOSIT.toString(),
                  Constants.DEFAULT_POOL_DEPOSIT.toString()));
    } catch (CborSerializationException | AddressExcepion | CborException e) {
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
    List<Signatures> signaturesList = (unsignedTransaction.addresses()).stream().map(address -> {
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

    String transaction = buildTransaction(unsignedTransaction.bytes(), signaturesList,
        unsignedTransaction.metadata());
    return ((double) transaction.length() / 2);

  }

  @Override
  public String buildTransaction(String unsignedTransaction, List<Signatures> signaturesList,
      String transactionMetadata) {
    log.info("[buildTransaction] About to signed a transaction with {} signatures",
        signaturesList.size());
    TransactionWitnessSet witnesses = getWitnessesForTransaction(signaturesList);

    log.info("[buildTransaction] Instantiating transaction body from unsigned transaction bytes");
    DataItem[] dataItems;
    try {
      dataItems = com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.deserialize(
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
        Array array = (Array) CborSerializationUtil.deserialize(
            HexUtil.decodeHexString(transactionMetadata));
        auxiliaryData = AuxiliaryData.deserialize(
            (co.nstant.in.cbor.model.Map) array.getDataItems().getFirst());
      }
      Transaction transaction = Transaction.builder().auxiliaryData(auxiliaryData)
          .witnessSet(witnesses).build();
      transaction.setBody(transactionBody);
      Array array = (Array) CborSerializationUtil.deserialize(transaction.serialize());
      if (transactionBody.getTtl() == 0) {
        co.nstant.in.cbor.model.Map dataItem1 = (co.nstant.in.cbor.model.Map) array.getDataItems()
            .getFirst();
        // Position of ttl in transaction body, it will be discarded while serialization if it's 0, but it needs to be in the Data map
        // otherwise a wrong hash will be produced.
        dataItem1.put(new UnsignedInteger(3), new UnsignedInteger(0));
        array.getDataItems().set(0, dataItem1);
      }
      if (!ObjectUtils.isEmpty(transactionMetadata)) {
        Array metadataArray = new Array();
        metadataArray.add(array.getDataItems().get(3));
        metadataArray.add(new Array());
        array.getDataItems().set(3, metadataArray);
      }
      return HexUtil.encodeHexString(
          com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array));
    } catch (CborDeserializationException e) {
      log.error(e.getMessage()
          + "[buildTransaction] CborDeserializationException while building transaction");
      throw new RuntimeException(e);
    } catch (CborSerializationException e) {
      log.error(e.getMessage()
          + "[buildTransaction] CborSerializationException while building transaction");
      throw new RuntimeException(e);
    }
  }

  @Override
  public TransactionWitnessSet getWitnessesForTransaction(List<Signatures> signaturesList) {
    TransactionWitnessSet witnesses = new TransactionWitnessSet();
    ArrayList<VkeyWitness> vKeyWitnesses = new ArrayList<>();
    ArrayList<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();
    log.info("[getWitnessesForTransaction] Extracting witnesses from signatures");
    signaturesList.forEach(signature -> {
      VerificationKey vKey = new VerificationKey();
      vKey.setCborHex(ObjectUtils.isEmpty(signature) ? null : signature.publicKey());
      EraAddressType eraAddressType = CardanoAddressUtils.getEraAddressType(signature.address());
      if (!ObjectUtils.isEmpty(signature)) {
        if (!ObjectUtils.isEmpty(signature.address()) && eraAddressType == EraAddressType.BYRON) {
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
  public UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations, int ttl, DepositParameters depositParameters)
      throws CborSerializationException, AddressExcepion, CborException {
    log.info(
        "[createUnsignedTransaction] About to create an unsigned transaction with {} operations",
        operations.size());
    ProcessOperationsReturn processOperationsReturnDto = processOperations(networkIdentifierType,
        operations, depositParameters);

    log.info("[createUnsignedTransaction] About to create transaction body");
    BigInteger fee = valueOf(processOperationsReturnDto.getFee());
    TransactionBody transactionBody = TransactionBody.builder()
        .inputs(processOperationsReturnDto.getTransactionInputs())
        .outputs(processOperationsReturnDto.getTransactionOutputs()).fee(fee).ttl(ttl).build();

    if (processOperationsReturnDto.getVoteRegistrationMetadata() != null) {
      log.info(
          "[createUnsignedTransaction] Hashing vote registration metadata and adding to transaction body");
      Array array = getArrayOfAuxiliaryData(processOperationsReturnDto);
      transactionBody.setAuxiliaryDataHash(Blake2bUtil.blake2bHash256(
          com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(array)));
    }

    if (!(processOperationsReturnDto.getCertificates()).isEmpty()) {
      transactionBody.setCerts(processOperationsReturnDto.getCertificates());
    }
    if (!ObjectUtils.isEmpty(processOperationsReturnDto.getWithdrawals())) {
      transactionBody.setWithdrawals(processOperationsReturnDto.getWithdrawals());
    }
    co.nstant.in.cbor.model.Map mapCbor = transactionBody.serialize();
    //  If ttl is 0, it will be discarded while serialization, but it needs to be in the Data map
    if (ttl == 0) {
      mapCbor.put(new UnsignedInteger(3), new UnsignedInteger(0));
    }
    String transactionBytes = HexUtil.encodeHexString(
        com.bloxbean.cardano.yaci.core.util.CborSerializationUtil.serialize(mapCbor));
    log.info("[createUnsignedTransaction] Hashing transaction body");
    String bodyHash = com.bloxbean.cardano.client.util.HexUtil.encodeHexString(
        Blake2bUtil.blake2bHash256(CborSerializationUtil.serialize(mapCbor)));
    UnsignedTransaction toReturn = new UnsignedTransaction(
        HexUtil.encodeHexString(HexUtil.decodeHexString(bodyHash)), transactionBytes,
        processOperationsReturnDto.getAddresses(),
        getHexEncodedAuxiliaryMetadataArray(processOperationsReturnDto));
    log.info(toReturn
        + "[createUnsignedTransaction] Returning unsigned transaction, hash to sign and addresses that will sign hash");
    return toReturn;
  }

  private static String getHexEncodedAuxiliaryMetadataArray(
      ProcessOperationsReturn processOperationsReturnDto)
      throws CborSerializationException, CborException {
    if (!ObjectUtils.isEmpty(processOperationsReturnDto.getVoteRegistrationMetadata())) {
      Array array = getArrayOfAuxiliaryData(processOperationsReturnDto);
      return HexUtil.encodeHexString(CborSerializationUtil.serialize(array));
    }
    return null;
  }

  @NotNull
  private static Array getArrayOfAuxiliaryData(ProcessOperationsReturn processOperationsReturnDto)
      throws CborSerializationException {
    AuxiliaryData auxiliaryData = processOperationsReturnDto.getVoteRegistrationMetadata();
    Array array = new Array();
    array.add(auxiliaryData.serialize());
    array.add(new Array());
    return array;
  }

  @Override
  public List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
      Set<String> addresses) {
    return addresses.stream().map(
        address -> new SigningPayload(null, new AccountIdentifier(address, null, null),
            transactionBodyHash, SignatureType.ED25519)).toList();
  }

  private ProcessOperationsReturn processOperations(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations, DepositParameters depositParameters) {
    ProcessOperations result = convertRosettaOperations(networkIdentifierType, operations);
    double refundsSum = result.getStakeKeyDeRegistrationsCount() * Long.parseLong(
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
    return fillProcessOperationsReturnObject(result, fee);
  }

  @NotNull
  private static ProcessOperationsReturn fillProcessOperationsReturnObject(ProcessOperations result,
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

  @Override
  public Long calculateFee(ArrayList<BigInteger> inputAmounts, ArrayList<BigInteger> outputAmounts,
      ArrayList<BigInteger> withdrawalAmounts, Map<String, Double> depositsSumMap) {
    long inputsSum =
        -1 * inputAmounts.stream().reduce(BigInteger.ZERO, BigInteger::add).longValue();
    long outputsSum = outputAmounts.stream().reduce(BigInteger.ZERO, BigInteger::add).longValue();
    long withdrawalsSum = withdrawalAmounts.stream().reduce(BigInteger.ZERO, BigInteger::add)
        .longValue();
    long fee = (long) (inputsSum + withdrawalsSum + depositsSumMap.get("keyRefundsSum") - outputsSum
        - depositsSumMap.get("keyDepositsSum") - depositsSumMap.get("poolDepositsSum"));
    if (fee < 0) {
      throw ExceptionFactory.outputsAreBiggerThanInputsError();
    }
    return fee;
  }

  @Override
  public ProcessOperations convertRosettaOperations(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations) {
    ProcessOperations processor = new ProcessOperations();

    for (Operation operation : operations) {
      String type = operation.getType();
      processor = OperationParseUtil.parseOperation(operation, networkIdentifierType, processor,
          type);
      if (ObjectUtils.isEmpty(processor)) {
        log.error("[processOperations] Operation with id {} has invalid type",
            operation.getOperationIdentifier());
        throw ExceptionFactory.invalidOperationTypeError();
      }
    }
    return processor;
  }

}
