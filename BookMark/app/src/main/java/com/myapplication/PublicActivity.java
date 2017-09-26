package com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.objects.BookObject;
import com.myapplication.threads.BaseThread;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class PublicActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, DataReceivedListener {

    private Spinner search_type_spinner;
    private CircularImageView search_icon;

    private RelativeLayout search_layout;
    private EditText search_layout_et;
    private boolean isShowingSearchLayout = false;
    private DisplayMetrics displayMetrics;

    private Toolbar toolbar;
    private ArrayList<BookObject> bArray = new ArrayList<>();
    private GridView booksGridView;
    private BookObject singleBook;

    private ListView bookGenresList;
    private String[] book_type = {"רומן","מתח","ריגול","מדע בדיוני","פנטזיה/הרפתקאות","ביוגרפיה/היסטוריה","פילוסופיה","תוכנה/מחשבים"};
    private final String IMAGE_URL = "http://www.loliapps.com/images/book_image/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public);

        createToolBar(this,R.id.my_tool_bar,R.id.TopBar_menu,R.color.publicDark,BaseActivity.PUBLIC_ACTIVITY);
        navigationBar();

        settings = getSharedPreferences(getString(R.string.sharedPref) ,MODE_PRIVATE);
        int userId = settings.getInt(String.valueOf(R.string.sharedPrefUserId),0);
        displayMetrics = new DisplayMetrics();

        Map<String,String> params = new HashMap<>();
        params.put("uid",userId + "");
        BaseThread prefThread = new BaseThread(BaseThread.request.GET_USER_BOOK_PREF,params,this);
        prefThread.start();

        toolbar = (Toolbar) findViewById(R.id.my_tool_bar);
        search_icon = (CircularImageView) toolbar.findViewById(R.id.TopBar_userImage);
        search_icon.setOnClickListener(this);

        booksGridView = (GridView)findViewById(R.id.publicActivity_GridView);

        search_layout = (RelativeLayout) findViewById(R.id.search_book_frame_layout);
        search_layout.setOnTouchListener(new View.OnTouchListener() {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    if (inputMethodManager.isAcceptingText()) {
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        v.clearFocus();
                    }else {
                        search_layout.animate().setDuration(500).y(-3000);
                    }
                }
                return true;
            }
        });
        search_layout.setY(-(3000));

        search_layout_et = (EditText) findViewById(R.id.publicActivity_search_layout_et);
        bookGenresList = (ListView) findViewById(R.id.publicActivity_Genres_listView);
        bookGenresList.setVisibility(View.GONE);
        bookGenresList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,book_type));
        bookGenresList.setOnItemClickListener(this);
    }



    // ------------------------- button functionality ---------------------------

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override

    public void onClick(View v) {

            if(isShowingSearchLayout == false){
                search_layout.animate().setDuration(500).y(new Float(displayMetrics.heightPixels + toolbar.getHeight()));
            }else{
                search_layout.animate().setDuration(500).y(-3000);
            }

            isShowingSearchLayout = !isShowingSearchLayout;

    }

    public void searchByBookName(View view) {

        Map<String,String> params = new HashMap<>();
        params.put("type",0+"");
        params.put("book_name", search_layout_et.getText().toString().trim());

        BaseThread byBookName = new BaseThread(BaseThread.request.FIND_BOOK,params,this);
        byBookName.start();

    }

    public void searchByAuthor(View view) {

        Map<String,String> params = new HashMap<>();
        params.put("type",1+"");
        params.put("book_name", search_layout_et.getText().toString().trim());

        BaseThread byAuthor = new BaseThread(BaseThread.request.FIND_BOOK,params,this);
        byAuthor.start();

    }

    public void searchByGenres(View view) {

        int genres = checkGenres(search_layout_et.getText().toString().trim());

        if(genres > 0){
            Map<String,String> params = new HashMap<>();
            params.put("type",2+"");
            params.put("book_name", genres+"");

            BaseThread byGenres = new BaseThread(BaseThread.request.FIND_BOOK,params,this);
            byGenres.start();

        }else{
            bookGenresList.setVisibility(View.VISIBLE);
        }
    }

    private int checkGenres(String genres){

        int gen = 0;
        for(int i = 0; i < book_type.length; i++){
            if(book_type[i].contentEquals(genres)){
                gen = i+1;
            }
        } return gen;

    }

// ---------------------- listeners -----------------------------

    @Override
    public void onDataReceived(String data) {

        if(data.contentEquals("incompatible")){
            Toast.makeText(this,"אין תוצאות להצגה",Toast.LENGTH_LONG).show();

        }else {

            try {
                JSONArray jArray = new JSONArray(data);
                convertObjectToArray(jArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void convertObjectToArray(JSONArray jArray) {

        if (jArray.length() > 1) {
            bArray.clear();
            for (int i = 0; i < jArray.length(); i++) {
                try {
                    bArray.add(new BookObject(jArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }   createBookGallery();
        }else if (jArray.length() == 1){
            try {
                singleBook = new BookObject(jArray.getJSONObject(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            search_layout.animate().setDuration(500).y(-3000);
            isShowingSearchLayout = false;
            Intent intent = new Intent(this,BookDetailActivity.class);
            intent.putExtra("book",singleBook);
            startActivity(intent);
        }else{
            return;
        }
    }


    private void createBookGallery() {

        BookGridViewAdapter adapter = new BookGridViewAdapter(this,bArray);
        booksGridView.setAdapter(adapter);
        booksGridView.setOnItemClickListener(this);
        search_layout.animate().setDuration(500).y(-3000);
        isShowingSearchLayout = false;

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(parent == booksGridView) {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("book", bArray.get(position));
            startActivity(intent);
        }

        if (parent == bookGenresList){

            Map<String,String> params = new HashMap<>();
            params.put("type",2+"");
            params.put("book_name",position+1+"");
            BaseThread genresThread = new BaseThread(BaseThread.request.FIND_BOOK,params,this);
            genresThread.start();
            search_layout_et.setText("");
            bookGenresList.setVisibility(View.GONE);
        }

    }
}
