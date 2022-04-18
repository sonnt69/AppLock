package com.tapbi.applock.ui.adapter.pager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tapbi.applock.ui.main.privacy.PrivacyFragment;
import com.tapbi.applock.ui.main.protect.ProtectFragment;

@SuppressWarnings("ALL")
public class HomePager extends FragmentStatePagerAdapter {

    public HomePager(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = new PrivacyFragment();
        } else if (position == 1) {
            fragment = new ProtectFragment();
        }
        return fragment;
    }


//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        switch (position) {
//            case 0:
//                return "Quyền riêng tư";
//            case 1:
//                return "Bảo vệ";
//            default:
//                return null;
//        }
//    }

    @Override
    public int getCount() {
        return 2;
    }
}

