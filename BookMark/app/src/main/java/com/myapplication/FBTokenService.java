package com.myapplication;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class FBTokenService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String my_token = FirebaseInstanceId.getInstance().getToken();
    }
}
