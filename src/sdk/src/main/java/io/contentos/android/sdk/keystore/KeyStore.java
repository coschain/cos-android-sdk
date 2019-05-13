package io.contentos.android.sdk.keystore;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.contentos.android.sdk.crypto.Hash;

public class KeyStore implements KeystoreAPI {

    private Map<String, String> keys = new HashMap<>();
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

    public synchronized void removeKey(String account) {
        keys.remove(account);
        save();
    }

    public synchronized Set<String> getAccounts() {
        return keys.keySet();
    }

    // load from keystore file
    private void load() {
        try {
            FileInputStream input = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            input.read(data);
            input.close();
            fromJson(decrypt(new String(data, "UTF-8"), password));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // save to keystore file
    private void save() {
        try {
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file, false);
            output.write(encrypt(toJson(), password).getBytes("UTF-8"));
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // JSON serialization
    //

    private String toJson() {
        String jsonStr = "";
        try {
            JSONArray accounts = new JSONArray();
            for (Map.Entry<String, String> entry: keys.entrySet()) {
                accounts.put(new JSONObject()
                        .put("name", entry.getKey())
                        .put("key", entry.getValue())
                );
            }
            jsonStr = new JSONObject().put("accounts", accounts).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    private void fromJson(String jsonStr) {
        HashMap<String, String> loaded = new HashMap<>();
        try {
            JSONArray accounts = new JSONObject(jsonStr).getJSONArray("accounts");
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject account = accounts.getJSONObject(i);
                loaded.put(account.getString("name"), account.getString("key"));
            }
        } catch (JSONException e) {
            loaded = null;
        }
        if (loaded != null) {
            keys = loaded;
        }
    }

    //
    // encrypt/decrypt: simple XOR using cyclic key
    //

    private static String encrypt(String text, String password) {
        String enc = "";
        if (password == null) {
            password = "";
        }
        try {
            byte[] key = Hash.sha256(password.getBytes("UTF-8"));
            byte[] data = text.getBytes("UTF-8");
            for (int i = 0; i < data.length; i++) {
                data[i] ^= key[i % key.length];
            }
            enc = new String(Base64.encode(data, Base64.DEFAULT), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enc;
    }

    private static String decrypt(String encrypted, String password) {
        String text = "";
        if (password == null) {
            password = "";
        }
        try {
            byte[] key = Hash.sha256(password.getBytes("UTF-8"));
            byte[] data = Base64.decode(encrypted, Base64.DEFAULT);
            for (int i = 0; i < data.length; i++) {
                data[i] ^= key[i % key.length];
            }
            text = new String(data, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
}
