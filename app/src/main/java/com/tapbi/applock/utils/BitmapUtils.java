package com.tapbi.applock.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapUtils {

    public static Uri saveImageBitmap(Context context, Bitmap bitmap, String name) {
        File mediaFile;
        String root =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir =new File(root+"/applock");
        if (!myDir.exists()) {
            if (myDir.mkdirs()) {
            }
        }
        long n = System.currentTimeMillis();
//        String videoName = "Image_$n.png";
        mediaFile =new  File(myDir.getAbsolutePath() + "/applock_" + name + ".png");
        try {
            mediaFile.createNewFile();
            if (mediaFile.exists()) {
                mediaFile.delete();
            }
            OutputStream outputStream =new FileOutputStream(mediaFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            context.sendBroadcast(
                    new Intent(

                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(mediaFile)
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(mediaFile);

    }
}
