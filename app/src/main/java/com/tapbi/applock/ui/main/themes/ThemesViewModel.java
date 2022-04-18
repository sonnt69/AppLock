package com.tapbi.applock.ui.main.themes;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.tapbi.applock.common.model.Theme;
import com.tapbi.applock.data.repository.AppLockRepository;
import com.tapbi.applock.ui.base.BaseViewModel;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class ThemesViewModel extends BaseViewModel {
    private AppLockRepository appLockRepository;
    public MutableLiveData<ArrayList<Theme>> themeLiveData = new MutableLiveData<>();

    @Inject
    public ThemesViewModel(AppLockRepository appLockRepository) {
        this.appLockRepository = appLockRepository;
    }

    public void getListTheme(Context context, String folder){
        appLockRepository.getListTheme(context, folder).subscribe(new SingleObserver<ArrayList<Theme>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(@NonNull ArrayList<Theme> themes) {
                themeLiveData.postValue(themes);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }
}
