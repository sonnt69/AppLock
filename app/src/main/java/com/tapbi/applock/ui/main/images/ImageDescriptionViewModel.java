package com.tapbi.applock.ui.main.images;

import android.content.Context;
import android.view.View;

import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.data.repository.AppLockRepository;
import com.tapbi.applock.ui.base.BaseViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ImageDescriptionViewModel extends BaseViewModel {
    private AppLockRepository appLockRepository;

    @Inject
    public ImageDescriptionViewModel(AppLockRepository appLockRepository){
        this.appLockRepository = appLockRepository;
    }

    public void deleteImage(ImageThief imageThief){
        appLockRepository.deleteImageThief(imageThief);
    }

    public void saveImageToStorage(Context context, View view, ImageThief thief){
        appLockRepository.saveImgToStorage(context, view, thief);
    }
}
