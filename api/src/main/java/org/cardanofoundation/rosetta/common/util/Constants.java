package org.cardanofoundation.rosetta.common.util;

import java.util.List;

import org.openapitools.client.model.CurveType;

import org.cardanofoundation.rosetta.common.enumeration.OperationType;

public class Constants {

  public static final String SINGLE_HOST_ADDR = "single_host_addr";
  public static final String SINGLE_HOST_NAME = "single_host_name";
  public static final String MULTI_HOST_NAME = "multi_host_name";
  public static final String RELATIVE_TTL = "relative_ttl";
  public static final String TRANSACTION_SIZE = "transaction_size";
  public static final int HEX_PREFIX_AND_REWARD_ACCOUNT_LENGTH = 58;
  public static final String SUBMIT_API_PATH = "/api/submit/tx";
  public static final int SUCCESS_SUBMIT_TX_HTTP_CODE = 202;
  public static final String CBOR_CONTENT_TYPE = "application/cbor";
  public static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
  public static final int TX_HASH_LENGTH = 64;
  public static final String PROTOCOL = "http://";
  public static final String NETWORK_MAGIC_NAME = "networkMagic";
  public static final String KEY_REFUNDS_SUM = "keyRefundsSum";
  public static final String KEY_DEPOSITS_SUM = "keyDepositsSum";
  public static final String POOL_DEPOSITS_SUM = "poolDepositsSum";
  public static final String CARDANO_BLOCKCHAIN = "cardano";
  public static final int MAINNET_NETWORK_MAGIC = 764824073;
  public static final int PREPROD_NETWORK_MAGIC = 1;
  public static final int PREVIEW_NETWORK_MAGIC = 2;
  public static final int SANCHONET_NETWORK_MAGIC = 4;
  public static final int DEVKIT_NETWORK_MAGIC = 42;

  public static final int OPERATION_TYPE_VOTE_DELEGATION = 43;

  private Constants() {
  }

  public static final String CARDANO = "cardano";
  public static final byte STAKE_KEY_HASH_HEADER_KIND = (byte) -32;

  public static final int PREFIX_LENGTH = 10;

  public static final String ADA = "ADA";
  public static final int ASSET_NAME_LENGTH = 64;
  public static final int POLICY_ID_LENGTH = 56;
  public static final int ADA_DECIMALS = 6;
  public static final String ADA_DECIMALS_STRING = "6";
  public static final int CHAIN_CODE_LENGTH = 64;
  public static final int MULTI_ASSET_DECIMALS = 0;

  public static final String MAINNET = "mainnet";
  public static final String VALID_CURVE_TYPE = CurveType.EDWARDS25519.getValue();
  public static final int PUBLIC_KEY_BYTES_LENGTH = 64;
  public static final Integer DEFAULT_RELATIVE_TTL = 1000;

  public static final String EMPTY_HEX = "\\x";

  public static final String IS_POSITIVE_NUMBER = "^\\+?\\d+";

  public static final String TOKEN_NAME_VALIDATION = "^[0-9a-fA-F]{0," + ASSET_NAME_LENGTH + "}$";

  public static final String POLICY_ID_VALIDATION = "^[0-9a-fA-F]{" + POLICY_ID_LENGTH + "}$";

  public static final Integer SIGNATURE_LENGTH = 128;

  // Shelley
  public static final Long DEFAULT_POOL_DEPOSIT = 500000000L;

  public static final Long DEFAULT_KEY_DEPOSIT = 2000000L;

  public static final String PREPROD = "preprod";
  public static final String PREVIEW = "preview";

  public static final String COIN_SPENT_ACTION = "coin_spent";
  public static final String SHELLEY_DUMMY_SIGNATURE = new String(
      new char[SIGNATURE_LENGTH]).replace("\0", "0");
  public static final String SHELLEY_DUMMY_PUBKEY = new String(
      new char[PUBLIC_KEY_BYTES_LENGTH]).replace("\0", "0");

