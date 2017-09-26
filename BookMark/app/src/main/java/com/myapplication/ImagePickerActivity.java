package com.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.myapplication.listeners.UserImageDownloadListener;
import com.myapplication.threads.DownloadUserImageThread;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;



public class ImagePickerActivity extends AppCompatActivity implements ScaleGestureDetector.OnScaleGestureListener, UserImageDownloadListener {

    private static final int SELECT_PICT = 7;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private ImageView frame,v = null;
    private ScaleGestureDetector scaleGD;
    private Drawable d;
    private int imgNewHeight, imgNewWidth, LEFT_BORDER = 0, TOP_BORDER = 0, RIGHT_BORDER, BOTTOM_BORDER;
    private float fixX,fixY,xyRatio,scale = 1f, FirstX, FirstY;
    private boolean dragging;
    private int[] location;
    private LinearLayout mainLayout;
    private LinearLayout.LayoutParams params;
    private double height, width, ratio;
    private UserImageDownloadListener listener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RIGHT_BORDER = metrics.widthPixels;
        BOTTOM_BORDER = metrics.heightPixels;
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        frame    = (ImageView) findViewById(R.id.imagePicker_frame);
        scaleGD = new ScaleGestureDetector(this,this);
        Button openGal  = (Button) findViewById(R.id.imagePicker_open_gallery_btn);
        openGal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageGallery();
            }
        });
        openImageGallery();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Bitmap scaled = null;
        Bitmap blurImage = null;


        if(requestCode == SELECT_PICT && resultCode == RESULT_OK) {

            if (data == null) {
                return;
            } else {

                if(v != null) {
                    mainLayout.removeView(v);
                }
                scale = 1f;

                Bitmap bitmap = convertUriToBitmap(data.getData());
                height = bitmap.getHeight();
                width = bitmap.getWidth();

                if(height > width){  // portrate

                    ratio = frame.getLayoutParams().width / width;
                    scaled = bitmap.createScaledBitmap(bitmap,(int)(width*ratio),(int)(height * ratio),false);
                    v = new ImageView(this);
                    v.setImageBitmap(scaled);

                    FirstX = (RIGHT_BORDER-scaled.getWidth())/2;
                    FirstY = (BOTTOM_BORDER-scaled.getHeight())/2;
                    location = new int[2];

                    imgNewWidth = scaled.getWidth();
                    imgNewHeight = scaled.getHeight();

                    params = new LinearLayout.LayoutParams(imgNewWidth,imgNewHeight);
                    mainLayout.addView(v,params);
                    xyRatio = scale;
                }

                else if(height < width){ // landscape
                    ratio = frame.getLayoutParams().height / height;
                    scaled = bitmap.createScaledBitmap(bitmap,(int)(width*ratio),(int)(height * ratio),false);
                    v = new ImageView(this);
                    v.setImageBitmap(scaled);

                    FirstX = (RIGHT_BORDER-scaled.getWidth())/2;
                    FirstY = (BOTTOM_BORDER-scaled.getHeight())/2;

                    location = new int[2];

                    imgNewWidth = scaled.getWidth();
                    imgNewHeight = scaled.getHeight();
                    params = new LinearLayout.LayoutParams(imgNewWidth,imgNewHeight);
                    mainLayout.addView(v,params);
                    xyRatio = scale;


                }else{
                    ratio = frame.getLayoutParams().height / height;
                    scaled = bitmap.createScaledBitmap(bitmap,(int)(width*ratio),(int)(height * ratio),false);
                    v = new ImageView(this);
                    v.setImageBitmap(scaled);

                    FirstX = (RIGHT_BORDER-scaled.getWidth())/2;
                    FirstY = (BOTTOM_BORDER-scaled.getHeight())/2;
                    location = new int[2];

                    imgNewWidth = scaled.getWidth();
                    imgNewHeight = scaled.getHeight();

                    params = new LinearLayout.LayoutParams(imgNewWidth,imgNewHeight);
                    mainLayout.addView(v,params);
                    xyRatio = scale;
                }

            }
        }else {return;}

    }


