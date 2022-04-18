package com.tapbi.applock.ui.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.AppInfo;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.LayoutRowAppshowBinding;
import com.tapbi.applock.interfaces.ClickAppLock;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class PrivacyAdapter extends RecyclerView.Adapter<PrivacyAdapter.PrivacyHolder> implements ClickAppLock {
    private final Context context;
    private List<AppInfo> list;
    private final ClickAppLock clickAppLock;
    private String passDraw = "";
    public PrivacyAdapter(Context context, ClickAppLock clickAppLock) {
        this.context = context;
        this.clickAppLock = clickAppLock;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<AppInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrivacyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutRowAppshowBinding layoutRowAppshowBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_row_appshow, parent, false);
        return new PrivacyHolder(layoutRowAppshowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull final PrivacyHolder holder, int position) {
         passDraw = SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, "");
        boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
        if(!setQuestion)
            passDraw = "";

        final AppInfo appInfo = list.get(position);
        if(appInfo != null){
            holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(appInfo.isLocked());
        }

        if (appInfo.getAppPackage() == null) {
            holder.layoutRowAppshowBinding.rcvTvDetailApp.setText(appInfo.getAppType());
            holder.layoutRowAppshowBinding.scvSwitchCompat.setVisibility(View.GONE);
            holder.layoutRowAppshowBinding.rcvTvNameApp.setVisibility(View.GONE);
            holder.layoutRowAppshowBinding.rcvIconShowApp.setVisibility(View.GONE);
            holder.layoutRowAppshowBinding.rcvTvDetailApp.setVisibility(View.VISIBLE);
            if (appInfo.getAppType().equals(context.getResources().getString(R.string.otherApp))){
                holder.layoutRowAppshowBinding.vSpace.setVisibility(View.VISIBLE);
            }else {
                holder.layoutRowAppshowBinding.vSpace.setVisibility(View.GONE);
            }

        } else {
            holder.layoutRowAppshowBinding.vSpace.setVisibility(View.GONE);
            holder.layoutRowAppshowBinding.rcvTvNameApp.setText(appInfo.getAppName());
            holder.layoutRowAppshowBinding.rcvTvNameApp.setVisibility(View.VISIBLE);
            holder.layoutRowAppshowBinding.scvSwitchCompat.setVisibility(View.VISIBLE);
            holder.layoutRowAppshowBinding.rcvIconShowApp.setVisibility(View.VISIBLE);
            holder.layoutRowAppshowBinding.rcvTvDetailApp.setVisibility(View.GONE);
            if (appInfo.getAppPackage().equalsIgnoreCase(Constant.PACKAGE_NAME)) {
                holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(true);
                holder.layoutRowAppshowBinding.scvSwitchCompat.setEnabled(false);
            } else {
                holder.layoutRowAppshowBinding.scvSwitchCompat.setEnabled(true);
                holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(appInfo.isLocked());
            }

            Bitmap finalBitmap = getBitmapFromDrawable(appInfo.getAppIcon());
            holder.layoutRowAppshowBinding.rcvIconShowApp.setImageBitmap(finalBitmap);
        }

        if(appInfo.getAppName().equals("")){
            holder.layoutRowAppshowBinding.itemView.setClickable(false);
        } else {
            holder.layoutRowAppshowBinding.itemView.setClickable(true);
            holder.layoutRowAppshowBinding.itemView.setOnClickListener(view -> holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(!holder.layoutRowAppshowBinding.scvSwitchCompat.isChecked()));
        }


        holder.layoutRowAppshowBinding.scvSwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!Utils.showDiaLogPermission(context)) {
                EventBus.getDefault().post(new EventLock(Constant.REQUEST_PERMISITON));
                holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(!holder.layoutRowAppshowBinding.scvSwitchCompat.isChecked());
//                holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(false);

            } else {
                final AppLock appLock = new AppLock();
                appLock.setAppPackage(appInfo.getAppPackage());
                appLock.setAppPass("");
                if (b) {
                    clickAppLock.locked(appLock);
                    if(!passDraw.equals(""))
                        appInfo.setLocked(b);
                    else appInfo.setLocked(!b);
                } else {
                    if (appInfo.getAppPackage().contains("settings")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setCancelable(false);
                        builder.setTitle(R.string.warning);
                        builder.setMessage(R.string.warningUnlockSettings);
                        builder.setNegativeButton(R.string.lock, (dialogInterface, i) -> {
                            holder.layoutRowAppshowBinding.scvSwitchCompat.setChecked(true);
                            appInfo.setLocked(true);
                        });
                        builder.setPositiveButton(R.string.unlock, (dialogInterface, i) -> {
                            clickAppLock.unLock(appLock);
                            appInfo.setLocked(false);
                        });
                        builder.show();
                    } else {
                        clickAppLock.unLock(appLock);
                        appInfo.setLocked(false);
                    }
                }
            }
        });

    }

    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private static final int RGB_MASK = 0x00FFFFFF;

    public static Bitmap invert(Bitmap original) {
        // Create mutable Bitmap to invert, argument true makes it mutable
        Bitmap inversion = original.copy(Bitmap.Config.ARGB_8888, true);

        // Get info about Bitmap
        int width = inversion.getWidth();
        int height = inversion.getHeight();
        int pixels = width * height;

        // Get original pixels
        int[] pixel = new int[pixels];
        inversion.getPixels(pixel, 0, width, 0, 0, width, height);

        // Modify pixels
        for (int i = 0; i < pixels; i++)
            pixel[i] ^= RGB_MASK;
        inversion.setPixels(pixel, 0, width, 0, 0, width, height);

        // Return inverted Bitmap
        return inversion;
    }


    @Override
    public int getItemCount() {
        if(list!= null){
            return list.size();
        }
        return 0;
    }


    @Override
    public void onViewRecycled(@NonNull PrivacyHolder holder) {
        super.onViewRecycled(holder);

        holder.layoutRowAppshowBinding.scvSwitchCompat.setOnCheckedChangeListener(null);
    }

    @Override
    public void locked(AppLock appLock) {

    }

    @Override
    public void unLock(AppLock appLock) {

    }

    public static class PrivacyHolder extends RecyclerView.ViewHolder {
        public LayoutRowAppshowBinding layoutRowAppshowBinding;
        public PrivacyHolder(LayoutRowAppshowBinding itemView) {
            super(itemView.getRoot());
            layoutRowAppshowBinding = itemView;
        }
    }
}

