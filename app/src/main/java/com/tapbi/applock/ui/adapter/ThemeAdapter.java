package com.tapbi.applock.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tapbi.applock.common.model.Theme;
import com.tapbi.applock.databinding.ItemBackgroundAdapterBinding;
import com.tapbi.applock.interfaces.ClickTheme;

import java.util.List;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemesHolder> {
    private List<Theme> list;
    private final Context context;
    private final ClickTheme themeClick;
    private int selected = -1;

    public ThemeAdapter(Context context, ClickTheme themeClick) {
        this.context = context;
        this.themeClick = themeClick;
    }

    public void setSelected(int selected){
        this.selected = selected;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeList(List<Theme> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThemesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemesHolder(ItemBackgroundAdapterBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemesHolder holder, @SuppressLint("RecyclerView") int position) {
        Theme theme = list.get(position);
        float dip = 6.5f;
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );

        Glide.with(context).load(theme.getUri()).override(323, 615).transform(new CenterCrop(), new RoundedCorners(20)).into(holder.binding.img);

        holder.itemView.setOnClickListener(view -> {
            themeClick.click(theme);
            notifyItemChanged(selected);
            selected = position;
            notifyItemChanged(selected);
        });

        holder.binding.imgIcTick.setVisibility(selected == position ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        if(list != null)
            return list.size();
        return 0;
    }

    public static class ThemesHolder extends RecyclerView.ViewHolder {
        ItemBackgroundAdapterBinding binding;

        public ThemesHolder(ItemBackgroundAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

