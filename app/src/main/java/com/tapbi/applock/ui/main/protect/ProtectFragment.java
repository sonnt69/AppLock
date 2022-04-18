package com.tapbi.applock.ui.main.protect;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.tapbi.applock.common.Constant.MY_CAMERA_PERMISSION_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.Navigation;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.databinding.FragmentProtectBinding;
import com.tapbi.applock.databinding.ViewUnlockPopupBinding;
import com.tapbi.applock.interfaces.ResultDialog;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.ui.dialog.DialogPermissions;
import com.tapbi.applock.utils.Utils;

@SuppressWarnings("ALL")
public class ProtectFragment extends BaseBindingFragment<FragmentProtectBinding, MainViewModel> {
    private FingerprintManager.AuthenticationCallback authenticationCallback;
    private boolean lockNewApp;
    private String type;
    private boolean checkSwitch;
    private boolean checkPrint;
    private String passDraw = "";

    private PopupWindow myPopupWindow;
    private ViewUnlockPopupBinding popupBinding;
    private View viewPopUp;
    private FingerprintManager fingerprintManager;

    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_protect;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        passDraw = SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, "");
        boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
        if(!setQuestion)
            passDraw = "";

        popupBinding = ViewUnlockPopupBinding.inflate(getLayoutInflater());
        viewPopUp = popupBinding.getRoot();
        myPopupWindow = new PopupWindow(viewPopUp, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        myPopupWindow.setOutsideTouchable(true);
        myPopupWindow.setFocusable(true);
        myPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initView();
        initViewFinger();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    protected void onPermissionGranted() {

    }

    @Override
    public void onResume() {
        super.onResume();

        if (checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e("NGOCANH","onResume protect");
            SharedPreferenceHelper.storeBoolean(Constant.CATCH_INTRUDER, false);

            binding.swtCatchIntruder.setChecked(false);
        }

        initViewFinger();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        type = (SharedPreferenceHelper.getStringWithDefault(Constant.TYPE_LOCK, Constant.PATTERN_LOCK));

        if (type.equalsIgnoreCase(Constant.PATTERN_LOCK)) {
            binding.tvTypeUnlock.setText(getText(R.string.patternLock));
        } else {
            binding.tvTypeUnlock.setText(getText(R.string.passCode));
        }

        lockNewApp = SharedPreferenceHelper.getBoolean(Constant.LOCK_NEW_APP, false);
        binding.swtNewlyInstalledAppLock.setChecked(lockNewApp);

        eventClick();
    }



    private void initViewFinger() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
             fingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            if(fingerprintManager != null){
                if (!fingerprintManager.isHardwareDetected()) {
                    // Device doesn't support fingerprint authentication
                    binding.vFingerPrint.setVisibility(View.GONE);
                    binding.imgFingerPrint.setVisibility(View.GONE);
                    binding.tvFingerPrint.setVisibility(View.GONE);
                    binding.tvFingerPrintDes.setVisibility(View.GONE);
                    binding.swtFingerPrint.setVisibility(View.GONE);
                } else if (!fingerprintManager.hasEnrolledFingerprints()) {
//                    Log.e("NGOCANH", "not hasEnrolledFingerprints");
                    // User hasn't enrolled any fingerprints to authenticate with

                    SharedPreferenceHelper.storeBoolean(Constant.FINGER, false);
                    binding.swtFingerPrint.setClickable(false);
                    binding.swtFingerPrint.setChecked(false);

                    binding.vFingerPrint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           setClickDisable();
//                            checkClick();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    setClickEnable();
                                    binding.swtFingerPrint.setClickable(false);
                                }
                            }, 400);

                            initDialogAddFinger();
                        }
                    });
                } else {
//                    Log.e("NGOCANH", "hasEnrolledFingerprints");

                    // Everything is ready for fingerprint authentication
                    boolean fingerPrint = SharedPreferenceHelper.getBoolean(Constant.FINGER, false);
                    binding.swtFingerPrint.setChecked(fingerPrint);
                    binding.swtFingerPrint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                        if (!Utils.showDiaLogPermission2(getContext())) {
//                            binding.swtFingerPrint.setChecked(!binding.swtFingerPrint.isChecked());
//                        } else {
//                            SharedPreferenceHelper.storeBoolean(Constant.FINGER, b);
//                        }

                            binding.swtFingerPrint.setChecked(b);
                            SharedPreferenceHelper.storeBoolean(Constant.FINGER, b);
                        }
                    });

                    binding.vFingerPrint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkLongClick();

                            binding.swtFingerPrint.setChecked(!binding.swtFingerPrint.isChecked());
                        }
                    });
                }
            }
            else{
                // Device doesn't support fingerprint authentication
                binding.vFingerPrint.setVisibility(View.GONE);
                binding.imgFingerPrint.setVisibility(View.GONE);
                binding.tvFingerPrint.setVisibility(View.GONE);
                binding.tvFingerPrintDes.setVisibility(View.GONE);
                binding.swtFingerPrint.setVisibility(View.GONE);
            }

        } else {
           // initDialogAddFinger();
        }
    }




    private void eventClick() {
        binding.vCatchIntruder.setOnClickListener(view -> {
            checkLongClick();
            Navigation.findNavController(binding.getRoot()).navigate(R.id.catchIntruderFragment);
        });

        binding.vUnlockSetting.setOnClickListener(view -> {
            checkLongClick();
            Navigation.findNavController(binding.getRoot()).navigate(R.id.unlockSettingFragment);
        });


        binding.vSecuritySetting.setOnClickListener(view -> {
            checkLongClick();

            Navigation.findNavController(binding.getRoot()).navigate(R.id.settingQuestionFragment);
        });

        binding.vNewlyInstalledAppLock.setOnClickListener(view -> {
            binding.swtNewlyInstalledAppLock.setChecked(!binding.swtNewlyInstalledAppLock.isChecked());
        });

        binding.swtNewlyInstalledAppLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkPrint = binding.swtNewlyInstalledAppLock.isChecked();

                if (checkPrint) {
                    if (Utils.showDiaLogPermission(getContext())) {
                        Log.e("NGOCANH", "onCheckedChanged swtNewlyInstalledAppLock permission");
                        if (passDraw.equals("")) {
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(Constant.FIRST_OPEN, true);
                            Navigation.findNavController(binding.getRoot()).navigate(R.id.changeDrawFragment, bundle);
                        }
                        else SharedPreferenceHelper.storeBoolean(Constant.LOCK_NEW_APP, b);
                    } else {
                        Log.e("NGOCANH", "onCheckedChanged swtNewlyInstalledAppLock not permission");
                        binding.swtNewlyInstalledAppLock.setChecked(!binding.swtNewlyInstalledAppLock.isChecked());
                    }
                }
            }
        });


        binding.imgTypeUnlock.setOnClickListener(view1 -> {
            checkLongClick();
            setMyPopupWindow(view1);
        });

        catchIntruder();
    }



    private void setMyPopupWindow(View v) {

        //check dismiss

        //check type
        String s = SharedPreferenceHelper.getString(Constant.TYPE_LOCK);
        if (s.equals(Constant.PASS_CODE)) {
            popupBinding.ivPassCode.setVisibility(View.VISIBLE);
            popupBinding.ivPatternLock.setVisibility(View.GONE);
        } else {
            popupBinding.ivPassCode.setVisibility(View.GONE);
            popupBinding.ivPatternLock.setVisibility(View.VISIBLE);
        }

        popupBinding.vPassCode.setOnClickListener(view1 -> {
            String passcode = (SharedPreferenceHelper.getStringWithDefault(Constant.PASS_CODE, ""));
            if (!passcode.equals("")) {
                myPopupWindow.dismiss();
                myPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                binding.tvTypeUnlock.setText(popupBinding.tvPassCode.getText().toString());
                SharedPreferenceHelper.storeString(Constant.TYPE_LOCK, Constant.PASS_CODE);
            } else {
                Toast.makeText(getContext(), getText(R.string.pleaseSetPassCode), Toast.LENGTH_SHORT).show();
                myPopupWindow.dismiss();
                myPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                Bundle bundle = new Bundle();
                bundle.putBoolean(Constant.PASS_CODE, true);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.changePassCodeFragment, bundle);
            }


        });
        popupBinding.vPatternLock.setOnClickListener(view1 -> {
            myPopupWindow.dismiss();
            myPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            binding.tvTypeUnlock.setText(popupBinding.tvPatternLock.getText().toString());
            SharedPreferenceHelper.storeString(Constant.TYPE_LOCK, Constant.PATTERN_LOCK);
        });

        myPopupWindow.showAsDropDown(v, -30, 0);
    }

    private void initDialogAddFinger() {
        DialogPermissions customDialogPermissions = new DialogPermissions(getContext(), getString(R.string.cancel), getString(R.string.ok), getString(R.string.addFinger), R.drawable.ic_notification, new ResultDialog() {
            @Override
            public void result(boolean b) {
                if (b) {
                    startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
                }
                binding.swtFingerPrint.setChecked(false);

            }
        });
        customDialogPermissions.showDialog();
    }

    private void catchIntruder() {
        boolean takePhotoTheif = SharedPreferenceHelper.getBoolean(Constant.CATCH_INTRUDER, false);
        binding.swtCatchIntruder.setChecked(takePhotoTheif);

        binding.swtCatchIntruder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSwitch = binding.swtCatchIntruder.isChecked();

                if (checkSwitch) {
                    setClickDisable();

                    binding.swtCatchIntruder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setClickEnable();
                        }
                    }, 300);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                        } else {
                            SharedPreferenceHelper.storeBoolean(Constant.CATCH_INTRUDER, b);
                        }
                    } else {
                        SharedPreferenceHelper.storeBoolean(Constant.CATCH_INTRUDER, b);
                    }
                } else {
                    return;
                }
            }
        });
    }

    private void setClickDisable() {
        binding.swtCatchIntruder.setClickable(false);
        binding.vCatchIntruder.setClickable(false);
        binding.vFingerPrint.setClickable(false);
        binding.swtFingerPrint.setClickable(false);
        binding.vSecuritySetting.setClickable(false);
        binding.vUnlockSetting.setClickable(false);
        binding.imgTypeUnlock.setClickable(false);
        binding.vSecuritySetting.setClickable(false);
    }

    private void setClickEnable(){
        binding.swtCatchIntruder.setClickable(true);
        binding.vCatchIntruder.setClickable(true);
        binding.vFingerPrint.setClickable(true);
        binding.swtFingerPrint.setClickable(true);
        binding.vSecuritySetting.setClickable(true);
        binding.vUnlockSetting.setClickable(true);
        binding.imgTypeUnlock.setClickable(true);
        binding.vSecuritySetting.setClickable(true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("NGOCANH", "onRequestPermissionsResult ok");
                SharedPreferenceHelper.storeBoolean(Constant.CATCH_INTRUDER, true);

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                    // now, user has denied permission (but not permanently!)
                    SharedPreferenceHelper.storeBoolean(Constant.CATCH_INTRUDER, false);

                    binding.swtCatchIntruder.setChecked(false);
                    Log.e("NGOCANH", "onRequestPermissionsResult not ok");

                } else {
//                    SharedPreferenceHelper.storeBoolean(Constant.CATCH_INTRUDER, false);
//
//                    binding.swtCatchIntruder.setChecked(false);
                    Log.e("NGOCANH", "onRequestPermissionsResult permanently not ok");
                    Utils.showDialogPermission(requireContext());
                }
            }
        }
    }

}
