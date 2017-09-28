package com.myapplication.threads;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.myapplication.listeners.UserImageDownloadListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;


public class DownloadUserImageThread extends Thread{

    private UserImageDownloadListener listener;
    private Handler handler;
    private Bitmap imageUri;
    private Context context;

    public DownloadUserImageThread(Context context,UserImageDownloadListener listener,Bitmap imageUri){
        this.listener = listener;
        this.context = context;
        this.imageUri = imageUri;
        handler = new Handler();
    }

    @Override
    public void run() {

        FileOutputStream out = null;
        InputStream in = null;

        try {

            out = context.openFileOutput("userImage.jpg", context.MODE_PRIVATE);
            imageUri.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            imageUri.recycle();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        handler.post(new Runnable() {
            @Override
            public void run() {
           listener.onUserImageDownloadCompleted("userImage.jpg");
            }
        });


    }

}
