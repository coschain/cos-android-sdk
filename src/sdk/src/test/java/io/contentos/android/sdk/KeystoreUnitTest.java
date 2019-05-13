package io.contentos.android.sdk;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;

import io.contentos.android.sdk.keystore.KeyStore;

public class KeystoreUnitTest {
    @Test
    public void keystore_isCorrect() {
        try {

            // keystore password
            String password = "keystore password";

            // keystore file
            File file = Files.createTempDirectory("keystoreTestDir").resolve("keystoreFile").toFile();

            // create a new and empty keystore
            KeyStore ks = KeyStore.openOrCreate(file, password);
            assertEquals(0, ks.getAccounts().size());

            // add accounts and keys
            ks.addKey("alice", "3u4KKa4vdA1M2s2YVkjuBnVkdLVVgUkbp5ksnDtXZdQcaEoj8n");
            ks.addKey("bob", "4jzj76fLAcJC7y58GzMUADGr9d7cP1Bs7BFMUY2ptgfbMY1iTA");
            ks.addKey("charlie", "4jwPFJaPMaLycRMzi7L3WidUx7GXzsPH9qknX2aEk68tTxhYNX");

            // query
            assertEquals(3, ks.getAccounts().size());
            assertEquals("3u4KKa4vdA1M2s2YVkjuBnVkdLVVgUkbp5ksnDtXZdQcaEoj8n", ks.getKey("alice"));
            assertEquals("4jzj76fLAcJC7y58GzMUADGr9d7cP1Bs7BFMUY2ptgfbMY1iTA", ks.getKey("bob"));
            assertEquals("4jwPFJaPMaLycRMzi7L3WidUx7GXzsPH9qknX2aEk68tTxhYNX", ks.getKey("charlie"));
            assertNull(ks.getKey("tom"));
            assertNull(ks.getKey("jerry"));

            // remove an account and check
            ks.removeKey("bob");
            assertEquals("3u4KKa4vdA1M2s2YVkjuBnVkdLVVgUkbp5ksnDtXZdQcaEoj8n", ks.getKey("alice"));
            assertNull(ks.getKey("bob"));
            assertEquals("4jwPFJaPMaLycRMzi7L3WidUx7GXzsPH9qknX2aEk68tTxhYNX", ks.getKey("charlie"));

            // create another keystore by loading the keystore file
            KeyStore ks2 = KeyStore.openOrCreate(file, password);
            assertEquals(2, ks2.getAccounts().size());
            assertEquals("3u4KKa4vdA1M2s2YVkjuBnVkdLVVgUkbp5ksnDtXZdQcaEoj8n", ks2.getKey("alice"));
            assertEquals("4jwPFJaPMaLycRMzi7L3WidUx7GXzsPH9qknX2aEk68tTxhYNX", ks2.getKey("charlie"));

        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
