package com.tapbi.applock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.tapbi.applock.service.Accessibility;

public class Restart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("NGOCANH", "Restart");
        //if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        if (intent.getAction().equals("RS")) {
            //Log.e("Broadcast Listened", "Service tried to stop");
//            Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context.getApplicationContext(), Accessibility.class));
            } else {
                context.startService(new Intent(context.getApplicationContext(), Accessibility.class));
            }
        }

    }
}
