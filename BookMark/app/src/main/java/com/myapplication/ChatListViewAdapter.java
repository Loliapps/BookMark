package com.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.myapplication.objects.UserObject;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;



public class ChatListViewAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<UserObject>users;

    public ChatListViewAdapter(Context context, ArrayList<UserObject>users) {
        super(context, R.layout.chat_list_item,users);
        this.users = users;
        this.context = context;
    }


    private class ViewHolder{
        ImageView userImage;
        TextView userName,lastMsg,date;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_list_item,null);
            viewHolder.userImage = (ImageView)convertView.findViewById(R.id.chatListView_userImage);
            viewHolder.userName = (TextView)convertView.findViewById(R.id.chatListView_userName);
            viewHolder.lastMsg = (TextView)convertView.findViewById(R.id.chatListView_lastMsg);
            viewHolder.date = (TextView)convertView.findViewById(R.id.chatListView_time);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(users.get(position).getImg().contentEquals("default.jpg")){
            Picasso.with(context).load(R.drawable.u1).into(viewHolder.userImage);
        }else {
            Picasso.with(context).invalidate("http://www.loliapps.com/images/" + users.get(position).getImg());
            Picasso.with(context).load("http://www.loliapps.com/images/" + users.get(position).getImg()).networkPolicy(NetworkPolicy.NO_CACHE).error(R.drawable.u1).into(viewHolder.userImage);
        }


        viewHolder.userName.setText(users.get(position).getuName());
        viewHolder.lastMsg.setText(users.get(position).getLastMsg());
        viewHolder.date.setText(users.get(position).getDate());

        return convertView;

    }

}
