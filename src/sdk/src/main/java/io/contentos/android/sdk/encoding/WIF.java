package io.contentos.android.sdk.encoding;

import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.UnknownFormatConversionException;
import io.contentos.android.sdk.crypto.Hash;
import io.contentos.android.sdk.prototype.Type;

public class WIF {
    private static final String PUBLIC_KEY_PREFIX = "COS";

    public static String fromPublicKey(Type.public_key_type publicKey) {
        return PUBLIC_KEY_PREFIX + fromByteArray(publicKey.getData().toByteArray(), false);
    }

    public static String fromPrivateKey(Type.private_key_type privateKey) {
        return fromByteArray(privateKey.getData().toByteArray(), true);
    }

    public static Type.public_key_type toPublicKey(String wif) {
        if (!wif.startsWith(PUBLIC_KEY_PREFIX)) {
            throw new UnknownFormatConversionException("WIF string without public key prefix");
        }
        return Type.public_key_type.newBuilder()
                .setData(ByteString.copyFrom(
                        toByteArray(wif.substring(PUBLIC_KEY_PREFIX.length()), false))
                ).build();
    }

    public static Type.private_key_type toPrivateKey(String wif) {
        return Type.private_key_type.newBuilder()
                .setData(ByteString.copyFrom(toByteArray(wif, true)))
                .build();
    }

    private static String fromByteArray(byte[] data, Boolean addLeadingOne) {
        byte[] h = hash(data);
        byte[] dataWithHash = new byte[data.length + (addLeadingOne? 5:4)];
        int offset = 0;
        if (addLeadingOne) {
            dataWithHash[0] = 1;
            offset++;
        }
        System.arraycopy(data, 0, dataWithHash, offset, data.length);
        System.arraycopy(h, 0, dataWithHash, offset + data.length, 4);
        return Base58.encode(dataWithHash);
    }

    private static byte[] toByteArray(String wif, Boolean removeLeadingOne) {
        byte[] dataWithHash = Base58.decode(wif);
        int offset = removeLeadingOne? 1:0;
        int minSize = removeLeadingOne? 5:4;
        if (dataWithHash.length < minSize) {
            throw new UnknownFormatConversionException("WIF string too short");
        }
        if (removeLeadingOne && dataWithHash[0] != 1) {
            throw new UnknownFormatConversionException("invalid WIF string");
        }
        int hashOffset = dataWithHash.length - 4;
        byte[] h = hash(dataWithHash, offset, hashOffset - offset);
        for (int i = 0; i < 4; i++) {
            if (h[i] != dataWithHash[hashOffset + i]) {
                throw new UnknownFormatConversionException("WIF string checksum mismatched");
            }
        }
        return Arrays.copyOfRange(dataWithHash, offset, hashOffset);
    }

    private static byte[] hash(byte[] data, int offset, int size) {
        return Hash.sha256(Hash.sha256(data, offset, size));
    }

    private static byte[] hash(byte[] data) {
        return Hash.sha256(Hash.sha256(data));
    }
}
