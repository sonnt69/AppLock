package com.tapbi.applock.ui.main.splash;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;

import com.tapbi.applock.R;
import com.tapbi.applock.databinding.FragmentSplashBinding;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;

public class SplashFragment extends BaseBindingFragment<FragmentSplashBinding, MainViewModel> {
    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_splash;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)requireActivity()).changeSystemUIColor(Color.parseColor("#1DB854"), Color.TRANSPARENT, true, true);
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isAdded())
                    ((MainActivity)requireActivity()).changeMainScreen(R.id.homeFragment, null);
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)requireActivity()).changeSystemUIColor(Color.TRANSPARENT, Color.TRANSPARENT, true, true);
    }


    @Override
    protected void onPermissionGranted() {

    }
}
