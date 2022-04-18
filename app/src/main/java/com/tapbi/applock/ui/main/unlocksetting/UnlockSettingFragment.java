package com.tapbi.applock.ui.main.unlocksetting;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.navigation.Navigation;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.databinding.FragmentUnlockSettingBinding;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;


public class UnlockSettingFragment extends BaseBindingFragment<FragmentUnlockSettingBinding, MainViewModel> {
    private boolean vibrate = true;
    private boolean showDraw = true;

    @Override
    protected Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_unlock_setting;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        //StatusBarUtil.setTransparent(requireActivity());
        Utils.setTheme(requireContext());

        initView();
    }

    private void initView() {
        binding.inHeader.tvTitle.setText(getResources().getText(R.string.unlockSetting));
        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.GONE);
        vibrate = SharedPreferenceHelper.getBoolean(Constant.VIBRATE, true);
        showDraw = SharedPreferenceHelper.getBoolean(Constant.SHOW_DRAWER, true);
        binding.swtVibration.setChecked(vibrate);
        binding.swtStyleDisplay.setChecked(showDraw);

        evenClick();
    }

    private void evenClick() {

        binding.inHeader.imgIcHome.setOnClickListener(view -> {
            if (requireActivity() instanceof MainActivity) {
                requireActivity().onBackPressed();
            }
        });

        binding.vPatternLockSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.changeDrawFragment);
            }
        });

        binding.vPassCodeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.changePassCodeFragment);
            }
        });

        binding.vVibration.setOnClickListener(view -> {
            binding.swtVibration.setChecked(!binding.swtVibration.isChecked());
        });
        binding.vStyleDisplay.setOnClickListener(view -> {
            binding.swtStyleDisplay.setChecked(!binding.swtStyleDisplay.isChecked());
        });

        binding.swtVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferenceHelper.storeBoolean(Constant.VIBRATE, b);

            }
        });
        binding.swtStyleDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferenceHelper.storeBoolean(Constant.SHOW_DRAWER, b);
            }
        });
    }

    @Override
    protected void onPermissionGranted() {

    }
}