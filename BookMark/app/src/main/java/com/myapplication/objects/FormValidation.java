package com.myapplication.objects;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.myapplication.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class FormValidation {

    private Context context;

    public FormValidation(Context context) {
        this.context = context;
    }


    public boolean validateForm(String uName, String uPass, String rePass, String city ) {

        String[]et = {uName,uPass,rePass,city};

        for (int i = 0; i < et.length; i++) {
            if (et[i].isEmpty() || et[i] == null) {
                createAlert("empty");
                return false;
            }
        }

        Pattern uNamePattern = Pattern.compile("^([A-Za-z0-9])[A-Za-z0-9\\_\\-\\.\\s]{4,15}$");
        Matcher nameMatcher = uNamePattern.matcher(uName);
        if (nameMatcher.matches() == false) {
            createAlert("name");
            return false;
        }

        Pattern passPattern = Pattern.compile("^[A-Za-z0-9]{4,15}$");
        Matcher passMatcher = passPattern.matcher(uPass);
        if (passMatcher.matches() == false) {
            createAlert("pWord");
            return false;
        }


        if(!uPass.equals(rePass)){
            createAlert("no match");
            return false;
        }

        return true;
    }


    public void createAlert(String alertMsg) {

        final Dialog builder = new Dialog(context);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setCanceledOnTouchOutside(false);

        View view               = LayoutInflater.from(context).inflate(R.layout.default_fragment, null);
        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.default_fragment_MainLayout);
        TextView message        = (TextView) view.findViewById(R.id.default_fragment_textView);
        TextView separator      = (TextView) view.findViewById(R.id.default_fragment_separator);
        TextView confirmBtn     = (TextView)view.findViewById(R.id.default_fragment_btn);

        String msg = "";

        if(alertMsg.contentEquals("empty")){
           msg = "חובה למלא את כל שדות הטופס";
        }

        if(alertMsg.contentEquals("name")){
            msg = "שם משתמש מכיל 5-15 תוים,חייב להתחיל באות או מספר.יכול להכיל אותיות באנגלית, מספרים, מקפים, נקודות ורווחים.";
        }

        if(alertMsg.contentEquals("pWord")){
            msg = "הסיסמה מכילה 5-15 תוים יכולה להכיל ממספרים ואותיות באנגלית בלבד.";
        }

        if(alertMsg.contentEquals("no match")){
            msg = "סיסמאות לא תואמות";
        }

        if(alertMsg.contentEquals("registered device")){
            msg = "ניתן להרשם פעם אחת מכל מכשיר.";
        }

        if(alertMsg.contains("cities")){

            ArrayList<String> cityArray = convertJsonToArray(alertMsg);
            final ArrayAdapter adapter = new ArrayAdapter(context,R.layout.city_list_item,cityArray);
            ListView citiesList = new ListView(context);
            citiesList.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            citiesList.setAdapter(adapter);
            citiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String cName = adapter.getItem(position).toString();
                    Intent intent = new Intent("cName");
                    intent.putExtra("cityName",cName);
                    context.sendBroadcast(intent);
                    builder.dismiss();
                }
            });
            msg = "אולי התכוונת ל:";
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(35,0,35,35);
            mainLayout.addView(citiesList,1,params);
            confirmBtn.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);
        }

        if(alertMsg.contentEquals("שם משתמש תפוס") || alertMsg.contentEquals("שגיאה בקליטת נתונים")
                || alertMsg.contentEquals("יישוב לא מוכר")){
            msg = alertMsg;
        }

        message.setText(msg);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        builder.setContentView(view);
        builder.show();

    }

    private ArrayList<String> convertJsonToArray(String alertMsg) {

        ArrayList<String> cities = new ArrayList<>();

        try {
            JSONObject obj = new JSONObject(alertMsg);
            JSONArray arr = obj.getJSONArray("cities");
            for (int i = 0; i< arr.length(); i++){
                cities.add(arr.getString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cities;
    }

}
