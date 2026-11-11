package org.cardanofoundation.rosetta.testgenerator.common;

public class TestConstants {

  public static final String FILE_SAVE_PATH = "./testData/testdata.json";
  public static final String TEST_ACCOUNT_ADDRESS = "addr_test1qz5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxvw2pfap6uqmkj3n6zmlrsgz397md2gt7yqs5p255uygaesx608y5";
  public static final String ACCOUNT_BALANCE_LOVELACE_AMOUNT = "2000000";
  public static final String ACCOUNT_BALANCE_LOVELACE_SMALL_AMOUNT = "1";
  public static final String ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT = "1000";
  public static final String STAKE_ACCOUNT_BALANCE_AMOUNT = "996685608";
  public static final String URL = "http://localhost:";
  public static final String TEST_BLOCKCHAIN = "cardano";
  public static final String TEST_NETWORK = "devkit";
  public static final String MY_ASSET_SYMBOL = "\\x";
  public static final String MY_ASSET_NAME = "MyAsset";
  public static final String CURRENCY_HEX_SYMBOL = "1A2B3C4D5E6F";

  // Account addresses
  public static final String SENDER_1_MNEMONIC = "clog book honey force cricket stamp until seed minimum margin denial kind volume undo simple federal then jealous solid legal crucial crazy acoustic thank";
  public static final String SENDER_2_MNEMONIC = "drive useless envelope shine range ability time copper alarm museum near flee wrist live type device meadow allow churn purity wisdom praise drop code";
  public static final String SENDER_2_ADDRESS = "addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah";
  public static final String RECEIVER_1 = "addr_test1qz3s0c370u8zzqn302nppuxl840gm6qdmjwqnxmqxme657ze964mar2m3r5jjv4qrsf62yduqns0tsw0hvzwar07qasqeamp0c";
  public static final String RECEIVER_2 = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";
  public static final String RECEIVER_3 = "addr_test1qqqvjp4ffcdqg3fmx0k8rwamnn06wp8e575zcv8d0m3tjn2mmexsnkxp7az774522ce4h3qs4tjp9rxjjm46qf339d9sk33rqn";

  // Stake addresses
  public static final String STAKE_ADDRESS_WITH_NO_EARNED_REWARDS = "stake_test1uzz49ldva0xpzucv2q8rt0tk0r3ekyl3r8j9e3tf5zf834qet948h";
  public static final String STAKE_ADDRESS_WITH_EARNED_REWARDS = "stake_test1uqjg6peuwpjaexgdd2vy2h69zn6pjz33pdsvekjcq88uxmgc6qc7u";
  public static final String STAKE_ADDRESS_WITH_MINTED_TOKENS = "stake_test1uq54hxr3x4ssv9hnca8prj2dw7mva4wveya86uvulvf4qcsceluue";

  // CIP-68 tokens minted by Step4_Cip68Transactions. The policy is a single-signature native script on
  // SENDER_1's payment key, so the policy ID is the same on every regeneration of the fixture.
  // The tokens and both reference NFTs go to account CIP68_HOLDER_ACCOUNT (address 0) of SENDER_1_MNEMONIC
  // (CIP68_HOLDER_ADDRESS): a separate account, so its stake key differs from SENDER_1's and SENDER_1's
  // balances are untouched. It also pays for the mints and is topped up by the regenerate-testdata-db skill.
  public static final int CIP68_HOLDER_ACCOUNT = 1;
  public static final String CIP68_HOLDER_ADDRESS = "addr_test1qpx3zpsg0wl3hfmmsdeumustk5zm3j9a74n9upu46dxfvzf6sa4xylhy9l2p6lv9ezd5lvmfxunwrpz4284rz2hll89qlvteha";
  public static final String CIP68_FT_NAME = "RosettaFT";            // 333 fungible token
  public static final String CIP68_FT_TICKER = "RFT";
  public static final int CIP68_FT_DECIMALS = 6;
  public static final String CIP68_FT_DESCRIPTION = "Rosetta CIP-68 fungible test token";
  public static final String CIP68_FT_URL = "https://cardano-foundation.github.io/cardano-rosetta-java";
  public static final String CIP68_FT_LOGO = "https://cardano-foundation.github.io/cardano-rosetta-java/img/ft.png";
  public static final long CIP68_FT_QUANTITY = 1_000_000L;
  public static final String CIP68_RFT_NAME = "RosettaRFT";          // 444 rich fungible token
  public static final int CIP68_RFT_DECIMALS = 2;
  public static final String CIP68_RFT_DESCRIPTION = "Rosetta CIP-68 rich fungible test token";
  public static final String CIP68_RFT_IMAGE = "ipfs://QmRosettaRichFungibleTestTokenImage";
  public static final long CIP68_RFT_QUANTITY = 500L;

  private TestConstants() {
  }
}