  // Byron
  public static final String BYRON_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH]).replace(
      "\0", "0");
  public static final String BYRON_DUMMY_PUBKEY = new String(
      new char[PUBLIC_KEY_BYTES_LENGTH]).replace("\0", "0");

  // Cold keys
  public static final String COLD_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH]).replace(
      "\0", "0");
  public static final String COLD_DUMMY_PUBKEY = new String(
      new char[PUBLIC_KEY_BYTES_LENGTH]).replace("\0", "0");

  public static final String CHAIN_CODE_DUMMY = new String(new char[CHAIN_CODE_LENGTH]).replace(
      "\0", "0");

  public static final List<String> STAKING_OPERATIONS =
      List.of(OperationType.STAKE_DELEGATION.getValue(),
          OperationType.STAKE_KEY_REGISTRATION.getValue(),
          OperationType.STAKE_KEY_DEREGISTRATION.getValue(),
          OperationType.WITHDRAWAL.getValue());
  public static final List<String> POOL_OPERATIONS =
      List.of(OperationType.POOL_RETIREMENT.getValue(),
          OperationType.POOL_REGISTRATION.getValue(),
          OperationType.POOL_REGISTRATION_WITH_CERT.getValue());
  public static final List<String> STAKE_POOL_OPERATIONS =
      List.of(OperationType.STAKE_DELEGATION.getValue(),
          OperationType.STAKE_KEY_REGISTRATION.getValue(),
          OperationType.STAKE_KEY_DEREGISTRATION.getValue(),
          OperationType.POOL_RETIREMENT.getValue(),
          OperationType.POOL_REGISTRATION.getValue(),
          OperationType.POOL_REGISTRATION_WITH_CERT.getValue());
  public static final List<String> VOTE_OPERATIONS =
      List.of(OperationType.VOTE_REGISTRATION.getValue());

  public static final String OPERATION_TYPE_INPUT = "input";
  public static final String OPERATION_TYPE_OUTPUT = "output";
  public static final String OPERATION_TYPE_STAKE_KEY_REGISTRATION = "stakeKeyRegistration";
  public static final String OPERATION_TYPE_STAKE_DELEGATION = "stakeDelegation";
  public static final String OPERATION_TYPE_WITHDRAWAL = "withdrawal";
  public static final String OPERATION_TYPE_STAKE_KEY_DEREGISTRATION = "stakeKeyDeregistration";
  public static final String OPERATION_TYPE_POOL_REGISTRATION = "poolRegistration";
  public static final String OPERATION_TYPE_POOL_REGISTRATION_WITH_CERT = "poolRegistrationWithCert";
  public static final String OPERATION_TYPE_POOL_RETIREMENT = "poolRetirement";
  public static final String OPERATION_TYPE_VOTE_REGISTRATION = "voteRegistration";

  // Plomin hard fork governance related
  public static final String OPERATION_TYPE_DREP_VOTE_DELEGATION = "dRepVoteDelegation";

  public static final Integer ED_25519_KEY_SIGNATURE_BYTE_LENGTH = 64;

  public static final String CERTIFICATE = "certificate";
  public static final String ADDRESS = "address";
  public static final String ADDRESS_PREFIX = "addr";

  public static final String POOL_KEY_HASH = "pool_key_hash";
  public static final String VOTING_KEY = "votingKey";
  public static final String STAKE_KEY = "stakeKey";
  public static final String REWARD_ADDRESS = "rewardAddress";
  public static final String VOTING_NONCE = "votingNonce";
  public static final String VOTING_SIGNATURE = "votingSignature";
  public static final String NETWORK_INDEX = "network_index";
  public static final String INDEX = "index";
  public static final String ACCOUNT = "account";
  public static final String AMOUNT = "amount";
  public static final String PLEDGE = "pledge";
  public static final String DECIMALS = "decimals";
  public static final String METADATA = "metadata";
  public static final String SUB_ACCOUNT = "sub_account";
  public static final String CHAIN_CODE = "chain_code";
  public static final String COIN_IDENTIFIER = "coin_identifier";
  public static final String COIN_ACTION = "coin_action";
  public static final String COIN_ACTION_CREATED = "coin_created";
  public static final String COIN_ACTION_SPENT = "coin_spent";
  public static final String OPERATION_IDENTIFIER = "operation_identifier";
  public static final String RELATED_OPERATION = "related_operations";
  public static final String STATUS = "status";
  public static final String COIN_CHANGE = "coin_change";
  public static final String INPUT = "input";
  public static final String OUTPUT = "output";
  public static final String SUCCESS = "success";
  public static final String STAKING_CREDENTIAL = "staking_credential";
  public static final String WITHDRAWALAMOUNT = "withdrawalAmount";
  public static final String DEPOSITAMOUNT = "depositAmount";
  public static final String REFUNDAMOUNT = "refundAmount";
  public static final String EPOCH = "epoch";
  public static final String POLICYID = "policyId";
  public static final String TOKENS = "tokens";
  public static final String TOKENBUNDLE = "tokenBundle";
  public static final String NUMERATOR = "numerator";
  public static final String DENOMINATOR = "denominator";
  public static final String POOLMETADATA = "poolMetadata";
  public static final String POOLREGISTRATIONPARAMS = "poolRegistrationParams";
  public static final String POOLREGISTRATIONCERT = "poolRegistrationCert";
  public static final String VALUE = "value";
  public static final String VRFKEYHASH = "vrfKeyHash";
  public static final String POOLOWNERS = "poolOwners";
  public static final String DNSNAME = "dnsName";
  public static final String RELAYS = "relays";
  public static final String MARGIN = "margin";
  public static final String MARGIN_PERCENTAGE = "margin_percentage";
  public static final String VOTEREGISTRATIONMETADATA = "voteRegistrationMetadata";
  public static final String OPERATIONS = "operations";
  public static final String TRANSACTIONMETADATAHEX = "transactionMetadataHex";
  public static final String HEX_BYTES = "hex_bytes";
  public static final String CURVE_TYPE = "curve_type";
  public static final String CURRENCY = "currency";
  public static final String SYMBOL = "symbol";
  public static final String TYPE = "type";
  public static final String IPV4 = "ipv4";
  public static final String IPV6 = "ipv6";
  public static final String URL = "url";
  public static final String HASH = "hash";
  public static final String PORT = "port";
  public static final String IDENTIFIER = "identifier";
  public static final String COST = "cost";
  public static final String EMPTY_SYMBOL = "0x";
  public static final String SYMBOL_REGEX = "\\x";


  public static final String LOVELACE = "lovelace";
  public static final String DEVKIT = "devkit";
  public static final String SANCHONET = "sanchonet";

  public static final String ROSETTA_API_PATH = "classpath:/rosetta-specifications-1.4.15/api.yaml";
}
