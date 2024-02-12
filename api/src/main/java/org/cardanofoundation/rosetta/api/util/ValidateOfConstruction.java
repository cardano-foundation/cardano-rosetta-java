package org.cardanofoundation.rosetta.api.util;


import static java.math.BigInteger.valueOf;

import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.ByronAddress;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.util.HexUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.Amount;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.PoolMetadata;
import org.cardanofoundation.rosetta.api.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Relay;
import org.cardanofoundation.rosetta.api.model.TokenBundleItem;
import org.cardanofoundation.rosetta.api.model.VoteRegistrationMetadata;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistationParametersReturnDto;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistrationCertReturnDto;

@Slf4j
public class ValidateOfConstruction {

  private ValidateOfConstruction() {

  }

  public static Set<String> validateAndParsePoolOwners(List<String> owners) {
    Set<String> parsedOwners = new HashSet<>();
    try {
      owners.forEach(owner -> {
        Address address = new Address(owner);
        Optional<byte[]> bytes = address.getDelegationCredentialHash();
        bytes.ifPresent(value -> parsedOwners.add(HexUtil.encodeHexString(value)));
      });
    } catch (Exception error) {
      log.error("[validateAndParsePoolOwners] there was an error parsing pool owners");
      throw ExceptionFactory.invalidPoolOwnersError(error.getMessage());
    }
    if (parsedOwners.size() != owners.size()) {
      throw ExceptionFactory.invalidPoolOwnersError("Invalid pool owners addresses provided");
    }
    return parsedOwners;
  }

  public static void validateChainCode(String chainCode){
    if (ObjectUtils.isEmpty(chainCode)) {
      log.error(
          "[getWitnessesForTransaction] Missing chain code for byron address signature");
      throw ExceptionFactory.missingChainCodeError();
    }
  }

  public static Map<String, Object> validateAndParseVoteRegistrationMetadata(
      VoteRegistrationMetadata voteRegistrationMetadata) {

    log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse voting key");
    String parsedVotingKey = validateAndParseVotingKey(
        voteRegistrationMetadata.getVotingKey());
    log.info("[validateAndParseVoteRegistrationMetadata] About to validate and parse stake key");
    if (ObjectUtils.isEmpty(voteRegistrationMetadata.getStakeKey().getHexBytes())) {
      throw ExceptionFactory.missingStakingKeyError();
    }
    boolean checkKey = CardanoAddressUtils.isKeyValid(voteRegistrationMetadata.getStakeKey().getHexBytes(),
        voteRegistrationMetadata.getStakeKey().getCurveType());
    if (!checkKey) {
      throw ExceptionFactory.invalidStakingKeyFormat();
    }
    String parsedStakeKey = voteRegistrationMetadata.getStakeKey().getHexBytes();
    log.info(
        "[validateAndParseVoteRegistrationMetadata] About to validate and parse reward address");
    String parsedAddress;
    try {
      if (voteRegistrationMetadata.getRewardAddress().startsWith("addr")) {
        throw ExceptionFactory.invalidAddressError();
      }
      Address address = new Address(voteRegistrationMetadata.getRewardAddress());
      parsedAddress = HexUtil.encodeHexString(address.getBytes());
    } catch (Exception exception) {
      throw ExceptionFactory.invalidAddressError();
    }

    log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting nonce");
    if (voteRegistrationMetadata.getVotingNonce() <= 0) {
      log.error("[validateAndParseVoteRegistrationMetadata] Given voting nonce {} is invalid",
          voteRegistrationMetadata.getVotingNonce());
      throw ExceptionFactory.votingNonceNotValid();
    }

    log.info("[validateAndParseVoteRegistrationMetadata] About to validate voting signature");
    if (ObjectUtils.isEmpty(voteRegistrationMetadata.getVotingSignature())) {
      throw ExceptionFactory.invalidVotingSignature();
    }
    if (!CardanoAddressUtils.isEd25519Signature(voteRegistrationMetadata.getVotingSignature())) {
      log.error(
          "[validateAndParseVoteRegistrationMetadata] Voting signature has an invalid format");
      throw ExceptionFactory.invalidVotingSignature();
    }
    String votingKeyHex = CardanoAddressUtils.add0xPrefix(parsedVotingKey);
    String stakeKeyHex = CardanoAddressUtils.add0xPrefix(parsedStakeKey);
    String rewardAddressHex = CardanoAddressUtils.add0xPrefix(parsedAddress);
    String votingSignatureHex = CardanoAddressUtils.add0xPrefix(voteRegistrationMetadata.getVotingSignature());
    HashMap<String, Object> map = new HashMap<>();
    map.put(Constants.VOTING_KEY, votingKeyHex);
    map.put(Constants.STAKE_KEY, stakeKeyHex);
    map.put(Constants.REWARD_ADDRESS, rewardAddressHex);
    map.put(Constants.VOTING_NONCE, voteRegistrationMetadata.getVotingNonce());
    map.put(Constants.VOTING_SIGNATURE, votingSignatureHex);

    return map;
  }

