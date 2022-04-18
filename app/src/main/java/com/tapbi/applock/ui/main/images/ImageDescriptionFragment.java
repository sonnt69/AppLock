package com.tapbi.applock.ui.main.images;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.jaeger.library.StatusBarUtil;
import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.DeleteIntruder;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.databinding.FragmentImageDescriptionBinding;
import com.tapbi.applock.databinding.ViewCatchIntruderPopupBinding;
import com.tapbi.applock.databinding.ViewDeleteDialogBinding;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.utils.BitmapUtils;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import timber.log.Timber;


public class ImageDescriptionFragment extends BaseBindingFragment<FragmentImageDescriptionBinding, ImageDescriptionViewModel> {
    private ImageThief imageThief;
    private static final int PERMISSION_WRITE = 200;

    private void initView() {
        binding.inHeader.tvTitle.setText(getResources().getText(R.string.catchIntruder));
        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.VISIBLE);
        binding.inHeader.imgIcSearch.setImageResource(R.drawable.ic_more_vert_white);

        float dip = 10f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );

        Glide.with(this).load(imageThief.getUri()).transform(new RoundedCorners((int) Math.round(px))).into(binding.ImgDesAvImg);
//        Glide.with(this).load(imageThief.getUri()).into(binding.ImgDesAvImg);

        binding.ImgDesAvTvTime.setText(imageThief.getTime());

//        StatusBarUtil.setTransparent(requireActivity());
        evenClick();
    }

    private void evenClick() {
        binding.inHeader.imgIcHome.setOnClickListener(view -> {
            if(requireActivity() instanceof MainActivity){
                requireActivity().onBackPressed();
            }
        });

        binding.inHeader.imgIcSearch.setOnClickListener(view -> {
            setMyPopupWindow(view);
        });
    }

    private void setMyPopupWindow(View v) {
        ViewCatchIntruderPopupBinding popupBinding = ViewCatchIntruderPopupBinding.inflate(getLayoutInflater());
        View view = popupBinding.getRoot();
        PopupWindow myPopupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        popupBinding.tvDelete.setText(getText(R.string.delete));
        popupBinding.tvPatternLock.setText(getText(R.string.save));
        popupBinding.vSave.setOnClickListener(view1 -> {
            myPopupWindow.dismiss();
            saveImage();
        });
        popupBinding.vDelete.setOnClickListener(view1 -> {
            myPopupWindow.dismiss();
            showDialogClear();
        });

        myPopupWindow.showAtLocation(view, Gravity.TOP | Gravity.END, 20, 125);

//        myPopupWindow.showAsDropDown(v, v.getWidth() - myPopupWindow.getWidth(), 0);
//        myPopupWindow.showAsDropDown(v, 0, -20);
    }


    private void showDialogClear(){

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        ViewDeleteDialogBinding deleteDialogBinding = ViewDeleteDialogBinding.inflate(getLayoutInflater());
        builder.setView(deleteDialogBinding.getRoot());
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialogBinding.inBtn.btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        deleteDialogBinding.inBtn.btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage();

                alertDialog.dismiss();
                if(requireActivity() instanceof MainActivity){
                    requireActivity().onBackPressed();
                }
            }
        });

        Timber.e("alertDialog.show()");
        alertDialog.show();

    }

    private void saveImage() {
        //Log.e("sds", "sdsd");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            }else
            {
                saveToStorage(requireContext(), binding.ImageDesAvFl, imageThief);
            }
        }else {
            saveToStorage(requireContext(), binding.ImageDesAvFl, imageThief);
        }
    }



    public void saveToStorage(Context context, View view, ImageThief thief){
//        Bitmap bitmap = Utils.viewToBitmap(view);
//        BitmapUtils.saveImageBitmap(requireContext(), bitmap,  "i"+ thief.getId());

        viewModel.saveImageToStorage(context, view, thief);
        Toast.makeText(requireContext(), getText(R.string.save_image), Toast.LENGTH_SHORT).show();
    }

    private void deleteImage() {
        deleteDbImage();
        File dir = new File(imageThief.getCachePath());
        dir.delete();
    }

    public void deleteDbImage(){
        viewModel.deleteImage(imageThief);
        Toast.makeText(requireActivity().getBaseContext(), R.string.deleteSuccess, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_WRITE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                saveToStorage(requireContext(), binding.ImageDesAvFl, imageThief);
            }
            else
            {
                Toast.makeText(requireContext(), R.string.cantSave, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected Class<ImageDescriptionViewModel> getViewModel() {
        return ImageDescriptionViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_image_description;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(Constant.IMAGE_THIEF, imageThief);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        if(getArguments() != null){
            imageThief = (ImageThief) getArguments().getSerializable(Constant.IMAGE_THIEF);
        }

        if(savedInstanceState != null){
            imageThief = (ImageThief) savedInstanceState.getSerializable(Constant.IMAGE_THIEF);
        }


        initView();
    }

    @Override
    protected void onPermissionGranted() {

    }
}
