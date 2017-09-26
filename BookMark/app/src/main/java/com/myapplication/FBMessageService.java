package com.myapplication;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;



public class FBMessageService extends FirebaseMessagingService {

    private SQLiteDatabase database;
    private DBOpenHelper helper;
    private int borrowerId;
    private String borrowerName,borrowerImg;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        String title = data.get("title");
        String messageBody = data.get("body");
        String sound = data.get("sound");

        int ts = (int) System.currentTimeMillis();

        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyHH:mm");
        String formattedDate = df.format(gc.getTime());
        String date = formattedDate.substring(0, 8);
        String time = formattedDate.substring(8, formattedDate.length());


        if(messageBody.contains("קוד")){

            RemoteViews rView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.remote_view_notification);
            rView.setTextViewText(R.id.notification_title,title);
            rView.setTextViewText(R.id.notification_message,messageBody);
            rView.setTextViewText(R.id.notification_time,time);
            rView.setImageViewResource(R.id.notification_user_img,R.drawable.b_icon_bmp);

            SharedPreferences settings = getApplicationContext().getSharedPreferences(String.valueOf(R.string.sharedPref),MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(String.valueOf(R.string.sharedPrefConfirmCode),messageBody);
            editor.commit();

            Intent intent = new Intent(this, NewUserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, borrowerId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent cInent = new Intent("confirm_notification");
            cInent.putExtra("message", messageBody);
            cInent.putExtra("time", time);
            this.sendBroadcast(cInent);


            builder.setSmallIcon(R.drawable.b_icon).setOngoing(true)
                   .setCustomBigContentView(rView)
                   .setContentIntent(pendingIntent)
                   .setDefaults(NotificationCompat.DEFAULT_SOUND)
                   .setAutoCancel(false);
            manager.notify(9,builder.build());
        }else {

            String sender_id = data.get("id");
            borrowerImg = data.get("icon");
            borrowerId = Integer.parseInt(sender_id);
            RemoteViews smallView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.small_remote_notification);


            if(borrowerImg == "default.jpg"){
                smallView.setImageViewResource(R.id.small_notification_user_img, R.drawable.u1);
            }else {

                Bitmap bitImage = null;
                HttpURLConnection con = null;
                OutputStream out = null;

                try {
                    URL url = new URL("http://www.loliapps.com/images/" + borrowerImg);
                    con = (HttpURLConnection) url.openConnection();
                    bitImage = BitmapFactory.decodeStream(con.getInputStream());

                    File file = new File(getFilesDir(),borrowerImg);
                    out = new FileOutputStream(file);
                    bitImage.compress(Bitmap.CompressFormat.JPEG,100,out);
                    out.flush();
                    out.close();
                    con.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    smallView.setImageViewBitmap(R.id.small_notification_user_img, bitImage);
                    if(con != null){
                        con.disconnect();
                    }
                    if(out != null){
                        try{
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            borrowerName = data.get("name");

            helper = new DBOpenHelper(this);
            database = helper.getWritableDatabase();
            insertChatters();
            helper.createNewTable("table"+borrowerId);
            helper.insertIntoTable("table"+borrowerId, messageBody, time, date, 1);

            Activity currentActivity = ((MyApp)this.getApplicationContext()).getCurrentActivity();

            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra("notification_id", borrowerId);
            intent.putExtra("borrowerName",borrowerName);
            intent.putExtra("from_name", "table"+borrowerId);
            intent.putExtra("from_id", borrowerId);
            intent.putExtra("message", messageBody);
            intent.putExtra("borrowerImg", borrowerImg);
            intent.setAction("fcm_data");
            if(currentActivity != null){
               intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_NEW_TASK);
            }

            smallView.setTextViewText(R.id.small_notification_title, borrowerName);
            smallView.setTextViewText(R.id.small_notification_message, messageBody);
            smallView.setTextViewText(R.id.small_notification_time, time);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, borrowerId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setSmallIcon(R.drawable.b_icon)
                    .setCustomContentView(smallView)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setShowWhen(true);
            manager.notify(borrowerId, builder.build());

            // ------------- send brodcast --------------

            Intent receiverIntent = new Intent("from_fcm");
            receiverIntent.putExtra("message", messageBody);
            receiverIntent.putExtra("from_name", borrowerName);
            receiverIntent.putExtra("from_id", borrowerId);
            receiverIntent.putExtra("time", time);
            this.sendBroadcast(receiverIntent);

        }
    }


    private void insertChatters() {

        Cursor tableCursor = database.rawQuery("SELECT " + helper.COLUMN_CHATTER_ID + " FROM " + helper.TABLE_NAME + " WHERE " + helper.COLUMN_CHATTER_ID + "=" + borrowerId + ";", null);

        if(tableCursor.getCount()>0) {
            tableCursor.moveToFirst();
            helper.updateChatters(borrowerId, borrowerName, borrowerImg);
            tableCursor.close();
        }else{
            helper.insertIntoChatters(borrowerId, borrowerName, borrowerImg,"table"+borrowerId);
            tableCursor.close();
        }
    }

}
