package com.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.myapplication.objects.MessageObject;
import java.util.ArrayList;



public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageObject> messages;
    private static int INCOMING = 1;
    private static int OUTGOING = 2;


    public MessageAdapter(@NonNull Context context, ArrayList<MessageObject>messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        if(viewType == INCOMING){
            view = LayoutInflater.from(context).inflate(R.layout.layout_message_in,parent,false);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.layout_message_out,parent,false);
        }

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        MessageObject mObject = messages.get(position);
        holder.msgText.setText(mObject.getMsg());

    }

    @Override
    public int getItemCount() {

        if(messages.size() > 0){
            return messages.size();
        }
        return 0;
    }


    @Override
    public int getItemViewType(int position) {

        MessageObject messageObject = messages.get(position);
        if(messageObject.getIncoming() == 0){
            return OUTGOING;
        }else{
            return INCOMING;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView msgText;

        public ViewHolder(View itemView) {
            super(itemView);
            msgText = (TextView) itemView.findViewById(R.id.chatters_msg);
        }
    }
}



