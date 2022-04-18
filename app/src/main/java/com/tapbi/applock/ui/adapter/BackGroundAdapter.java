package com.tapbi.applock.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tapbi.applock.common.model.BackGround;
import com.tapbi.applock.databinding.ItemBackgroundAdapterBinding;
import com.tapbi.applock.interfaces.ClickBackGround;

import java.util.List;

public class BackGroundAdapter extends RecyclerView.Adapter<BackGroundAdapter.BackGroundHolder> {
    private final Context context;
    private final ClickBackGround click;
    private List<BackGround> list;
    private int selected;

    public BackGroundAdapter(Context context, ClickBackGround click) {
        this.context = context;
        this.click = click;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeList(List<BackGround> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @NonNull
    @Override
    public BackGroundAdapter.BackGroundHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BackGroundHolder(ItemBackgroundAdapterBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BackGroundAdapter.BackGroundHolder holder,
                                 @SuppressLint("RecyclerView") int position) {

        BackGround backGround = list.get(position);
        holder.binding.tvDefault.setVisibility(View.GONE);
        if (!TextUtils.isDigitsOnly(backGround.getUri())) {
            Glide.with(context).load(backGround.getUri()).override(323, 615).
                    transform(new CenterCrop(), new RoundedCorners((20))).into(holder.binding.img);
            holder.itemView.setOnClickListener(view -> {
                Log.e("NGOCANH", "adapter click1: " +selected);

                notifyItemChanged(selected);
                selected = position;
                notifyItemChanged(selected);
                click.click(list.get(selected));

                Log.e("NGOCANH", "adapter click2: " +selected);

            });
            holder.binding.imgIcTick.setVisibility(selected == position ? View.VISIBLE : View.GONE);

            holder.binding.img.setVisibility(View.VISIBLE);
            holder.binding.imgAdd.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.img.setVisibility(View.INVISIBLE);
            if (!backGround.getUri().isEmpty()) {
                holder.binding.imgIcTick.setVisibility(View.GONE);
                holder.binding.imgAdd.setImageResource(Integer.parseInt(backGround.getUri()));
                holder.itemView.setOnClickListener(view -> click.addBackGroundFromDevice());
                holder.binding.imgAdd.setVisibility(View.VISIBLE);

            } else {
                holder.binding.imgAdd.setVisibility(View.INVISIBLE);

                holder.binding.tvDefault.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(view -> {
                    notifyItemChanged(selected);
                    selected = position;
                    notifyItemChanged(selected);
                    click.click(list.get(selected));
                });
                holder.binding.imgIcTick.setVisibility(selected == position ? View.VISIBLE : View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        if (list != null)
            return list.size();
        return 0;
    }

    public static class BackGroundHolder extends RecyclerView.ViewHolder {
        ItemBackgroundAdapterBinding binding;

        public BackGroundHolder(ItemBackgroundAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}


