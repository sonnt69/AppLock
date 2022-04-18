package com.tapbi.applock.ui.main.themes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.tapbi.applock.R;
import com.tapbi.applock.common.Constant;
import com.tapbi.applock.common.model.Theme;
import com.tapbi.applock.data.local.SharedPreferenceHelper;
import com.tapbi.applock.databinding.FragmentThemesBinding;
import com.tapbi.applock.interfaces.ClickTheme;
import com.tapbi.applock.ui.adapter.ThemeAdapter;
import com.tapbi.applock.ui.base.BaseBindingFragment;
import com.tapbi.applock.ui.main.MainActivity;
import com.tapbi.applock.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ThemesFragment extends BaseBindingFragment<FragmentThemesBinding, ThemesViewModel> implements ClickTheme {
    private ThemeAdapter themeAdapter;

    @Override
    protected Class<ThemesViewModel> getViewModel() {
        return ThemesViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_themes;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        viewModel.getListTheme(requireContext(), Constant.LIST_THEME);
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

    private void setAdapter() {
         themeAdapter = new ThemeAdapter(requireContext(), this);
        binding.rcvThemes.setAdapter(themeAdapter);
        binding.rcvThemes.setLayoutManager(new GridLayoutManager(requireContext(), 3));
    }

    @Override
    protected void onPermissionGranted() {

    }

    private void initView() {
        binding.inHeader.tvTitle.setText(getResources().getText(R.string.themes));
        binding.inHeader.imgIcHome.setImageResource(R.drawable.ic_back);
        binding.inHeader.imgIcSearch.setVisibility(View.GONE);

        String theme = (SharedPreferenceHelper.getStringWithDefault(Constant.THEME_NAME, Constant.THEME_DEFAULT));

        viewModel.themeLiveData.observe(getViewLifecycleOwner(), themes -> {
            if(themes != null){
                int selected = -1;
                for (int i = 0; i < themes.size(); i++) {
                    String uri = themes.get(i).getUri().toLowerCase();
                    if (uri.contains(theme.toLowerCase())) {
                        selected = i;
                        break;
                    }
                }
                themeAdapter.changeList(themes);
                themeAdapter.setSelected(selected);
            }
        });
    }

    @Override
    public void click(Theme theme) {
        String uri = theme.getUri().toLowerCase();
        if (uri.contains(Constant.THEME_01.toLowerCase())) {
            SharedPreferenceHelper.storeString(Constant.THEME_NAME, Constant.THEME_01);
        } else if (uri.contains(Constant.THEME_02.toLowerCase())) {
            SharedPreferenceHelper.storeString(Constant.THEME_NAME, Constant.THEME_02);
        } else if (uri.contains(Constant.THEME_03.toLowerCase())) {
            SharedPreferenceHelper.storeString(Constant.THEME_NAME, Constant.THEME_03);
        } else if (uri.contains(Constant.THEME_04.toLowerCase())) {
            SharedPreferenceHelper.storeString(Constant.THEME_NAME, Constant.THEME_04);
        } else {
            SharedPreferenceHelper.storeString(Constant.THEME_NAME, Constant.THEME_DEFAULT);
        }

        SharedPreferenceHelper.storeString(Constant.LINK_BACKGROUND, "");

    }
}