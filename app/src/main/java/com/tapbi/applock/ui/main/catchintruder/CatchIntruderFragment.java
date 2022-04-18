package com.tapbi.applock.ui.main.catchintruder;

import static android.bluetooth.BluetoothGattDescriptor.PERMISSION_WRITE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.databinding.FragmentCatchIntruderBinding;
import com.tapbi.applock.databinding.ViewCatchIntruderPopupBinding;
import com.tapbi.applock.interfaces.ClickCatchIntruder;
import com.tapbi.applock.interfaces.ResultDialog;
import com.tapbi.applock.ui.adapter.CatchIntruderAdapter;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.ui.dialog.DialogPermissions;
import com.tapbi.applock.utils.BitmapUtils;
import com.tapbi.applock.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CatchIntruderFragment extends BaseBindingFragment<FragmentCatchIntruderBinding, CatchIntruderViewModel> implements ClickCatchIntruder {
    private long lastClickTime = 0;

    private final boolean selectAll = false;
    //    private AppDatabase db;
    private List<ImageThief> list;
    private DialogPermissions dialogPermissions;
    private CatchIntruderAdapter catchIntruderAdapter;
//    private boolean doNotFinish = false;
    private boolean multipleSelect = false;
    private int countSelect = 0;
    private boolean clickShare = true;

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    @Override
    public void onResume() {
        super.onResume();
//        countSelect = 0;
        checkIsSelect();
//        getDBandSetAdapter();

        if (multipleSelect) {
            multipleSelect = false;
            catchIntruderAdapter.changeMultipleSelect(true);
        }

    }

    private void initView() {
        binding.inHeader.tvTitle.setText(getResources().getText(R.string.catchIntruder));
        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.VISIBLE);
        binding.inHeader.imgIcSearch.setImageResource(R.drawable.ic_more_vert_white);
        binding.inHeader.imgIcSearch.setVisibility(View.GONE);
    }


    private void evenClick() {
        binding.inHeader.imgIcHome.setOnClickListener(view -> {
            checkClick();

            if (requireActivity() instanceof MainActivity) {
                requireActivity().onBackPressed();
            }
        });


        binding.tvChooseAll.setOnClickListener(view -> {
            selectAll();
        });

        binding.inHeader.imgIcSearch.setOnClickListener(view -> {
            setMyPopupWindow(view);
        });

        binding.tvChooseOrCancel.setOnClickListener(view -> {
            if (binding.viewBtn.getRoot().getVisibility() == View.VISIBLE) {
                binding.tvChooseOrCancel.setText(getText(R.string.choose));
                binding.viewBtn.getRoot().setVisibility(View.GONE);
                binding.tvChooseAll.setVisibility(View.GONE);
                binding.inHeader.imgIcHome.setVisibility(View.VISIBLE);
                catchIntruderAdapter.changeMultipleSelect(false);
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setSelected(false);
                }

            } else {
                binding.tvChooseOrCancel.setText(getText(R.string.cancel));
                binding.viewBtn.getRoot().setVisibility(View.VISIBLE);
                binding.tvChooseAll.setVisibility(View.VISIBLE);
                binding.inHeader.imgIcHome.setVisibility(View.GONE);
                catchIntruderAdapter.changeMultipleSelect(true);

            }
            countSelect = 0;
            checkIsSelect();
        });

        binding.viewBtn.imgShare.setOnClickListener(view -> {
            checkClick();
            if (clickShare) {
                clickShare = false;
                multipleSelect = true;
                ArrayList<Uri> uriList = new ArrayList<>();
                for (ImageThief imageThief : list) {
                    if (imageThief.isSelected()) {
                        File fileCache = new File(imageThief.getCachePath());
                        Uri contentUri = FileProvider.getUriForFile(requireContext(), "com.tapbi.applock.provider", fileCache);
                        if (contentUri != null) {
                            uriList.add(contentUri);
                        }
                    }
                }

                Utils.shareImage(requireActivity(), uriList);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clickShare = true;
                    }
                }, 2000);
            }
        });
    }

    private void saveToStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            } else {
                doSave();
            }
        } else {
            doSave();
        }
    }

    private void doSave() {
        for (ImageThief imageThief : list) {

            if (imageThief.isSelected()) {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), Uri.parse(imageThief.getUri()));
//                    BitmapUtils.saveImageBitmap(requireContext(), bitmap, String.valueOf(imageThief.getId()));

                viewModel.saveImg(requireContext(), imageThief);
            }
        }
        Toast.makeText(requireContext(), getText(R.string.save_image), Toast.LENGTH_SHORT).show();
    }


    private void setAdapter() {
        catchIntruderAdapter = new CatchIntruderAdapter(requireContext(), CatchIntruderFragment.this);
        binding.thiefAvRcv.setAdapter(catchIntruderAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        binding.thiefAvRcv.setLayoutManager(gridLayoutManager);
    }

    private void selectAll() {
        countSelect = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isSelected()) {
                countSelect++;
            }
        }

        if (countSelect != list.size()) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setSelected(true);
            }
            countSelect = list.size();
        } else {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setSelected(false);
            }
            countSelect = 0;
        }
        checkIsSelect();
        catchIntruderAdapter.changeList(list);

        if(list.size() == 0){
            binding.tvNoIntruder.setVisibility(View.VISIBLE);
            binding.tvChooseAll.setVisibility(View.GONE);
            binding.inHeader.imgIcHome.setVisibility(View.VISIBLE);
            binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        }
    }

    private void checkIsSelect() {
        Timber.e("countSelect: " + countSelect);
        if (countSelect > 0) {
            binding.viewBtn.tvSave.setTextColor(getResources().getColor(R.color.color_1DB854));
            binding.viewBtn.imgDelete.setImageResource(R.drawable.ic_delete_green);
            binding.viewBtn.imgShare.setImageResource(R.drawable.ic_share_green);

            binding.viewBtn.imgDelete.setOnClickListener(view -> {
                checkClick();

                showDialogClear();
            });

            binding.viewBtn.tvSave.setOnClickListener(view -> {
                checkClick();

                saveToStorage();
            });
        } else {
            binding.viewBtn.imgDelete.setImageResource(R.drawable.ic_delete_black);
            binding.viewBtn.imgShare.setImageResource(R.drawable.ic_share_black);
            binding.viewBtn.tvSave.setTextColor(getResources().getColor(R.color.color_777777));

            binding.viewBtn.imgDelete.setOnClickListener(view -> {

            });

            binding.viewBtn.tvSave.setOnClickListener(view -> {

            });
        }
    }

    private void checkClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 3000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    private void setMyPopupWindow(View v) {
        ViewCatchIntruderPopupBinding popupBinding = ViewCatchIntruderPopupBinding.inflate(getLayoutInflater());
        View view = popupBinding.getRoot();
        PopupWindow myPopupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        popupBinding.vSave.setOnClickListener(view1 -> {
            myPopupWindow.dismiss();
            showDialogClear();
        });
        popupBinding.vDelete.setOnClickListener(view1 -> {
            myPopupWindow.dismiss();
            showDialogClear();
        });

        myPopupWindow.showAsDropDown(v, -150, 0);
    }

    private void showDialogClear() {

        initDialog();
    }

    private void initDialog() {
        dialogPermissions = new DialogPermissions(requireContext(), getString(R.string.cancel), getString(R.string.delete), getString(R.string.deleteDes), R.drawable.ic_warning, new ResultDialog() {
            @Override
            public void result(boolean b) {
                deleteFile(list);
            }
        });

        dialogPermissions.showDialog();
    }

    private void deleteFile(List<ImageThief> listSelect) {
        for (ImageThief imageThief : listSelect) {
            if (imageThief.isSelected()) {
                File dir = new File(imageThief.getCachePath());
                dir.delete();
                viewModel.deleteImageByID(imageThief.getId());

            }
        }

        countSelect = 0;
        checkIsSelect();
    }

    @Override
    public void multipleSelect() {
        checkIsSelect();
    }

    @Override
    public void select() {
        countSelect++;
    }

    @Override
    public void unSelect() {
        countSelect--;
    }

    @Override
    public void clickItem(ImageThief imageThief) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.IMAGE_THIEF, (Serializable) imageThief);

        Navigation.findNavController(binding.getRoot()).navigate(R.id.imageDescriptionFragment, bundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_WRITE) {
            multipleSelect = true;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToStorage();
            } else {
                Toast.makeText(requireContext(), R.string.cantSave, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected Class<CatchIntruderViewModel> getViewModel() {
        return CatchIntruderViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_catch_intruder;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
//        StatusBarUtil.setTransparent(requireActivity());

        list = new ArrayList<>();

        setAdapter();
        initView();

        viewModel.getImageThiefInDB().observe(getViewLifecycleOwner(),imageThiefs -> {
            if(imageThiefs != null){
                Log.e("NGOCANH", "get image thief "+imageThiefs.size());

                list.clear();
                list.addAll(imageThiefs);
                catchIntruderAdapter.changeList(imageThiefs);

                if(imageThiefs.size() == 0){
                    binding.tvNoIntruder.setVisibility(View.VISIBLE);
                    binding.tvChooseOrCancel.setVisibility(View.GONE);
                    binding.tvChooseAll.setVisibility(View.GONE);
                    binding.viewBtn.getRoot().setVisibility(View.GONE);
                    binding.inHeader.imgIcHome.setVisibility(View.VISIBLE);
                    binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
                }
                else{
                    binding.tvNoIntruder.setVisibility(View.GONE);
                    binding.tvChooseOrCancel.setVisibility(View.VISIBLE);
                }
            }
        });

        evenClick();


    }

    @Override
    protected void onPermissionGranted() {

    }
}
