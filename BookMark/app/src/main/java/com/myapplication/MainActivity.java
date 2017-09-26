package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.io.File;



public class MainActivity extends BaseAuthenticatedActivity {

    private TextView userName;
    private com.mikhaellopez.circularimageview.CircularImageView user_image;
    private Uri imageFile;


    @Override
    protected void onLoggedIn(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(getString(R.string.sharedPref), MODE_PRIVATE);
        String user = settings.getString(String.valueOf(R.string.sharedPrefUserName), "");

        this.registerReceiver(receiver,new IntentFilter("userUpdated"));

        userName = (TextView) findViewById(R.id.mainActivity_userName_label);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Tekton_pro_bold.otf");
        userName.setTypeface(typeface);
        userName.setText("שלום " + user);

        user_image = (com.mikhaellopez.circularimageview.CircularImageView) findViewById(R.id.MainActivity_user_image);
        File fImage = new File(getFilesDir(), "userImage.jpg");
        if (fImage.exists()) {
            user_image.setImageURI(Uri.fromFile(fImage));
        } else {
            user_image.setImageResource(R.drawable.u1);
        }

    }

    public void goToPublicActivity(View view) {
        Intent intent = new Intent(this, PublicActivity.class);
        startActivity(intent);
    }

    public void goToPersonalActivity(View view) {
        Intent intent = new Intent(this,UpdateUser.class);
        startActivity(intent);
    }

    public void goToMyBooksActivity(View view) {
        Intent intent = new Intent(this, MyBooksActivity.class);
        startActivity(intent);
    }

    public void goToChatActivity(View view) {
        Intent intent = new Intent(this, ChatMain.class);
        startActivity(intent);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        userName.setText("שלום " + intent.getStringExtra("userNameChaned"));
        Boolean uImg = intent.getBooleanExtra("newImage",false);
        if(uImg == true) {
            imageFile = Uri.fromFile(new File(getFilesDir(),"userImage.jpg"));
            user_image.setImageURI(null);
            user_image.setImageURI(imageFile);
        }
        }
    };


    @Override
    protected void onDestroy() {

        try
        { if (receiver != null)
                unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e) { Log.d("lil",e.toString());}

        super.onDestroy();

    }

}