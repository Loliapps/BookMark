package com.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.objects.UserObject;
import com.myapplication.threads.BaseThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class AreaBookOwners extends BaseActivity implements DataReceivedListener, AdapterView.OnItemClickListener {

    private ListView ownersListView;
    private LinearLayout loadingLayout;
    private TextView searchResultTV;
    private ArrayList<UserObject> bOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_book_owners);

        createToolBar(this,R.id.my_tool_bar,R.id.TopBar_menu,R.color.publicDark,BaseActivity.PUBLIC_ACTIVITY);
        navigationBar();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_tool_bar);
        CircularImageView userImage = (CircularImageView) toolbar.findViewById(R.id.TopBar_userImage);
        File fImage = new File(getFilesDir(),"userImage.jpg");
        if(fImage.exists()) {
            userImage.setImageURI(Uri.fromFile(fImage));
        }else{
            userImage.setImageResource(R.drawable.u1);
        }

        int area = settings.getInt(String.valueOf(R.string.sharedPrefUserCountyId),0);
        String bookName = getIntent().getStringExtra(BookDetailActivity.SEARCH_BOOK_BY_NAME);

        searchResultTV = (TextView)findViewById(R.id.AreaBookOwners_search_result);
        searchResultTV.setText("תוצאות החיפוש עבור " + bookName);

        loadingLayout = (LinearLayout)findViewById(R.id.AreaBookOwners_progressBar);
        ownersListView = (ListView)findViewById(R.id.AreaBookOwners_listView);
        ownersListView.setOnItemClickListener(this);

        Map<String,String>params = new HashMap<>();
        params.put("area",area+"");
        params.put("bname",bookName);
        BaseThread getBOwner = new BaseThread(BaseThread.request.GET_BOOK_OWNERS,params,this);
        getBOwner.start();

    }


    @Override
    public void onDataReceived(String data) {

        loadingLayout.setVisibility(View.GONE);
        bOwner = new ArrayList<>();

        if(!bOwner.isEmpty()){
            bOwner.clear();
        }

        try {

            JSONObject obj = new JSONObject(data);
            JSONArray arr = obj.getJSONArray("owners");
            if(arr.length() == 0){
               searchResultTV.setText("לא נמצאו תוצאות העונות על החיפוש");
            }else {
                for (int i = 0; i < arr.length(); i++) {
                    bOwner.add(new UserObject(arr.getJSONObject(i)));
                }
                OwnersAdapter adapter = new OwnersAdapter(this,bOwner);
                adapter.notifyDataSetChanged();
                ownersListView.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final int bPosition = position;

        if(bOwner.get(position).getImg() != "default.jpg") {

        }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {


                    HttpURLConnection con = null;
                    FileOutputStream out = null;

                    try {
                        URL url = new URL("http://www.loliapps.com/images/" + bOwner.get(bPosition).getImg());
                        con = (HttpURLConnection) url.openConnection();
                        Bitmap bitmap = BitmapFactory.decodeStream(con.getInputStream());

                        File file = new File(getApplicationContext().getFilesDir(), bOwner.get(bPosition).getImg());
                        out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        con.disconnect();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (con != null) {
                            con.disconnect();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

        thread.start();


        Intent intent = new Intent(this,ChatRoomActivity.class);
        intent.putExtra("chatter_id",bOwner.get(position).getUserId());
        intent.putExtra("chatter_name",bOwner.get(position).getuName());
        intent.putExtra("chatter_image",bOwner.get(position).getImg());
        intent.setAction("chatter_data");
        startActivity(intent);

    }
}
