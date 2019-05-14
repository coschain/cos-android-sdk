package io.contentos.android.sdkdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class NewAccountFragment extends Fragment implements View.OnClickListener {

    private EditText mTextAccountName;
    private Button mBtnCreate;

    private Listener mListener;

    public static NewAccountFragment newInstance() {
        return new NewAccountFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_account, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTextAccountName = view.findViewById(R.id.new_acc_name);
        mBtnCreate = view.findViewById(R.id.new_acc_button);
        mBtnCreate.setOnClickListener(this);
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
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onNewAccountClicked(mTextAccountName.getText().toString());
        }
    }


    public interface Listener {
        void onNewAccountClicked(String name);
    }
}
