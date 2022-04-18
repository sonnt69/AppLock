package com.tapbi.applock.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.OnBackPressedCallback;

import com.tapbi.applock.databinding.ViewDeleteDialogBinding;
import com.tapbi.applock.interfaces.ResultDialog;
import com.tapbi.applock.utils.Utils;

import java.util.Objects;


public class DialogPermissions {
    private final AlertDialog.Builder builder;
    private final ResultDialog resultDialog;
    private final AlertDialog alertDialog;

    public DialogPermissions(Context context, String textBtnLeft, String textBtnRight, String typeDialog, int resource, ResultDialog resultDialog) {
        this.resultDialog = resultDialog;
        builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        ViewDeleteDialogBinding deleteDialogBinding = ViewDeleteDialogBinding.inflate(LayoutInflater.from(context));
        builder.setView(deleteDialogBinding.getRoot());
        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        deleteDialogBinding.inBtn.btnLeft.setText(textBtnLeft);
        deleteDialogBinding.inBtn.btnRight.setText(textBtnRight);
        deleteDialogBinding.tvTypeDialog.setText(typeDialog);
        deleteDialogBinding.imgWarning.setImageResource(resource);


        //click outside dismiss dialog
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.dismiss();
        new Utils();
        Utils.showDiaLogPermission = true;

        deleteDialogBinding.inBtn.btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                new Utils();
                Utils.showDiaLogPermission = true;
            }
        });
        deleteDialogBinding.inBtn.btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                resultDialog.result(true);
                new Utils();
                Utils.showDiaLogPermission = true;
            }
        });

        //click back dismiss dialog
        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d("NGOCANH", "back dialog");
                    alertDialog.dismiss();
                    new Utils();
                    Utils.showDiaLogPermission = true;
                }
                return true;
            }
        });

    }

    public boolean isShow(){
        return alertDialog.isShowing();
    }

    public void showDialog() {
        alertDialog.show();
    }

    public void hideDialog() {
        alertDialog.dismiss();
    }
}

