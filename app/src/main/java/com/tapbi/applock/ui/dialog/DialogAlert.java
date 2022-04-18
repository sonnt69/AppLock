package com.tapbi.applock.ui.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;


import com.tapbi.applock.databinding.DialogChangePassSuccessBinding;

import java.util.Objects;

public class DialogAlert {
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;


    public DialogAlert(Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        DialogChangePassSuccessBinding dialogBinding = DialogChangePassSuccessBinding.inflate(LayoutInflater.from(context));
        builder.setView(dialogBinding.getRoot());
        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }


    public void dismissDialog() {
        alertDialog.dismiss();
    }
}

