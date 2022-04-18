package com.tapbi.applock.data.repository;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.common.model.BackGround;
import com.tapbi.applock.common.model.Theme;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.local.db.AppDatabase;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.utils.BitmapUtils;
import com.tapbi.applock.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppLockRepository {
    private SharedPreferenceHelper sharedPreferenceHelper;

    @Inject
    public AppLockRepository(SharedPreferenceHelper sharedPreferenceHelper) {
        this.sharedPreferenceHelper = sharedPreferenceHelper;
    }

    public Single<List<BackGround>> getBackGround() {
        return Single.fromCallable(Utils::getBackGround).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Theme>> getTheme() {
        return Single.fromCallable(Utils::getTheme).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    //    public Single<List<ImageThief>> getImageThief() {
//        return Single.fromCallable(Utils::getImageThief).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }
    public LiveData<List<ImageThief>> getImageThief() {
        return AppDatabase.getInstance().getImageDAO().getImageThiefInDB();
    }

    public Single<List<ApplicationInfo>> getApplication() {
        return Single.fromCallable(Utils::getApplication).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public Single<List<AppLock>> getAppLock() {
        return Single.fromCallable(() -> AppDatabase.getInstance().getAppDAO().getAppLock()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public LiveData<List<AppLock>> getLockedApps() {
        return AppDatabase.getInstance().getAppDAO().getLockedApp();
    }

    public LiveData<Integer> getCountLockedApp() {
        return AppDatabase.getInstance().getAppDAO().getRowCount();
    }


    public Single<ArrayList<String>> getName() {
        return Single.fromCallable(Utils::getName).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ArrayList<String>> getListAppImportant() {
        return Single.fromCallable(this::getAppsImportant).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void saveLockApp(AppLock appLocks) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getAppDAO().insertAppLock(appLocks);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void saveImageThief(ImageThief imageThief) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getImageDAO().insertImageThief(imageThief);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void deleteImageThief(ImageThief imageThief) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getImageDAO().deleteImageThief(imageThief);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void deleteImageByID(int id) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getImageDAO().deleteImageByID(id);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void insertLockApp(AppLock... appLocks) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getAppDAO().insertAppLock(appLocks);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void deleteLockApp(AppLock appLock) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getAppDAO().deleteAppLock(appLock);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void deleteLockAppByName(String appPackage) {
        Completable.fromRunnable(() -> {
            AppDatabase.getInstance().getAppDAO().deleteByPackage(appPackage);
        }).subscribeOn(Schedulers.io()).subscribe();
    }


    public Single<ArrayList<Theme>> getListTheme(Context context, String folder) {
        return Single.fromCallable(() -> Utils.listTheme(context, folder)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ArrayList<BackGround>> getListBackground(Context context, String folder) {
        return Single.fromCallable(() -> Utils.listBackground(context, folder)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public void deleteAppNotExist(Activity activity) {
        Completable.fromRunnable(() -> {
            deleteLockedAppNotExist(activity);
        }).subscribeOn(Schedulers.io()).subscribe();
    }


    public void saveImgToStorage(Context context, View view, ImageThief thief) {
        Completable.fromRunnable(() -> {
            saveToStorage(context, view, thief);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void saveImgToStorage2(Context context, ImageThief thief) {
        Completable.fromRunnable(() -> {
            saveToStorage2(context, thief);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void saveToStorage(Context context, View view, ImageThief thief) {
        Bitmap bitmap = Utils.viewToBitmap(view);
        BitmapUtils.saveImageBitmap(context, bitmap, "i" + thief.getId());
    }

    public void saveToStorage2(Context context, ImageThief thief) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(thief.getUri()));
            BitmapUtils.saveImageBitmap(context, bitmap, String.valueOf(thief.getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteAppByName(String name) {
        AppDatabase.getInstance().getAppDAO().deleteByPackage(name);
    }

    private void deleteLockedAppNotExist(Activity activity) {
        List<AppLock> lockList = AppDatabase.getInstance().getAppDAO().getAppLock();
        List<String> appInDevice = new ArrayList<>();
        List<String> lockList2 = new ArrayList<>();

        PackageManager pm = activity.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        //check list app in device
        for (ApplicationInfo packageInfo : packages) {
            appInDevice.add(packageInfo.packageName);
        }
        for (AppLock appLock : lockList) {
            lockList2.add(appLock.getAppPackage());
        }

        //check lock app exist
        for (String app : lockList2) {
            if (!appInDevice.contains(app)) {
                deleteAppByName(app);
            }
        }
    }


    public Single<ArrayList<AppInfo>> getListAppsInfo(Activity activity) {
        List<AppLock> lockList = AppDatabase.getInstance().getAppDAO().getAppLock();
        List<String> appImportant = getAppsImportant();

        ArrayList<AppInfo> infoList = new ArrayList<>();
        infoList.add(new AppInfo("", activity.getString(R.string.otherApp)));
        infoList.add(new AppInfo("", activity.getString(R.string.importantApp)));

        PackageManager pm = activity.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        return Single.zip(getListAppInFoPX(activity,packages,pm,appImportant,lockList,infoList),objects -> infoList);
    }

    private ArrayList<Single<ArrayList<AppInfo>>> getListAppInFoPX(Activity activity,List<ApplicationInfo> packages
            ,PackageManager pm,List<String> appImportant,List<AppLock> lockList,ArrayList<AppInfo> infoList){
        ArrayList<Single<ArrayList<AppInfo>>> list = new ArrayList<>();
        int count = 6;
        int divide =  packages.size()/count;
        for (int i=0;i<count;i++){
            int start = i*divide;
            int end =(i+1)*divide;
            if(i==count-1)
                end = packages.size();
            list.add(getAppInFoRx(activity,start,end,packages,pm,appImportant,lockList,infoList));
        }

        return list;
    }

    private Single<ArrayList<AppInfo>> getAppInFoRx(Activity activity, int start, int end, List<ApplicationInfo> packages
            , PackageManager pm, List<String> appImportant, List<AppLock> lockList, ArrayList<AppInfo> infoList) {
        return Single.fromCallable(() -> getAppsInfo(activity, start, end, packages, pm, appImportant, lockList, infoList)).subscribeOn(Schedulers.io());
    }

    private ArrayList<AppInfo> getAppsInfo(Activity activity, int start, int end, List<ApplicationInfo> packages
            , PackageManager pm, List<String> appImportant, List<AppLock> lockList, ArrayList<AppInfo> infoList) {

        for (int i = start; i < end; i++) {
            ApplicationInfo packageInfo = packages.get(i);
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                Utils.listAppName.add(packageInfo.packageName);
                if (!packageInfo.packageName.contains(Constant.PACKAGE_NAME) && !packageInfo.packageName.toLowerCase().contains(Constant.PACKAGE_NAME_APP_INPUTMETHOD.toLowerCase()) && !packageInfo.packageName.toLowerCase().contains(Constant.PACKAGE_NAME_APP_KEYBOARD.toLowerCase()) && !packageInfo.packageName.toLowerCase().contains(Constant.PACKAGE_NAME_APP_SEARCH_BOX.toLowerCase())) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppIcon(packageInfo.loadIcon(pm));
                    appInfo.setAppName((String) packageInfo.loadLabel(pm));
                    appInfo.setAppPackage(packageInfo.packageName);
                    for (AppLock appLock : lockList) {
                        if (appInfo.getAppPackage() != null) {

                            if (appInfo.getAppPackage().equalsIgnoreCase(appLock.getAppPackage() + "")) {
                                appInfo.setLocked(true);
                                break;
                            } else {
                                appInfo.setLocked(false);
                            }
                        }
                    }
                    if (appImportant.contains(appInfo.getAppPackage())) {
                        appInfo.setAppType(activity.getString(R.string.importantApp));
                    } else {
                        appInfo.setAppType(activity.getString(R.string.otherApp));
                    }

                    infoList.add(appInfo);
                }
            }
        }
        return infoList;
    }

    private ArrayList<String> getAppsImportant() {
        ArrayList<String> appImportant = new ArrayList<>();
        //android
        appImportant.add("com.android.settings");
        appImportant.add("com.sec.android.app.myfiles");
        appImportant.add("com.android.mms");
        appImportant.add("com.android.contacts");
        appImportant.add("com.android.email");
        appImportant.add("com.samsung.android.email.provider");
        appImportant.add("com.android.vending");
        appImportant.add("com.sec.android.app.voicenote");
        //TODO:
        // packageList.add("com.android.settings");
        appImportant.add("com.android.dialer");
        appImportant.add("com.android.camera");
        appImportant.add("com.sec.android.app.camera");
        //......

        //google apps
        appImportant.add("com.google.android.apps.photos");
        appImportant.add("com.google.android.gm");
        appImportant.add("com.google.android.youtube");
        appImportant.add("com.vanced.android.youtube");
        appImportant.add("com.google.android.apps.tachyon");//duo

        //social app
        appImportant.add("org.thoughtcrime.securesms");//signal
        appImportant.add("org.telegram.messenger");
        appImportant.add("com.whatsapp");
        appImportant.add("com.twitter.android");
        appImportant.add("com.facebook.katana");
        appImportant.add("com.facebook.lite");
        appImportant.add("com.facebook.mlite");
        appImportant.add("com.facebook.orca");
        appImportant.add("com.zing.zalo");
        appImportant.add("com.tinder");
        appImportant.add("com.ss.android.ugc.trill");

        //samsung
        appImportant.add("com.samsung.android.video");

        //BANK
        appImportant.add("vn.tpb.token.baseotp");
        appImportant.add("com.vnpay.Agribank3g");
        appImportant.add("com.vnpay.bidv");
        appImportant.add("com.vnpay.hdbank");
        appImportant.add("vn.com.paysmart");
        appImportant.add("com.vnpay.SCB");
        appImportant.add("com.vnpay.namabank");
        appImportant.add("com.vietinbank.ipay");
        appImportant.add("com.VCB");
        appImportant.add("com.mbmobile");
        appImportant.add("mobile.acb.com.vn");
        appImportant.add("com.tpb.mb.gprsandroid");
        appImportant.add("src.com.sacombank");
        appImportant.add("com.nu.production");
        appImportant.add("com.shinhan.global.vn.bank");
        appImportant.add("com.ocb.omniextra");
        appImportant.add("vn.com.msb.smartBanking");
        appImportant.add("vn.shb.mbanking");
        appImportant.add("com.vnp.kienlongbank");
        appImportant.add("com.vsii.pvcombank");
        appImportant.add("com.bplus.vtpay");
        appImportant.add("vn.com.vng.zalopay");
        appImportant.add("com.vnpay.Agribank");
        appImportant.add("com.mservice.momotransfer");
        appImportant.add("com.beeasy.toppay");
        appImportant.add("com.vnpay.merchant");
        appImportant.add("ops.namabank.com.vn");
        appImportant.add("vn.tpb.token.baseotp");
        appImportant.add("com.vnpay.eximbank");
        ;
        appImportant.add("com.dongabank.ebankinternet");
        appImportant.add("com.vnpay.vpbankonline");
        appImportant.add("vietcombank.itcenter.vcbsmartoptv10");
        appImportant.add("vn.tpbank.savy");

        //etc
        appImportant.add("org.fdroid.fdroid");
        appImportant.add("org.mozilla.firefox");
        appImportant.add("org.schabi.newpipe");
        appImportant.add("eu.faircode.email");//fair mail
        appImportant.add("com.simplemobile.gallery.pro");// simple gallery

        appImportant.add("com.mediatek.filemanager");
        appImportant.add("com.sec.android.gallery3d");
        //

        return appImportant;
    }
}
