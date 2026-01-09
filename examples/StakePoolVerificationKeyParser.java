//DEPS com.bloxbean.cardano:cardano-client-lib:0.6.3
//JAVA 21

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;
import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static com.bloxbean.cardano.client.crypto.Bech32.encode;
import static com.bloxbean.cardano.client.crypto.Bech32.decode;
import static com.bloxbean.cardano.client.crypto.Blake2bUtil.blake2bHash224;

/**
 * Utility to convert a hex-encoded Stake Pool Verification Key (potentially CBOR-prefixed)
 * into a Cardano Pool Hash (Blake2b-224 hash) and its Bech32 representation ("pool" HRP).
 * * Usage: jbang StakePoolVerificationKeyParser.java <HEX_POOL_VERIFICATION_KEY>
 */
public class StakePoolVerificationKeyParser {

    private static final int CBOR_PREFIX_LENGTH = 4;
    private static final int STANDARD_KEY_LENGTH = 64; // Expected length for a standard hex key

    public static void main(String[] args) {
        if (args.length != 1) {
            // Throwing an exception is cleaner than printing to System.err and returning
            throw new IllegalArgumentException(
                "Usage: jbang StakePoolVerificationKeyParser.java <HEX_POOL_VERIFICATION_KEY>\n" +
                "Example: jbang StakePoolVerificationKeyParser.java 582060afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b"
            );
        }

        final var poolVerificationKey = args[0];
        
        if (poolVerificationKey.length() < STANDARD_KEY_LENGTH) {
             throw new IllegalArgumentException(
                "Invalid key length: Expected at least 64 hex characters (32 bytes), but got " + poolVerificationKey.length()
            );
        }

        // The key is assumed to be an extended format (e.g., CBOR-prefixed `5820...`), 
        // requiring the first 4 hex characters (2 bytes) to be stripped before hashing.
        final var poolVerifcationKeyStripped = poolVerificationKey.substring(CBOR_PREFIX_LENGTH);

        System.out.println("Input_Pool_Verification_Key: " + poolVerificationKey);
        System.out.println("Input_Key_Length: " + poolVerificationKey.length());

        // Log the stripped key only if it was actually stripped
        if (poolVerificationKey.length() != poolVerifcationKeyStripped.length()) {
            System.out.println("Stripped_Key: " + poolVerifcationKeyStripped);
            System.out.println("Stripped_Key_Length: " + poolVerifcationKeyStripped.length());
        }

        // Decode the stripped hex string to bytes and compute the Blake2b-224 hash.
        // This 28-byte hash is the canonical Pool Hash.
        final var keyBytesStripped = decodeHexString(poolVerifcationKeyStripped);
        final var poolHashBytes = blake2bHash224(keyBytesStripped);
        
        final var poolHashHex = encodeHexString(poolHashBytes);
        
        // Encode the pool hash bytes using the "pool" Human-Readable Part (HRP)
        final var poolHashBech32 = encode(poolHashBytes, "pool");

        System.out.println("--- Results ---");
        System.out.println("Pool_Hash (Hex): " + poolHashHex);
        System.out.println("Pool_Hash (Bech32): " + poolHashBech32);

        // Optional: Decode the Bech32 string to verify
        final var decodedBech32 = decode(poolHashBech32);
        System.out.println("Decoded_Bech32_HRP: " + decodedBech32.hrp);
        System.out.println("Decoded_Bech32_Data (Hex): " + encodeHexString(decodedBech32.data));
    }
}
