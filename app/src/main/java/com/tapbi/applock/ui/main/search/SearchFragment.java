package com.tapbi.applock.ui.main.search;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.common.model.SearchApp;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.FragmentSearchBinding;
import com.tapbi.applock.interfaces.ClickAppLock;
import com.tapbi.applock.service.Accessibility;
import com.tapbi.applock.ui.adapter.PrivacyAdapter;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchFragment extends BaseBindingFragment<FragmentSearchBinding, MainViewModel> implements ClickAppLock {
    private PrivacyAdapter adapter;
    private long lastClickTime = 0;
    private String passDraw = "";
    private List<AppInfo> lSearch = new ArrayList<>();

    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        view.requestFocus();

        setAdapter();

        observerData();

        eventSearch();

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1200) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();

//                if(requireActivity() instanceof MainActivity){
//                    requireActivity().onBackPressed();
//                }

                Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), mBackPressedCallback);

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
                lSearch.clear();
                lSearch.addAll(appInfos);
                adapter.setList(appInfos);

                binding.progressBarCyclic.setVisibility(View.GONE);
            }
        });
    }

    private void eventSearch() {
        binding.lyoSearch.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                binding.progressBarCyclic.setVisibility(View.GONE);

                if(lSearch != null){
                    if (editable.length() == 0) {
                        adapter.setList(lSearch);
                    } else {
                        List<AppInfo> list = new ArrayList<>();
                        for (AppInfo appInfo : lSearch) {
                            if (appInfo.getAppName().toLowerCase().contains(editable.toString().toLowerCase())) {
                                list.add(appInfo);
                            }
                        }
                        adapter.setList(list);
                    }
                }
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
    protected void onPermissionGranted() {

    }

    @Override
    public void onPause() {
        super.onPause();

        Utils.closeKeyboard(requireContext(), getView());
    }


    @Override
    public void locked(AppLock appLock) {
        if (Utils.showDiaLogPermission(requireContext())) {
            if (passDraw.equals("")) {
                mainViewModel.appLockMutableLiveData.postValue(appLock);
                mainViewModel.checkFragment.postValue(Constant.SEARCH_FRAGMENT);

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

    @Override
    public void onResume() {
        super.onResume();

        passDraw = SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, "");
        boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
        if(!setQuestion)
            passDraw = "";

        mainViewModel.getAppsInfo(requireActivity());
        //check permission
        mainViewModel.getCountLockedApps().observe(getViewLifecycleOwner(), integer -> {
            if (integer > 1) {
                Utils.showDiaLogPermission(requireContext());
            }
        });
    }

    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
        }
    };

}