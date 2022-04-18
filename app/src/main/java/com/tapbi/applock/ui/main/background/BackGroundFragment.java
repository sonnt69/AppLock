package com.tapbi.applock.ui.main.background;

import static android.app.Activity.RESULT_OK;
import static com.tapbi.applock.ui.main.MainActivity.heightNavigationBar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.BackGround;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.databinding.FragmentBackGroundBinding;
import com.tapbi.applock.interfaces.ClickBackGround;
import com.tapbi.applock.ui.adapter.BackGroundAdapter;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.main.MainViewModel;
import com.tapbi.applock.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("ALL")
public class BackGroundFragment extends BaseBindingFragment<FragmentBackGroundBinding, BackgroundViewModel> implements ClickBackGround {
    private long lastClickTime = 0;

    private BackGroundAdapter backGroundAdapter;
    private List<BackGround> backGroundList = new ArrayList<>();
    private int selected = -1;
    private String linkBackGround;
    @Override
    protected Class<BackgroundViewModel> getViewModel() {
        return BackgroundViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_back_ground;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        setAdapter();
        initView();

        binding.inHeader.imgIcHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requireActivity() instanceof MainActivity) {
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onPermissionGranted() {

    }

    private void initView() {
        binding.inHeader.tvTitle.setText(getResources().getText(R.string.backGround));
        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.GONE);

        backGroundList = new ArrayList<>();

        linkBackGround = SharedPreferenceHelper.getStringWithDefault(Constant.LINK_BACKGROUND, "");
        if(linkBackGround.equals("")){
            backGroundAdapter.setSelected(1);

        }
        viewModel.getListBackground(requireContext(), Constant.LIST_BACKGROUND);
        viewModel.listBackground.observe(getViewLifecycleOwner(), backGrounds -> {
            if(backGrounds != null){
                backGroundList.addAll((Collection<? extends BackGround>) backGrounds);

                setSelected();

                backGroundAdapter.setSelected(selected);

                backGroundAdapter.changeList((List<BackGround>) backGrounds);

            }
        });
    }

    private void setAdapter() {
        backGroundAdapter = new BackGroundAdapter(requireContext(), this);
        binding.rcvBackground.setAdapter(backGroundAdapter);
        binding.rcvBackground.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rcvBackground.setPadding(0, binding.rcvBackground.getPaddingTop(), 0, heightNavigationBar);

        ((SimpleItemAnimator) binding.rcvBackground.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @Override
    public void click(BackGround backGround) {
        String uri = backGround.getUri().toLowerCase();
        if (uri.isEmpty()) {
            uri = Constant.BACKGROUND_DEFAULT;
        }
        linkBackGround = uri;
        SharedPreferenceHelper.storeString(Constant.LINK_BACKGROUND, uri);
    }

    @Override
    public void addBackGroundFromDevice() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1200) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        pickPhoto();
    }

    private void pickPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permission, Constant.REQUEST_CODE_IMAGE);
            } else {
                //pick img from gallery
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType(Constant.TYPE_IMAGE_VIDEO);
                startActivityForResult(pickIntent, Constant.RESULT_LOAD_IMAGE);
            }
        }
        else{
            //pick img from gallery
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType(Constant.TYPE_IMAGE_VIDEO);
            startActivityForResult(pickIntent, Constant.RESULT_LOAD_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.RESULT_LOAD_IMAGE) {
                Uri selectedImage = data.getData();
                if (selectedImage.toString().contains(Constant.TYPE_IMAGE)) {
                    Glide.with(getContext()).asBitmap().load(selectedImage).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            String uri = Utils.saveImgToCache(requireContext(), resource);
                            BackGround backGround = new BackGround(false, uri);
                            backGroundList.add(2, backGround);

                            setSelected();
                            backGroundAdapter.setSelected(selected);
                            backGroundAdapter.changeList(backGroundList);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
                }
                else  if (selectedImage.toString().contains(Constant.TYPE_VIDEO)) {
                    Toast.makeText(requireContext(), getString(R.string.no_support_get_video), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setSelected(){
        for (int i = 0; i < backGroundList.size(); i++) {
            if (!backGroundList.get(i).getUri().isEmpty()) {
                String s = backGroundList.get(i).getUri().toLowerCase();
                if (linkBackGround.contains(s)) {
                    selected = i;
                    break;
                }
            }
        }
        if(selected < 0){
            selected = 1;
        }
        Log.e("NGOCANH", "setSelected: " + selected);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQUEST_CODE_IMAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    pickPhoto();
                } else {
//                    Toast.makeText(requireContext(), R.string.permissionDenied, Toast.LENGTH_SHORT).show();
                    Utils.showDialogPermission(requireContext());
                }
                break;
            default:
                break;
        }
    }

}