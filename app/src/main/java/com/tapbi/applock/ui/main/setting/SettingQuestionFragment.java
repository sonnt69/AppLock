package com.tapbi.applock.ui.main.setting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.DialogAnswerIsNotCorrectBinding;
import com.tapbi.applock.databinding.FragmentSettingQuestionBinding;
import com.tapbi.applock.service.Accessibility;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.Objects;

import timber.log.Timber;

public class SettingQuestionFragment extends BaseBindingFragment<FragmentSettingQuestionBinding, MainViewModel> {
    private long lastClickTime = 0;
    private boolean isLockApp;
    private boolean save = true;
    private String loverName;
    private String birthDay;
    private String email;
    private boolean forgetPass = false;
    private boolean firstOpen;
    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.e("NGOCANH", "setting fragment handleOnBackPressed: ");

            if (firstOpen) {
                mainViewModel.setPassEvent.postValue(true);
                String s = mainViewModel.checkFragment.getValue();
                if(s.equals(Constant.SEARCH_FRAGMENT)){
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.searchFragment);
                }
                else Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);

            } else if (forgetPass) {
                if (isLockApp) {
                    isLockApp = false;
                    Log.e("NGOCANH", "handleOnBackPressed forgetPass isLockApp");
                    startActivity(Intent.makeRestartActivityTask(getActivity().getIntent().getComponent()));
                } else {
                    Log.e("NGOCANH", "handleOnBackPressed forgetPass not isLockApp");
                    requireActivity().finish();
                }

                Intent intent2 = new Intent(requireContext(), Accessibility.class);
                intent2.putExtra(Constant.LOCK, true);
                requireActivity().startService(intent2);
            } else {
                Log.e("NGOCANH", "setting fragment handleOnBackPressed: not forgetPass ");
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        }
    };
    private String loverNameInput;
    private String birthDayInput;
    private String emailInput;
    private int day = 1;
    private int month = 1;
    private int yearDatePicker = 2000;
    private boolean showDate;
    private boolean forgetToChangeDraw;


    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_setting_question;
    }

    @Override
    public void onPause() {
        super.onPause();

        Utils.closeKeyboard(requireContext(), getView());

        Log.d("NgocAnh", "onPause setting: ");
        if (forgetPass && !forgetToChangeDraw) {
            if (isLockApp) {
                Log.d("NgocAnh", "onPause: forgetPass");

                Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
            } else {
                ((MainActivity) requireActivity()).changeMainScreen(R.id.homeFragment, null);
//                    requireActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        EventBus.getDefault().post(new EventLock(Constant.CHECK_QUESTION));

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        requireActivity().getWindow().setBackgroundDrawableResource(R.drawable.bg_drawer) ;


        if (getArguments() != null) {
            isLockApp = getArguments().getBoolean(Constant.IS_LOCK_APP);
            firstOpen = getArguments().getBoolean(Constant.FIRST_OPEN);
            forgetPass = getArguments().getBoolean(Constant.FORGET_PASS);

            getArguments().remove(Constant.IS_LOCK_APP);
            getArguments().remove(Constant.FIRST_OPEN);
            getArguments().remove(Constant.FORGET_PASS);

        }

        if(savedInstanceState != null){
            Log.d("NGOCANH", "onCreatedView savedInstanceState");
            firstOpen = savedInstanceState.getBoolean(Constant.FIRST_OPEN);
            forgetPass = savedInstanceState.getBoolean(Constant.FORGET_PASS);
            isLockApp = savedInstanceState.getBoolean(Constant.IS_LOCK_APP);
        }


        initView();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), mBackPressedCallback);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(Constant.FIRST_OPEN, firstOpen);
        outState.putBoolean(Constant.FORGET_PASS, forgetPass);
        outState.putBoolean(Constant.IS_LOCK_APP, isLockApp);

        super.onSaveInstanceState(outState);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        if (forgetPass) {
            binding.inHeader.imgIcHome.setVisibility(View.INVISIBLE);
            binding.inHeader.tvTitle.setText(getResources().getText(R.string.inputQuestionToRetrievePass));
//            binding.vBackGround.setVisibility(View.GONE);
            binding.inBtn.getRoot().setVisibility(View.GONE);
            binding.btnConfirm.setVisibility(View.VISIBLE);
        } else if (firstOpen) {
//            firstOpen = true;

            binding.btnConfirm.setText(getResources().getText(R.string.save));
            binding.btnConfirm.setVisibility(View.VISIBLE);
            binding.inBtn.getRoot().setVisibility(View.GONE);
            binding.tvDes.setVisibility(View.GONE);
            binding.inHeader.tvTitle.setText(getResources().getText(R.string.setquestion));
            binding.inHeader.imgIcHome.setVisibility(View.INVISIBLE);
        } else {
            binding.btnConfirm.setVisibility(View.GONE);
            binding.tvDes.setVisibility(View.GONE);
            binding.inHeader.tvTitle.setText(getResources().getText(R.string.setquestion));
            binding.inBtn.getRoot().setVisibility(View.VISIBLE);
        }

        Log.e("NGOCANH", "SettingQuestionFragment forget = " + forgetPass);

        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);

        binding.inBtn.btnLeft.setText(getText(R.string.save));
        binding.inBtn.btnRight.setText(getText(R.string.cancel));
        binding.inBtn.btnLeft.setBackground(requireActivity().getDrawable(R.drawable.state_btn_green));
        binding.inBtn.btnRight.setBackground(requireActivity().getDrawable(R.drawable.state_btn_black));
        binding.inBtn.btnLeft.setTextColor(getResources().getColor(R.color.colorWhite));
        binding.inBtn.btnRight.setTextColor(getResources().getColor(R.color.color_777777));

        loverName = SharedPreferenceHelper.getStringWithDefault(Constant.LOVER_NAME, "");
        birthDay = SharedPreferenceHelper.getStringWithDefault(Constant.BIRTHDAY, "");
        email = SharedPreferenceHelper.getStringWithDefault(Constant.EMAIL, "");

        if (!forgetPass) {
            binding.SetQuesAvEdtEmail.setText(email);
            binding.SetQuesAvEdtDateOfBirth.setText(birthDay);
            binding.SetQuesAvEdtNameLover.setText(loverName);

            yearDatePicker = SharedPreferenceHelper.getIntWithDefault(Constant.YEAR, 2000);
            month = SharedPreferenceHelper.getIntWithDefault(Constant.MONTH, 2);
            day = SharedPreferenceHelper.getIntWithDefault(Constant.DAY, 1);

            Timber.e("asdasdasdasdasdasdasdasd");
        }


        evenClick();
    }

    private void evenClick() {

        binding.SetQuesAvEdtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    binding.SetQuesAvEdtEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.SetQuesAvEdtNameLover.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    binding.SetQuesAvEdtNameLover.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.inHeader.imgIcHome.setOnClickListener(view -> {
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });

        binding.SetQuesAvEdtDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkClick();
                binding.SetQuesAvEdtDateOfBirth.setClickable(false);
                binding.SetQuesAvEdtDateOfBirth.setEnabled(false);

                showDatePicker();

            }
        });

        binding.inBtn.btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveQuestion();
            }
        });

        binding.btnConfirm.setOnClickListener(view -> {
            if(firstOpen)
                saveQuestion();
            else checkQuestion();
        });

        binding.inBtn.btnRight.setOnClickListener(view -> {
                    if (firstOpen) {
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
                    } else {
                        Navigation.findNavController(binding.getRoot()).popBackStack();
                    }
                }
        );
    }

    private void saveQuestion() {
        checkDate();
        checkLoverName();
        checkEmail();
        if (save) {
            SharedPreferenceHelper.storeString(Constant.BIRTHDAY, birthDayInput);
            SharedPreferenceHelper.storeString(Constant.LOVER_NAME, loverNameInput);
            SharedPreferenceHelper.storeString(Constant.EMAIL, emailInput);

            Toast.makeText(requireActivity(), R.string.inputQuestionSuccess, Toast.LENGTH_SHORT).show();

            SharedPreferenceHelper.storeInt(Constant.DAY, day);
            SharedPreferenceHelper.storeInt(Constant.MONTH, month);
            SharedPreferenceHelper.storeInt(Constant.YEAR, yearDatePicker);

            SharedPreferenceHelper.storeBoolean(Constant.SET_QUESTION_SETTING, true);

            if(firstOpen){
                Log.e("NGOCANH", "checkQuestion checkQuestionAnswer firstOpen");
                AppLock appLock3 = new AppLock();
                appLock3.setAppPackage("com.tapbi.applock");
                mainViewModel.insertLockApp(appLock3);

                //lock app
                if(mainViewModel.appLockMutableLiveData.getValue() != null){
                    mainViewModel.insertLockApp(mainViewModel.appLockMutableLiveData.getValue());
                }
            }

            if (firstOpen) {
                mainViewModel.setPassEvent.postValue(true);
                String s = mainViewModel.checkFragment.getValue();
                if(s.equals(Constant.SEARCH_FRAGMENT)){
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.searchFragment);
                }
                else Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
            } else {
                if (requireActivity() instanceof MainActivity) {
                    requireActivity().onBackPressed();
                }
            }

        } else {
            Toast.makeText(requireActivity(), R.string.inputQuestionEr, Toast.LENGTH_SHORT).show();
            save = true;
        }
    }


    private void checkDate() {
        birthDayInput = binding.SetQuesAvEdtDateOfBirth.getText().toString().trim();
        if (birthDayInput.isEmpty()) {
            binding.SetQuesAvEdtDateOfBirth.setError(getText(R.string.erroEmpty));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.SetQuesAvEdtDateOfBirth.setError(null);
                }
            }, 1000);

            save = false;
        }

    }

    private void checkAnimator(){

    }

    private void checkClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 5000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    private void checkLoverName() {
        loverNameInput = binding.SetQuesAvEdtNameLover.getText().toString().trim();
        if (loverNameInput.isEmpty()) {
            binding.SetQuesAvEdtNameLover.setError(getText(R.string.erroEmptyLover));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.SetQuesAvEdtNameLover.setError(null);
                }
            }, 1000);

            save = false;
        } else {
            binding.SetQuesAvEdtNameLover.setError(null);
        }
    }

    private void checkEmail() {
        emailInput = binding.SetQuesAvEdtEmail.getText().toString().trim();
        if (!Utils.isEmail(emailInput)) {
            binding.SetQuesAvEdtEmail.setError(getText(R.string.errorEmail));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.SetQuesAvEdtEmail.setError(null);
                }
            }, 1000);


            save = false;
        } else {
            binding.SetQuesAvEdtEmail.setError(null);
        }
    }


    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(requireActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // Display Selected date in textbox
                        binding.SetQuesAvEdtDateOfBirth.setText(dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year);

                        binding.SetQuesAvEdtDateOfBirth.setError(null);

                        yearDatePicker = year;
                        month = monthOfYear + 1;
                        day = dayOfMonth;
                    }
                }, mYear, mMonth, mDay);


        dpd.show();

        if (dpd.isShowing()) {
            binding.SetQuesAvEdtDateOfBirth.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.SetQuesAvEdtDateOfBirth.setClickable(true);
                    binding.SetQuesAvEdtDateOfBirth.setEnabled(true);
                }
            }, 800);
        }

        Timber.e(yearDatePicker + "");
        Timber.e(month + "");
        Timber.e(day + "");
        dpd.updateDate(yearDatePicker, month - 1, day);

    }

    private void checkQuestion() {
        checkDate();
        checkLoverName();
        checkEmail();
        if (save) {
            if (checkQuestionAnswer()) {
                if (forgetPass) {
                    forgetToChangeDraw = true;

                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constant.FORGET_PASS, true);
                    bundle.putBoolean(Constant.IS_LOCK_APP, isLockApp);
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.changeDrawFragment, bundle);

//                    if(isLockApp){
//                        Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
//                    }
//                    else{
//                        requireActivity().finish();
//                    }
                } else if (!firstOpen) {
//                    SharedPreferenceHelper.storeBoolean(Constant.SET_QUESTION_SETTING, true);
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.homeFragment);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                DialogAnswerIsNotCorrectBinding dialogBinding = DialogAnswerIsNotCorrectBinding.inflate(getLayoutInflater());
                builder.setView(dialogBinding.getRoot());
                AlertDialog alertDialog = builder.create();
                Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        } else {
            Toast.makeText(requireActivity(), R.string.inputQuestionEr, Toast.LENGTH_SHORT).show();
            save = true;
        }
    }

    private boolean checkQuestionAnswer() {
        if (loverNameInput.equalsIgnoreCase(loverName) && birthDayInput.equalsIgnoreCase(birthDay) && emailInput.equalsIgnoreCase(email)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPermissionGranted() {

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}