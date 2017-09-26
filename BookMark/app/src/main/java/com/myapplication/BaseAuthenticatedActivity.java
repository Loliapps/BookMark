package com.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;


public abstract class BaseAuthenticatedActivity extends BaseActivity {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!userExist()){
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        if(isVerified()){
            finish();
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);
            return;
        }
        else {

            onLoggedIn(savedInstanceState);
        }

    }

    protected abstract void onLoggedIn(Bundle savedInstanceState);



    private boolean userExist() {
        settings = getSharedPreferences(getString(R.string.sharedPref) ,MODE_PRIVATE);
        if(settings.contains(String.valueOf(R.string.sharedPrefUserName)))
            return true;
        else
            return false;
    }

    private boolean isVerified(){

        settings = getSharedPreferences(getString(R.string.sharedPref), MODE_PRIVATE);

        if(settings.contains(String.valueOf(R.string.sharedPrefVerify)))
            return true;

        else
        return false;
    }

}
