package io.contentos.android.sdkdemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_text_transfer,
            R.string.tab_text_post,
            R.string.tab_text_create_account,
            R.string.tab_text_me,
    };
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (TAB_TITLES[position]) {
            case R.string.tab_text_transfer:
                return TransferFragment.newInstance();
            case R.string.tab_text_post:
                return PostFragment.newInstance();
            case R.string.tab_text_create_account:
                return NewAccountFragment.newInstance();
            case R.string.tab_text_me:
                return MyInfoFragment.newInstance();
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}