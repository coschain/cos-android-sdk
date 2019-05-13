package io.contentos.android.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import io.contentos.android.sdk.Wallet;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private File keyStoreFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        keyStoreFile = Constants.keyStoreFile(this);
        Button b = findViewById(R.id.btnOpenWallet);
        b.setOnClickListener(this);
        b.setText(keyStoreFile.exists()? R.string.wallet_open : R.string.wallet_create);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOpenWallet:
                onClickOpenWalletButton();
        }
    }

    private void onClickOpenWalletButton() {
        EditText textPasswd = findViewById(R.id.editPassword);
        String password = textPasswd.getText().toString();
        Wallet wallet = new Wallet(Constants.serverHost, Constants.serverPort);
        try {
            wallet.openKeyStore(keyStoreFile, password);
            if (wallet.getAccounts().size() == 0) {
                wallet.addKey(Constants.testAccountName, Constants.testAccountPrivateKey);
            }
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, OpActivity.class);
            intent.putExtra(Constants.EXTRA_WALLET_PASSWORD, password);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.wallet_open_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
