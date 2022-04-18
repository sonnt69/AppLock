package com.tapbi.applock.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.tapbi.applock.R;
import com.tapbi.applock.data.model.ImageThief;
import com.tapbi.applock.databinding.ItemBackgroundAdapterBinding;
import com.tapbi.applock.interfaces.ClickCatchIntruder;
import com.tapbi.applock.ui.main.MainActivity;

import java.util.List;

public class CatchIntruderAdapter extends RecyclerView.Adapter<CatchIntruderAdapter.CatchIntruderHolder> {

    private final Context context;
    private List<ImageThief> list;
    private boolean multipleSelect = false;
    private final ClickCatchIntruder catchIntruderClick;

    public CatchIntruderAdapter(Context context, ClickCatchIntruder catchIntruderClick) {
        this.context = context;
        this.catchIntruderClick = catchIntruderClick;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeList(List<ImageThief> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeMultipleSelect(boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatchIntruderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CatchIntruderHolder(ItemBackgroundAdapterBinding.inflate(LayoutInflater.from(context)));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull CatchIntruderHolder holder, int position) {
        final ImageThief imageThief = list.get(position);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Glide.with(context).load(imageThief.getUri()).override(323, 615).transform(new CenterCrop(), new RoundedCorners((20))).into(holder.binding.img);
            }
        });

        if (multipleSelect) {
            holder.binding.imgIcTick.setVisibility(View.VISIBLE);
            if (!imageThief.isSelected()) holder.binding.imgIcTick.setImageDrawable(null);
            else holder.binding.imgIcTick.setImageDrawable(context.getDrawable(R.drawable.ic_tick));

            holder.itemView.setOnClickListener(view -> {
                imageThief.setSelected(!imageThief.isSelected());
                if (imageThief.isSelected())
                    holder.binding.imgIcTick.setImageDrawable(context.getDrawable(R.drawable.ic_tick));
                else holder.binding.imgIcTick.setImageDrawable(null);

                if (imageThief.isSelected()) {
                    catchIntruderClick.select();
                } else {
                    catchIntruderClick.unSelect();
                }

                catchIntruderClick.multipleSelect();
            });

        } else {
            holder.binding.imgIcTick.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    catchIntruderClick.clickItem(imageThief);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(list != null){
            return list.size();
        }
        return 0;
    }

    public static class CatchIntruderHolder extends RecyclerView.ViewHolder {
        ItemBackgroundAdapterBinding binding;

        public CatchIntruderHolder(ItemBackgroundAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

//        public ImageView imageView;
//        public TextView tvTime;
//        public ImageView imgIcon;
//
//        public CatchIntruderHolder(@NonNull View itemView) {
//            super(itemView);
//            imageView = itemView.findViewById(R.id.imgRecentList);
//            imgIcon = itemView.findViewById(R.id.imgRecent_icon);
//            tvTime = itemView.findViewById(R.id.tvTimeList);
//        }
    }
}
