package io.contentos.android.sdkdemo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.HashMap;

import io.contentos.android.sdk.Wallet;
import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        AccountListDialogFragment.Listener,
        TransferFragment.Listener,
        PostFragment.Listener,
        NewAccountFragment.Listener
{

    private TextView mTitle;
    private Wallet m_wallet;
    private String m_account;
    private AlertDialog mWaitDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        mTitle = findViewById(R.id.title);
        mTitle.setOnClickListener(this);

        m_wallet = new Wallet(Constants.serverHost, Constants.serverPort);
        m_wallet.openKeyStore(Constants.keyStoreFile(this), getIntent().getStringExtra(Constants.EXTRA_WALLET_PASSWORD));
        setCurrentAccount(m_wallet.getAccounts().get(0));

        mWaitDlg = new AlertDialog.Builder(this).setMessage("Please wait...").create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_wallet.close();
    }

    public Wallet wallet() {
        return m_wallet;
    }

    public String currentAccount() {
        return m_account;
    }

    public void setCurrentAccount(String name) {
        m_account = name;
        mTitle.setText(getString(R.string.tab_title) + " " + m_account);
    }

    public void onClick(View v) {
        AccountListDialogFragment.newInstance(m_wallet.getAccounts()).show(getSupportFragmentManager(), "AccountList");
    }

    public void onAccountClicked(int position) {
        setCurrentAccount(m_wallet.getAccounts().get(position));
    }

    public void onTransferClicked(final String receiver, final long amount) {
        runTask(new Runnable() {
            @Override
            public void run() {
                String me = currentAccount();
                wallet().account(me).transfer(me, receiver, amount, "");
            }
        });
    }

    public void onPostClicked(final String title, final String content) {
        runTask(new Runnable() {
            @Override
            public void run() {
                String me = currentAccount();
                wallet().account(me).post(me, title, content, Arrays.asList("sdkdemo", me), new HashMap<String, Integer>());
            }
        });
    }

    public void onNewAccountClicked(final String name) {
        runTask(new Runnable() {
            @Override
            public void run() {
                String me = currentAccount();
                String privateKey = WIF.fromPrivateKey(Key.generate());
                wallet().account(me).accountCreate(me, name, 1, Key.publicKeyOf(WIF.toPrivateKey(privateKey)), "");
                wallet().addKey(name, privateKey);
            }
        });
    }

    public void showWaitDlg(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    mWaitDlg.show();
                } else {
                    mWaitDlg.dismiss();
                }
            }
        });
    }

    public void showToast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, duration).show();
            }
        });
    }

    public void runTask(final Runnable task) {
        showWaitDlg(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Exception exc = null;
                try {
                    task.run();
                } catch (Exception e) {
                    exc = e;
                }
                showWaitDlg(false);
                if (exc == null) {
                    showToast("Success", Toast.LENGTH_SHORT);
                } else {
                    showToast("Failed: " + exc.toString(), Toast.LENGTH_LONG);
                    Log.e("OPTask", exc.toString());
                }
            }
        }).start();
    }
}
