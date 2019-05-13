package io.contentos.android.sdkdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.contentos.android.sdk.Wallet;
import io.contentos.android.sdkdemo.ui.main.SectionsPagerAdapter;

public class OpActivity extends AppCompatActivity {

    private Wallet m_wallet;
    private String m_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_op);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        m_wallet = new Wallet(Constants.serverHost, Constants.serverPort);
        m_wallet.openKeyStore(Constants.keyStoreFile(this), getIntent().getStringExtra(Constants.EXTRA_WALLET_PASSWORD));
        m_account = m_wallet.getAccounts().get(0);

        fixTitle();
    }

    private void fixTitle() {
        TextView textTitle = findViewById(R.id.title);
        textTitle.setText(getString(R.string.tab_title) + " " + m_account);
    }

    public Wallet wallet() {
        return m_wallet;
    }
}
