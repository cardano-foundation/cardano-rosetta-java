package org.cardanofoundation.rosetta.crawler.construction.data;

import org.cardanofoundation.rosetta.crawler.construction.data.type.OperationType;
import org.cardanofoundation.rosetta.crawler.model.CurveType;

import java.util.ArrayList;
import java.util.List;

public class Const {
    List arrlist = new ArrayList();
    public static final String MAINNET = "mainnet";
    public static final String VALID_CURVE_TYPE = CurveType.EDWARDS25519.getValue();
    public static final int PUBLIC_KEY_BYTES_LENGTH = 64;
    public static final Double DEFAULT_RELATIVE_TTL = 1000.0;
    public static final Integer POLICY_ID_LENGTH = 56;

    public static final Integer ASSET_NAME_LENGTH = 64;

    public static final String EMPTY_HEX = "\\x";

    public static final String ADA = "ADA";

    public static final Integer ADA_DECIMALS = 6;

    public static final String IS_POSITIVE_NUMBER = "^\\+?\\d+";

    public static final String Token_Name_Validation = "^[0-9a-fA-F]{0," + ASSET_NAME_LENGTH + "}$";

    public static final String PolicyId_Validation = "^[0-9a-fA-F]{" + POLICY_ID_LENGTH + "}$";

    public static final Integer SIGNATURE_LENGTH = 128;
    public static final Integer CHAIN_CODE_LENGTH = 64;

    // Shelley
    public static final Long DEFAULT_POOL_DEPOSIT = 500000000l;

    public static final Long DEFAULT_KEY_DEPOSIT = 2000000l;

    public static final String SIGNATURE_TYPE = "ed25519";

    public static final String CARDANO="cardano";

    public static final Integer PREPROD_NETWORK_MAGIC = 1;
    public static final Integer PREVIEW_NETWORK_MAGIC = 2;

    public static final String PREPROD = "preprod";
    public static final String PREVIEW = "preview";

    public static final String COIN_SPENT_ACTION = "coin_spent";
    public static final String SHELLEY_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH]).replace("\0", "0");
    public static final String SHELLEY_DUMMY_PUBKEY = new String(new char[PUBLIC_KEY_BYTES_LENGTH]).replace("\0", "0");

    // Byron
    public static final String BYRON_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH]).replace("\0", "0");
    public static final String BYRON_DUMMY_PUBKEY = new String(new char[PUBLIC_KEY_BYTES_LENGTH]).replace("\0", "0");

    // Cold keys
    public static final String COLD_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH]).replace("\0", "0");
    public static final String COLD_DUMMY_PUBKEY = new String(new char[PUBLIC_KEY_BYTES_LENGTH]).replace("\0", "0");

    public static final String CHAIN_CODE_DUMMY = new String(new char[CHAIN_CODE_LENGTH]).replace("\0", "0");

    public static final ArrayList<String> StakingOperations = new ArrayList<>(List.of(OperationType.STAKE_DELEGATION.getValue(),
            OperationType.STAKE_KEY_REGISTRATION.getValue(),
            OperationType.STAKE_KEY_DEREGISTRATION.getValue(),
            OperationType.WITHDRAWAL.getValue()));
    public static final ArrayList<String> PoolOperations = new ArrayList<>(List.of(OperationType.POOL_RETIREMENT.getValue(),
            OperationType.POOL_REGISTRATION.getValue(),
            OperationType.POOL_REGISTRATION_WITH_CERT.getValue()));
    public static final ArrayList<String> StakePoolOperations = new ArrayList<>(List.of(OperationType.STAKE_DELEGATION.getValue(),
            OperationType.STAKE_KEY_REGISTRATION.getValue(),
            OperationType.STAKE_KEY_DEREGISTRATION.getValue(),
            OperationType.POOL_RETIREMENT.getValue(),
            OperationType.POOL_REGISTRATION.getValue(),
            OperationType.POOL_REGISTRATION_WITH_CERT.getValue()));
    public static final ArrayList<String> VoteOperations = new ArrayList<>(List.of(OperationType.VOTE_REGISTRATION.getValue()));

}
