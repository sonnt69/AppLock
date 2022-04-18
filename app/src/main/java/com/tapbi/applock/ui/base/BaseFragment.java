package com.tapbi.applock.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tapbi.applock.utils.LocaleUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LocaleUtils.applyLocale(requireContext());
        super.onCreate(savedInstanceState);
    }
}

