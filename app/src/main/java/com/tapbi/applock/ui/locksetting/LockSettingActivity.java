package com.tapbi.applock.ui.locksetting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.databinding.ActivityLockSettingBinding;
import com.tapbi.applock.interfaces.OnBackPressed;
import com.tapbi.applock.ui.base.BaseBindingActivity;
import com.tapbi.applock.ui.main.MainViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class LockSettingActivity extends BaseBindingActivity<ActivityLockSettingBinding, MainViewModel> {

    @Override
    public int getLayoutId() {
        return R.layout.activity_lock_setting;
    }

    @Override
    public Class<MainViewModel> getViewModel() {
        return MainViewModel.class;
    }

    @Override
    public void setupView(Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra(Constant.IS_SETTING_APP)) {
                intent.removeExtra(Constant.IS_SETTING_APP);

            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("NgocAnh", "onDestroy: ");
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void setupData() {

    }

    @Subscribe(sticky = true)
    public void onEventLock(EventLock event) {
        Log.d("NgocAnh", "onEventLock: lockstting");
        if (event.getType().equals(Constant.GO_TO_SETTING))
            finish();
    }
    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new EventLock(Constant.EXIT));
        //finishAffinity();
    }
}