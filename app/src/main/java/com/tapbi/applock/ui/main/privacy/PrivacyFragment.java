package com.tapbi.applock.ui.main.privacy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.FragmentPrivacyBinding;
import com.tapbi.applock.interfaces.ClickAppLock;
import com.tapbi.applock.ui.adapter.PrivacyAdapter;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Comparator;

public class PrivacyFragment extends BaseBindingFragment<FragmentPrivacyBinding, MainViewModel> implements ClickAppLock {
    private PrivacyAdapter adapter;
    private String passDraw = "";

    @Override
    protected Class getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_privacy;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        view.requestFocus();

        passDraw = SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, "");
        boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
        if(!setQuestion)
            passDraw = "";

        setAdapter();

        observerData();
    }


    private void observerData() {
        mainViewModel.infoApps.observe(getViewLifecycleOwner(), appInfos -> {
            if (appInfos != null) {
                Collections.sort(appInfos, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo t1, AppInfo t2) {
                        return t1.getAppType().compareTo(t2.getAppType());
                    }
                });

                adapter.setList(appInfos);
                binding.progressBarCyclic.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onPermissionGranted() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        super.onResume();

        mainViewModel.getAppsInfo(requireActivity());

        //check permission
        mainViewModel.getCountLockedApps().observe(getViewLifecycleOwner(), integer -> {
            if (integer > 1) {
                Utils.showDiaLogPermission(requireContext());
            }
        });
    }

    private void setAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        binding.rcvShowApp.setLayoutManager(linearLayoutManager);
        adapter = new PrivacyAdapter(getContext(), this);
        binding.rcvShowApp.setAdapter(adapter);
    }

    @Override
    public void locked(AppLock appLock) {
        if (Utils.showDiaLogPermission(requireContext())) {
            if (passDraw.equals("")) {
                mainViewModel.appLockMutableLiveData.postValue(appLock);
                mainViewModel.checkFragment.postValue(Constant.HOME_FRAGMENT);

                Bundle bundle = new Bundle();
                bundle.putBoolean(Constant.FIRST_OPEN, true);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.changeDrawFragment, bundle);
            }

            else mainViewModel.insertLockApp(appLock);
        }


    }

    @Override
    public void unLock(AppLock appLock) {
        if (Utils.showDiaLogPermission(requireContext())) {
        }

        if(!passDraw.equals("")){
            mainViewModel.deleteLockAppByName(appLock.getAppPackage());
        }

    }
}
