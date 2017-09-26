package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.objects.FormValidation;
import com.myapplication.threads.BaseThread;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class UpdateUser extends BaseActivity implements DataReceivedListener{

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private CheckBox c;
    private LinearLayout radio1, radio2;
    private String[] book_type  = {"רומן","מתח","ריגול","מדע בדיוני","פנטזיה/הרפתקאות","ביוגרפיה/היסטוריה","פילוסופיה","תוכנה/מחשבים"};
    private com.mikhaellopez.circularimageview.CircularImageView imageView;
    private Uri imageFile;
    private ImageView unlockUNameLock,unlockPassLocker,unlockCityLock;
    private EditText userNameET,passwordET,rewritePassword,cityET;
    private Map <String,String> params = new HashMap<>();
    private String uName,cityName;
    private Boolean imageChanged = false, userChanged = false;
    private Intent userUpdatedIntent = new Intent("userUpdated");
    private FormValidation validation;
    private Button updateBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        createToolBar(this,R.id.my_tool_bar,R.id.TopBar_menu,R.color.personal,BaseActivity.PERSONAL_ACTIVITY);
        navigationBar();

        settings = getSharedPreferences(getString(R.string.sharedPref),MODE_PRIVATE);
        editor = settings.edit();

        this.registerReceiver(receiver,new IntentFilter("imageReceived"));
        this.registerReceiver(receiver,new IntentFilter("cName"));

        validation = new FormValidation(this);

        userNameET       = (EditText) findViewById(R.id.updateUser_userName);
        passwordET       = (EditText) findViewById(R.id.updateUser_password_et);
        rewritePassword  = (EditText) findViewById(R.id.updateUser_retype_password_et);
        cityET           = (EditText) findViewById(R.id.updateUser_city);
        imageView        = (com.mikhaellopez.circularimageview.CircularImageView)findViewById(R.id.updateUser_user_image);
        unlockUNameLock  = (ImageView) findViewById(R.id.updateUser_unlock_userName_img);
        unlockPassLocker = (ImageView) findViewById(R.id.updateUser_unlock_password_img);
        unlockCityLock   = (ImageView) findViewById(R.id.updateUser_unlock_city_img);
        updateBtn        = (Button) findViewById(R.id.updateUser_updateBtn);

        File file = new File(getFilesDir(),"userImage.jpg");

        if(file.exists()){
            imageFile = Uri.fromFile(file);
            imageView.setImageURI(imageFile);
        }else{
            imageView.setImageResource(R.drawable.u1);
        }

        userNameET.setText(settings.getString(String.valueOf(R.string.sharedPrefUserName),""));
        passwordET.setText(settings.getString(String.valueOf(R.string.sharedPrefUserPass),""));
        rewritePassword.setText(settings.getString(String.valueOf(R.string.sharedPrefUserPass),""));
        cityET.setText(settings.getString(String.valueOf(R.string.sharedPrefUserCity),""));

        // ------ creating checkboxes --------

        radio1 = (LinearLayout) findViewById(R.id.updateUser_radioGroup1);
        radio2 = (LinearLayout) findViewById(R.id.updateUser_radioGroup2);

        for (int i = 0; i < 4; i++) {
            c = new CheckBox(this);
            c.setText(book_type[i].toString());
            c.setTextSize(12);
            c.setTag(i + 1);
            radio1.addView(c);
            checkBoxes.add(c);
        }

        for (int i = 4; i < 8; i++) {
            c = new CheckBox(this);
            c.setText(book_type[i].toString());
            c.setTag(i + 1);
            c.setTextSize(12);
            radio2.addView(c);
            checkBoxes.add(c);
        }

        for (int c = 0; c < checkBoxes.size(); c++) {
            boolean check = settings.getBoolean(checkBoxes.get(c).getTag() + "", false);
            if (check == false) {
                checkBoxes.get(c).setChecked(false);
            }
            if (check == true) {
                checkBoxes.get(c).setChecked(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

// ------------------------- methods ------------------------------------


    public void update(View view) {

        int uId = settings.getInt(String.valueOf(R.string.sharedPrefUserId),0);
        params.put("uid",uId+"");

        uName = userNameET.getText().toString();
        String passWord = passwordET.getText().toString().trim();
        String repeatPassword = rewritePassword.getText().toString().trim();
        cityName = cityET.getText().toString().trim();


        if (validation.validateForm(uName,passWord,repeatPassword,cityName) == true) {

            if (userNameET.isEnabled()) {
                params.put("uname", uName);
                editor.putString(String.valueOf(R.string.sharedPrefUserName), uName);
                editor.commit();
                userChanged = true;
            }

            if (passwordET.isEnabled()) {
                params.put("pass", passWord);
                editor.putString(String.valueOf(R.string.sharedPrefUserPass), passWord);
                editor.commit();
            }

            if (cityET.isEnabled()) {
                params.put("city", cityName);
            }

            checkChangedData();

            String selected = "";

            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    selected += "1:";
                    editor.putBoolean(checkBoxes.get(i).getTag() + "", true);
                    editor.commit();
                } else {
                    selected += "0:";
                    editor.putBoolean(checkBoxes.get(i).getTag() + "", false);
                    editor.commit();
                }
            }
            params.put("pref", selected);

            BaseThread updateUserThread = new BaseThread(BaseThread.request.UPDATE, params, this);
            updateUserThread.start();

            updateBtn.setEnabled(false);
        }
    }

    private void checkChangedData() {

        if(imageChanged == true) {

            String image = "";

            if (imageFile != null) {

                File fImage = new File(getFilesDir(), "userImage.jpg");
                Bitmap bitImage = BitmapFactory.decodeFile(fImage.getPath());
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                bitImage.compress(Bitmap.CompressFormat.JPEG, 50, bOut);
                image = Base64.encodeToString(bOut.toByteArray(), Base64.DEFAULT);

            } else {
                image = "default.jpg";
            }

            params.put("image", image);
        }

        if(userChanged == true){
            userUpdatedIntent.putExtra("userNameChaned",uName);
        }
            getApplicationContext().sendBroadcast(userUpdatedIntent);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return super.onTouchEvent(event);
    }


    public void unlockUserName(View view) {
        userNameET.setEnabled(true);
        unlockUNameLock.setImageResource(R.drawable.open_lock);
    }


    public void unlockPassword(View view) {
        passwordET.setEnabled(true);
        rewritePassword.setEnabled(true);
        unlockPassLocker.setImageResource(R.drawable.open_locker);

    }


    public void unlockCity(View view) {
        cityET.setEnabled(true);
        unlockCityLock.setImageResource(R.drawable.open_lock);
    }


    public void selectImage(View view) {
        Intent intent = new Intent(this,ImagePickerActivity.class);
        startActivity(intent);
    }




// ------------------------------------ user selected image -----------------------------


    @Override
    public void onDataReceived(String data) {

        if (data.contains("cities")){
            validation.createAlert(data);
            updateBtn.setEnabled(true);
        }

        if (data.contentEquals("שם משתמש תפוס") || data.contentEquals("שגיאה בקליטת נתונים") || data.contentEquals("יישוב לא מוכר")){
            validation.createAlert(data);
            updateBtn.setEnabled(true);
        }

        if(data.contains("updated")){

            try {
                JSONObject object = new JSONObject(data);

                if(object.getInt("city") != 0 && object.getInt("county") != 0) {
                    editor.putString(String.valueOf(R.string.sharedPrefUserCity),cityName);
                    editor.putInt(String.valueOf(R.string.sharedPrefUserCityId),object.getInt("city"));
                    editor.putInt(String.valueOf(R.string.sharedPrefUserCountyId),object.getInt("county"));
                    editor.commit();
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

       if(intent.getAction() == "imageReceived") {
           imageFile = Uri.fromFile(new File(getFilesDir(), "userImage.jpg"));
           imageView.setImageURI(null);
           imageView.setImageURI(imageFile);
           imageChanged = true;
       }

       if(intent.getAction() == "cName"){
           cityET.setText(intent.getStringExtra("cityName"));
       }
        }
    };
}
