package com.tapbi.applock.ui.main.changedraw;

import static com.tapbi.applock.utils.Utils.BACKGROUND_DEFAULT;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.FragmentChangeDrawBinding;
import com.tapbi.applock.service.Accessibility;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;

import java.util.List;


public class ChangeDrawFragment extends BaseBindingFragment<FragmentChangeDrawBinding, MainViewModel> {
    private final Handler handler = new Handler();
    boolean stop = false;
    private boolean isLockApp;
    private boolean isSettingApp;
    private boolean checkPass = true;
    private String pass1 = "";
    //    private android.content.SharedPreferences sharedPreferences;
    private boolean vibrate;
    private boolean showDraw;
    private boolean forgetPass;
    private String theme = "";
    private boolean firstOpen;
    private final PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            //binding.ChangeDrawTvAlert.setText(getText(R.string.dropWhenDone));
            stop = true;
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            String passDefault = "";
            String pass = "";
            if (isSettingApp) {
                pass1 = (SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, Constant.PASS_DEFAULT_DRAW));

                checkPass = false;
                stop = false;
            }

            pass = PatternLockUtils.patternToString(binding.layoutPattern.ChangeDrawPatternLock, pattern);

            if (!pass.equals("") && pass.length() < 4) {
                //binding.ChangeDrawTvAlert.setText(getText(R.string.draw4));
                Toast.makeText(requireContext(), getString(R.string.minumum_pass), Toast.LENGTH_SHORT).show();
                binding.layoutPattern.ChangeDrawPatternLock.setViewMode(PatternLockView.PatternViewMode.WRONG);
                stop = false;
                setClearPattern(500);
                return;
            }

            if (!isSettingApp) {
                binding.layoutTypeAction.tvDesTypeAction.setText(getText(R.string.confirmPassWord));
            }

            if (checkPass) {
                pass1 = pass;
                //binding.ChangeDrawTvAlert.setText(getText(R.string.redrawToComfirm));
                checkPass = false;
                stop = false;
                setClearPattern(500);

                Log.e("NGOCANH", "check pass ok");

            } else {
                if (!pass1.equals(pass)) {
                    Log.e("NGOCANH", "check pass not ok");

                    binding.layoutTypeAction.tvDesTypeAction.setText(R.string.wrong_pass);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.layoutTypeAction.tvDesTypeAction.setTextColor(requireActivity().getColor(R.color.color_FB5A07));
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.layoutTypeAction.tvDesTypeAction.setText(R.string.confirmPassWord);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                binding.layoutTypeAction.tvDesTypeAction.setTextColor(requireActivity().getColor(R.color.color_777777));
                            }
                        }
                    }, 500);
//                    Toast.makeText(requireContext(), getString(R.string.wrong_pass), Toast.LENGTH_SHORT).show();

                    //binding.ChangeDrawTvAlert.setText(getText(R.string.errorChangePass));
                    stop = false;
                    binding.layoutPattern.ChangeDrawPatternLock.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    setClearPattern(500);
                    return;
                }
//                SharedPreferenceHelper.storeBoolean(Constant.FIRST_OPEN, false);

                SharedPreferenceHelper.storeString(Constant.PATTERN_LOCK, pass1);

                if (firstOpen) {


                    Log.e("NGOCANH", "firstOpen ChangeDrawFragment");
                    boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
                    if(!setQuestion){
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(Constant.FIRST_OPEN, true);
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.settingQuestionFragment, bundle);
                    }
                    else{
                        AppLock appLock3 = new AppLock();
                        appLock3.setAppPackage("com.tapbi.applock");
                        mainViewModel.insertLockApp(appLock3);

                        //lock app
                        if(mainViewModel.appLockMutableLiveData.getValue() != null){
                            mainViewModel.insertLockApp(mainViewModel.appLockMutableLiveData.getValue());
                        }

                        Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
                    }

                } else {
                    Log.e("NGOCANH", "!firstOpen ChangeDrawFragment");

                    if (!forgetPass && !isSettingApp) {
                        if(isAdded())
                            Utils.showDialogSuccess(false, requireActivity());
//                        Toast.makeText(requireContext(), getString(R.string.successfulManipulation), Toast.LENGTH_SHORT).show();
                    }

                    if (forgetPass) {
                        Toast.makeText(requireContext(), getString(R.string.successfulManipulation), Toast.LENGTH_SHORT).show();

                        if (isLockApp) {
                            Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
                        } else {
                            requireActivity().finish();
                        }
                        Intent intent2 = new Intent(requireContext(), Accessibility.class);
                        intent2.putExtra(Constant.LOCK, true);
                        requireActivity().startService(intent2);

                    } else {
                        if (requireActivity() instanceof MainActivity) {
                            requireActivity().onBackPressed();
                        }
                    }
                }

            }

        }

        @Override
        public void onCleared() {
            //Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        Log.d("NGOCANH", "change draw onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("NGOCANH", "change draw onResume");
    }

    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_change_draw;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        theme = Utils.setTheme(requireContext());

        if (getArguments() != null) {
            firstOpen = getArguments().getBoolean(Constant.FIRST_OPEN);
            forgetPass = getArguments().getBoolean(Constant.FORGET_PASS);
            isLockApp = getArguments().getBoolean(Constant.IS_LOCK_APP);
            isSettingApp = getArguments().getBoolean(Constant.IS_SETTING_APP);

            getArguments().remove(Constant.FIRST_OPEN);
            getArguments().remove(Constant.FORGET_PASS);
            getArguments().remove(Constant.IS_LOCK_APP);
            getArguments().remove(Constant.IS_SETTING_APP);
        }

        if(savedInstanceState != null){
            Log.d("NGOCANH", "onCreatedView savedInstanceState");
            firstOpen = savedInstanceState.getBoolean(Constant.FIRST_OPEN);
            forgetPass = savedInstanceState.getBoolean(Constant.FORGET_PASS);
            isLockApp = savedInstanceState.getBoolean(Constant.IS_LOCK_APP);
            isSettingApp = savedInstanceState.getBoolean(Constant.IS_SETTING_APP);
        }


        initView();
        Utils.setScreenSize(requireActivity());

