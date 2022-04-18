package com.tapbi.applock.ui.main.changepass;

import static com.tapbi.applock.utils.Utils.BACKGROUND_DEFAULT;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.databinding.FragmentChangePassCodeBinding;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;


public class ChangePassCodeFragment extends BaseBindingFragment<FragmentChangePassCodeBinding, MainViewModel> {
    //    private SharedPreferences sharedPreferences;
    private boolean vibrate;
    private int ordinal = 0;
    private boolean allowClick = true;
    private boolean checkChangePass = false;
    private String theme = "";
    private Drawable ic_dot;
    private Drawable ic_dot_error;
    private Drawable bg_path;
    private Drawable bg_path_error;
    private boolean checkFirstSet;
    private String pass = "";
    private String pass1 = "";
    private boolean checkPass = true;

    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_change_pass_code;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
//        StatusBarUtil.setTransparent(requireActivity());

        theme = Utils.setTheme(requireContext());
        Log.e("NGOCANH", "ChangePassCodeFragment theme: " + theme);

        if (getArguments() != null) {
            checkFirstSet = getArguments().getBoolean(Constant.PASS_CODE);
            getArguments().remove(Constant.PASS_CODE);
        }

        if(savedInstanceState != null){
            checkFirstSet = savedInstanceState.getBoolean(Constant.PASS_CODE);
        }

        initView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(Constant.PASS_CODE, checkFirstSet);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPermissionGranted() {

    }

    private void initView() {
        if (!theme.equals(Constant.THEME_DEFAULT))
            binding.vBackGround.setVisibility(View.INVISIBLE);

        if (new Utils().setBackGround(requireContext(), binding.layoutChangePassCode)) {
            binding.vBackGround.setVisibility(View.INVISIBLE);
        }

        if (BACKGROUND_DEFAULT) {
            binding.vBackGround.setVisibility(View.VISIBLE);
        }

        TypedArray typedArray = requireActivity().obtainStyledAttributes(0, R.styleable.theme);
        ic_dot = typedArray.getDrawable(R.styleable.theme_dot_input_passCode);
        bg_path = typedArray.getDrawable(R.styleable.theme_bg_path_input_passCode);
        ic_dot_error = typedArray.getDrawable(R.styleable.theme_dot_input_passCode_error);
        bg_path_error = typedArray.getDrawable(R.styleable.theme_bg_path_input_passCode_error);


        vibrate = SharedPreferenceHelper.getBoolean(Constant.VIBRATE, true);

        binding.inHeader.tvTitle.setText(getResources().getText(R.string.passCodeSetting));
        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.GONE);
        binding.layoutTypeAction.tvDesTypeAction.setText("");

        evenClick();
    }



    private void evenClick() {

        binding.inKeyBoardPassCode.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ordinal > 0) {
                    --ordinal;

                    switch (ordinal) {
                        case 0:
                            setTextClearPass();
                            break;
                        case 1:
                            setTextClear2();
                            break;
                        case 2:
                            setTextClear3();
                            break;
                        case 3:
                            setTextClear4();
                            break;
                    }
                    if (checkChangePass) {
                        pass1 = Utils.removeLastChar(pass1);
                    } else {
                        pass = Utils.removeLastChar(pass);
                    }
                }
                else{
                    setTextClearPass();
                    ordinal = 0;
                    if (checkChangePass) {
                        pass1 = "";
                    } else {
                        pass = "";
                    }
                }
                