  public static String validateAndParseVotingKey(PublicKey votingKey) {
    if (ObjectUtils.isEmpty(votingKey.getHexBytes())) {
      log.error("[validateAndParsePublicKey] Voting key not provided");
      throw ExceptionFactory.missingVotingKeyError();
    }
    boolean checkKey = CardanoAddressUtils.isKeyValid(votingKey.getHexBytes(), votingKey.getCurveType());
    if (!checkKey) {
      log.info("[validateAndParsePublicKey] Voting key has an invalid format");
      throw ExceptionFactory.invalidVotingKeyFormat();
    }
    return votingKey.getHexBytes();
  }
  public static byte[] validateAndParsePoolKeyHash(String poolKeyHash) {
    if (ObjectUtils.isEmpty(poolKeyHash)) {
      log.error("[validateAndParsePoolKeyHash] no pool key hash provided");
      throw ExceptionFactory.missingPoolKeyError();
    }
    byte[] parsedPoolKeyHash;
    try {
      parsedPoolKeyHash = HexUtil.decodeHexString(poolKeyHash);

    } catch (Exception error) {
      log.error("[validateAndParsePoolKeyHash] invalid pool key hash");
      throw ExceptionFactory.invalidPoolKeyError(error.getMessage());
    }
    return parsedPoolKeyHash;
  }

