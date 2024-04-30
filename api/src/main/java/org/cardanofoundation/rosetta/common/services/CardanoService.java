package org.cardanofoundation.rosetta.common.services;


import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import org.openapitools.client.model.DepositParameters;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.SigningPayload;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;

public interface CardanoService {


  String getHashOfSignedTransaction(String signedTransaction);
  Array decodeTransaction(String encoded);
  Long calculateTtl(Long ttlOffset);

  TransactionParsed parseTransaction(NetworkIdentifierType networkIdentifierType,
      String transaction, boolean signed);

  Double checkOrReturnDefaultTtl(Integer relativeTtl);
  Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl);
  Long calculateTxMinimumFee(Long transactionSize, ProtocolParams protocolParameters);

  Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType,
      String address);

  Double calculateTxSize(NetworkIdentifierType networkIdentifierType, List<Operation> operations, int ttl, DepositParameters depositParameters);

  String buildTransaction(String unsignedTransaction,
      List<Signatures> signaturesList, String transactionMetadata);

  TransactionWitnessSet getWitnessesForTransaction(
      List<Signatures> signaturesList);

  List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
      Set<String> addresses);

  Long calculateFee(List<BigInteger> inputAmounts, List<BigInteger> outputAmounts,
      List<BigInteger> withdrawalAmounts, Map<String, Double> depositsSumMap);

  ProcessOperations convertRosettaOperations(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations) throws IOException;

  UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifier, List<Operation> operations, int ttl, DepositParameters depositParameters) throws IOException, CborSerializationException, AddressExcepion, CborException;

  String submitTransaction(String signedTransaction);
  DepositParameters getDepositParameters();

  String extractTransactionIfNeeded(String txWithExtraData);
}
