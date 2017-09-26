package com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.threads.BaseThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;



public class LoginActivity extends BaseActivity implements DataReceivedListener {

    private EditText userNameET , passwordET;
    private TextView F_uName, F_Pass;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        settings = getSharedPreferences(getString(R.string.sharedPref), MODE_PRIVATE);
        editor = settings.edit();

        userNameET      = (EditText) findViewById(R.id.loginActivity_userName_editText);
        passwordET      = (EditText) findViewById(R.id.loginActivity_password_editText);
        Button register = (Button) findViewById(R.id.loginActivity_register_btn);

        if(settings.contains(String.valueOf(R.string.sharedPrefRegistered))){
            register.setVisibility(View.GONE);
        }

    }

// ---------------------------- button functionality --------------------

    public void checkRegisteredUser (View view) {

        Map<String,String> params = new HashMap<>();
        params.put("name", userNameET.getText().toString().trim());
        params.put("pass", passwordET.getText().toString().trim());

        BaseThread checkUser =  new BaseThread(BaseThread.request.CHECK_USER, params, this);
        checkUser.start();
    }

    public void goToNewUserActivity(View view) {

        Intent intent = new Intent(this,NewUserActivity.class);
        startActivity(intent);
    }

// --------------------------- listeners -------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return super.onTouchEvent(event);
    }

    @Override
    public void onDataReceived(String data) {

        try {

            JSONObject object = new JSONObject(data);
            String status = object.getString("status");

            if(status.contentEquals("no such user name")){
                Toast.makeText(this,"לא קיים משתמש בשם זה",Toast.LENGTH_LONG).show();
            }

            if (status.contentEquals("incorrect password")){
                Toast.makeText(this,"שם משתמש או סיסמה שגויים", Toast.LENGTH_LONG).show();
            }

            if(status.contentEquals("confirmed")){

                editor.putString(String.valueOf(R.string.sharedPrefUserName),userNameET.getText().toString().trim());
                editor.putString(String.valueOf(R.string.sharedPrefUserPass),passwordET.getText().toString().trim());
                editor.putInt(String.valueOf(R.string.sharedPrefUserId),object.getInt("user_id"));

                JSONArray bookPref = object.getJSONArray("reading_pref");
                for (int i = 0; i < bookPref.length(); i++){
                    if(bookPref.getInt(i) == 0) {
                        editor.putBoolean(i + 1 + "", false);
                        editor.commit();
                    }else{
                        editor.putBoolean(i+1+"",true);
                        editor.commit();
                    }
                }
                String cityName = object.getString("city_name");
                editor.putString(String.valueOf(R.string.sharedPrefUserCity),cityName);

                int cityId = object.getInt("city_id");
                editor.putInt(String.valueOf(R.string.sharedPrefUserCityId), cityId);

                int county_id = object.getInt("county_id");
                editor.putInt(String.valueOf(R.string.sharedPrefUserCountyId),county_id);

                editor.commit();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
