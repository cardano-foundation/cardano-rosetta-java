package org.cardanofoundation.rosetta.yaciindexer.txSizeStore;

public class TransactionBuildingConstants {

  public static final long ALONZO_START_BLOCKNUMBER = 64902L;
  public static final int TX_BODY_INDEX = 0;
  public static final int WITNESS_SET_INDEX = 1;
  public static final int SUCCESS_INDICATOR_INDEX = 2;
  public static final int AUXILIARY_DATA_INDEX = 3;
  public static final int VKEY_WITNESS_INDEX = 0;
  public static final int NATIVESCRIPT_WITNESS_INDEX = 1;
  public static final int BOOTSTRAP_WITNESS_INDEX = 2;
  public static final int REDEEMER_WITNESS_INDEX = 5;
  public static final int PLUTUSV1_WITNESS_INDEX = 3;
  public static final int PLUTUSV2_WITNESS_INDEX = 6;
  public static final int PLUTUS_DATUM_WITNESS_INDEX = 4;
}