// --------------------------------------- handling bitmap -------------------------------------------


    private Bitmap convertUriToBitmap(Uri data) {

        InputStream is = null;
        Bitmap bitmap = null;

        try {
            is = getContentResolver().openInputStream(data);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

// --------------------------------------- on scale -------------------------------------------


    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        v.getLocationOnScreen(location);

        scale = scale * detector.getScaleFactor();
        if (scale > 2.5){
            scale = (float) 2.5;
        } if(scale < xyRatio){
            scale = xyRatio;
        }

        v.setScaleY(scale);
        v.setScaleX(scale);

        imgNewHeight = (int)(v.getHeight() * scale);
        imgNewWidth  = (int)(v.getWidth() * scale);

        FirstX = (RIGHT_BORDER-imgNewWidth)/2;
        FirstY = (BOTTOM_BORDER-imgNewHeight)/2;

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }


// --------------------------------------- on touch -------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        v.getLocationOnScreen(location);
        scaleGD.onTouchEvent(event);

        v.getLocationOnScreen(location);


        switch(event.getAction()){

            case MotionEvent.ACTION_DOWN:

                dragging = true;

                if(event.getX() <= FirstX + imgNewWidth && event.getX() >= FirstX &&
                        event.getY() <= FirstY + imgNewHeight && event.getY() >= FirstY){

                    fixX = event.getX()-v.getX();
                    fixY = event.getY()-v.getY();
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (dragging){

                    v.setX(event.getX()-fixX);
                    v.setY(event.getY()-fixY);

                }
                break;

            case MotionEvent.ACTION_UP:

                dragging = false;

                if(location[0] > frame.getLeft()){
                    location[0] = (imgNewWidth-frame.getWidth())/2;
                    v.setTranslationX(location[0]);
                }

                if(location[0]+imgNewWidth < frame.getRight()){
                    location[0] = -(imgNewWidth-frame.getWidth())/2;
                    v.setTranslationX(location[0]);
                }

                if(location[1] > frame.getTop()){
                    location[1] = (imgNewHeight - frame.getHeight()) / 2;
                    v.setTranslationY(location[1]);
                }

                if(location[1]+imgNewHeight < frame.getBottom()){
                    location[1] = -(imgNewHeight - frame.getHeight()) / 2;
                    v.setTranslationY(location[1]);
                }


                break;
        }

        return true;
    }



// --------------------------------------- buttons functionallity -------------------------------------------

    public void openImageGallery() {
        if(checkReadExternalStoragePermission() == true){
            startActivityForResult(startImagePicker(), SELECT_PICT);
        }
    }

    public void saveImage(View view) {

        if(v == null){
            return;
        }else {
            View screenView = frame.getRootView();
            screenView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
            screenView.setDrawingCacheEnabled(false);
            int[] l = new int[2];
            frame.getLocationInWindow(l);
            Bitmap cropped = Bitmap.createBitmap(bitmap, l[0], l[1], frame.getWidth(), frame.getHeight());
            cropped = Bitmap.createScaledBitmap(cropped, 100, 100, false);

            DownloadUserImageThread dThread = new DownloadUserImageThread(this, this, cropped);
            dThread.start();
        }
    }

    public void cancel(View view) {
        finish();
    }


    @Override
    public void onUserImageDownloadCompleted(String FileName) {
        Intent intent = new Intent("imageReceived");
        this.sendBroadcast(intent);
        finish();
    }


    private Boolean checkReadExternalStoragePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission
                    (this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                return true;

            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"app needs to view thumbnails", Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE_REQUEST_CODE);
                return false;
            }
        }else{
            return true;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivityForResult(startImagePicker(), SELECT_PICT);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }

    public Intent startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        return intent;
    }



}
