package com.tapbi.applock.ui.main.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.EventLock;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.data.model.AppLock;
import com.tapbi.applock.databinding.FragmentHomeBinding;
import com.tapbi.applock.ui.adapter.pager.HomePager;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.dialog.DialogPermissions;
import com.tapbi.applock.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import timber.log.Timber;

public class HomeFragment extends BaseBindingFragment<FragmentHomeBinding, HomeViewModel> {
    private long lastClickTime = 0;
    private boolean firstOpen;
    private String passDraw = "";

    @Override
    public Class getViewModel() {
        return HomeViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        Log.e("NGOCANH", "home fragment onCreatedView ");

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        passDraw = SharedPreferenceHelper.getStringWithDefault(Constant.PATTERN_LOCK, "");
        boolean setQuestion = SharedPreferenceHelper.getBoolean(Constant.SET_QUESTION_SETTING, false);
        if(!setQuestion)
            passDraw = "";

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), mBackPressedCallback);

        initView();
    }


    @Override
    protected void onPermissionGranted() {

    }


    private void initView() {
        setupViewPager();

        evenClick();
        tabLayoutClick();
        drawerClick();
    }

    private void drawerClick() {
        binding.inDrawerMain.vHome.setOnClickListener(view -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        binding.inDrawerMain.vThemes.setOnClickListener(view -> {
            checkClick();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.themesFragment);
                    }
                }
            }, 300);
        });

        binding.inDrawerMain.vBackGround.setOnClickListener(view -> {
            checkClick();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.backGroundFragment);
                    }
                }
            }, 300);
        });

        binding.inDrawerMain.vShare.setOnClickListener(view -> {
            binding.inDrawerMain.vShare.setClickable(false);
            binding.inDrawerMain.vShare.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    binding.inDrawerMain.vShare.setClickable(true);
                }
            }, 500);

            Utils.shareBrowser(requireActivity(), Constant.LINK_SHARE_APP);
        });

        binding.inDrawerMain.vContactUs.setOnClickListener(view -> {
            binding.inDrawerMain.vContactUs.setClickable(false);
//            checkClick();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isAdded()){
                        Utils.sendFeedback(requireContext(), Constant.EMAIL, "");
                        binding.inDrawerMain.vContactUs.setClickable(true);
                    }
                }
            }, 1000);
        });
        binding.inDrawerMain.vIntroduce.setOnClickListener(view -> {
            checkClick();
            Toast.makeText(requireContext(), getText(R.string.app_name_version) + "", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupViewPager() {
        binding.inTabLayout.tvLeft.setText(getResources().getText(R.string.privacy));
        binding.inTabLayout.tvLeft.setShadowLayer(20, 0, 0, getResources().getColor(R.color.color_C81DB854));
        binding.inTabLayout.tvRight.setText(getResources().getText(R.string.protect));

        HomePager homePager = new HomePager(getChildFragmentManager());
        binding.pager.setAdapter(homePager);
        binding.tabLayout.setupWithViewPager(binding.pager);
        binding.inHeader.imgIcSearch.setVisibility(View.VISIBLE);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void evenClick() {
        binding.inHeader.imgIcHome.setOnClickListener(view -> {
            binding.drawerLayout.openDrawer(Gravity.START);
        });

        binding.inHeader.imgIcSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkClick();

                binding.inHeader.imgIcSearch.setEnabled(false);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.searchFragment);
            }
        });
    }

    private void tabLayoutClick() {
        binding.inTabLayout.tvLeft.setOnClickListener(view -> {
            new Utils().selectTabLayout(requireContext(), binding.inTabLayout.tvLeft,
                    binding.inTabLayout.tvRight, binding.inTabLayout.vLeft, binding.inTabLayout.vRight, binding.inTabLayout.tvLeft);
            binding.pager.setCurrentItem(0);
        });

        binding.inTabLayout.tvRight.setOnClickListener(view -> {
            new Utils().selectTabLayout(requireContext(), binding.inTabLayout.tvLeft, binding.inTabLayout.tvRight,
                    binding.inTabLayout.vLeft, binding.inTabLayout.vRight, binding.inTabLayout.tvRight);
            binding.pager.setCurrentItem(1);
            //Timber.e("onPageScrolled:cvcv ");
        });

        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Timber.e("onPageScrolled: " + position);
            }

            @Override
            public void onPageSelected(int position) {
                Timber.e("onPageSelected: %s", position);
                if (position == 1) {
                    new Utils().selectTabLayout(requireContext(), binding.inTabLayout.tvLeft, binding.inTabLayout.tvRight,
                            binding.inTabLayout.vLeft, binding.inTabLayout.vRight, binding.inTabLayout.tvRight);
                    binding.inHeader.imgIcSearch.setVisibility(View.GONE);
                } else {
                    new Utils().selectTabLayout(requireContext(), binding.inTabLayout.tvLeft, binding.inTabLayout.tvRight,
                            binding.inTabLayout.vLeft, binding.inTabLayout.vRight, binding.inTabLayout.tvLeft);
                    binding.inHeader.imgIcSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Timber.e("onPageScrollStateChanged: "+state);
            }
        });
    }

    private void checkClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 2000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.e("NGOCANH", "home fragment handleOnBackPressed: ");

                Intent startMain = new Intent(Intent.ACTION_MAIN, null);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startMain);

                requireActivity().finish();
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();

        if (SharedPreferenceHelper.getBoolean(Constant.FIRST_SHOW_PERMISSION, true) && !checkShow) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Utils.showDiaLogPermission(requireContext()) ){
                        Log.d("NGOCANH", "SHOW DIALOG PERMISSIOn");
                        SharedPreferenceHelper.storeBoolean(Constant.IS_SETTING_APP_PERMISSION, true);
                        SharedPreferenceHelper.storeBoolean(Constant.FIRST_SHOW_PERMISSION, false);
                        if (passDraw.equals("")) {
                            mainViewModel.checkFragment.postValue(Constant.HOME_FRAGMENT);

                            Bundle bundle = new Bundle();
                            bundle.putBoolean(Constant.FIRST_OPEN, true);
                            Navigation.findNavController(binding.getRoot()).navigate(R.id.changeDrawFragment, bundle);
                        }
                    }
                }
            }, 1200);


        }
    }

    @Subscribe(sticky = true)
    public void onEventLock(EventLock event) {
        if (event.getType().equals(Constant.REQUEST_PERMISITON)) {
            Log.d("NgocAnh", "onEventLock: REQUEST_PERMISITON");
                checkShow = false;
    }
    }
    private boolean checkShow;
    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
        checkShow = true;
    }



}
