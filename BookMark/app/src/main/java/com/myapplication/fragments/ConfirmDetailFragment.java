package com.myapplication.fragments;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.myapplication.MainActivity;
import com.myapplication.R;
import com.myapplication.listeners.DataReceivedListener;
import com.myapplication.objects.FormValidation;
import com.myapplication.threads.BaseThread;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ConfirmDetailFragment extends DialogFragment implements DataReceivedListener, View.OnClickListener {

    private Context context;
    private String name;
    private EditText editText;
    private TextView confirmBtn;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private FormValidation validation;
    private InputMethodManager inputMethodManager;
    private String msg,time;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        validation = new FormValidation(context);
        settings = context.getSharedPreferences(getString(R.string.sharedPref), context.MODE_PRIVATE);
        editor = settings.edit();

        name = settings.getString(String.valueOf(R.string.sharedPrefUserName),"");

        Dialog builder = new Dialog(context);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        //builder.setCancelable(false);//.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(context).inflate(R.layout.fragment_confirm_detail, null);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                return true;
            }
        });

        RelativeLayout notification = (RelativeLayout) view.findViewById(R.id.confirm_detail_fragment_notification);

        ImageView img = (ImageView) notification.findViewById(R.id.notification_user_img);
        img.setImageResource(R.drawable.b_icon_bmp);
        TextView T_text = (TextView) notification.findViewById(R.id.notification_title);
        T_text.setText("הסיפרייה - פשוט מתחברים");
        TextView M_text = (TextView) notification.findViewById(R.id.notification_message);
        M_text.setText(msg);
        TextView Time_Text = (TextView) notification.findViewById(R.id.notification_time);
        Time_Text.setText(time);

        confirmBtn = (TextView)view.findViewById(R.id.confirm_detail_fragment_btn);
        editText = (EditText)view.findViewById(R.id.confirm_detail_fragment_edit_text);

        confirmBtn.setOnClickListener(this);
        builder.setContentView(view);
        return builder;

    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private boolean validateCode(String code){

        if(code.isEmpty()){
            return false;
        }

        else if (code != null && !code.isEmpty()) {
            Pattern pattern = Pattern.compile("^[a-z0-9]{4,20}$");
            Matcher matcher = pattern.matcher(code);
            if (matcher.matches() == false) {
                Toast.makeText(context,"שים לב !\nקוד האישור מכיל אותיות קטנות באנגלית\nומספרים בלבד", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {

        String codeText = editText.getText().toString().trim();
        editText.clearFocus();
        inputMethodManager.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if(validateCode(codeText) == true) {

            Map<String,String> params = new HashMap<>();
            params.put("code",codeText);
            params.put("name",name);

            BaseThread confirmThread = new BaseThread(BaseThread.request.CONFIRM_REGISTRATION,params,this);
            confirmThread.start();
        }

    }

    @Override
    public void onDataReceived(String data) {
        Log.d("lil",data);
        if (data.contentEquals("not")) {
            editText.setText("");
            editText.setHint("קוד אישור שגוי");
        }else if (data.contentEquals("data was not updated")){
            validation.createAlert("שגיאה בקליטת נתונים");
        }
        else{
            try {
                JSONObject object = new JSONObject(data);
                editor.putInt(String.valueOf(R.string.sharedPrefUserId), object.getInt("confirmed"));
                editor.putInt(String.valueOf(R.string.sharedPrefUserCityId), object.getInt("city"));
                editor.putInt(String.valueOf(R.string.sharedPrefUserCountyId), object.getInt("county"));
                editor.putInt(String.valueOf(R.string.sharedPrefRegistered),100);
                editor.remove(String.valueOf(R.string.sharedPrefVerify));
                editor.remove(String.valueOf(R.string.sharedPrefConfirmCode));
                editor.remove(String.valueOf(R.string.sharedPrefConfirmTime));
                editor.commit();
                dismiss();
                NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(9);
                getActivity().finish();
                Intent intent = new Intent(context,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void getNotificationData(String msg, String time){
        this.msg    = msg;
        this.time   = time;
    }

}