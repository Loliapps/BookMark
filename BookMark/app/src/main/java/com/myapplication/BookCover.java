package com.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;



public class BookCover extends DialogFragment{

    private Context context;
    private String url;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        url = getArguments().getString("url");
        Dialog builder = new Dialog(context);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_book_cover,null);
        ImageView cover = (ImageView)view.findViewById(R.id.only_cover);
        Picasso.with(context).load(url).into(cover);
        builder.setContentView(view);
        return builder;
    }

}
