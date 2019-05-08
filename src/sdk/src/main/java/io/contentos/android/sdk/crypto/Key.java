package io.contentos.android.sdk.crypto;

import com.google.protobuf.ByteString;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.math.ec.ECPoint;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import io.contentos.android.sdk.prototype.Type;

public class Key {

    // the chosen elliptic curve.
    static private final String ECName = "secp256k1";

    // frequently used properties of the chosen curve.
    static private final X9ECParameters ECParam = CustomNamedCurves.getByName(ECName);
    static private final BigInteger N = ECParam.getN();
    static private final int N_Bits = N.bitLength();
    static private final int N_Bytes = (N_Bits + 7) / 8;
    static private final BigInteger N_1 = N.subtract(BigInteger.ONE);
    static private final BigInteger HalfN = N.shiftRight(1);


    /**
     * Generate a new private key.
     * @return a private key.
     */
    static public Type.private_key_type generate() {
        // generate a random integer in range [1, N-1].
        BigInteger d = randInt(new SecureRandom());
        return Type.private_key_type.newBuilder().setData(
                ByteString.copyFrom(uintBytes(d))
        ).build();
    }

    /**
     * Get public key from given private key.
     * @param privateKey the private key.
     * @return public key of given private key.
     */
    static public Type.public_key_type publicKeyOf(Type.private_key_type privateKey) {
        // get the secret integer from private key bytes.
        BigInteger d = new BigInteger(1, privateKey.getData().toByteArray());

        // public key Q = dG, and we always use compressed public keys.
        byte[] pub = ECParam.getG().multiply(d).getEncoded(true);
        return Type.public_key_type.newBuilder().setData(
                ByteString.copyFrom(pub)
        ).build();
    }

    /**
     * Generate a public-key-recoverable signature of given digest by given private key.
     * @param digest        the digest to be signed.
     * @param privateKey    the signer's private key.
     * @return  bytes of signature.
     */
    static public byte[] signDigest(byte[] digest, Type.private_key_type privateKey) {
        byte[] priv = privateKey.getData().toByteArray();

        // initialize a CSPRNG based on private key and digest.
        // it will be used to generate random k values.
        SecureRandom sr = new SecureRandom();
        byte[] entropy = new byte[priv.length + 32 + digest.length];
        ByteBuffer buf = ByteBuffer.allocate(entropy.length)
                .put(privateKey.getData().toByteArray())
                .put(sr.generateSeed(32))
                .put(digest);
        buf.rewind();
        buf.get(entropy);
        sr.setSeed(Hash.sha256(entropy));

        BigInteger z = hashToInt(digest);
        BigInteger d = new BigInteger(1, priv);

        //
        // The first step of public key recovery is to identify kG(x,y) based on the r part of signature.
        //
        // According to the signing algorithm, x = r + bn, (b >= 0). We must record b to identify the
        // x-coordinate of kG.
        //
        // When x is fixed, we get a pair of symmetric y values, and one of them must be the
        // y-coordinate of kG. So we need to record another flag f indicating which one of the pair
        // is the real y-coordinate (f=0: the even one; f=1: the odd one).
        //
        // recovery id = b*2 + f.
        //
        byte recovery;

        BigInteger r, s;
        while (true) {
            // random signing secret k
            BigInteger k = randInt(sr);

            // kInv = k ^ -1
            BigInteger kInv = k.modInverse(N);

            ECPoint kG = ECParam.getG().multiply(k).normalize();
            BigInteger x = kG.getAffineXCoord().toBigInteger();

            // x = r + bn,  re[0] = b, re[1] = r.
            BigInteger[] re = x.divideAndRemainder(N);
            r = re[1];

            // r must not be 0.
            if (r.signum() == 0) {
                continue;
            }

            // recovery = b
            recovery = re[0].signum() != 0? (byte)(re[0].intValue()) : 0;

            // s = (k^-1)(z + rd)
            s = r.multiply(d).add(z).multiply(kInv).mod(N);

            // s must not be 0.
            if (s.signum() == 0) {
                continue;
            }

            //
            // resistant to ECDSA signature malleability attack by using BIP-0062's low-s-values solution.
            // https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#low-s-values-in-signatures
            //
            if (s.compareTo(HalfN) > 0) {
                s = N.subtract(s);
                kG = kG.negate().normalize();
            }

            // recovery = 2 * b + f
            recovery <<= 1;
            if (kG.getAffineYCoord().toBigInteger().testBit(0)) {
                recovery++;
            }

            break;
        }

        // signature structure: [ R: N_Bytes ][ S: N_Bytes ][ Recovery: 1 Byte]
        // e.g. for 256-bit curves, N_Bytes = 256/8 = 32, signature size = 32 + 32 + 1 = 65.
        byte[] rBytes = uintBytes(r);
        byte[] sBytes = uintBytes(s);
        byte[] signature = new byte[N_Bytes + N_Bytes + 1];
        System.arraycopy(rBytes, 0, signature, 0, N_Bytes);
        System.arraycopy(sBytes, 0, signature, N_Bytes, N_Bytes);
        signature[N_Bytes + N_Bytes] = recovery;
        return signature;
    }

    /**
     * Generate a public-key-recoverable signature of given message by given private key.
     * @param message       the message bytes to be signed.
     * @param privateKey    the signer's private key.
     * @return bytes of signature.
     */
    static public byte[] signMessage(byte[] message, Type.private_key_type privateKey) {
        return signDigest(Hash.sha256(message), privateKey);
    }

