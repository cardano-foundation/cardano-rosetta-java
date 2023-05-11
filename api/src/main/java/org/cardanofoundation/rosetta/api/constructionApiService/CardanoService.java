package org.cardanofoundation.rosetta.api.constructionApiService;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.Relay;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential;
import com.bloxbean.cardano.client.transaction.spec.script.Script;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import org.cardanofoundation.rosetta.api.addedClass.*;
import org.cardanofoundation.rosetta.api.addedenum.AddressType;
import org.cardanofoundation.rosetta.api.addedenum.EraAddressType;
import org.cardanofoundation.rosetta.api.addedenum.NetworkIdentifierEnum;
import org.cardanofoundation.rosetta.api.addedenum.StakeAddressPrefix;
import org.cardanofoundation.rosetta.api.model.*;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface CardanoService {
    String hex(byte[] bytes);

    String generateAddress(NetworkIdentifierEnum networkIdentifierEnum, String publicKeyString, String stakingCredentialString, AddressType type) throws IllegalAccessException, CborSerializationException;

    String generateRewardAddress(NetworkIdentifierEnum networkIdentifierEnum, HdPublicKey paymentCredential) throws CborSerializationException;

    String generateBaseAddress(NetworkIdentifierEnum networkIdentifierEnum, HdPublicKey paymentCredential, HdPublicKey stakingCredential) throws CborSerializationException;

    String generateEnterpriseAddress(HdPublicKey paymentCredential, NetworkIdentifierEnum networkIdentifierEnum) throws CborSerializationException;

    Double calculateRelativeTtl(Double relativeTtl);

    Double calculateTxSize(NetworkIdentifierEnum networkIdentifierEnum, ArrayList<Operation> operations, Integer ttl, DepositParameters depositParameters)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    String buildTransaction(String unsignedTransaction, List<AddedSignatures> addedSignaturesList, String transactionMetadata);

    TransactionWitnessSet getWitnessesForTransaction(List<AddedSignatures> addedSignaturesList);

    EraAddressType getEraAddressTypeOrNull(String address);

    boolean isEd25519KeyHash(String address);

    AddedSignatures signatureProcessor(EraAddressType eraAddressType, AddressType addressType, String address);

    UnsignedTransaction createUnsignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, List<Operation> operations, Integer ttl, DepositParameters depositParameters)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    Map<String, Object> processOperations(NetworkIdentifierEnum networkIdentifierEnum, List<Operation> operations, DepositParameters depositParameters);

    Long calculateFee(ArrayList<String> inputAmounts, ArrayList<String> outputAmounts, ArrayList<Long> withdrawalAmounts, Map<String, Double> depositsSumMap);

    ProcessOperationsResult convert(NetworkIdentifierEnum networkIdentifierEnum, List<Operation> operations);

    ProcessOperationsResult operationProcessor(Operation operation, NetworkIdentifierEnum networkIdentifierEnum,
                                               ProcessOperationsResult resultAccumulator, String type) throws JsonProcessingException, CborSerializationException, CborDeserializationException;

    AuxiliaryData processVoteRegistration(Operation operation) throws JsonProcessingException, CborDeserializationException;

    Map<String, Object> validateAndParseVoteRegistrationMetadata(VoteRegistrationMetadata voteRegistrationMetadata);

    String bytesToHex(byte[] bytes);

    String hexFormatter(byte[] bytes);

    String add0xPrefix(String hex);

    HdPublicKey validateAndParseVotingKey(PublicKey votingKey);

    Map<String, Object> processPoolRetirement(Operation operation);

    byte[] validateAndParsePoolKeyHash(String poolKeyHash);

    Map<String, Object> processPoolRegistration(NetworkIdentifierEnum networkIdentifierEnum, Operation operation);

    PoolMetadata validateAndParsePoolMetadata(PoolMetadata metadata);

    List<Relay> validateAndParsePoolRelays(List<Relay1> relays);

    Relay generateSpecificRelay(Relay1 relay);

    Inet4Address parseIpv4(String ip) throws UnknownHostException;

    Inet6Address parseIpv6(String ip) throws UnknownHostException;

    void validatePort(String port);

    Address validateAndParseRewardAddress(String rwrdAddress);

    Address parseToRewardAddress(String address);

    Map<String, Object> validateAndParsePoolRegistationParameters(PoolRegistrationParams poolRegistrationParameters);

    Map<String, Object> processWithdrawal(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) throws CborSerializationException;

    Map<String, Object> processOperationCertification(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) throws CborSerializationException;

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

    NetworkIdentifierEnum getNetworkIdentifierByRequestParameters(NetworkIdentifier networkRequestParameters);

    boolean isAddressTypeValid(String type);

    Amount mapAmount(String value, String symbol, Integer decimals, AddedMetadata addedMetadata);

    String hexStringFormatter(String toFormat);

    Long calculateTxMinimumFee(Long transactionSize, ProtocolParametersResponse protocolParameters);

    ProtocolParametersResponse getProtocolParameters();

    Long updateTxSize(Long previousTxSize, Long previousTtl, Long updatedTtl) throws CborSerializationException, CborException;

    Long calculateTtl(Long ttlOffset);

    BlockResponse getLatestBlock();

    Long findLatestBlockNumber();

    BlockResponse findBlock(Long blockNumber, byte[] blockHash);

    String encodeExtraData(String transaction, TransactionExtraData extraData) throws JsonProcessingException, CborSerializationException, CborException;

    co.nstant.in.cbor.model.Map decodeExtraData(String encoded);

    co.nstant.in.cbor.model.Map getPublicKeymap(PublicKey publicKey);

    co.nstant.in.cbor.model.Map getAmountMap(Amount amount);

    List<SigningPayload> constructPayloadsForTransactionBody(String transactionBodyHash, Set<String> addresses);

    TransactionExtraData changeFromMaptoObject(co.nstant.in.cbor.model.Map map);

    PublicKey getPublicKeyFromMap(co.nstant.in.cbor.model.Map stakingCredentialMap);

    Amount getAmountFromMap(co.nstant.in.cbor.model.Map amountMap);

    TransactionParsed parseUnsignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, String transaction, TransactionExtraData extraData) throws CborDeserializationException, UnknownHostException, AddressExcepion, JsonProcessingException;

    TransactionParsed parseSignedTransaction(NetworkIdentifierEnum networkIdentifierEnum, String transaction, TransactionExtraData extraData);

    List<String> getSignerFromOperation(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) throws CborSerializationException;

    List<String> getPoolSigners(NetworkIdentifierEnum networkIdentifierEnum, Operation operation) throws CborSerializationException;

    Map<String, Object> validateAndParsePoolRegistrationCert(NetworkIdentifierEnum networkIdentifierEnum, String poolRegistrationCert, String poolKeyHash) throws CborSerializationException;

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

    List<Relay1> parsePoolRelays(PoolRegistration poolRegistration) throws UnknownHostException;

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
