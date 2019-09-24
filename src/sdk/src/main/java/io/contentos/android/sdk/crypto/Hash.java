package io.contentos.android.sdk.crypto;

import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

    private static byte[] mac(byte[] key, byte[] data, String algorithm) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] hmac_sha512(byte[]key, byte[] data) {
        return mac(key, data, "HmacSHA512");
    }
}
