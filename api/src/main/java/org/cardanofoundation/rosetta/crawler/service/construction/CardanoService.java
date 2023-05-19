package org.cardanofoundation.rosetta.crawler.service.construction;

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
import org.cardanofoundation.rosetta.crawler.construction.data.Metadata;
import org.cardanofoundation.rosetta.crawler.construction.data.ProcessOperationsDto;
import org.cardanofoundation.rosetta.crawler.construction.data.ProtocolParametersResponse;
import org.cardanofoundation.rosetta.crawler.construction.data.Signatures;
import org.cardanofoundation.rosetta.crawler.construction.data.UnsignedTransaction;
import org.cardanofoundation.rosetta.crawler.construction.data.response.BlockResponse;
import org.cardanofoundation.rosetta.crawler.construction.data.type.AddressType;
import org.cardanofoundation.rosetta.crawler.construction.data.type.EraAddressType;
import org.cardanofoundation.rosetta.crawler.construction.data.type.NetworkIdentifierType;
import org.cardanofoundation.rosetta.crawler.construction.data.type.StakeAddressPrefix;
import org.cardanofoundation.rosetta.crawler.model.Amount;
import org.cardanofoundation.rosetta.crawler.model.DepositParameters;
import org.cardanofoundation.rosetta.crawler.model.Operation;
import org.cardanofoundation.rosetta.crawler.model.OperationIdentifier;
import org.cardanofoundation.rosetta.crawler.model.OperationMetadata;
import org.cardanofoundation.rosetta.crawler.model.PoolMargin;
import org.cardanofoundation.rosetta.crawler.model.PoolMetadata;
import org.cardanofoundation.rosetta.crawler.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.crawler.model.PublicKey;
import org.cardanofoundation.rosetta.crawler.model.Relay;
import org.cardanofoundation.rosetta.crawler.model.SigningPayload;
import org.cardanofoundation.rosetta.crawler.model.TokenBundleItem;
import org.cardanofoundation.rosetta.crawler.model.TransactionExtraData;
import org.cardanofoundation.rosetta.crawler.model.TransactionParsed;
import org.cardanofoundation.rosetta.crawler.model.VoteRegistrationMetadata;
import org.cardanofoundation.rosetta.crawler.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.TransactionIdentifierResponse;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cardanofoundation.rosetta.crawler.projection.BlockDto;


public interface CardanoService {
    String hex(byte[] bytes);

    String generateAddress(NetworkIdentifierType networkIdentifierType, String publicKeyString, String stakingCredentialString, AddressType type) throws IllegalAccessException, CborSerializationException;

    String generateRewardAddress(NetworkIdentifierType networkIdentifierType, HdPublicKey paymentCredential) throws CborSerializationException;

    String generateBaseAddress(NetworkIdentifierType networkIdentifierType, HdPublicKey paymentCredential, HdPublicKey stakingCredential) throws CborSerializationException;

    String generateEnterpriseAddress(HdPublicKey paymentCredential, NetworkIdentifierType networkIdentifierType) throws CborSerializationException;

    Double calculateRelativeTtl(Double relativeTtl);

    Double calculateTxSize(NetworkIdentifierType networkIdentifierType, ArrayList<Operation> operations, Integer ttl, DepositParameters depositParameters)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    String buildTransaction(String unsignedTransaction, List<Signatures> signaturesList, String transactionMetadata);

    TransactionWitnessSet getWitnessesForTransaction(List<Signatures> signaturesList);

    EraAddressType getEraAddressTypeOrNull(String address);

    boolean isEd25519KeyHash(String address);

    Signatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType, String address);

    UnsignedTransaction createUnsignedTransaction(NetworkIdentifierType networkIdentifierType, List<Operation> operations, Integer ttl, DepositParameters depositParameters)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    Map<String, Object> processOperations(NetworkIdentifierType networkIdentifierType, List<Operation> operations, DepositParameters depositParameters)
        throws IOException;

    Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts, ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap);

    ProcessOperationsDto convert(NetworkIdentifierType networkIdentifierType, List<Operation> operations)
        throws IOException;

    ProcessOperationsDto operationProcessor(Operation operation, NetworkIdentifierType networkIdentifierType,
                                               ProcessOperationsDto resultAccumulator, String type)
        throws IOException, CborSerializationException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException;

    AuxiliaryData processVoteRegistration(Operation operation)
        throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException;

    Map<String, Object> validateAndParseVoteRegistrationMetadata(
        VoteRegistrationMetadata voteRegistrationMetadata)
        throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException;

    String bytesToHex(byte[] bytes);

    String hexFormatter(byte[] bytes);

    String add0xPrefix(String hex);

    String validateAndParseVotingKey(PublicKey votingKey);

    Map<String, Object> processPoolRetirement(Operation operation);

    byte[] validateAndParsePoolKeyHash(String poolKeyHash);

    Map<String, Object> processPoolRegistration(NetworkIdentifierType networkIdentifierType, Operation operation);

    PoolMetadata validateAndParsePoolMetadata(PoolMetadata metadata);

    List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> validateAndParsePoolRelays(List<Relay> relays);

    com.bloxbean.cardano.client.transaction.spec.cert.Relay generateSpecificRelay(Relay relay);

    Inet4Address parseIpv4(String ip) throws UnknownHostException;

    Inet6Address parseIpv6(String ip) throws UnknownHostException;

    void validatePort(String port);

    Address validateAndParseRewardAddress(String rwrdAddress);

    Address parseToRewardAddress(String address);

    Map<String, Object> validateAndParsePoolRegistationParameters(
        PoolRegistrationParams poolRegistrationParameters);

    Map<String, Object> processWithdrawal(NetworkIdentifierType networkIdentifierType, Operation operation) throws CborSerializationException;

    Map<String, Object> processOperationCertification(NetworkIdentifierType networkIdentifierType, Operation operation) throws CborSerializationException;

    Certificate processStakeKeyRegistration(Operation operation);

    StakeCredential getStakingCredentialFromHex(PublicKey staking_credential);

    HdPublicKey getPublicKey(PublicKey publicKey);

    Boolean isKeyValid(String publicKeyBytes, String curveType);

    TransactionInput validateAndParseTransactionInput(Operation input);

    TransactionOutput validateAndParseTransactionOutput(Operation output);

    Object generateAddress(String address) throws AddressExcepion;

    EraAddressType getEraAddressType(String address);

    List<MultiAsset> validateAndParseTokenBundle(List<TokenBundleItem> tokenBundle);

    Boolean isPolicyIdValid(String policyId);

    Boolean isTokenNameValid(String name);

    Boolean isEmptyHexString(String toCheck);

    byte[] hexStringToBuffer(String input);

    NetworkIdentifierType getNetworkIdentifierByRequestParameters(NetworkIdentifier networkRequestParameters);

    boolean isAddressTypeValid(String type);

    Amount mapAmount(String value, String symbol, Integer decimals, Metadata metadata);

    String hexStringFormatter(String toFormat);

    Long calculateTxMinimumFee(Long transactionSize, ProtocolParametersResponse protocolParameters);

    ProtocolParametersResponse getProtocolParameters();

    Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborSerializationException, CborException;

    Long calculateTtl(Long ttlOffset);

    BlockDto getLatestBlock();

    Long findLatestBlockNumber();

    BlockDto findBlock(Long blockNumber, String blockHash);

    String encodeExtraData(String transaction, TransactionExtraData extraData) throws JsonProcessingException, CborSerializationException, CborException;

    Array decodeExtraData(String encoded);

    co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey);

    co.nstant.in.cbor.model.Map getAmountMap(Amount amount);

    List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash, Set<String> addresses);

    TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map);

    PublicKey getPublicKeyFromMap(co.nstant.in.cbor.model.Map stakingCredentialMap);

    Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap);

    TransactionParsed parseUnsignedTransaction(NetworkIdentifierType networkIdentifierType, String transaction, TransactionExtraData extraData) throws CborDeserializationException, UnknownHostException, AddressExcepion, JsonProcessingException;

    TransactionParsed parseSignedTransaction(NetworkIdentifierType networkIdentifierType, String transaction, TransactionExtraData extraData);

    List<String> getSignerFromOperation(NetworkIdentifierType networkIdentifierType, Operation operation) throws CborSerializationException;

    List<String> getPoolSigners(NetworkIdentifierType networkIdentifierType, Operation operation) throws CborSerializationException;

    Map<String, Object> validateAndParsePoolRegistrationCert(
        NetworkIdentifierType networkIdentifierType, String poolRegistrationCert, String poolKeyHash) throws CborSerializationException;

    List<AccountIdentifier> getUniqueAccountIdentifiers(List<String> addresses);

    List<AccountIdentifier> addressesToAccountIdentifiers(Set<String> uniqueAddresses);

    List<Operation> convert(TransactionBody transactionBody, TransactionExtraData extraData, Integer network) throws UnknownHostException, JsonProcessingException, AddressExcepion, CborDeserializationException, CborException, CborSerializationException;

    //need to revise
    Operation parseVoteMetadataToOperation(Long index, String transactionMetadataHex) throws CborDeserializationException;

    String remove0xPrefix(String hex);

    //need to revise
    Address getAddressFromHexString(String hex);

    void parseWithdrawalsToOperations(List<Operation> withdrawalOps, Integer withdrawalsCount, List<Operation> operations, Integer network) throws CborSerializationException;

    Operation parseWithdrawalToOperation(String value, String hex, Long index, String address);

    List<Operation> parseCertsToOperations(TransactionBody transactionBody, List<Operation> certOps, int network) throws UnknownHostException, JsonProcessingException, CborException, CborSerializationException;

    Operation parsePoolCertToOperation(Integer network, Certificate cert, Long index, String type) throws UnknownHostException, JsonProcessingException, CborSerializationException, CborException;

    PoolRegistrationParams parsePoolRegistration(Integer network, PoolRegistration poolRegistration) throws UnknownHostException, CborSerializationException;

    PoolMetadata parsePoolMetadata(PoolRegistration poolRegistration);

    PoolMargin parsePoolMargin(PoolRegistration poolRegistration);

    List<Relay> parsePoolRelays(PoolRegistration poolRegistration) throws UnknownHostException;

    List<String> parsePoolOwners(Integer network, PoolRegistration poolRegistration) throws CborSerializationException;

    String parsePoolRewardAccount(Integer network, PoolRegistration poolRegistration) throws CborSerializationException;

    Operation parseCertToOperation(Certificate cert, Long index, String hash, String type, String address);

    Operation parseOutputToOperation(TransactionOutput output, Long index, List<OperationIdentifier> relatedOperations, String address);

    OperationMetadata parseTokenBundle(TransactionOutput output);

    List<String> keys(List<Asset> collection);

    TokenBundleItem parseTokenAsset(List<MultiAsset> multiAssets, String policyId);

    Amount parseAsset(List<Asset> assets, String key) throws CborException;

    String getAddressPrefix(Integer network, StakeAddressPrefix addressPrefix);

    String parseAddress(String address, String addressPrefix) throws AddressExcepion;

    List<OperationIdentifier> getRelatedOperationsFromInputs(List<Operation> inputs);

    Operation parseInputToOperation(TransactionInput input, Long index);

    String getHashOfSignedTransaction(String signedTransaction);

    TransactionIdentifierResponse mapToConstructionHashResponse(String transactionHash);

    Set<String> validateAndParsePoolOwners(List<String> owners);
}
