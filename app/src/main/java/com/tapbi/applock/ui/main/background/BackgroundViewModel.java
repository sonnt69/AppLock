package com.tapbi.applock.ui.main.background;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

import com.tapbi.applock.common.LiveEvent;
import com.tapbi.applock.common.model.BackGround;
import com.tapbi.applock.data.repository.AppLockRepository;
import com.tapbi.applock.ui.base.BaseViewModel;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class BackgroundViewModel extends BaseViewModel {
    public LiveEvent<ArrayList<BackGround>> listBackground = new LiveEvent<>();
    private AppLockRepository appLockRepository;

    @Inject
    public BackgroundViewModel(AppLockRepository appLockRepository) {
        this.appLockRepository = appLockRepository;
    }

    public void getListBackground(Context context, String folder) {
        appLockRepository.getListBackground(context, folder).subscribe(new SingleObserver<ArrayList<BackGround>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(@NonNull ArrayList<BackGround> backGrounds) {
                listBackground.postValue(backGrounds);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

}
