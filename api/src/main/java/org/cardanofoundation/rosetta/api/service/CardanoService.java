package org.cardanofoundation.rosetta.api.service;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.StakeAddressPrefix;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessOperationsDto;
import org.cardanofoundation.rosetta.api.model.Signatures;
import org.cardanofoundation.rosetta.api.model.UnsignedTransaction;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessOperationsReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessPoolRegistrationReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.ProcessWithdrawalReturnDto;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.DepositParameters;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationIdentifier;
import org.cardanofoundation.rosetta.api.model.OperationMetadata;
import org.cardanofoundation.rosetta.api.model.PoolMargin;
import org.cardanofoundation.rosetta.api.model.PoolMetadata;
import org.cardanofoundation.rosetta.api.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Relay;
import org.cardanofoundation.rosetta.api.model.SigningPayload;
import org.cardanofoundation.rosetta.api.model.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionParsed;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;


public interface CardanoService {

    Double calculateRelativeTtl(Double relativeTtl);

    Double calculateTxSize(NetworkIdentifierType networkIdentifierType, ArrayList<Operation> operations, Integer ttl, DepositParameters depositParameters)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    String buildTransaction(String unsignedTransaction, List<Signatures> signaturesList, String transactionMetadata);

    TransactionWitnessSet getWitnessesForTransaction(List<Signatures> signaturesList);

    Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType, String address);

    UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifierType, List<Operation> operations, Integer ttl, DepositParameters depositParameters)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    ProcessOperationsReturnDto processOperations(NetworkIdentifierType networkIdentifierType, List<Operation> operations, DepositParameters depositParameters)
        throws IOException;

    Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts, ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap);

    ProcessOperationsDto convert(NetworkIdentifierType networkIdentifierType, List<Operation> operations)
        throws IOException;

    ProcessOperationsDto operationProcessor(Operation operation, NetworkIdentifierType networkIdentifierType,
                                               ProcessOperationsDto resultAccumulator, String type)
        throws IOException, CborSerializationException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException;

    NetworkIdentifierType getNetworkIdentifierByRequestParameters(NetworkIdentifier networkRequestParameters);

    boolean isAddressTypeValid(String type);

    Long calculateTxMinimumFee(Long transactionSize, ProtocolParameters protocolParameters);

    ProtocolParameters getProtocolParameters();

    Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborSerializationException, CborException;

    Long calculateTtl(Long ttlOffset);

    BlockDto getLatestBlock();

    Long findLatestBlockNumber();


    String encodeExtraData(String transaction, TransactionExtraData extraData) throws JsonProcessingException, CborSerializationException, CborException;

    Array decodeExtraData(String encoded);

    co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey);

    co.nstant.in.cbor.model.Map getAmountMap(Amount amount);

    List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash, Set<String> addresses);

    TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map);

    PublicKey getPublicKeyFromMap(co.nstant.in.cbor.model.Map stakingCredentialMap);

    Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap);

    String getHashOfSignedTransaction(String signedTransaction);

    TransactionIdentifierResponse mapToConstructionHashResponse(String transactionHash);

}
