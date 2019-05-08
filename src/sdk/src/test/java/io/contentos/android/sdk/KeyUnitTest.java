package io.contentos.android.sdk;

import com.google.protobuf.ByteString;

import org.junit.Test;

import java.math.BigInteger;
import java.util.HashSet;

import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import io.contentos.android.sdk.prototype.Type;
import static io.contentos.android.sdk.KeyUnitTestData.*;
import static org.junit.Assert.*;


public class KeyUnitTest {
    static private final int DataItemCount = getTestDataItemCount();

    @Test
    public void wif_PrivateKey_isCorrect() {
        for (int i = 0; i < DataItemCount; i++) {
            Type.private_key_type key = Type.private_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PRIVATE_KEY))
            ).build();
            String wif = getTestDataString(i, Entry.PRIVATE_KEY_WIF);
            assertEquals(wif, WIF.fromPrivateKey(key));
            assertArrayEquals(key.getData().toByteArray(), WIF.toPrivateKey(wif).getData().toByteArray());
        }
    }

    @Test
    public void wif_PublicKey_isCorrect() {
        for (int i = 0; i < DataItemCount; i++) {
            Type.public_key_type key = Type.public_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PUBLIC_KEY))
            ).build();
            String wif = getTestDataString(i, Entry.PUBLIC_KEY_WIF);
            assertEquals(wif, WIF.fromPublicKey(key));
            assertArrayEquals(key.getData().toByteArray(), WIF.toPublicKey(wif).getData().toByteArray());
        }
    }

    @Test
    public void publicKey_from_PrivateKey_isCorrect() {
        for (int i = 0; i < DataItemCount; i++) {
            Type.private_key_type priv = Type.private_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PRIVATE_KEY))
            ).build();
            Type.public_key_type pub = Type.public_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PUBLIC_KEY))
            ).build();
            Type.public_key_type derived = Key.publicKeyOf(priv);
            assertArrayEquals(pub.getData().toByteArray(), derived.getData().toByteArray());
        }
    }

    @Test
    public void generateKey_isCorrect() {
        final int count = 10000;
        HashSet<String> keys = new HashSet<>(count);

        // todo: Order of secp256k1 hardcoded here. Update N's value if we change the curve in future.
        BigInteger N = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

        for (int i = 0; i < count; i++) {
            Type.private_key_type priv = Key.generate();
            BigInteger d = new BigInteger(1, priv.getData().toByteArray());
            assertTrue(d.signum() > 0);
            assertTrue(d.compareTo(N) < 0);

            String s = d.toString();
            assertFalse(keys.contains(s));
            keys.add(s);
        }
    }

    @Test
    public void verify_isCorrect() {
        for (int i = 0; i < DataItemCount; i++) {
            byte[] msg = getTestDataBytes(i, Entry.MESSAGE);
            byte[] sig = getTestDataBytes(i, Entry.SIGNATURE);
            Type.public_key_type pub = Type.public_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PUBLIC_KEY))
            ).build();
            assertTrue(Key.verifyMessage(sig, msg, pub));
        }
    }

    @Test
    public void signature_isVerifiable() {

        for (int i = 0; i < DataItemCount; i++) {
            byte[] msg = getTestDataBytes(i, Entry.MESSAGE);
            Type.private_key_type priv = Type.private_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PRIVATE_KEY))
            ).build();
            Type.public_key_type pub = Type.public_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PUBLIC_KEY))
            ).build();
            byte[] sig = Key.signMessage(msg, priv);
            assertNotNull(sig);
            assertTrue(Key.verifyMessage(sig, msg, pub));
        }
    }

    @Test
    public void recovery_isCorrect() {
        for (int i = 0; i < DataItemCount; i++) {
            byte[] msg = getTestDataBytes(i, Entry.MESSAGE);
            byte[] sig = getTestDataBytes(i, Entry.SIGNATURE);
            Type.public_key_type pub = Type.public_key_type.newBuilder().setData(
                    ByteString.copyFrom(getTestDataBytes(i, Entry.PUBLIC_KEY))
            ).build();
            Type.public_key_type recovered = Key.publicKeyFromSignatureMessage(sig, msg);
            assertNotNull(recovered);
            assertArrayEquals(pub.getData().toByteArray(), recovered.getData().toByteArray());
        }
    }
}