  public static PoolMetadata validateAndParsePoolMetadata(PoolMetadata metadata) {
    PoolMetadata parsedMetadata = null;
    try {
      if (!ObjectUtils.isEmpty(metadata)) {
        HexUtil.decodeHexString(metadata.getHash());
        parsedMetadata = new PoolMetadata(metadata.getUrl(), metadata.getHash());
        return parsedMetadata;
      }
    } catch (Exception error) {
      log.error("[validateAndParsePoolMetadata] invalid pool metadata");
      throw ExceptionFactory.invalidPoolMetadataError(error.getMessage());
    }
    return parsedMetadata;
  }
  public static List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> validateAndParsePoolRelays(
      List<Relay> relays) {
    if (relays.isEmpty()) {
      throw ExceptionFactory.invalidPoolRelaysError();
    }
    List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> generatedRelays = new ArrayList<>();
    for (Relay relay : relays) {
      if (!ObjectUtils.isEmpty(relay.getPort())) {
        validatePort(relay.getPort());
      }
      com.bloxbean.cardano.client.transaction.spec.cert.Relay generatedRelay = CardanoAddressUtils.generateSpecificRelay(
          relay);
      generatedRelays.add(generatedRelay);
    }

    return generatedRelays;
  }
  public static void validatePort(String port) {
    try {
      if (!port.matches(Constants.IS_POSITIVE_NUMBER)) {
        log.error("[validateAndParsePort] Invalid port {} received", port);
        throw ExceptionFactory.invalidPoolRelaysError("Invalid port " + port + " received");
      }
    } catch (Exception e) {
      throw ExceptionFactory.invalidPoolRelaysError("Invalid port " + port + " received");
    }
  }
  public static void validateDnsName(String dnsName){
    if (ObjectUtils.isEmpty(dnsName)) {
      throw ExceptionFactory.missingDnsNameError();
    }
  }
  public static Address validateAndParseRewardAddress(String rwrdAddress) {
    Address rewardAddress = null;
    try {
      rewardAddress = parseToRewardAddress(rwrdAddress);
    } catch (Exception error) {
      log.error("[validateAndParseRewardAddress] invalid reward address {}", rewardAddress);
      throw ExceptionFactory.invalidAddressError();
    }
    if (ObjectUtils.isEmpty(rewardAddress)) {
      throw ExceptionFactory.invalidAddressError();
    }
    return rewardAddress;
  }
  public static Address parseToRewardAddress(String address) {
    return new Address(address);
  }
  public static PoolRegistationParametersReturnDto validateAndParsePoolRegistationParameters(
      PoolRegistrationParams poolRegistrationParameters) {
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
      throw ExceptionFactory.invalidPoolRegistrationParameters("Missing margin parameter at pool registration parameters");
    }
    logInvalidValue(denominator,numerator,poolRegistrationParameters);
    Map<String,String> map=new HashMap<>();
    map.put("cost",poolRegistrationParameters.getCost());
    map.put("pledge",poolRegistrationParameters.getPledge());
    map.put("numerator",numerator);
    map.put("denominator",denominator);
    for(Entry<String, String> s:map.entrySet()){
      if(s.getValue()!=null&&!s.getValue().matches(Constants.IS_POSITIVE_NUMBER)){
        throw ExceptionFactory.invalidPoolRegistrationParameters("Given "+s.getKey()+" "+s.getValue()+" is invalid");
      }
    }
    try {
      PoolRegistationParametersReturnDto poolRegistationParametersReturnDto = new PoolRegistationParametersReturnDto();
      poolRegistationParametersReturnDto.setCost(valueOf(Long.parseLong(poolRegistrationParameters.getCost())));
      poolRegistationParametersReturnDto.setPledge(valueOf(Long.parseLong(poolRegistrationParameters.getPledge())));
      poolRegistationParametersReturnDto.setNumerator(valueOf(Long.parseLong(numerator)));
      poolRegistationParametersReturnDto.setDenominator(valueOf(Long.parseLong(denominator)));

      return poolRegistationParametersReturnDto;
    } catch (Exception error) {
      log.error("[validateAndParsePoolRegistationParameters] Given pool parameters are invalid");
      throw ExceptionFactory.invalidPoolRegistrationParameters(error.getMessage());
    }
  }
  public static void logInvalidValue(String denominator,
      String numerator,
      PoolRegistrationParams poolRegistrationParameters){
    if(numerator!=null&&denominator!=null && (!poolRegistrationParameters.getCost().matches(Constants.IS_POSITIVE_NUMBER) ||
        !poolRegistrationParameters.getPledge().matches(Constants.IS_POSITIVE_NUMBER) ||
        !numerator.matches(Constants.IS_POSITIVE_NUMBER) ||
        !denominator.matches(Constants.IS_POSITIVE_NUMBER))) {
      log.error("[validateAndParsePoolRegistationParameters] Given value is invalid");

    }
  }
  public static TransactionInput validateAndParseTransactionInput(Operation input) {
    if (ObjectUtils.isEmpty(input.getCoinChange())) {
      log.error("[validateAndParseTransactionInput] Input has missing coin_change");
      throw ExceptionFactory.transactionInputsParametersMissingError("Input has missing coin_change field");
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
      throw ExceptionFactory.transactionInputsParametersMissingError("Input has invalid coin_identifier field");
    }
    if (ObjectUtils.isEmpty(transactionId) || ObjectUtils.isEmpty(index)) {
      log.error("[validateAndParseTransactionInput] Input has missing transactionId and index");
      throw ExceptionFactory.transactionInputsParametersMissingError("Input has invalid coin_identifier field");
    }
    String value = ObjectUtils.isEmpty(input.getAmount()) ? null : input.getAmount().getValue();
    if (value==null) {
      log.error("[validateAndParseTransactionInput] Input has missing amount value field");
      throw ExceptionFactory.transactionInputsParametersMissingError("Input has missing amount value field");
    }
    if (value.matches(Constants.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParseTransactionInput] Input has positive value");
      throw ExceptionFactory.transactionInputsParametersMissingError("Input has positive amount value");
    }
    try {
      return new TransactionInput(
          HexUtil.encodeHexString(HexUtil.decodeHexString(transactionId)),
          Integer.parseInt(index));
    } catch (Exception e) {
      throw ExceptionFactory.deserializationError(e.getMessage());
    }
  }

  public static TransactionOutput validateAndParseTransactionOutput(Operation output) {
    Object address;
    try {
      address = ObjectUtils.isEmpty(output.getAccount()) ? null
          : CardanoAddressUtils.generateAddress(output.getAccount().getAddress());
    } catch (Exception error) {
      throw ExceptionFactory.transactionOutputDeserializationError("Invalid input: " + output.getAccount().getAddress() + " "
          + error.getMessage());
    }
    if (ObjectUtils.isEmpty(address)) {
      log.error("[validateAndParseTransactionOutput] Output has missing address field");
      throw ExceptionFactory.transactionOutputDeserializationError("Output has missing address field");
    }
    String outputValue =
        ObjectUtils.isEmpty(output.getAmount()) ? null : output.getAmount().getValue();
    if (ObjectUtils.isEmpty(output.getAmount()) || outputValue == null) {
      log.error("[validateAndParseTransactionOutput] Output has missing amount value field");
      throw ExceptionFactory.transactionOutputDeserializationError("Output has missing amount field");
    }
    if (!outputValue.matches(Constants.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParseTransactionOutput] Output has negative or invalid value {}",
          outputValue);
      throw ExceptionFactory.transactionOutputDeserializationError("Output has negative amount value");
    }
    Value value = Value.builder().coin(valueOf(Long.parseLong(outputValue))).build();
    if (!ObjectUtils.isEmpty(output.getMetadata()) && !ObjectUtils.isEmpty(
        output.getMetadata().getTokenBundle())) {
      value.setMultiAssets(validateAndParseTokenBundle(output.getMetadata().getTokenBundle()));
    }
    if(address!=null){
      if (address instanceof Address address1) {
        return new TransactionOutput(address1.getAddress(), value);
      }
      if (address instanceof ByronAddress address1) {
        return new TransactionOutput(address1.getAddress(), value);
      }
    }
    return new TransactionOutput(null, value);
  }

  public static List<MultiAsset> validateAndParseTokenBundle(List<TokenBundleItem> tokenBundle) {
    List<MultiAsset> multiAssets = new ArrayList<>();
    tokenBundle.forEach(tokenBundleItem -> {
      validateCheckKey(tokenBundleItem.getPolicyId());
      List<Asset> assets = new ArrayList<>();
      List<Asset> assetsCheck = new ArrayList<>();
      tokenBundleItem.getTokens().forEach(token -> {
        validateTokenName(token.getCurrency().getSymbol());
        String assetName = token.getCurrency().getSymbol();
        if (assetsCheck.stream().anyMatch(asset -> asset.getName().equals(assetName))) {
          log.error(
              "[validateAndParseTokenBundle] Token name {} has already been added for policy {}",
              token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
          throw ExceptionFactory.transactionOutputsParametersMissingError("Token name " + token.getCurrency().getSymbol() + " has already been added for policy " +
              tokenBundleItem.getPolicyId() + " and will be overriden");
        }
        validateTokenValue(token,tokenBundleItem);
        //revise
        if (token.getCurrency().getSymbol().equals(Constants.SYMBOL_REGEX)) {
          token.getCurrency().setSymbol("");
        }
        assets.add(new Asset(
            token.getCurrency().getSymbol().startsWith(Constants.EMPTY_SYMBOl) ?
                token.getCurrency().getSymbol() : Constants.EMPTY_SYMBOl + token.getCurrency().getSymbol(),
            valueOf(Long.parseLong(token.getValue()))));
        assetsCheck.add(new Asset(token.getCurrency().getSymbol(),
            valueOf(Long.parseLong(token.getValue()))));
      });
      multiAssets.add(new MultiAsset(tokenBundleItem.getPolicyId(), assets));
    });
    return multiAssets;
  }
  public static void validateCheckKey(String policyId){
    boolean checckKey = CardanoAddressUtils.isPolicyIdValid(policyId);
    if (!checckKey) {
      log.error("[validateAndParseTokenBundle] PolicyId {} is not valid",
          policyId);
      throw ExceptionFactory.transactionOutputsParametersMissingError("PolicyId " + policyId
          + " is not valid");
    }
  }
  public static void validateTokenName(String tokenName){
    boolean checkTokenName = CardanoAddressUtils.isTokenNameValid(tokenName);
    if (!checkTokenName) {
      log.error("validateAndParseTokenBundle] Token name {} is not valid",
          tokenName);
      throw ExceptionFactory.transactionOutputsParametersMissingError("Token name " + tokenName + " is not valid");
    }
  }

  public static void validateTokenValue(Amount token, TokenBundleItem tokenBundleItem){
    if (ObjectUtils.isEmpty(token.getValue()) || ObjectUtils.isEmpty(
        token.getValue().charAt(0))) {
      log.error(
          "[validateAndParseTokenBundle] Token with name {} for policy {} has no value or is empty",
          token.getCurrency().getSymbol(), tokenBundleItem.getPolicyId());
      throw ExceptionFactory.transactionOutputsParametersMissingError("Token with name " + token.getCurrency().getSymbol() + " for policy "
          + tokenBundleItem.getPolicyId() + " has no value or is empty");
    }
    if (!token.getValue().matches(Constants.IS_POSITIVE_NUMBER)) {
      log.error("[validateAndParseTokenBundle] Asset {} has negative or invalid value {}",
          token.getCurrency().getSymbol(), token.getValue());
      throw ExceptionFactory.transactionOutputsParametersMissingError("Asset " + token.getCurrency().getSymbol()
          + " has negative or invalid value " + token.getValue());
    }
  }
  public static PoolRegistrationCertReturnDto validateAndParsePoolRegistrationCert(
      NetworkIdentifierType networkIdentifierType, String poolRegistrationCert, String poolKeyHash) {
    if (ObjectUtils.isEmpty(poolKeyHash)) {
      log.error(
          "[validateAndParsePoolRegistrationCert] no cold key provided for pool registration");
      throw ExceptionFactory.missingPoolKeyError();
    }
    if (ObjectUtils.isEmpty(poolRegistrationCert)) {
      log.error(
          "[validateAndParsePoolRegistrationCert] no pool registration certificate provided for pool registration"
      );
      throw ExceptionFactory.missingPoolCertError();
    }
    PoolRegistration parsedCertificate;
    try {
      DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
          HexUtil.decodeHexString(poolRegistrationCert));
      parsedCertificate = PoolRegistration.deserialize(dataItem);
    } catch (Exception error) {
      log.error("[validateAndParsePoolRegistrationCert] invalid pool registration certificate");
      throw ExceptionFactory.invalidPoolRegistrationCert(error.getMessage());
    }
    if (ObjectUtils.isEmpty(parsedCertificate)) {
      log.error("[validateAndParsePoolRegistrationCert] invalid certificate type");
      throw ExceptionFactory.invalidPoolRegistrationCertType();
    }
    List<String> ownersAddresses = ParseConstructionUtils.parsePoolOwners(networkIdentifierType.getValue(),
        parsedCertificate);
    String rewardAddress = ParseConstructionUtils.parsePoolRewardAccount(networkIdentifierType.getValue(),
        parsedCertificate);
    Set<String> addresses = new HashSet<>(new HashSet<>(ownersAddresses));
    addresses.add(poolKeyHash);
    addresses.add(rewardAddress);
    PoolRegistrationCertReturnDto poolRegistrationCertReturnDto = new PoolRegistrationCertReturnDto();
    poolRegistrationCertReturnDto.setCertificate(parsedCertificate);
    poolRegistrationCertReturnDto.setAddress(addresses);
    return poolRegistrationCertReturnDto;

  }
  public static Certificate validateCert(List<Certificate> certs, int i) {
    return ObjectUtils.isEmpty(certs) ? null : certs.get(i);
  }
  public static void validateHex(String hex) {
    if (ObjectUtils.isEmpty(hex)) {
      log.error("[parseCertsToOperations] Staking key not provided");
      throw ExceptionFactory.missingStakingKeyError();
    }
  }
  public static boolean validateAddressPresence(Operation operation) {
    return !ObjectUtils.isEmpty(ObjectUtils.isEmpty(operation.getAccount()) ? null
        : operation.getAccount().getAddress());
  }
  public static String validateValueAmount(Operation operation) {
    return ObjectUtils.isEmpty(operation.getAmount()) ? null : operation.getAmount().getValue();
  }
}
