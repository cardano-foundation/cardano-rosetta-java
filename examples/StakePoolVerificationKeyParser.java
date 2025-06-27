//DEPS com.bloxbean.cardano:cardano-client-lib:0.6.3
//JAVA 24

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;
import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static com.bloxbean.cardano.client.crypto.Bech32.encode;
import static com.bloxbean.cardano.client.crypto.Bech32.decode;
import static com.bloxbean.cardano.client.crypto.Blake2bUtil.blake2bHash224;

public class StakePoolVerificationKeyParser {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Bech32Pool usage, jbang Bech32Pool.java 582060afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b");
        }
        var poolVerificationKey = args[0];
        var poolVerifcationKeyStripped = poolVerificationKey.substring(4);

        System.out.println("pool_verification_key: " + poolVerificationKey);
        System.out.println("pool_verification_key_stripped: " + poolVerifcationKeyStripped);

        System.out.println("pool_verification_key_length: " + poolVerificationKey.length());
        System.out.println("pool_verification_key_stripped_length: " + poolVerifcationKeyStripped.length());

        var poolVerificationKeyBlake224Hash = blake2bHash224(decodeHexString(poolVerificationKey));
        var poolVerificationKeyBlake224HashStripped = blake2bHash224(decodeHexString(poolVerifcationKeyStripped));

        var poolVerificationKeyBech32 = encode(poolVerificationKeyBlake224HashStripped, "pool");

        System.out.println("pool_hash: " + encodeHexString(poolVerificationKeyBlake224HashStripped));
        System.out.println("pool_hash_as_bech32: " + poolVerificationKeyBech32);

        var decodedBech32 = decode(poolVerificationKeyBech32);
        System.out.println("decoded_bech32_hrp: " + decodedBech32.hrp);
        System.out.println("decoded_bech32_data: " + encodeHexString(decodedBech32.data));
    }

}
