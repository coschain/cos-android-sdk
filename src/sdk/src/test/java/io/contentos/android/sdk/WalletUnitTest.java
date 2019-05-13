package io.contentos.android.sdk;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import static org.junit.Assert.*;

import static io.contentos.android.sdk.Utils.randomAccountName;

public class WalletUnitTest {

    private static final String serverHost = "18.233.234.27";
    private static final int serverPort = 8888;

    private static final String keyStorePassword = "my password";

    private static final String testAccountPrivateKey = "4QMbCzf1GVD86UqngHPPX2HGSxU7tUuup2qirNS8JjiY3xKpWx";
    private static final String testAccountName = "sdktest";

    private static File getKeyStoreFile() {
        File file = null;
        try {
            file = Files.createTempDirectory("keystoreTestDir").resolve("keystoreFile").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @Test
    public void wallet_isCorrect() {
        // create the wallet
        Wallet wallet = new Wallet(serverHost, serverPort);

        // open keystore
        wallet.openKeyStore(getKeyStoreFile(), keyStorePassword);

        // import sdk test account
        wallet.addKey(testAccountName, testAccountPrivateKey);

        // create a new account
        String newAccount = randomAccountName();
        String privateKey = WIF.fromPrivateKey(Key.generate());
        wallet.account(testAccountName).accountCreate(
                testAccountName,
                newAccount,
                1,
                Key.publicKeyOf(WIF.toPrivateKey(privateKey)),
                "");

        // add new account and her key into keystore
        wallet.addKey(newAccount, privateKey);

        // transfer 100 tokens from sdk-test account to new account
        wallet.account(testAccountName).transfer(testAccountName, newAccount, 100, "enjoy");

        // transfer 23 tokens from new account to sdk-test account
        wallet.account(newAccount).transfer(newAccount, testAccountName, 23, "your change");

        // check new account's balance
        assertEquals(100 - 23, wallet.getAccountByName(newAccount).getInfo().getCoin().getValue());

        // close wallet
        wallet.close();
    }
}
