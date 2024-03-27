package org.cardanofoundation.rosetta.common.util;

import static com.bloxbean.cardano.client.address.AddressType.Reward;
import static java.math.BigInteger.valueOf;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadataMap;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.transaction.spec.cert.MultiHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.PoolRetirement;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostAddr;
import com.bloxbean.cardano.client.transaction.spec.cert.SingleHostName;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDelegation;
import com.bloxbean.cardano.client.util.HexUtil;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.common.enumeration.CatalystLabels;
import org.cardanofoundation.rosetta.common.enumeration.CatalystSigIndexes;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.model.cardano.network.RelayType;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.CoinChange;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurveType;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolMargin;
import org.openapitools.client.model.PoolMetadata;
import org.openapitools.client.model.PoolRegistrationParams;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.Relay;
import org.openapitools.client.model.TokenBundleItem;
import org.openapitools.client.model.VoteRegistrationMetadata;

@Slf4j
public class ParseConstructionUtil {

  public static Inet4Address parseIpv4(String ip) throws UnknownHostException {
    if (!ObjectUtils.isEmpty(ip)) {
      String[] ipNew = ip.split("\\.");
      byte[] bytes = new byte[ipNew.length];
      for (int i = 0; i < ipNew.length; i++) {
        bytes[i] = Byte.parseByte(ipNew[i]);
      }
      return (Inet4Address) InetAddress.getByAddress(bytes);
    }
    throw new UnknownHostException("Error Parsing IP Address");
  }

  public static Inet6Address parseIpv6(String ip) throws UnknownHostException {
    if (!ObjectUtils.isEmpty(ip)) {
      String ipNew = ip.replace(":", "");
      byte[] parsedIp = HexUtil.decodeHexString(ipNew);
      return (Inet6Address) InetAddress.getByAddress(parsedIp);
    }
    throw new UnknownHostException("Error Parsing IP Address");
  }


  public static List<String> getOwnerAddressesFromPoolRegistrations(String networkId, PoolRegistration poolRegistration) {
    List<String> poolOwners = new ArrayList<>();
    Set<String> owners = poolRegistration.getPoolOwners();
    for(String owner : owners) {
      if (networkId.equals(NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getNetworkId())) {
        Address address = CardanoAddressUtils.getAddress(null,
            HexUtil.decodeHexString(owner), Constants.STAKE_KEY_HASH_HEADER_KIND,
            Networks.testnet(), Reward);
        poolOwners.add(address.getAddress());
      }
      if (networkId.equals(NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getNetworkId())) {
        Address address = CardanoAddressUtils.getAddress(null,
            HexUtil.decodeHexString(owner), Constants.STAKE_KEY_HASH_HEADER_KIND,
            Networks.preprod(), Reward);
        poolOwners.add(address.getAddress());
      }
      if (networkId.equals(NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getNetworkId())) {
        Address address = CardanoAddressUtils.getAddress(null,
            HexUtil.decodeHexString(owner), Constants.STAKE_KEY_HASH_HEADER_KIND,
            Networks.mainnet(), Reward);
        poolOwners.add(address.getAddress());
      }
    }
    return poolOwners;
  }