    /**
     * ECDSA signature verification.
     * @param signature     the signature
     * @param digest        the message digest
     * @param publicKey     the public key of signer
     * @return  true if signature verification passed, otherwise false.
     */
    static public Boolean verifyDigest(byte[] signature, byte[] digest, Type.public_key_type publicKey) {
        // check signature size
        if (signature.length != N_Bytes * 2 + 1) {
            return false;
        }

        // extract (r, s) and validate them
        BigInteger r = new BigInteger(1, Arrays.copyOf(signature, N_Bytes));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, N_Bytes, N_Bytes + N_Bytes));
        if (r.signum() <= 0 || r.compareTo(N) >= 0 || s.signum() <= 0 || s.compareTo(N) >= 0) {
            return false;
        }

        // convert public key bytes to curve point
        ECPoint q = ECParam.getCurve().decodePoint(publicKey.getData().toByteArray());

        // convert digest to finite field integer
        BigInteger z = hashToInt(digest);

        // kG(x,y) = zs^(-1)G + rs^(-1)Q
        BigInteger sInv = s.modInverse(N);
        BigInteger u1 = sInv.multiply(z).mod(N);
        BigInteger u2 = sInv.multiply(r).mod(N);
        ECPoint kG = ECParam.getG().multiply(u1).add(q.multiply(u2));
        if (kG.isInfinity()) {
            return false;
        }

        // check if r == x mod N
        return r.compareTo(kG.normalize().getAffineXCoord().toBigInteger().mod(N)) == 0;
    }

    /**
     * ECDSA signature verification.
     * @param signature     the signature
     * @param message       the message data
     * @param publicKey     the public key of signer
     * @return  true if signature verification passed, otherwise false.
     */
    static public Boolean verifyMessage(byte[] signature, byte[] message, Type.public_key_type publicKey) {
        return verifyDigest(signature, Hash.sha256(message), publicKey);
    }

    /**
     * Recover the signer's public key from a signature.
     * @param signature     the signature
     * @param digest        the message digest
     * @return  signer's public key or null if recovery failed.
     */
    static public Type.public_key_type publicKeyFromSignatureDigest(byte[] signature, byte[] digest) {
        if (signature.length != N_Bytes * 2 + 1) {
            return null;
        }
        BigInteger r = new BigInteger(1, Arrays.copyOf(signature, N_Bytes));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, N_Bytes, N_Bytes + N_Bytes));
        long recovery = (0xff & signature[N_Bytes + N_Bytes]);
        long b = recovery >> 1;
        boolean f = (recovery & 1) == 1;
        if (r.signum() <= 0 || r.compareTo(N) >= 0 || s.signum() <= 0 || s.compareTo(N) >= 0) {
            return null;
        }
        BigInteger x = r;
        if (b != 0) {
            x = BigInteger.valueOf(b).multiply(N).add(r);
        }
        byte[] xBytes = uintBytes(x);
        byte[] kGBytes = new byte[1 + N_Bytes];
        kGBytes[0] = 2;
        System.arraycopy(xBytes, 0, kGBytes, 1, N_Bytes);
        if (f) {
            kGBytes[0]++;
        }
        ECPoint kG = ECParam.getCurve().decodePoint(kGBytes);
        BigInteger z = hashToInt(digest);
        BigInteger rInv = r.modInverse(N);
        BigInteger u1 = rInv.multiply(z).mod(N);
        BigInteger u2 = rInv.multiply(s).mod(N);
        ECPoint q = ECParam.getG().multiply(u1).negate().add(kG.multiply(u2)).normalize();

        return Type.public_key_type.newBuilder().setData(
                ByteString.copyFrom(q.getEncoded(true))
        ).build();
    }

    /**
     * Recover the signer's public key from a signature.
     * @param signature     the signature
     * @param message       the message data
     * @return  signer's public key or null if recovery failed.
     */
    static public Type.public_key_type publicKeyFromSignatureMessage(byte[] signature, byte[] message) {
        return publicKeyFromSignatureDigest(signature, Hash.sha256(message));
    }

    // convert a hash value to a finite field integer.
    static private BigInteger hashToInt(byte[] hash) {
        byte[] z = hash;
        if (hash.length > N_Bytes) {
            z = Arrays.copyOf(hash, N_Bytes);
        }
        BigInteger r = new BigInteger(1, z);
        int excess = z.length * 8 - N_Bits;
        if (excess > 0) {
            r = r.shiftRight(excess);
        }
        return r;
    }

    // generate a finite field integer in range [1, N-1].
    static private BigInteger randInt(Random rand) {
        byte[] randBytes = new byte[N_Bytes + 8];
        rand.nextBytes(randBytes);
        return new BigInteger(1, randBytes).mod(N_1).add(BigInteger.ONE);
    }

    // return a N_Bytes-long byte array representing the given non-negative integer.
    // the byte array doesn't contain sign bit.
    static private byte[] uintBytes(BigInteger n) {
        byte[] bytes = n.toByteArray();
        if (bytes.length > N_Bytes) {

            //
            // the integer is too long, cut leading bytes (most significant bytes)
            //
            // BigInteger.toByteArray() always outputs sign bit, so that it possibly returns a
            // 33-byte-long byte array even if n is smaller than 2^256.
            // e.g. n = 2^256 - 1, n.bitLength()==257 (256 value bits + 1 sign bit).
            // n.toByteArray() is 33-byte-long coz 32 bytes is smaller than 257 bits.
            //
            bytes = Arrays.copyOfRange(bytes, bytes.length - N_Bytes, bytes.length);

        } else if (bytes.length < N_Bytes) {

            // the integer is short, add leading 0x00-bytes
            byte[] b = bytes;
            bytes = new byte[N_Bytes];
            System.arraycopy(b, 0, bytes, N_Bytes - b.length, b.length);
        }
        return bytes;
    }
}
