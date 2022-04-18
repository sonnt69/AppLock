package com.tapbi.applock.ui.main.catchintruder;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.data.repository.AppLockRepository;
import com.tapbi.applock.ui.base.BaseViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CatchIntruderViewModel extends BaseViewModel {
//    public MutableLiveData<List<ImageThief>> imageList = new MutableLiveData<>();
    private AppLockRepository appLockRepository;

    @Inject
    public CatchIntruderViewModel(AppLockRepository appLockRepository){
        this.appLockRepository = appLockRepository;
    }

    public LiveData<List<ImageThief>> getImageThiefInDB() {
        return appLockRepository.getImageThief();
    }

    public void deleteImage(ImageThief imageThief){
        appLockRepository.deleteImageThief(imageThief);
    }
    public void deleteImageByID(int id){
        appLockRepository.deleteImageByID(id);
    }

    public void saveImg(Context context, ImageThief thief){
        appLockRepository.saveImgToStorage2(context, thief);
    }
}
