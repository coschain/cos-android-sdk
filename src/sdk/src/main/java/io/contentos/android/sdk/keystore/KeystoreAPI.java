package io.contentos.android.sdk.keystore;

import java.util.List;

public interface KeystoreAPI {
    /**
     * Get private key of specific account.
     * @param account name of account
     * @return private key in WIF encoding.
     */
    String getKey(String account);

    /**
     * Add an account and her private key into keystore.
     * @param account       name of account
     * @param wifPrivateKey private key in WIF encoding
     */
    void addKey(String account, String wifPrivateKey);

    /**
     * Remove an account and her private key from keystore.
     * @param account   name of account
     */
    void removeKey(String account);

    /**
     * Get all accounts from keystore.
     * @return set of account names.
     */
    List<String> getAccounts();
}
