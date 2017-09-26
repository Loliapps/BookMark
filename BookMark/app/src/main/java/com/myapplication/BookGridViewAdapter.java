package com.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.myapplication.objects.BookObject;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;



public class BookGridViewAdapter extends ArrayAdapter {

    private ArrayList<BookObject> bookArray;
    private Context context;
    private final String IMAGE_URL = "http://www.loliapps.com/images/book_image/";


    public BookGridViewAdapter(Context context, ArrayList<BookObject> bookArray) {
        super(context, R.layout.books_gridview, bookArray);
        this.context = context;
        this.bookArray = bookArray;
    }


    private class ViewHolder {

        ImageView bookCover;
        TextView bookTitle;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
           viewHolder = new ViewHolder();
           convertView = LayoutInflater.from(context).inflate(R.layout.books_gridview,parent,false);
           viewHolder.bookTitle = (TextView)convertView.findViewById(R.id.gridview_text);
           viewHolder.bookCover = (ImageView)convertView.findViewById(R.id.gridview_img);
           convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(bookArray.get(position).getImg().equals("cover.jpg")){
            Picasso.with(context).load(R.drawable.cover).into(viewHolder.bookCover);
            viewHolder.bookTitle.setText(bookArray.get(position).getTitle());
        }
        else{
            Picasso.with(context).load(IMAGE_URL + bookArray.get(position).getImg()).into(viewHolder.bookCover);
        }

        return convertView;
    }

}