//                if (requireActivity() instanceof MainActivity) {
//                    requireActivity().onBackPressed();
//                }
            }
        });

        binding.inHeader.imgIcHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requireActivity() instanceof MainActivity) {
                    requireActivity().onBackPressed();
                }
            }
        });

        binding.inKeyBoardPassCode.btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                setTextClearPass();
                ordinal = 0;
                if (checkChangePass) {
                    pass1 = "";
                } else {
                    pass = "";
                }
            }
        });

        binding.inKeyBoardPassCode.btnNumberOne.setOnClickListener(view -> setPass("1"));
        binding.inKeyBoardPassCode.btnNumberTwo.setOnClickListener(view -> setPass("2"));
        binding.inKeyBoardPassCode.btnNumberThree.setOnClickListener(view -> setPass("3"));
        binding.inKeyBoardPassCode.btnNumberFour.setOnClickListener(view -> setPass("4"));
        binding.inKeyBoardPassCode.btnNumberFive.setOnClickListener(view -> setPass("5"));
        binding.inKeyBoardPassCode.btnNumberSix.setOnClickListener(view -> setPass("6"));
        binding.inKeyBoardPassCode.btnNumberSeven.setOnClickListener(view -> setPass("7"));
        binding.inKeyBoardPassCode.btnNumberEight.setOnClickListener(view -> setPass("8"));
        binding.inKeyBoardPassCode.btnNumberNine.setOnClickListener(view -> setPass("9"));
        binding.inKeyBoardPassCode.btnNumberZero.setOnClickListener(view -> setPass("0"));


    }

    private void setPass(String number) {
        if (allowClick) {
            if (vibrate) {
                Vibrator v = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
                assert v != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(100);
                }
            }
            if (checkChangePass) {
                pass1 = pass1 + number;
            } else {
                pass = pass + number;
            }
            ++ordinal;
            if (ordinal == 1) {
                binding.inInputPass.imgPassOne.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassOne);
            } else if (ordinal == 2) {
                binding.inInputPass.imgPassTwo.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassTwo);
            } else if (ordinal == 3) {
                binding.inInputPass.imgPassThree.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassThree);
            } else {
                binding.inInputPass.imgPassFour.setVisibility(View.VISIBLE);
                setBackGroundInputPass(binding.inInputPass.bgPassFour);

                allowClick = false;
                if (checkChangePass) {
                    if (pass.equals(pass1)) {

                        SharedPreferenceHelper.storeString(Constant.PASS_CODE, pass);
                        Utils.showDialogSuccess(false, requireActivity());

                        if (checkFirstSet) {
                            SharedPreferenceHelper.storeString(Constant.TYPE_LOCK, Constant.PASS_CODE);
                        }

                        if (requireActivity() instanceof MainActivity) {
                            requireActivity().onBackPressed();
                        }

                    } else {
                        setErrorPass();
                        checkChangePass();
                    }
                } else {
                    checkChangePass();
                }
            }
        }
    }

    private void setErrorPass() {
        binding.layoutTypeAction.tvDesTypeAction.setText(R.string.wrong_pass);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.layoutTypeAction.tvDesTypeAction.setTextColor(requireActivity().getColor(R.color.color_FB5A07));
        }

        binding.layoutTypeAction.tvDesTypeAction.postDelayed(() -> {
            binding.layoutTypeAction.tvDesTypeAction.setText(R.string.confirmPassWord);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.layoutTypeAction.tvDesTypeAction.setTextColor(requireActivity().getColor(R.color.color_777777));
            }
        }, 500);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                binding.layoutTypeAction.tvDesTypeAction.setText(R.string.confirmPassWord);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    binding.layoutTypeAction.tvDesTypeAction.setTextColor(requireActivity().getColor(R.color.color_777777));
//                }
//            }
//        }, 500);

        pass1 = "";
        binding.inInputPass.imgPassOne.setImageDrawable(ic_dot_error);
        binding.inInputPass.imgPassTwo.setImageDrawable(ic_dot_error);
        binding.inInputPass.imgPassThree.setImageDrawable(ic_dot_error);
        binding.inInputPass.imgPassFour.setImageDrawable(ic_dot_error);
        binding.inInputPass.bgPassOne.setImageDrawable(bg_path_error);
        binding.inInputPass.bgPassTwo.setImageDrawable(bg_path_error);
        binding.inInputPass.bgPassThree.setImageDrawable(bg_path_error);
        binding.inInputPass.bgPassFour.setImageDrawable(bg_path_error);
    }


    private void checkChangePass() {
        checkChangePass = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                allowClick = true;
//                setBackGroundInputPass(binding.inInputPass.bgPassOne);
                if (ordinal == 4) {
                    binding.layoutTypeAction.tvDesTypeAction.setText(getText(R.string.confirmPassWord));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.layoutTypeAction.tvDesTypeAction.setTextColor(requireActivity().getColor(R.color.color_777777));
                    }

                    ordinal = 0;
                    setTextClearPass();
                }
            }
        }, 300);
    }

    private void setTextClear2(){
        binding.inInputPass.imgPassTwo.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassTwo.setVisibility(View.INVISIBLE);
        binding.inInputPass.bgPassTwo.setImageResource(R.drawable.bg_path_white);
    }
    private void setTextClear3(){
        binding.inInputPass.imgPassThree.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassThree.setVisibility(View.INVISIBLE);
        binding.inInputPass.bgPassThree.setImageResource(R.drawable.bg_path_white);
    }
    private void setTextClear4(){
        binding.inInputPass.imgPassFour.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassFour.setVisibility(View.INVISIBLE);
        binding.inInputPass.bgPassFour.setImageResource(R.drawable.bg_path_white);
    }

    private void setTextClearPass() {
        binding.inInputPass.imgPassOne.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassTwo.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassThree.setImageDrawable(ic_dot);
        binding.inInputPass.imgPassFour.setImageDrawable(ic_dot);

        binding.inInputPass.imgPassOne.setVisibility(View.INVISIBLE);
        binding.inInputPass.imgPassTwo.setVisibility(View.INVISIBLE);
        binding.inInputPass.imgPassThree.setVisibility(View.INVISIBLE);
        binding.inInputPass.imgPassFour.setVisibility(View.INVISIBLE);

        binding.inInputPass.bgPassOne.setImageResource(R.drawable.bg_path_white);
        binding.inInputPass.bgPassTwo.setImageResource(R.drawable.bg_path_white);
        binding.inInputPass.bgPassThree.setImageResource(R.drawable.bg_path_white);
        binding.inInputPass.bgPassFour.setImageResource(R.drawable.bg_path_white);
    }

    private void setBackGroundInputPass(ImageView imageView) {
        imageView.setImageDrawable(bg_path);
    }

}