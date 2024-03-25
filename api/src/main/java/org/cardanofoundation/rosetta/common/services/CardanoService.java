package org.cardanofoundation.rosetta.common.services;


import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import java.util.Set;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import java.math.BigInteger;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.openapitools.client.model.DepositParameters;
import org.openapitools.client.model.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.openapitools.client.model.SigningPayload;

public interface CardanoService {


  String getHashOfSignedTransaction(String signedTransaction);
  Array decodeExtraData(String encoded);
  Long calculateTtl(Long ttlOffset);
  Double checkOrReturnDefaultTtl(Integer relativeTtl);
  Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborSerializationException, CborException;
  Long calculateTxMinimumFee(Long transactionSize, ProtocolParams protocolParameters);

  Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType,
      String address);

  Double calculateTxSize(NetworkIdentifierType networkIdentifierType, List<Operation> operations, int ttl, DepositParameters depositParameters) throws IOException, CborException, AddressExcepion, CborSerializationException;

  String buildTransaction(String unsignedTransaction,
      List<Signatures> signaturesList, String transactionMetadata);

  TransactionWitnessSet getWitnessesForTransaction(
      List<Signatures> signaturesList);

  List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
      Set<String> addresses);

  Long calculateFee(ArrayList<BigInteger> inputAmounts, ArrayList<BigInteger> outputAmounts,
      ArrayList<BigInteger> withdrawalAmounts, Map<String, Double> depositsSumMap);

  ProcessOperations convertRosettaOperations(NetworkIdentifierType networkIdentifierType,
      List<Operation> operations) throws IOException;

  UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifier, List<Operation> operations, int ttl, DepositParameters depositParameters) throws IOException, CborSerializationException, AddressExcepion, CborException;

  String submitTransaction(String signedTransaction) throws IOException;
}
