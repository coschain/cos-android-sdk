package io.contentos.android.sdkdemo;

import android.content.Context;

import java.io.File;

public class Constants {

    public static final String serverHost = "18.233.234.27";
    public static final int serverPort = 8888;

    public static final String testAccountPrivateKey = "4QMbCzf1GVD86UqngHPPX2HGSxU7tUuup2qirNS8JjiY3xKpWx";
    public static final String testAccountName = "sdktest";

    private static final String keyStoreFileName = "cos_wallet_keystore";

    public static final String EXTRA_WALLET_PASSWORD = "_WALLET_PASSWORD";

    public static File keyStoreFile(Context context) {
        return new File(context.getFilesDir(), keyStoreFileName);
    }
}
