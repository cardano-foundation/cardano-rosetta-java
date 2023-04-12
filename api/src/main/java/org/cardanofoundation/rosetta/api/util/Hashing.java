//package org.cardanofoundation.rosetta.api.util;
//
//import org.bouncycastle.crypto.digests.Blake2bDigest;
//import org.bouncycastle.util.encoders.Hex;
//
//public class Hashing {
//    public static final int BLAKE2B_224_DIGEST_BYTES_LENGTH = 28;
//    public static final int BLAKE2B_256_DIGEST_BYTES_LENGTH = 32;
//
//    private static byte[] blake2bDigest(final byte[] input, final int digestBytes) {
//        final Blake2bDigest b2b = new Blake2bDigest(digestBytes * 8);
//        b2b.update(input, 0, input.length);
//        final int digestSize = b2b.getDigestSize();
//        final byte[] digestRaw = new byte[digestSize];
//        b2b.doFinal(digestRaw, 0);
//        return digestRaw;
//    }
//
//    public static byte[] blake2b256Digest(final byte[] input) {
//        return blake2bDigest(input, BLAKE2B_256_DIGEST_BYTES_LENGTH);
//    }
//
//    public static byte[] blake2b224Digest(final byte[] input) {
//        return blake2bDigest(input, BLAKE2B_224_DIGEST_BYTES_LENGTH);
//    }
//
//    public static String blake2b224Hex(final byte[] input) {
//        return Hex.toHexString(blake2b224Digest(input));
//    }
//
//    public static String blake2b256Hex(final byte[] input) {
//        return Hex.toHexString(blake2b256Digest(input));
//    }
//}