//        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), mBackPressedCallback);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(Constant.FIRST_OPEN, firstOpen);
        outState.putBoolean(Constant.FORGET_PASS, forgetPass);
        outState.putBoolean(Constant.IS_LOCK_APP, isLockApp);
        outState.putBoolean(Constant.IS_SETTING_APP, isSettingApp);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPermissionGranted() {

    }

    private void initView() {
        if (!theme.equals(Constant.THEME_DEFAULT))
            binding.vBackGround.setVisibility(View.INVISIBLE);

        if (new Utils().setBackGround(requireContext(), binding.layoutChangePattern)) {
            binding.vBackGround.setVisibility(View.INVISIBLE);
        }

        if (BACKGROUND_DEFAULT) {
            binding.vBackGround.setVisibility(View.VISIBLE);
        }

        if (isSettingApp) {
            binding.inHeader.tvTitle.setVisibility(View.GONE);

        } else {
            binding.inHeader.tvTitle.setText(getResources().getText(R.string.patternLockSetting));
        }

        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.GONE);


//        firstOpen = SharedPreferenceHelper.getBoolean(Constant.FIRST_OPEN, true);
        vibrate = SharedPreferenceHelper.getBoolean(Constant.VIBRATE, true);
        showDraw = SharedPreferenceHelper.getBoolean(Constant.SHOW_DRAWER, true);
        binding.layoutPattern.ChangeDrawPatternLock.addPatternLockListener(mPatternLockViewListener);
        if (!vibrate) {
            binding.layoutPattern.ChangeDrawPatternLock.setTactileFeedbackEnabled(false);
        }
        if (!showDraw) {
            binding.layoutPattern.ChangeDrawPatternLock.setInStealthMode(true);
        }

        if (firstOpen) {
            binding.vBackGround.setVisibility(View.INVISIBLE);
            binding.inHeader.getRoot().setVisibility(View.VISIBLE);
            binding.inHeader.tvTitle.setVisibility(View.INVISIBLE);
            binding.inHeader.imgIcSearch.setVisibility(View.INVISIBLE);
            binding.inHeader.imgIcHome.setVisibility(View.GONE);

            binding.layoutTypeAction.tvTypeAction.setText(getText(R.string.enterYourPassLock));
            binding.layoutTypeAction.tvDesTypeAction.setText(getText(R.string.youNeedToSetPassForFirstTime));
        }

        else if (isSettingApp) {
            binding.layoutTypeAction.tvDesTypeAction.setText(getText(R.string.forgetPass));
            binding.layoutTypeAction.tvDesTypeAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("NGOCANH", "onClick: is forget");
                }
            });
        }
        else {
//            binding.layoutTypeAction.getRoot().setVisibility(View.INVISIBLE);
            binding.layoutTypeAction.tvDesTypeAction.setText("");
            if(forgetPass){
                binding.inHeader.imgIcHome.setVisibility(View.GONE);
            }
        }

        evenClick();
    }

    private void evenClick() {
        binding.inHeader.imgIcHome.setOnClickListener(view -> {
            String type = (SharedPreferenceHelper.getStringWithDefault(Constant.TYPE_LOCK, Constant.PASS_CODE));

            if (firstOpen) {
                Navigation.findNavController(binding.getRoot()).popBackStack();
//                System.exit(0);
//                Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
            } else {
                if (isSettingApp) {
                    requireActivity().finish();
                } else {
                    if (requireActivity() instanceof MainActivity) {
                        requireActivity().onBackPressed();
                    }
                }

            }
        });
    }

    private void setClearPattern(final int millisecond) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stop) {
                    handler.postDelayed(this, millisecond);
                    binding.layoutPattern.ChangeDrawPatternLock.clearPattern();
                }
            }
        }, millisecond);
    }


}