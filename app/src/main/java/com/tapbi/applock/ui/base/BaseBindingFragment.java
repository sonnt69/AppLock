package com.tapbi.applock.ui.base;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import com.tapbi.applock.ui.main.MainViewModel;

public abstract class BaseBindingFragment<B extends ViewDataBinding, T extends BaseViewModel> extends BaseFragment {

    public B binding;
    public T viewModel;
    public MainViewModel mainViewModel;

    protected abstract Class<T> getViewModel();

    public abstract int getLayoutId();

    protected abstract void onCreatedView(View view, Bundle savedInstanceState);

    protected abstract void onPermissionGranted();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(getViewModel());
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        onCreatedView(view, savedInstanceState);
    }
    @Override
    public void onStart() {
        super.onStart();
        lastClickTime =0;
    }


    protected boolean checkTime() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastClickTime) < 600) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }
    private long lastClickTime = 0;

    public void checkLongClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 2000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }
}


