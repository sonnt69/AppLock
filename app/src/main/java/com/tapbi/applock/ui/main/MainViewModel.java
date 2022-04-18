package com.tapbi.applock.ui.main;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tapbi.applock.common.LiveEvent;
import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.repository.AppLockRepository;
import com.tapbi.applock.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class MainViewModel extends BaseViewModel {
    public MutableLiveData<ArrayList<String>> importantApps = new MutableLiveData<>();
    public LiveEvent<List<AppLock>> lockAppLiveEvent = new LiveEvent<>();
    public MutableLiveData<ArrayList<AppInfo>> infoApps = new MutableLiveData<>();
    public LiveEvent<Boolean> setPassEvent = new LiveEvent<>();
    public MutableLiveData<AppLock> appLockMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<String> checkFragment = new MutableLiveData<>();

    private AppLockRepository appLockRepository;

    @Inject
    public MainViewModel(AppLockRepository appLockRepository) {
        this.appLockRepository = appLockRepository;
    }

    public void insertLockApp(AppLock appLocks) {
        appLockRepository.saveLockApp(appLocks);
    }

    public void deleteAppNotExist(Activity activity){
        appLockRepository.deleteAppNotExist(activity);
    }

    public void deleteLockApp(AppLock appLock) {
        appLockRepository.deleteLockApp(appLock);
    }

    public void deleteLockAppByName(String appPackage) {
        appLockRepository.deleteLockAppByName(appPackage);
    }

    public LiveData<List<AppLock>> getLockedApps() {
        return appLockRepository.getLockedApps();
    }

    public LiveData<Integer> getCountLockedApps() {
        return appLockRepository.getCountLockedApp();
    }


    public void insertLockApp(AppLock... appLocks) {
        appLockRepository.insertLockApp(appLocks);
    }

    public void getAppsInfo(Activity activity) {
        appLockRepository.getListAppsInfo(activity).subscribe(new SingleObserver<ArrayList<AppInfo>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(@NonNull ArrayList<AppInfo> appInfos) {
                Log.e("NGOCANH", "size app: "+appInfos.size());
                Log.d("HaiPd", "onSuccess: "+appInfos.size());
                infoApps.postValue(appInfos);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }
}
