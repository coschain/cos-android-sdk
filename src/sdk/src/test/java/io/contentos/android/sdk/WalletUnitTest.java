package io.contentos.android.sdk;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;
import io.contentos.android.sdk.rpc.Grpc;

import static org.junit.Assert.*;

import static io.contentos.android.sdk.Utils.randomAccountName;

public class WalletUnitTest {
    private static final String keyStorePassword = "my password";

    private static final String testAccountPrivateKey = "3diUftkv1rsSn45bTNBZgtaYbSstX9eHZfz3WGoX7r7UBsFgLV";
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
        Wallet wallet = Network.Main.wallet();

        // open keystore
        wallet.openKeyStore(getKeyStoreFile(), keyStorePassword);

        // import sdk test account
        wallet.addKey(testAccountName, testAccountPrivateKey);

        // how much is it to create a new account
        long accountCreationFee = wallet.getChainState().getState().getDgpo().getAccountCreateFee().getValue();

        // create a new account
        String newAccount = randomAccountName();
        String privateKey = WIF.fromPrivateKey(Key.generate());
        Grpc.BroadcastTrxResponse resp = wallet.account(testAccountName).accountCreate(
                testAccountName,
                newAccount,
                accountCreationFee,
                Key.publicKeyOf(WIF.toPrivateKey(privateKey)),
                "");

        if(resp.getInvoice().getStatus() == 200) {
            System.out.println("success");
        } else {
            System.out.println(resp.getInvoice().getErrorInfo());
        }

        // close wallet
        wallet.close();
    }
}
