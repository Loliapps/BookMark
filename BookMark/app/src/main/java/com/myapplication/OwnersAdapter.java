package com.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.myapplication.objects.UserObject;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;



public class OwnersAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<UserObject> owners;


    public OwnersAdapter(Context context, ArrayList<UserObject> owners) {
        super(context, R.layout.owner_list_item,owners);
        this.context = context;
        this.owners = owners;
    }


    private class ViewHolder{
        TextView userName, city, userId, sendMsg;
        com.mikhaellopez.circularimageview.CircularImageView img;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.owner_list_item,parent,false);
            viewHolder.userName = (TextView)convertView.findViewById(R.id.Owners_list_item_uName);
            viewHolder.city = (TextView)convertView.findViewById(R.id.Owners_list_item_city);
            viewHolder.img = (com.mikhaellopez.circularimageview.CircularImageView) convertView.findViewById(R.id.Owners_list_item_img);
            viewHolder.userId = (TextView)convertView.findViewById(R.id.Owners_list_item_uId);
            viewHolder.sendMsg = (TextView)convertView.findViewById(R.id.Owners_list_item_sendMsg);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.userName.setText(owners.get(position).getuName());
        viewHolder.city.setText(owners.get(position).getCity());
        viewHolder.userId.setText(owners.get(position).getUserId() + "");
        if(owners.get(position).getImg().contentEquals("default.jpg")){
            Picasso.with(context).load(R.drawable.u1).into(viewHolder.img);
        }else {
            Picasso.with(context).invalidate("http://www.loliapps.com/images/" + owners.get(position).getImg());
            Picasso.with(context).load("http://www.loliapps.com/images/" + owners.get(position).getImg()).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.u1).into(viewHolder.img);
        }

        String uId = viewHolder.userId.getText().toString();

        return convertView;
    }
}
