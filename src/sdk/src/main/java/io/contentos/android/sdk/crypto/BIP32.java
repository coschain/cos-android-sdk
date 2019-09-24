package io.contentos.android.sdk.crypto;

import com.google.protobuf.ByteString;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import io.contentos.android.sdk.prototype.Type;

public class BIP32 {

    static class ExtendedKey {
        private static final X9ECParameters ECParam = CustomNamedCurves.getByName("secp256k1");
        private static final int N_Bytes = (ECParam.getN().bitLength() + 7) / 8;

        final BigInteger key;
        final byte[] chainCode;
        final byte[] pubKey;

        private ExtendedKey(BigInteger k, byte[] c) {
            key = k;
            chainCode = c;
            pubKey = ECParam.getG().multiply(k).getEncoded(true);
        }

        ExtendedKey derive(int child) {
            byte[] data = new byte[37];
            ByteBuffer.wrap(data, 33, 4).putInt(child);
            if (child < 0) {
                System.arraycopy(Helper.uintBytes(key, N_Bytes), 0, data, 1, 32);
            } else {
                System.arraycopy(pubKey, 0, data, 0, 33);
            }
            return newKey(chainCode, data, key);
        }

        static boolean isInvalidPrivateKey(BigInteger k) {
            return k.compareTo(BigInteger.ZERO) <= 0 || k.compareTo(ECParam.getN()) >= 0;
        }

        static ExtendedKey newKey(byte[] key, byte[] data, BigInteger parent) {
            byte[] h = Hash.hmac_sha512(key, data);
            BigInteger k = new BigInteger(1, Arrays.copyOfRange(h, 0, 32));
            k = k.add(parent).mod(ECParam.getN());
            if (isInvalidPrivateKey(k)) {
                throw new IllegalArgumentException("cannot create key");
            }
            return new ExtendedKey(k, Arrays.copyOfRange(h, 32, 64));
        }

        static ExtendedKey master(byte[] seed) {
            return newKey("Bitcoin seed".getBytes(), seed, BigInteger.ZERO);
        }
    }

    public static Type.private_key_type cos_private_key(byte[] seed) {
        ExtendedKey key = ExtendedKey.master(seed)
                .derive(0x8000002c)
                .derive(0x80000c05)
                .derive(0x80000000)
                .derive(0)
                .derive(0);
        return Type.private_key_type.newBuilder().setData(
                ByteString.copyFrom(Helper.uintBytes(key.key, ExtendedKey.N_Bytes))
        ).build();
    }
}
