package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.myapplication.fragments.ConfirmDetailFragment;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.objects.FormValidation;
import com.myapplication.threads.BaseThread;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class NewUserActivity extends BaseActivity implements DataReceivedListener {

    private EditText userNameET,passwordET,rewritePassword,cityET;
    private String uName, uPass, uCity;
    private Uri imageFile;
    private Button register_btn;
    private CheckBox c;
    private ConfirmDetailFragment cFrag = new ConfirmDetailFragment();
    private String deviceId, token;
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private LinearLayout radio1, radio2;
    private String[] book_type  = {"רומן","מתח","ריגול","מדע בדיוני","פנטזיה/הרפתקאות","ביוגרפיה/היסטוריה","פילוסופיה","תוכנה/מחשבים"};
    private com.mikhaellopez.circularimageview.CircularImageView imageView;
    private FormValidation validation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        if(settings.contains(String.valueOf(R.string.sharedPrefVerify))) {
            cFrag.getNotificationData(settings.getString(String.valueOf(R.string.sharedPrefConfirmCode),""), settings.getString(String.valueOf(R.string.sharedPrefConfirmTime),""));
            cFrag.setCancelable(false);
            cFrag.show(getSupportFragmentManager(),"confirmReg");
        }

        this.registerReceiver(receiver,new IntentFilter("imageReceived"));
        this.registerReceiver(receiver,new IntentFilter("cName"));
        this.registerReceiver(receiver,new IntentFilter("confirm_notification"));

        validation = new FormValidation(this);

        deviceId        = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        token           = FirebaseInstanceId.getInstance().getToken();
        imageView       = (com.mikhaellopez.circularimageview.CircularImageView)findViewById(R.id.NewUserActivity_userImage);
        userNameET      = (EditText) findViewById(R.id.NewUserActivity_userName_editText);
        passwordET      = (EditText) findViewById(R.id.NewUserActivity_password_editText);
        rewritePassword = (EditText) findViewById(R.id.NewUserActivity_rewrite_password_editText);
        cityET          = (EditText) findViewById(R.id.NewUserActivity_city_editText);
        register_btn    = (Button) findViewById(R.id.newUser_register_btn);


        // ------ creating checkboxes --------

        radio1 = (LinearLayout) findViewById(R.id.radioGroup1);
        radio2 = (LinearLayout) findViewById(R.id.radioGroup2);
        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1);


        for (int i = 0; i < 4; i++) {
            c = new CheckBox(this);
            c.setText(book_type[i].toString());
            c.setLayoutParams(cParams);
            c.setMaxLines(1);
            c.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            c.setTag(i + 1);
            radio1.addView(c);
            checkBoxes.add(c);
        }

        for (int i = 4; i < 8; i++) {
            c = new CheckBox(this);
            c.setText(book_type[i].toString());
            c.setTag(i + 1);
            c.setLayoutParams(cParams);
            c.setMaxLines(1);
            c.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            radio2.addView(c);
            checkBoxes.add(c);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    // ----------------------- button functions ----------------------------



    public void openGallery(View view) {
        Intent intent = new Intent(this,ImagePickerActivity.class);
        startActivity(intent);
    }


    public void register(View view) {

        settings = getSharedPreferences(getString(R.string.sharedPref), MODE_PRIVATE);
        editor = settings.edit();
        String selected = "";

        uName = userNameET.getText().toString();
        uPass = passwordET.getText().toString().trim();
        String rePass = rewritePassword.getText().toString().trim();
        uCity = cityET.getText().toString().trim();

        if(validation.validateForm(uName,uPass,rePass,uCity) == true) {

            for(int i = 0; i < checkBoxes.size(); i++){
                if (checkBoxes.get(i).isChecked()){
                    selected += "1:";
                    editor.putBoolean(checkBoxes.get(i).getTag() + "", true);
                    editor.commit();
                }else{
                    selected += "0:";
                    editor.putBoolean(checkBoxes.get(i).getTag() + "", false);
                    editor.commit();
                }
            }

            File fImage;
            String image = "";

            if(imageFile != null) {

                fImage = new File(getFilesDir(),"userImage.jpg");
                Bitmap bitImage = BitmapFactory.decodeFile(fImage.getPath());
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                bitImage.compress(Bitmap.CompressFormat.JPEG,100,bOut);
                bitImage = Bitmap.createScaledBitmap(bitImage,100,100,false);
                image = Base64.encodeToString(bOut.toByteArray(),Base64.DEFAULT);

            }else{
                fImage = null;
                image = "default.jpg";
            }

            Map<String,String> params = new HashMap<>();
            params.put("uname",uName);
            params.put("pass",uPass);
            params.put("city",uCity);
            params.put("pref",selected);
            params.put("token",token);
            params.put("deviceid",deviceId);
            params.put("image",image);

            BaseThread RegisterThread = new BaseThread(BaseThread.request.REGISTER,params,this);
            RegisterThread.start();


            register_btn.setEnabled(false);
        }
    }



// ------------------------- listeners ----------------------------


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return super.onTouchEvent(event);
    }


    @Override
    public void onDataReceived(String data) {

        if (data.contains("cities")){
            validation.createAlert(data);
            register_btn.setEnabled(true);
        }

        if (data.contentEquals("שם משתמש תפוס") || data.contentEquals("שגיאה בקליטת נתונים") || data.contentEquals("יישוב לא מוכר") || data.contentEquals("registered device")){
            validation.createAlert(data);
            register_btn.setEnabled(true);
        }

        if (data.contentEquals("משתמש חדש נוצר בהצלחה")){
            editor.putString(String.valueOf(R.string.sharedPrefUserName),uName);
            editor.putString(String.valueOf(R.string.sharedPrefUserPass),uPass);
            editor.putString(String.valueOf(R.string.sharedPrefUserCity),uCity);
            editor.putString(String.valueOf(R.string.sharedPrefVerify),"notVerified");
            editor.commit();
        }


    }


    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

       if(intent.getAction() == "imageReceived") {
           imageFile = Uri.fromFile(new File(getFilesDir(), "userImage.jpg"));
           imageView.setImageURI(null);
           imageView.setImageURI(imageFile);
       }

       if(intent.getAction() == "cName"){
           cityET.setText(intent.getStringExtra("cityName"));
       }

       if(intent.getAction() == "confirm_notification"){
           editor.putString(String.valueOf(R.string.sharedPrefConfirmCode),intent.getStringExtra("message"));
           editor.putString(String.valueOf(R.string.sharedPrefConfirmTime),intent.getStringExtra("time"));
           editor.putString(String.valueOf(R.string.sharedPrefVerify),"not verified");
           editor.commit();
           cFrag.getNotificationData(intent.getStringExtra("message"), intent.getStringExtra("time"));
           cFrag.setCancelable(false);
           cFrag.show(getSupportFragmentManager(),"confirmReg");
       }
        }
    };
}
