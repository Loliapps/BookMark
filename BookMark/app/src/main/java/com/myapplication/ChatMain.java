package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.myapplication.objects.UserObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;



public class ChatMain extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView chatList;
    private DBOpenHelper helper;
    private SQLiteDatabase database;
    private String chatterName;
    private ArrayList<UserObject> users = new ArrayList<>();
    private ChatListViewAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        String user = settings.getString(String.valueOf(R.string.sharedPrefUserName),"");

        createToolBar(this,R.id.my_tool_bar, R.id.TopBar_menu, R.color.brightBackground,BaseActivity.CHAT_ACTIVITY);
        navigationBar();

        this.registerReceiver(receiver, new IntentFilter("toChatMain"));
        this.registerReceiver(receiver, new IntentFilter("from_fcm"));

        chatList = (ListView)findViewById(R.id.chatActivity_listView);

        helper = new DBOpenHelper(this);
        database = helper.getReadableDatabase();
        readFromDB();

    }

    private void readFromDB(){

        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = df.format(gc.getTime());

        Cursor tableCursor = database.rawQuery(helper.readFromChatters(), null);
        if(tableCursor.getCount() > 0) {

            tableCursor.moveToFirst();
            users.add(addUserToArray(tableCursor,formattedDate));

            while (tableCursor.moveToNext()) {
                users.add(addUserToArray(tableCursor,formattedDate));
            }

            adapter = new ChatListViewAdapter(this,users);
            chatList.setAdapter(adapter);
            chatList.setOnItemClickListener(this);
        }
    }



    private UserObject addUserToArray(Cursor tableCursor, String formattedDate){

        chatterName = tableCursor.getString(tableCursor.getColumnIndex(helper.COLUMN_CHATTER_NAME));
        int cId = tableCursor.getInt(tableCursor.getColumnIndex(helper.COLUMN_CHATTER_ID));
        String chatterImage = tableCursor.getString(tableCursor.getColumnIndex(helper.COLUMN_CHATTER_IMAGE));

        String tableName = tableCursor.getString(tableCursor.getColumnIndex(helper.COLUMN_CHATTER_TABLE));
        String msgDate = "";
        String msg = "";
        Cursor lastMsg = database.rawQuery(helper.readFromTable(tableName) , null);

        if (lastMsg.getCount() > 0){

            lastMsg.moveToLast();
            String sentDate = lastMsg.getString(lastMsg.getColumnIndex("cur_date"));

            if(formattedDate.equals(sentDate)){
                msgDate = lastMsg.getString(lastMsg.getColumnIndex("cur_time"));
            }else {
                msgDate = sentDate;
            }
            msg = lastMsg.getString(lastMsg.getColumnIndex("message"));
        }
        return new UserObject(chatterName, cId, chatterImage, msg, msgDate);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatter_name",users.get(position).getuName());
        intent.putExtra("chatter_id", users.get(position).getUserId());
        intent.putExtra("chatter_image",users.get(position).getImg());
        intent.setAction("chatter_data");
        startActivity(intent);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

       String message = intent.getStringExtra("message");
       int sender_id  = intent.getIntExtra("from_id",0);
       String time    = intent.getStringExtra("time");
       String name    = intent.getStringExtra("from_name");


         for(int i = 0; i< users.size(); i++){
           if (users.get(i).getUserId() == sender_id){
               users.get(i).setDate(time);
               users.get(i).setLastMsg(message);

               if(name != null) {
                   users.get(i).setName(name);
               }
               View view = chatList.getChildAt(i);
               chatList.getAdapter().getView(i,view,chatList);
           }
       }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}
