package com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.threads.BaseThread;
import java.util.HashMap;
import java.util.Map;



public class MyBooksActivity extends BaseActivity implements DataReceivedListener {

    private int uId,uiOptions;
    private LinearLayout addBookLayout,viewBooksLayout,slideLayout;
    private EditText[]etCollection = new EditText[4];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        createToolBar(this,R.id.my_tool_bar,R.id.TopBar_menu,R.color.darkGreen,BaseActivity.MY_BOOKS_ACTIVITY);
        settings = getSharedPreferences(getString(R.string.sharedPref),MODE_PRIVATE);
        uId = settings.getInt(String.valueOf(R.string.sharedPrefUserId),0);

        navigationBar();


        etCollection[0] = (EditText) findViewById(R.id.addBook_et1);
        etCollection[1] = (EditText) findViewById(R.id.addBook_et2);
        etCollection[2] = (EditText) findViewById(R.id.addBook_et3);
        etCollection[3] = (EditText) findViewById(R.id.addBook_et4);

        addBookLayout = (LinearLayout) findViewById(R.id.MyBooksActivity_addBooks);
        viewBooksLayout = (LinearLayout) findViewById(R.id.MyBooksActivity_viewMyBooks);

        slideLayout = (LinearLayout) findViewById(R.id.addBook_frame_layout);
        slideLayout.setX(-(3000));

        slideLayout.setOnTouchListener(new View.OnTouchListener() {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    if (inputMethodManager.isAcceptingText()) {
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        v.clearFocus();
                    }else{
                        slideLayout.animate().setDuration(500).translationX(-3000);
                    }
                }

                return true;
            }
        });

        addBookLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    slideLayout.animate().setDuration(500).translationX(0);
                }
                return true;
            }
        });

        viewBooksLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent(getApplicationContext(), MyBookList.class);
                    startActivity(intent);
                }
                    return true;
            }
        });

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void upload_books(View view) {

        Map<String,String> params = new HashMap<>();
        String books = "";

        for (int i = 0; i < etCollection.length; i++){
            if (etCollection[i].getText().toString().trim().length() > 0){
                books += etCollection[i].getText().toString().trim() + ":";
            }
        }

        params.put("books",books);
        params.put("userid",uId+"");

        BaseThread addBooksThread = new BaseThread(BaseThread.request.ADD_BOOK,params,this);
        addBooksThread.start();

        for (int i = 0; i < etCollection.length; i++){
            etCollection[i].setText("");
        }

    }

    @Override
    public void onDataReceived(String data) {

        if (data.contentEquals("success")){
            Toast.makeText(this,"הספרים נרשמו בהצלחה",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this,"שגיאה בקליטת הנתונים",Toast.LENGTH_LONG).show();
        }
    }

}
