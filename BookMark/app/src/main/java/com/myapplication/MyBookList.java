package com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.myapplication.fragments.DefaultFragment;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.objects.MyBookObject;
import com.myapplication.threads.BaseThread;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class MyBookList extends BaseActivity implements DataReceivedListener {

    private int ownerId;
    private RecyclerView myBookListView;
    private LinearLayout borrower_list_fragment;
    private MyBooksListAdapter adapter;
    private ListView bListView;
    private DBOpenHelper helper;
    private Parcelable recyclerViewState;
    private SQLiteDatabase database;
    private ArrayList<String> borrowers = new ArrayList<>();
    private ArrayList<MyBookObject>myBooks = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_book_list);

        createToolBar(this,R.id.my_tool_bar,R.id.TopBar_menu,R.color.darkGreen,BaseActivity.MY_BOOKS_ACTIVITY);
        navigationBar();

        settings = getSharedPreferences(getString(R.string.sharedPref),MODE_PRIVATE);
        ownerId = settings.getInt(String.valueOf(R.string.sharedPrefUserId),0);

        Map<String,String>params = new HashMap<>();
        params.put("userid",ownerId+"");
        BaseThread getMyBooks = new BaseThread(BaseThread.request.MY_BOOK_STATUS,params,this);
        getMyBooks.start();

        myBookListView = (RecyclerView) findViewById(R.id.MyBookListActivity_ListView);
        myBookListView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewState = myBookListView.getLayoutManager().onSaveInstanceState();

        borrower_list_fragment = (LinearLayout) findViewById(R.id.MyBookListActivity_fragment);
        bListView = (ListView) findViewById(R.id.MyBookListActivity_fragment_borrower_ListView);
    }


    @Override
    public void onDataReceived(String data) {

        if(data.contentEquals("no books")){
            DefaultFragment fragment = new DefaultFragment();
            fragment.show(getSupportFragmentManager(),"df");
        }else {
            try {
                JSONArray jsonArray = new JSONArray(data);
                for (int i = 0; i < jsonArray.length(); i++) {
                    myBooks.add(new MyBookObject(jsonArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter = new MyBooksListAdapter(this,myBooks,ownerId,borrower_list_fragment,bListView);
            myBookListView.setAdapter(adapter);
        }
    }



    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "reloadMyBooks"){
                myBookListView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(receiver,new IntentFilter("reloadMyBooks"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
