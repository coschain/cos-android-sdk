package io.contentos.android.sdk.crypto;

import java.math.BigInteger;
import java.util.Arrays;

public class Helper {

    public static byte[] uintBytes(BigInteger n, int bytesLen) {
        byte[] bytes = n.toByteArray();
        if (bytes.length > bytesLen) {

            //
            // the integer is too long, cut leading bytes (most significant bytes)
            //
            // BigInteger.toByteArray() always outputs sign bit, so that it possibly returns a
            // 33-byte-long byte array even if n is smaller than 2^256.
            // e.g. n = 2^256 - 1, n.bitLength()==257 (256 value bits + 1 sign bit).
            // n.toByteArray() is 33-byte-long coz 32 bytes is smaller than 257 bits.
            //
            bytes = Arrays.copyOfRange(bytes, bytes.length - bytesLen, bytes.length);

        } else if (bytes.length < bytesLen) {

            // the integer is short, add leading 0x00-bytes
            byte[] b = bytes;
            bytes = new byte[bytesLen];
            System.arraycopy(b, 0, bytes, bytesLen - b.length, b.length);
        }
        return bytes;
    }
}
