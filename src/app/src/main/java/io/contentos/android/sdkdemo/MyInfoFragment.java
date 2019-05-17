package io.contentos.android.sdkdemo;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import io.contentos.android.sdk.crypto.Key;
import io.contentos.android.sdk.encoding.WIF;


public class MyInfoFragment extends Fragment implements View.OnClickListener {

    private EditText mAccountName;
    private TextView mPublicKey;
    private TextView mPrivateKey;
    private View mButtonGroup1;
    private View mButtonGroup2;

    private Listener mListener;

    public static MyInfoFragment newInstance() {
        return new MyInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAccountName = view.findViewById(R.id.text_account_name);
        mPublicKey = view.findViewById(R.id.text_public_key);
        mPrivateKey = view.findViewById(R.id.text_private_key);
        mButtonGroup1 = view.findViewById(R.id.btn_container1);
        mButtonGroup2 = view.findViewById(R.id.btn_container2);

        view.findViewById(R.id.btn_switch).setOnClickListener(this);
        view.findViewById(R.id.btn_import).setOnClickListener(this);
        view.findViewById(R.id.btn_import_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_import_cancel).setOnClickListener(this);

        refreshInfo();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (Listener) parent;
        } else {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                onSwitchButtonClicked();
                break;
            case R.id.btn_import:
                onImportButtonClicked();
                break;
            case R.id.btn_import_ok:
                onImportOkButtonClicked();
                break;
            case R.id.btn_import_cancel:
                onImportCancelButtonClicked();
                break;
        }
    }

    protected void onSwitchButtonClicked() {
        if (mListener != null) {
            mListener.onSwitchAccountClicked();
        }
    }

    protected void onImportButtonClicked() {
        String privateKey = "";
        int hintId = R.string.me_import_hint1;

        try {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                String clipStr = clipData.getItemAt(0).getText().toString();
                privateKey = WIF.fromPrivateKey(WIF.toPrivateKey(clipStr));
            }
        } catch (Exception e) {

        }

        if (privateKey.length() > 0) {
            MainActivity mainActivity = (MainActivity)getActivity();
            for (String name: mainActivity.wallet().getAccounts()) {
                if (privateKey.equals(mainActivity.wallet().getKey(name))) {
                    privateKey = "";
                    hintId = R.string.me_import_hint2;
                    break;
                }
            }
        }

        if (privateKey.length() > 0) {
            String publicKey = WIF.fromPublicKey(Key.publicKeyOf(WIF.toPrivateKey(privateKey)));

            mButtonGroup1.setVisibility(View.INVISIBLE);
            mButtonGroup2.setVisibility(View.VISIBLE);

            mPrivateKey.setText(privateKey);
            mPublicKey.setText(publicKey);

            mAccountName.setFocusable(true);
            mAccountName.setFocusableInTouchMode(true);
            mAccountName.setText("");
            mAccountName.setHint(R.string.me_account_hint);

        } else {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            AlertDialog dlg = b.setTitle(R.string.me_import_hint_title).setMessage(hintId)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            dlg.show();
        }
    }

    protected void onImportOkButtonClicked() {
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.wallet().addKey(mAccountName.getText().toString(), mPrivateKey.getText().toString());
        mainActivity.setCurrentAccount(mAccountName.getText().toString());
    }

    protected void onImportCancelButtonClicked() {
        refreshInfo();
    }

    public void refreshInfo() {
        MainActivity mainActivity = (MainActivity)getActivity();
        String account = mainActivity.currentAccount();
        String privateKey = mainActivity.wallet().getKey(account);
        String publicKey = WIF.fromPublicKey(Key.publicKeyOf(WIF.toPrivateKey(privateKey)));

        mButtonGroup1.setVisibility(View.VISIBLE);
        mButtonGroup2.setVisibility(View.INVISIBLE);

        mAccountName.setFocusable(false);
        mAccountName.setFocusableInTouchMode(false);

        mAccountName.setText(account);
        mPrivateKey.setText(privateKey);
        mPublicKey.setText(publicKey);
    }

    public interface Listener {
        void onSwitchAccountClicked();
    }
}
