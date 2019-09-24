package io.contentos.android.sdk.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import io.contentos.android.sdk.crypto.Hash;
import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import io.contentos.android.sdk.prototype.Type;

public class KeyStore implements KeystoreAPI {
    private static final String CRYPTO_ALGORITHM = "AES";
    private static final String CRYPTO_TRANSFORM = "AES/ECB/PKCS5Padding";

    private HashMap<String, String> keys = new HashMap<>();

    private String password;
    private File file;

    /**
     * Open an existing or create a new keystore.
     * @param file      the keystore file
     * @param password  password of keystore
     * @return the keystore instance.
     */
    public static KeyStore openOrCreate(File file, String password) {
        KeyStore keyStore = new KeyStore(file, password);
        if (!file.exists()) {
            keyStore.save();
        } else {
            keyStore.load();
        }
        return keyStore;
    }

    private KeyStore(File file, String password) {
        this.file = file;
        this.password = password;
    }

    //
    // KeyStoreAPI implementation
    //

    public synchronized String getKey(String account) {
        return keys.get(account);
    }

    public synchronized void addKey(String account, String wifPrivateKey) {
        keys.put(account, wifPrivateKey);
        save();
    }

    public synchronized void addKeyByMnemonic(String account, String mnemonic) {
        Type.private_key_type privateKey = Key.generateFromMnemonic(mnemonic);
        String wifPrivateKey = WIF.fromPrivateKey(privateKey);
        keys.put(account, wifPrivateKey);
        save();
    }

    public synchronized void removeKey(String account) {
        keys.remove(account);
        save();
    }

    public synchronized List<String> getAccounts() {
        return new ArrayList<>(keys.keySet());
    }

    // load from keystore file
    private void load() {
        try {
            SecretKeySpec sks = new SecretKeySpec(Hash.sha256(password.getBytes()), CRYPTO_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CRYPTO_TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, sks);

            FileInputStream fInput = new FileInputStream(file);
            CipherInputStream cIn = new CipherInputStream(fInput, cipher);
            ObjectInputStream objIn = new ObjectInputStream(cIn);

            SealedObject so = (SealedObject) objIn.readObject();
            this.keys = (HashMap<String, String>) so.getObject(cipher);

            objIn.close();
            cIn.close();
            fInput.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // save to keystore file
    private void save() {
        try {
            SecretKeySpec sks = new SecretKeySpec(Hash.sha256(password.getBytes()), CRYPTO_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CRYPTO_TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, sks);

            SealedObject so = new SealedObject(this.keys, cipher);

            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file, false);
            CipherOutputStream cOut = new CipherOutputStream(fout, cipher);
            ObjectOutputStream objOut = new ObjectOutputStream(cOut);
            objOut.writeObject(so);

            objOut.close();
            cOut.close();
            fout.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
