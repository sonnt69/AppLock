package com.tapbi.applock.ui.main.home;

import androidx.lifecycle.MutableLiveData;

import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.repository.AppLockRepository;
import com.tapbi.applock.ui.base.BaseViewModel;

import java.util.List;

import javax.inject.Inject;

public class HomeViewModel extends BaseViewModel {
    private AppLockRepository appLockRepository;
    public MutableLiveData<List<AppLock>> appLock = new MutableLiveData<>();

    @Inject
    public HomeViewModel(){

    }
}
