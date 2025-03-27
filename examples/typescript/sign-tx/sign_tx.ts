import { Bip32PrivateKey, FixedTransaction, Transaction } from "@emurgo/cardano-serialization-lib-nodejs";
import { mnemonicToEntropy } from "bip39";
import { Buffer } from "node:buffer";
import { decode } from "cbor";

// Setting constants 
const CBOR_ARRAY_PREFIX = "84";
const CBOR_ARRAY_SUFFIX = "a0f5f6";
const VALID_CBOR_MAP_PREFIXES = ["a4", "a5"];

// Configuration
const mnemonicPhrase = "key in your 24 words of your mnemonic here, words separated by spaces";
//const rosettaUnsignedTx = "a400818258202138e16c10911f1c0e264909b0c8e6af903e3370b2a8a75ebf5443081100ec71000182825839000743d16cfe3c4fcc0c11c2403bbc10dbc7ecdd4477e053481a368e7a06e2ae44dff6770dc0f4ada3cf4cf2605008e27aecdb332ad349fda71a7735940082581d60d6dbfa87f1054da9d47d612b82634eff44871c371554c8e6732d53f41b00000001dcd3c37f021a00028c81031a026c822a";
const rosettaUnsignedTx = "8278f26134303064393031303238313832353832306633383131613830386135326362393366616362303538393465363861323066333266333438663131366436626231306236613234616266306639306630373330313031383138323538333930303332656434626132643437393133393530653938346565326138313335653536323334333532326139346130636562383965363561663239396431343364646433613633383634303834346161646166303735383930303563616132336138316662336638616266363532346238613431613038313661343438303231613030303238366139303331613034383335366531a16a6f7065726174696f6e7381a6746f7065726174696f6e5f6964656e746966696572a265696e646578006d6e6574776f726b5f696e64657800676163636f756e74a16761646472657373786c616464725f74657374317170726668356a6c357a666b6c616b68636d65657a6b7376727830796c70366861613234706b356d7434746477326634386e6c7237646d70716767337666303632367134336366616c65703973386b6e6c30337772786e6c6c3664736c723777666a66616d6f756e74a26863757272656e6379a26673796d626f6c6341444168646563696d616c73066576616c75656a2d3133353836373132316b636f696e5f6368616e6765a26f636f696e5f6964656e746966696572a16a6964656e7469666965727842663338313161383038613532636239336661636230353839346536386132306633326633343866313136643662623130623661323461626630663930663037333a316b636f696e5f616374696f6e6a636f696e5f7370656e74667374617475736773756363657373647479706565696e707574";

// Function to get Rosetta Payload
function processRosettaPayload(unsignedTx: string): string | null {
  if (VALID_CBOR_MAP_PREFIXES.some(prefix => unsignedTx.startsWith(prefix))) {
    return `${CBOR_ARRAY_PREFIX}${unsignedTx}${CBOR_ARRAY_SUFFIX}`;
  }

  try {
    const bytes = Buffer.from(unsignedTx, "hex");
    const decodedData = decode(bytes) as (string | object)[];
    if (decodedData.length > 0 && typeof decodedData[0] === "string") {
      return `${CBOR_ARRAY_PREFIX}${decodedData[0]}${CBOR_ARRAY_SUFFIX}`;
    }
  } catch (error) {
    console.error("Invalid Rosetta transaction format:", error);
  }
  return null;
}

// Retrieve payment/stake key pair from mnemonic function
function generateKeysFromMnemonic(mnemonic: string) {
  const entropy = mnemonicToEntropy(mnemonic);
  const rootKey = Bip32PrivateKey.from_bip39_entropy(Buffer.from(entropy, "hex"), Buffer.from(""));
  const accountKey = rootKey.derive(harden(1852)).derive(harden(1815)).derive(harden(0));
  return {
    utxoPrivKey: accountKey.derive(0).derive(0),
    // stakePrivKey: accountKey.derive(2).derive(0), //In some operations you need stakeSkey as well
  };
}

// Sign transaction with payment vkey
function signTransaction(txBodyBytes: Buffer, privateKey: Bip32PrivateKey): string {
  const tx = Transaction.from_bytes(txBodyBytes);
  const txBody = tx.body();
  const transaction = FixedTransaction.new_from_body_bytes(txBody.to_bytes());
  transaction.sign_and_add_vkey_signature(privateKey.to_raw_key());
  return transaction.to_hex();
}

// Main function
function main() {
  const unsignedTxCborHex = processRosettaPayload(rosettaUnsignedTx);
  if (!unsignedTxCborHex) {
    console.error("Your Hex string is not in Rosetta-Java format.");
    return;
  }
  console.log("Unsigned Transaction:", unsignedTxCborHex);

  const { utxoPrivKey } = generateKeysFromMnemonic(mnemonicPhrase);
  const txBytes = Buffer.from(unsignedTxCborHex, "hex");
  const signedTxHex = signTransaction(txBytes, utxoPrivKey);
  // in case your TX needs more than 1 key to sign, add signatures by using transaction.sign_and_add_vkey_signature
  // eg: adding signature signed by stake key
  // transaction.sign_and_add_vkey_signature(stakePrivKey.to_raw_key());
  console.log("Signed Transaction:", signedTxHex);
}

function harden(num: number): number {
  return 0x80000000 + num;
}

main();