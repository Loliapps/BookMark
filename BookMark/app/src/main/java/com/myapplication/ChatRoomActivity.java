package com.myapplication;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.myapplication.objects.MessageObject;
import com.myapplication.threads.BaseThread;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatRoomActivity extends BaseActivity implements View.OnClickListener{

    private ImageView sendMsg;
    private int borrowerId,myId;
    private EditText et;
    private DBOpenHelper helper;
    private RecyclerView msgLisy;
    private String borrowerName,borrowerImg;
    private SQLiteDatabase writToDb;
    private ArrayList<MessageObject>messages = new ArrayList<>();
    private MessageAdapter adapter;
    private NotificationManager manager;
    private InputMethodManager imm;
    protected MyApp mMyApp;



    @Override
    public void onResume() {
        super.onResume();
        mMyApp.setCurrentActivity(this);
        this.registerReceiver(mMessageReceiver, new IntentFilter("from_fcm"));
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMyApp.setCurrentActivity(null);
        this.unregisterReceiver(mMessageReceiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        mMyApp = (MyApp)this.getApplicationContext();

        getWindow().setBackgroundDrawableResource(R.drawable.chat_bg_two);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        createToolBar(this,R.id.my_tool_bar,R.id.TopBar_menu,R.color.brightBackground,BaseActivity.CHAT_ACTIVITY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setNavigationBarColor(getApplicationContext().getResources().getColor(R.color.half_transparent, null));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getApplicationContext().getResources().getColor(R.color.half_transparent));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_tool_bar);
        TextView chatter_name = (TextView)toolbar.findViewById(R.id.TopBar_activity_name);
        CircularImageView userImage = (CircularImageView) toolbar.findViewById(R.id.TopBar_userImage);


        settings = getSharedPreferences(getString(R.string.sharedPref),MODE_PRIVATE);
        myId = settings.getInt(String.valueOf(R.string.sharedPrefUserId),0);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        helper = new DBOpenHelper(this);

        if(getIntent().getAction() == "chatter_data"){

            borrowerId = getIntent().getIntExtra("chatter_id",0);
            borrowerName = getIntent().getStringExtra("chatter_name");
            chatter_name.setText(borrowerName);
            borrowerImg = getIntent().getStringExtra("chatter_image");
            if(borrowerImg != "default.jpg") {
                userImage.setImageURI(Uri.fromFile(new File(getFilesDir(), borrowerImg)));
            }else{
                userImage.setImageResource(R.drawable.u1);
            }

            insertToChatters();
            helper.createNewTable("table"+borrowerId);
            readFromDB("table"+borrowerId);
        }

        if(getIntent().getAction() == "fcm_data"){

            borrowerName = getIntent().getStringExtra("borrowerName");
            chatter_name.setText(borrowerName);
            borrowerId = getIntent().getIntExtra("from_id",0);
            manager.cancel(getIntent().getIntExtra("notification_id",0));
            borrowerImg = getIntent().getStringExtra("borrowerImg");

            if(borrowerImg != "default.jpg") {
                userImage.setImageURI(Uri.fromFile(new File(getFilesDir(), borrowerImg)));
            }else{
                userImage.setImageResource(R.drawable.u1);
            }

            readFromDB("table"+borrowerId);
        }


        msgLisy = (RecyclerView) findViewById(R.id.chatRoom_scroller);
        msgLisy.setHasFixedSize(true);
        RecyclerView.LayoutManager lManager = new LinearLayoutManager(this,LinearLayout.VERTICAL,false);
        msgLisy.setLayoutManager(lManager);

        adapter = new MessageAdapter(this,messages);
        msgLisy.setAdapter(adapter);
        scrollMyListViewToBottom();

        et = (EditText)findViewById(R.id.chatRoom_et);

        sendMsg = (ImageView)findViewById(R.id.chatRoom_sendMsgBtn);
        sendMsg.setOnClickListener(this);

    }




// ----------------------------- methods ---------------------------------



    private void insertToChatters() {

        writToDb = helper.getReadableDatabase();
        Cursor tableCursor = writToDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='table"+borrowerId+"'", null);

        if(tableCursor.getCount()>0) {
            tableCursor.close();
            return;
        }else{
            helper.insertIntoChatters(borrowerId, borrowerName, borrowerImg,"table"+borrowerId);
            tableCursor.close();
        }
        writToDb.close();
    }

    private void readFromDB(String table) {

        writToDb = helper.getReadableDatabase();
        Cursor cursor = writToDb.rawQuery(helper.readFromTable(table), null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String msg = cursor.getString(cursor.getColumnIndex("message"));
            int incoming = cursor.getInt(cursor.getColumnIndex("incomming"));
            messages.add(new MessageObject(msg,incoming));

            while (cursor.moveToNext()) {
                msg = cursor.getString(cursor.getColumnIndex("message"));
                incoming = cursor.getInt(cursor.getColumnIndex("incomming"));
                messages.add(new MessageObject(msg,incoming));
            }
        }

        writToDb.close();
    }

    @Override
    public void onClick(View v) {
        // send msg via fcm

        String g = et.getText().toString();
        Pattern unSeenPattern = Pattern.compile("^[\\s\\n\\t]*$");
        Matcher unSeenMatcher = unSeenPattern.matcher(g);

        if (!g.isEmpty() && unSeenMatcher.matches() == false) {

            // date time check
            GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyHH:mm");
            String formattedDate = df.format(gc.getTime());
            String date = formattedDate.substring(0, 8);
            String time = formattedDate.substring(8, formattedDate.length());

            String msg = et.getText().toString().trim();

            Intent receiverIntent = new Intent("toChatMain");
            receiverIntent.putExtra("message",msg);
            receiverIntent.putExtra("from_id",borrowerId);
            receiverIntent.putExtra("time",time);
            this.sendBroadcast(receiverIntent);

            messages.add(new MessageObject(msg,0));
            adapter.notifyDataSetChanged();
            scrollMyListViewToBottom();

            HashMap<String, String> params = new HashMap<>();
            params.put("fromuser", myId + "");
            params.put("touser", borrowerId + "");
            params.put("msg", msg);

            BaseThread insertToChat = new BaseThread(BaseThread.request.INSERT_INTO_CHAT_DB, params, null);
            insertToChat.start();

            // insert to app db
            helper.insertIntoTable("table"+borrowerId, msg, time, date, 0);
            et.setText("");

        } else {
            return;
        }
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        String message = intent.getStringExtra("message");
        int sender_id = intent.getIntExtra("from_id",0);
        String time = intent.getStringExtra("time");

        if(borrowerId == sender_id) {
            manager.cancel(borrowerId);
            messages.add(new MessageObject(message, 1));
            adapter.notifyDataSetChanged();
            scrollMyListViewToBottom();
        }
        }
    };


    private void scrollMyListViewToBottom() {
        msgLisy.post(new Runnable() {
            @Override
            public void run() {
                msgLisy.scrollToPosition(messages.size()-1);
            }
        });
    }

}
