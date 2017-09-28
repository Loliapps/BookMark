package com.myapplication.objects;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;



public class BookObject implements Serializable {

    private String title, author, genres, takzir, img;

    public BookObject(JSONObject job){
        try {

            title = job.getString("title");
            author = job.getString("author");
            genres = job.getString("genres");
            takzir = job.getString("takzir");
            img = job.getString("cover");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenres() {
        return genres;
    }

    public String getTakzir() {
        return takzir;
    }

    public String getImg() {
        return img;
    }
}
