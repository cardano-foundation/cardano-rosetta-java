package org.cardanofoundation.rosetta.api.addedconsotant;

import org.cardanofoundation.rosetta.api.model.CurveType;

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

    public static final String IS_POSITIVE_NUMBER = "/^\\+?\\d+/";

    public static final String Token_Name_Validation = "^[0-9a-fA-F]{0," + ASSET_NAME_LENGTH + "}$";

    public static final String PolicyId_Validation = "^[0-9a-fA-F]{" + POLICY_ID_LENGTH + "}$";

    public static final Integer SIGNATURE_LENGTH = 128;
    public static final Integer CHAIN_CODE_LENGTH = 64;

    // Shelley
    public static final String DEFAULT_POOL_DEPOSIT="500000000";

    public static final String DEFAULT_KEY_DEPOSIT="2000000";
    public static final String SHELLEY_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH + 1]).replace("\0", "0");
    public static final String SHELLEY_DUMMY_PUBKEY = new String(new char[PUBLIC_KEY_BYTES_LENGTH + 1]).replace("\0", "0");

    // Byron
    public static final String BYRON_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH + 1]).replace("\0", "0");
    public static final String BYRON_DUMMY_PUBKEY = new String(new char[PUBLIC_KEY_BYTES_LENGTH + 1]).replace("\0", "0");

    // Cold keys
    public static final String COLD_DUMMY_SIGNATURE = new String(new char[SIGNATURE_LENGTH + 1]).replace("\0", "0");
    public static final String COLD_DUMMY_PUBKEY = new String(new char[PUBLIC_KEY_BYTES_LENGTH + 1]).replace("\0", "0");

    public static final String CHAIN_CODE_DUMMY= new String(new char[CHAIN_CODE_LENGTH + 1]).replace("\0", "0");

}
