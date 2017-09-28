package com.myapplication.threads;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.myapplication.listeners.DataReceivedListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;


public class BaseThread extends Thread {


    private String url;
    private Map<String,String> params = null;
    private DataReceivedListener dataReceivedListener;
    private Handler handler;


    public BaseThread (request r, @Nullable Map<String,String> params, @Nullable DataReceivedListener dataReceivedListener ){

        url = r.sendRequest();
        this.params = params;
        this.dataReceivedListener = dataReceivedListener;
        handler = new Handler();

    }


    public enum request {

        CHECK_USER,
        REGISTER,
        CONFIRM_REGISTRATION,
        UPDATE,
        CITIES_LIST,

        FIND_BOOK,
        ADD_BOOK,
        AUTO_BOOK_NAME,
        MY_BOOK_STATUS,
        UPDATE_MY_BOOKS,

        GET_USERS,
        GET_USER_BOOK_PREF,
        GET_BOOK_OWNERS,
        INSERT_INTO_CHAT_DB;

        public String sendRequest() {

            String baseUrl = "http://www.loliapps.com/php/";
            String url = "";

            switch (this) {

                case CHECK_USER:
                    url = baseUrl + "check_user.php";
                    break;
               case REGISTER:
                    url = baseUrl + "register.php";
                    break;
                case CONFIRM_REGISTRATION:
                    url = baseUrl + "confirm_checked.php";
                    break;
                case UPDATE:
                    url = baseUrl + "update_user.php";
                    break;
                case CITIES_LIST:
                    url = baseUrl + "cities.php";
                    break;
                case FIND_BOOK:
                    url = baseUrl + "book_search.php";
                    break;
                case ADD_BOOK:
                    url = baseUrl + "add_book.php";
                    break;
                case AUTO_BOOK_NAME:
                    url = baseUrl + "book_name_list.php";
                    break;
                case MY_BOOK_STATUS:
                    url = baseUrl + "my_books.php";
                    break;
                case UPDATE_MY_BOOKS:
                    url = baseUrl + "update_my_books.php";
                    break;
                case GET_USER_BOOK_PREF:
                    url = baseUrl + "get_books_by_user_pref.php";
                    break;
                case GET_USERS:
                    url = baseUrl + "get_users.php";
                    break;
                case GET_BOOK_OWNERS:
                    url = baseUrl + "get_book_owner.php";
                    break;
                case INSERT_INTO_CHAT_DB:
                    url = baseUrl + "send_fcm_msg.php";
                    break;
            }

            return url;
        }

    }


    @Override
    public void run() {

        HttpURLConnection con = null;

        try {
            URL myRequest = new URL(url);
            con = (HttpURLConnection) myRequest.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);

            if (params != null){

                String sentData = "";
                ArrayList<String> keys = new ArrayList<String>(params.keySet());

                for (int p = 0; p < keys.size(); p++) {

                    if (p == keys.size() - 1) {
                        sentData += URLEncoder.encode(keys.get(p).toString(), "UTF-8") + "=" + URLEncoder.encode(params.get(keys.get(p)), "UTF-8");
                    } else {
                        sentData += URLEncoder.encode(keys.get(p).toString(), "UTF-8") + "=" + URLEncoder.encode(params.get(keys.get(p)), "UTF-8") + "&";
                    }
                }

                Log.d("lil",sentData);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
                writer.write(sentData);
                writer.flush();
                writer.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            String receiveData = "";

            while ((line = reader.readLine()) != null){
                receiveData += line;
            }

            reader.close();

            if(dataReceivedListener != null) {

                final String result = receiveData;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataReceivedListener.onDataReceived(result);
                    }
                });

            }

        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }finally {
            if (con != null){
                con.disconnect();
            }
        }
    }

}