  public static String getRewardAddressFromPoolRegistration(String networkId, PoolRegistration poolRegistration) {
    String cutRewardAccount = poolRegistration.getRewardAccount();
    if (cutRewardAccount.length() == Constants.HEX_PREFIX_AND_REWARD_ACCOUNT_LENGTH) {
      // removing prefix 0x from reward account, reward account is 56 bytes
      cutRewardAccount = poolRegistration.getRewardAccount().substring(2);
    }
    if (networkId.equals(NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getNetworkId())) {
      return CardanoAddressUtils.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
                      Constants.STAKE_KEY_HASH_HEADER_KIND,
                      Networks.testnet(), Reward)
              .getAddress();
    }
    if (networkId.equals(NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getNetworkId())) {
      return CardanoAddressUtils.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
              Constants.STAKE_KEY_HASH_HEADER_KIND,
                      Networks.preprod(), Reward)
              .getAddress();
    }
    if (networkId.equals(NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getNetworkId())) {
      return CardanoAddressUtils.getAddress(null,
                      HexUtil.decodeHexString(cutRewardAccount),
              Constants.STAKE_KEY_HASH_HEADER_KIND,
                      Networks.mainnet(), Reward)
              .getAddress();
    }

    throw ExceptionFactory.invalidAddressError("Can't get Reward address from PoolRegistration");
  }

  public static Operation TransactionInputToOperation(TransactionInput input, Long index) {
    return new Operation(new OperationIdentifier(index, null), null, OperationType.INPUT.getValue(),
        "", null, null,
        new CoinChange(new CoinIdentifier(
            input.getTransactionId() + ":"
                + input.getIndex()), CoinAction.SPENT), null);
  }

  public static Operation TransActionOutputToOperation(TransactionOutput output, Long index,
      List<OperationIdentifier> relatedOperations) {
    OperationIdentifier operationIdentifier = new OperationIdentifier(index, null);
    AccountIdentifier account = new AccountIdentifier(output.getAddress(), null, null);
    Amount amount = new Amount(output.getValue().getCoin().toString(),
        new Currency(Constants.ADA, Constants.ADA_DECIMALS, null), null);
    return new Operation(operationIdentifier, relatedOperations, OperationType.OUTPUT.getValue(),
        "",
        account, amount, null, parseTokenBundle(output));
  }

  public static List<OperationIdentifier> getRelatedOperationsFromInputs(List<Operation> inputs) {
    return inputs.stream()
        .map(input -> new OperationIdentifier(input.getOperationIdentifier().getIndex(), null))
        .toList();
  }

  public static OperationMetadata parseTokenBundle(TransactionOutput output) {
    List<MultiAsset> multiassets = output.getValue().getMultiAssets();
    List<TokenBundleItem> tokenBundle = new ArrayList<>();
    if (!ObjectUtils.isEmpty(multiassets)) {
      log.info("[parseTokenBundle] About to parse {} multiassets from token bundle",
          multiassets.size());
      tokenBundle = multiassets.stream()
          .map(key -> parseTokenAsset(multiassets, key.getPolicyId()))
          .sorted(Comparator.comparing(TokenBundleItem::getPolicyId))
          .toList();
    }

    return !ObjectUtils.isEmpty(multiassets) ? OperationMetadata.builder().tokenBundle(tokenBundle).build() : null;
  }

  public static TokenBundleItem parseTokenAsset(List<MultiAsset> multiAssets, String policyId) {
    MultiAsset mergedMultiAssets = multiAssets.get(0);
    for (int i = 1; i < multiAssets.size(); i++) {
      mergedMultiAssets.plus(multiAssets.get(i));
    }
    if (ObjectUtils.isEmpty(mergedMultiAssets.getAssets())) {
      log.error("[parseTokenBundle] assets for policyId: {} not provided", policyId);
      throw ExceptionFactory.tokenBundleAssetsMissingError();
    }
    List<Amount> tokens = (keys(mergedMultiAssets.getAssets())).stream()
        .map(key -> {
          try {
            return ParseConstructionUtil.parseAsset(mergedMultiAssets.getAssets(), key);
          } catch (CborException e) {
            throw ExceptionFactory.unspecifiedError(e.getMessage());
          }
        })
        .sorted(Comparator.comparing(assetA -> assetA.getCurrency().getSymbol())).toList();
    return new TokenBundleItem(policyId, tokens);
  }

  public static List<String> keys(List<Asset> collection) {
    return collection.stream().map(Asset::getName).toList();
  }

  public static Amount parseAsset(List<Asset> assets, String key) throws CborException {
// When getting the key we are obtaining a cbor encoded string instead of the actual name.
    // This might need to be changed in the serialization lib in the future
    for (Asset a : assets) {
      if (a.getName().startsWith("0x")) {
        a.setName(a.getName().substring(2));
        if (a.getName().equals("")) {
          a.setName("\\x");
        }
      }
    }
    if (key.startsWith("0x")) {
      key = key.substring(2);
    }
    if (key.equals("")) {
      key = "\\x";
    }
    String assetSymbol = key;
    AtomicLong assetValue = new AtomicLong();
    for (Asset a : assets) {
      if (a.getName().equals(key) && !ObjectUtils.isEmpty(a.getValue())) {
        assetValue.addAndGet(a.getValue().longValue());
      }
    }
    if (assetValue.get() == 0) {
      log.error("[parseTokenBundle] asset value for symbol: {} not provided", assetSymbol);
      throw ExceptionFactory.tokenAssetValueMissingError();
    }
    return DataMapper.mapAmount(assetValue.toString(), assetSymbol, 0, null);
  }

  public static List<Operation> parseCertsToOperations(TransactionBody transactionBody,
      List<Operation> certOps, int network)
      throws CborException, CborSerializationException {
    List<Operation> parsedOperations = new ArrayList<>();
    List<Certificate> certs = transactionBody.getCerts();
    int certsCount = getCertSize(certs);
    log.info("[parseCertsToOperations] About to parse {} certs", certsCount);

    for (int i = 0; i < certsCount; i++) {
      Operation certOperation = certOps.get(i);
      if (Constants.StakingOperations.contains(certOperation.getType())) {
        String hex = null;
        if (checkStakeCredential(certOperation)) {
          hex = certOperation.getMetadata().getStakingCredential().getHexBytes();
        }
        Optional.ofNullable(hex).orElseThrow(ExceptionFactory::missingStakingKeyError);
        HdPublicKey hdPublicKey = new HdPublicKey();
        hdPublicKey.setKeyData(HexUtil.decodeHexString(hex));
        String address = CardanoAddressUtils.generateRewardAddress(
            Objects.requireNonNull(NetworkIdentifierType.find(network)), hdPublicKey);
        Certificate cert = ValidateParseUtil.validateCert(certs, i);
        if (!ObjectUtils.isEmpty(cert)) {
          Operation parsedOperation = parseCertToOperation(
              cert,
              certOperation.getOperationIdentifier().getIndex(),
              hex,
              certOperation.getType(),
              address
          );
          parsedOperations.add(parsedOperation);
        }
      } else {
        Certificate cert = ValidateParseUtil.validateCert(certs, i);
        if (!ObjectUtils.isEmpty(cert)) {
          Operation parsedOperation = parsePoolCertToOperation(
              network,
              cert,
              certOperation.getOperationIdentifier().getIndex(),
              certOperation.getType()
          );
          parsedOperation.setAccount(certOperation.getAccount());
          parsedOperations.add(parsedOperation);
        }
      }

    }

    return parsedOperations;
  }

  public static Operation parsePoolCertToOperation(Integer network, Certificate cert, Long index,
      String type)
      throws CborSerializationException, CborException {
    Operation operation = Operation.builder()
        .operationIdentifier(new OperationIdentifier(index, null))
        .type(type)
        .status("")
        .build();

    if (type.equals(OperationType.POOL_RETIREMENT.getValue())) {
      PoolRetirement poolRetirementCert = (PoolRetirement) cert;
      if (!ObjectUtils.isEmpty(poolRetirementCert)) {
        operation.getMetadata().setEpoch((int) poolRetirementCert.getEpoch());
      }
    } else {
      PoolRegistration poolRegistrationCert = null;
      try {
        poolRegistrationCert = (PoolRegistration) cert;
      } catch (Exception e) {
        log.info("Not a PoolRegistration");
      }
      if (!ObjectUtils.isEmpty(poolRegistrationCert)) {
        if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
          PoolRegistrationParams poolRegistrationParams = parsePoolRegistration(network,
              poolRegistrationCert);
          operation.getMetadata().setPoolRegistrationParams(poolRegistrationParams);
        } else {
          String parsedPoolCert = HexUtil.encodeHexString(
              com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.serialize(
                  poolRegistrationCert.serialize()));
          operation.getMetadata().setPoolRegistrationCert(parsedPoolCert);
        }
      }
    }
    return operation;
  }

  public static PoolRegistrationParams parsePoolRegistration(Integer network,
      PoolRegistration poolRegistration) {
    return new PoolRegistrationParams(
        HexUtil.encodeHexString(poolRegistration.getVrfKeyHash()),
        parsePoolRewardAccount(network, poolRegistration),
        poolRegistration.getPledge().toString(),
        poolRegistration.getCost().toString(),
        parsePoolOwners(network, poolRegistration),
        parsePoolRelays(poolRegistration),
        parsePoolMargin(poolRegistration), null,
        parsePoolMetadata(poolRegistration)
    );
  }

  public static List<Operation> parseWithdrawalsToOperations(List<Operation> withdrawalOps,
      Integer withdrawalsCount, Integer network) {

    log.info("[parseWithdrawalsToOperations] About to parse {} withdrawals", withdrawalsCount);
    List<Operation> withdrawalOperations = new ArrayList<>();
    for (int i = 0; i < withdrawalsCount; i++) {
      Operation withdrawalOperation = withdrawalOps.get(i);
      HdPublicKey hdPublicKey = new HdPublicKey();
      hdPublicKey.setKeyData(HexUtil.decodeHexString(
          withdrawalOperation.getMetadata().getStakingCredential().getHexBytes()));
      String address = CardanoAddressUtils.generateRewardAddress(
          Objects.requireNonNull(NetworkIdentifierType.find(network)), hdPublicKey);
      Operation parsedOperation = parseWithdrawalToOperation(
          withdrawalOperation.getAmount().getValue(),
          withdrawalOperation.getMetadata().getStakingCredential().getHexBytes(),
          withdrawalOperation.getOperationIdentifier().getIndex(),
          address
      );
      withdrawalOperations.add(parsedOperation);
    }
    return withdrawalOperations;
  }

  public static Operation parseWithdrawalToOperation(String value, String hex, Long index,
      String address) {
    return Operation.builder()
        .operationIdentifier(new OperationIdentifier(index, null))
        .type(OperationType.WITHDRAWAL.getValue())
        .status("")
        .account(new AccountIdentifier(address, null, null))
        .amount(Amount.builder().value(value)
            .currency(new Currency(Constants.ADA, Constants.ADA_DECIMALS, null))
            .build())
        .metadata(OperationMetadata.builder()
            .stakingCredential(new PublicKey(hex, CurveType.EDWARDS25519))
            .build())
        .build();
  }

  public static Operation parseVoteMetadataToOperation(Long index, String transactionMetadataHex)
      throws CborDeserializationException {
    log.info("[parseVoteMetadataToOperation] About to parse a vote registration operation");
    if (ObjectUtils.isEmpty(transactionMetadataHex)) {
      log.error("[parseVoteMetadataToOperation] Missing vote registration metadata");
      throw ExceptionFactory.missingVoteRegistrationMetadata();
    }
    Array array = (Array) com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(
        HexUtil.decodeHexString(transactionMetadataHex));
    AuxiliaryData transactionMetadata = AuxiliaryData.deserialize(
        (co.nstant.in.cbor.model.Map) array.getDataItems().get(0));
    CBORMetadata metadata = (CBORMetadata) transactionMetadata.getMetadata();
    CBORMetadataMap data = (CBORMetadataMap) metadata.get(
        valueOf(Long.parseLong(CatalystLabels.DATA.getLabel())));
    CBORMetadataMap sig = (CBORMetadataMap) metadata.get(
        valueOf(Long.parseLong(CatalystLabels.SIG.getLabel())));
    if (ObjectUtils.isEmpty(data)) {
      throw ExceptionFactory.missingVoteRegistrationMetadata();
    }
    if (ObjectUtils.isEmpty(sig)) {
      throw ExceptionFactory.invalidVotingSignature();
    }
    byte[] rewardAddressP = (byte[]) data.get(
        valueOf(CatalystDataIndexes.REWARD_ADDRESS.getValue()));
//need to revise
    Address rewardAddress = CardanoAddressUtils.getAddressFromHexString(
        Formatters.remove0xPrefix(HexUtil.encodeHexString(rewardAddressP))
    );
    if (rewardAddress.getAddress() == null) {
      throw ExceptionFactory.invalidAddressError();
    }
    BigInteger votingNonce = (BigInteger) data.get(
        valueOf(CatalystDataIndexes.VOTING_NONCE.getValue()));
    VoteRegistrationMetadata parsedMetadata = new VoteRegistrationMetadata(
        new PublicKey(Formatters.remove0xPrefix(HexUtil.encodeHexString(
            (byte[]) data.get(valueOf(CatalystDataIndexes.STAKE_KEY.getValue())))),
            CurveType.EDWARDS25519),
        new PublicKey(Formatters.remove0xPrefix(HexUtil.encodeHexString(
            (byte[]) data.get(valueOf(CatalystDataIndexes.VOTING_KEY.getValue())))),
            CurveType.EDWARDS25519),
        rewardAddress.toBech32(), votingNonce.intValue(),
        Formatters.remove0xPrefix(HexUtil.encodeHexString((byte[]) sig.get(valueOf(
            CatalystSigIndexes.VOTING_SIGNATURE.getValue())))
        ));
    return Operation.builder()
        .operationIdentifier(new OperationIdentifier(index, null))
        .type(OperationType.VOTE_REGISTRATION.getValue())
        .status("")
        .metadata(OperationMetadata.builder()
            .voteRegistrationMetadata(parsedMetadata)
            .build())
        .build();
  }

  public static List<String> parsePoolOwners(Integer network, PoolRegistration poolRegistration) {
    List<String> poolOwners = new ArrayList<>();
    Set<String> owners = poolRegistration.getPoolOwners();
    int ownersCount = owners.size();
    for (int i = 0; i < ownersCount; i++) {
      if (network == NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getValue()) {
        Address address = CardanoAddressUtils.getAddress(null,
            HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), (byte) -32,
            Networks.testnet(), com.bloxbean.cardano.client.address.AddressType.Reward);
        poolOwners.add(address.getAddress());
      }
      if (network == NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getValue()) {
        Address address = CardanoAddressUtils.getAddress(null,
            HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), (byte) -32,
            Networks.preprod(), com.bloxbean.cardano.client.address.AddressType.Reward);
        poolOwners.add(address.getAddress());
      }
      if (network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()) {
        Address address = CardanoAddressUtils.getAddress(null,
            HexUtil.decodeHexString(new ArrayList<>(owners).get(i)), (byte) -32,
            Networks.mainnet(), com.bloxbean.cardano.client.address.AddressType.Reward);
        poolOwners.add(address.getAddress());
      }
    }
    return poolOwners;
  }

  public static String parsePoolRewardAccount(Integer network, PoolRegistration poolRegistration) {
    String cutRewardAccount = poolRegistration.getRewardAccount();
    if (poolRegistration.getRewardAccount().length() == 58) {
      cutRewardAccount = poolRegistration.getRewardAccount().substring(2);
    }
    if (network == NetworkIdentifierType.CARDANO_TESTNET_NETWORK.getValue()) {
      return CardanoAddressUtils.getAddress(null,
              HexUtil.decodeHexString(cutRewardAccount),
              (byte) -32,
              Networks.testnet(), com.bloxbean.cardano.client.address.AddressType.Reward)
          .getAddress();
    }
    if (network == NetworkIdentifierType.CARDANO_PREPROD_NETWORK.getValue()) {
      return CardanoAddressUtils.getAddress(null,
              HexUtil.decodeHexString(cutRewardAccount),
              (byte) -32,
              Networks.preprod(), com.bloxbean.cardano.client.address.AddressType.Reward)
          .getAddress();
    }
    if (network == NetworkIdentifierType.CARDANO_MAINNET_NETWORK.getValue()) {
      return CardanoAddressUtils.getAddress(null,
              HexUtil.decodeHexString(cutRewardAccount),
              (byte) -32,
              Networks.mainnet(), com.bloxbean.cardano.client.address.AddressType.Reward)
          .getAddress();
    }

    return null;
  }

  public static PoolMetadata parsePoolMetadata(PoolRegistration poolRegistration) {
    if (poolRegistration.getPoolMetadataUrl() != null
        || poolRegistration.getPoolMetadataHash() != null) {
      return new PoolMetadata(poolRegistration.getPoolMetadataHash(),
          poolRegistration.getPoolMetadataUrl());
    }
    return null;
  }

  public static PoolMargin parsePoolMargin(PoolRegistration poolRegistration) {
    return new PoolMargin(poolRegistration.getMargin().getDenominator().toString(),
        poolRegistration.getMargin().getNumerator().toString());
  }

  public static List<Relay> parsePoolRelays(PoolRegistration poolRegistration) {
    List<Relay> poolRelays = new ArrayList<>();
    List<com.bloxbean.cardano.client.transaction.spec.cert.Relay> relays = poolRegistration.getRelays();
    for (com.bloxbean.cardano.client.transaction.spec.cert.Relay relay : relays) {
      MultiHostName multiHostRelay = getMultiHostRelay(relay);
      SingleHostName singleHostName = getSingleHostName(relay);
      SingleHostAddr singleHostAddr = getSingleHostAddr(relay);
      if (multiHostRelay!=null || singleHostName!=null) {
        addRelayToPoolReLayOfTypeMultiHostOrSingleHostName(poolRelays, multiHostRelay,
            singleHostName);
        continue;
      }
      if (singleHostAddr!=null) {
        addRelayToPoolReLayOfTypeSingleHostAddr(poolRelays, singleHostAddr);
      }
    }
    return poolRelays;
  }

  public static void addRelayToPoolReLayOfTypeSingleHostAddr(List<Relay> poolRelays,
      SingleHostAddr singleHostAddr) {
    Relay relay1 = new Relay(RelayType.SINGLE_HOST_ADDR.getValue(),
        singleHostAddr.getIpv4().getHostAddress(), singleHostAddr.getIpv6().getHostAddress(),
        null, String.valueOf(singleHostAddr.getPort()));
    poolRelays.add(relay1);
  }

  public static MultiHostName getMultiHostRelay(
      com.bloxbean.cardano.client.transaction.spec.cert.Relay relay) {
    if (relay instanceof MultiHostName multiHostName) {
      return multiHostName;
    }
    log.info("not a MultiHostName");
    return null;
  }

  public static SingleHostName getSingleHostName(
      com.bloxbean.cardano.client.transaction.spec.cert.Relay relay) {
    if (relay instanceof SingleHostName singleHostName) {
      return singleHostName;
    }
    log.info("not a SingleHostName");
    return null;
  }

  public static SingleHostAddr getSingleHostAddr(
      com.bloxbean.cardano.client.transaction.spec.cert.Relay relay) {
    if (relay instanceof SingleHostAddr singleHostAddr) {
      return singleHostAddr;
    }
    log.info("not a SingleHostAddr");
    return null;
  }

  public static void addRelayToPoolReLayOfTypeMultiHostOrSingleHostName(List<Relay> poolRelays,
      MultiHostName multiHostRelay,
      SingleHostName singleHostName) {
    if (!ObjectUtils.isEmpty(multiHostRelay)) {
      poolRelays.add(
          Relay.builder()
              .type(RelayType.MULTI_HOST_NAME.getValue())
              .dnsName(multiHostRelay.getDnsName())
              .build());
    }
    if (!ObjectUtils.isEmpty(singleHostName)) {
      poolRelays.add(
          Relay.builder()
              .type(RelayType.SINGLE_HOST_NAME.getValue())
              .dnsName(singleHostName.getDnsName())
              .port(singleHostName.getPort())
              .build());
    }
  }

  public static int getCertSize(List<Certificate> certs) {
    return ObjectUtils.isEmpty(certs) ? 0 : certs.size();
  }

  public static boolean checkStakeCredential(Operation certOperation) {
    return certOperation.getMetadata() != null
        && certOperation.getMetadata().getStakingCredential() != null;
  }

  public static Operation parseCertToOperation(Certificate cert, Long index, String hash,
      String type,
      String address) {
    Operation operation = new Operation(new OperationIdentifier(index, null), null, type, "",
        new AccountIdentifier(address, null, null), null, null,
        OperationMetadata.builder().stakingCredential(new PublicKey(hash, CurveType.EDWARDS25519)).build());
    StakeDelegation delegationCert = null;
    try {
      delegationCert = (StakeDelegation) cert;
    } catch (Exception e) {
      log.info("not a StakeDelegation");
    }
    if (!ObjectUtils.isEmpty(delegationCert)) {
      operation.getMetadata().setPoolKeyHash(
          HexUtil.encodeHexString(delegationCert.getStakePoolId().getPoolKeyHash()));
    }
    return operation;
  }

}
