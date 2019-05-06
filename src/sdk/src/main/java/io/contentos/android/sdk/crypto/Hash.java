package io.contentos.android.sdk.crypto;

import java.security.MessageDigest;

public class Hash {

    private static byte[] hash(String algorithm, byte[] data, int offset, int size) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data, offset, size);
            return md.digest();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] sha256(byte[] data, int offset, int size) {
        return hash("SHA-256", data, offset, size);
    }

    public static byte[] sha256(byte[] data) {
        return hash("SHA-256", data, 0, data.length);
    }
}
