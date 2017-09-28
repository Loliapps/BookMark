package com.myapplication.objects;

import org.json.JSONException;
import org.json.JSONObject;


public class UserObject {

    private String uName,city,img,lastMsg,date;
    private int userId;

    public UserObject(JSONObject jsonObject){

        try {
            userId = jsonObject.getInt("user_id");
            uName  = jsonObject.getString("user_name");
            city   = jsonObject.getString("city");
            img    = jsonObject.getString("img");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public UserObject(String uName, int userId, String img, String lastMsg, String date){
        this.uName   = uName;
        this.userId  = userId;
        this.img     = img;
        this.lastMsg = lastMsg;
        this.date    = date;
    }

    public String getuName() {
            return uName;
        }

    public void setName(String uName) { this.uName = uName;}

    public String getCity() {
            return city;
        }

    public String getImg() {
            return img;
        }

    public void setImg(String img) { this.img = img;}

    public int getUserId() {
            return userId;
        }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
