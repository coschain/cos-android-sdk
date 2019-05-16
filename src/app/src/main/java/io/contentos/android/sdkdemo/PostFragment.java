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


public class PostFragment extends Fragment implements View.OnClickListener {

    private EditText mTextTitle;
    private EditText mTextContent;
    private Button mBtnPost;

    private Listener mListener;

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTextTitle = view.findViewById(R.id.post_title);
        mTextContent = view.findViewById(R.id.post_content);
        mBtnPost = view.findViewById(R.id.post_button);
        mBtnPost.setOnClickListener(this);
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
            mListener.onPostClicked(mTextTitle.getText().toString(), mTextContent.getText().toString());
        }
    }

    public interface Listener {
        void onPostClicked(String title, String content);
    }
}
