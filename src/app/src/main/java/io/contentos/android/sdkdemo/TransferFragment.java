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
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferFragment extends Fragment implements View.OnClickListener {

    private EditText mTextReceiver;
    private EditText mTextAmount;
    private Button mBtnTransfer;

    private Listener mListener;

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTextReceiver = view.findViewById(R.id.transfer_recv);
        mTextAmount = view.findViewById(R.id.transfer_token);
        mBtnTransfer = view.findViewById(R.id.transfer_btn);
        mBtnTransfer.setOnClickListener(this);
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
        long amount = -1;
        try {
            double tokens = Double.parseDouble(mTextAmount.getText().toString()) * 1000000;
            if (tokens > 0) {
                amount = (long)tokens;
            }
        } catch (Exception e) {

        }
        if (amount < 0) {
            Toast.makeText(getActivity(), R.string.transfer_err_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mListener != null) {
            mListener.onTransferClicked(mTextReceiver.getText().toString(), amount);
        }
    }

    public interface Listener {
        void onTransferClicked(String receiver, long amount);
    }

}
