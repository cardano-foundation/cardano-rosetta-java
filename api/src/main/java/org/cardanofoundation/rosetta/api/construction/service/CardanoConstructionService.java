package org.cardanofoundation.rosetta.api.construction.service;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import org.openapitools.client.model.DepositParameters;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.SigningPayload;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.construction.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;

public interface CardanoConstructionService {


  Array decodeTransaction(String encoded);
  Long calculateTtl(Long ttlOffset);

  TransactionParsed parseTransaction(Network network, String transaction, boolean signed);

  Integer checkOrReturnDefaultTtl(Integer relativeTtl);
  Long calculateTxMinimumFee(Long transactionSize, ProtocolParams protocolParameters);

  Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType,
      String address);

  Integer calculateTxSize(Network network, List<Operation> operations, long ttl);

  String buildTransaction(String unsignedTransaction,
      List<Signatures> signaturesList, String transactionMetadata);

  TransactionWitnessSet getWitnessesForTransaction(
      List<Signatures> signaturesList);

  List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash,
      Set<String> addresses);

  ProcessOperations convertRosettaOperations(Network network,
      List<Operation> operations) throws IOException;

  UnsignedTransaction createUnsignedTransaction(Network network, List<Operation> operations, long ttl, Optional<Long> calculatedFee) throws IOException, CborSerializationException, AddressExcepion, CborException;

  String submitTransaction(String signedTransaction);
  DepositParameters getDepositParameters();

  String extractTransactionIfNeeded(String txWithExtraData);

  String getCardanoAddress(AddressType addressType, PublicKey stakingCredential,
      PublicKey publicKey, NetworkEnum networkEnum);

}
