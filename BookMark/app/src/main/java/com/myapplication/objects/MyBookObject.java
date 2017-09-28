package com.myapplication.objects;

import android.support.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;


public class MyBookObject implements Serializable {

    private String title, borrower,bImage,bDate;
    private int isAvailable,bId;
    private Boolean isHidden = true;

    public MyBookObject (JSONObject j){
        try {
            title       = j.getString("title");
            isAvailable = j.getInt("status");
            borrower    = j.getString("borrow");
            bId         = j.getInt("b_id");
            bImage      = j.getString("img");
            bDate       = j.getString("b_date");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getBorrower() { return borrower;}
    public void setBorrower(@Nullable String borrower){
        this.borrower = borrower;
    }

    public String getbImage(){ return bImage; }
    public void setbImage(@Nullable String bImage) {
        this.bImage = bImage;
    }

    public String getbDate() {return bDate;}
    public void setbDate(@Nullable String bDate) {
        this.bDate = bDate;
    }

    public int getbId() {
        return bId;
    }
    public void setBorrowerId(int bId){
        this.bId = bId;
    }

    public int getIsAvailable() {
        return isAvailable;
    }
    public void setIsAvailable(int isAvailable){
        this.isAvailable = isAvailable;
    }

    public Boolean getHidden() {
        return isHidden;
    }
    public void setHidden(Boolean hidden) {
        isHidden = hidden;
    }
}
